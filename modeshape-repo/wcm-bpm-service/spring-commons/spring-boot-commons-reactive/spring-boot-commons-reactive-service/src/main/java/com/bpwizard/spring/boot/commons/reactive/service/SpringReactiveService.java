package com.bpwizard.spring.boot.commons.reactive.service;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import com.bpwizard.spring.boot.commons.SpringProperties;
import com.bpwizard.spring.boot.commons.SpringProperties.Admin;
import com.bpwizard.spring.boot.commons.domain.ChangePasswordForm;
import com.bpwizard.spring.boot.commons.domain.ResetPasswordForm;
import com.bpwizard.spring.boot.commons.exceptions.util.SpringExceptionUtils;
import com.bpwizard.spring.boot.commons.mail.MailSender;
import com.bpwizard.spring.boot.commons.mail.SpringMailData;
import com.bpwizard.spring.boot.commons.mongo.MongoUtils;
import com.bpwizard.spring.boot.commons.reactive.service.domain.AbstractMongoUser;
import com.bpwizard.spring.boot.commons.reactive.service.domain.AbstractMongoUserRepository;
import com.bpwizard.spring.boot.commons.reactive.service.forms.EmailForm;
import com.bpwizard.spring.boot.commons.reactive.service.util.ReactiveServiceUtils;
import com.bpwizard.spring.boot.commons.security.BlueTokenService;
import com.bpwizard.spring.boot.commons.security.GreenTokenService;
import com.bpwizard.spring.boot.commons.security.UserDto;
import com.bpwizard.spring.boot.commons.util.SecurityUtils;
import com.bpwizard.spring.boot.commons.util.UserUtils;
import com.bpwizard.spring.boot.commons.webflux.util.ReactiveUtils;
import com.nimbusds.jwt.JWTClaimsSet;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

public abstract class SpringReactiveService
	<U extends AbstractMongoUser<ID>, ID extends Serializable> {

    private static final Logger log = LogManager.getLogger(SpringReactiveService.class);
    
	protected SpringProperties properties;
	protected PasswordEncoder passwordEncoder;
	protected MailSender mailSender;
	protected AbstractMongoUserRepository<U, ID> userRepository;
	protected ReactiveUserDetailsService userDetailsService;
	protected BlueTokenService blueTokenService;
	protected GreenTokenService greenTokenService;

	@Autowired
	public void createSpringService(SpringProperties properties,
			PasswordEncoder passwordEncoder,
			MailSender mailSender,
			AbstractMongoUserRepository<U, ID> userRepository,
			ReactiveUserDetailsService userDetailsService,
			BlueTokenService blueTokenService,
			GreenTokenService greenTokenService) {
		
		this.properties = properties;
		this.passwordEncoder = passwordEncoder;
		this.mailSender = mailSender;
		this.userRepository = userRepository;
		this.userDetailsService = userDetailsService;
		this.blueTokenService = blueTokenService;
		this.greenTokenService = greenTokenService;
		
		log.info("Created");
	}


	/**
     * This method is called after the application is ready.
     * Needs to be public - otherwise Spring screams.
     * 
     * @param event
     */
    @EventListener
    public void afterApplicationReady(ApplicationReadyEvent event) {
    	
    	log.info("Starting up Spring Commons ...");
    	onStartup(); // delegate to onStartup()
    	log.info("Spring Commons started");	
    }

    
    public void onStartup() {
		
		userDetailsService
			.findByUsername(properties.getAdmin().getUsername()) // Check if the user already exists
			.doOnError(e -> e instanceof UsernameNotFoundException, e -> {
				// Doesn't exist. So, create it.
				log.debug("Creating first admin ... ");
		    	U user = createAdminUser();
		    	userRepository.insert(user).doOnError(err -> {
		    		log.warn("Error creating initial admin " + err);
		    	}).subscribe();
				log.debug("Created first admin.");		    	
			}).subscribe();
	}

    
	/**
	 * Creates the initial Admin user.
	 * Override this if needed.
	 */
    protected U createAdminUser() {
		
    	// fetch data about the user to be created
    	Admin initialAdmin = properties.getAdmin();
    	
    	log.info("Creating the first admin user: " + initialAdmin.getUsername());

    	// create the user
    	U user = newUser();
    	user.setEmail(initialAdmin.getUsername());
		user.setPassword(passwordEncoder.encode(
			properties.getAdmin().getPassword()));
		user.getRoles().add(UserUtils.Role.ADMIN);
		
		return user;
	}

    
	/**
	 * Creates a new user object. Must be overridden in the
	 * subclass, like this:
	 * 
	 * <pre>
	 * public User newUser() {
	 *    return new User();
	 * }
	 * </pre>
	 */
    public abstract U newUser();

    
	/**
	 * Returns the context data to be sent to the client,
	 * i.e. <code>reCaptchaSiteKey</code> and all the properties
	 * prefixed with <code>bpw.shared</code>.
	 * 
	 * To send custom properties, put those in your application
	 * properties in the format <em>bpw.shared.fooBar</em>.
	 * 
	 * If a user is logged in, it also returns the user data
	 * and a new authorization token. If expirationMillis is not provided,
	 * the expiration of the new token is set to the default.
	 *
	 * Override this method if needed.
	 */
	public Mono<Map<String, Object>> getContext(Optional<Long> expirationMillis, ServerHttpResponse response) {
		
		log.debug("Getting context ...");

		Mono<Optional<UserDto>> userDtoMono = ReactiveUtils.currentUser();
		return userDtoMono.map(optionalUser -> {
			
			Map<String, Object> context = buildContext();
			optionalUser.ifPresent(user -> {
				addAuthHeader(response, user,
					expirationMillis.orElse(properties.getJwt().getExpirationMillis()));
				context.put("user", user);
			});
			
			return context;			
		});
	}


	protected Map<String, Object> buildContext() {
		
		// make the context
		Map<String, Object> sharedProperties = new HashMap<String, Object>(2);
		sharedProperties.put("reCaptchaSiteKey", properties.getRecaptcha().getSitekey());
		sharedProperties.put("shared", properties.getShared());
		
		Map<String, Object> context = new HashMap<>();
		context.put("context", sharedProperties);
		
		return context;		
	}

	
    /**
	 * Signs up a user.
	 */
	public Mono<UserDto> signup(Mono<U> user) {
		
		log.debug("Signing up user: " + user);
		
		return user
			.doOnNext(this::initUser)
			.flatMap(userRepository::insert)
			.doOnNext(this::sendVerificationMail)
			.doOnNext(AbstractMongoUser::eraseCredentials)
			.map(AbstractMongoUser::toUserDto);
	}

	
	protected void initUser(U user) {
		
		log.debug("Initializing user: " + user);

		user.setPassword(passwordEncoder.encode(user.getPassword())); // encode the password
		makeUnverified(user); // make the user unverified
	}	

	
	/**
	 * Makes a user unverified
	 */
	protected void makeUnverified(U user) {
		
		user.getRoles().add(UserUtils.Role.UNVERIFIED);
		user.setCredentialsUpdatedMillis(System.currentTimeMillis());
	}
	
	
	/**
	 * Sends verification mail to a unverified user.
	 */
	protected void sendVerificationMail(final U user) {
		try {
			
			log.debug("Sending verification mail to: " + user);
			
			String verificationCode = greenTokenService.createToken(GreenTokenService.VERIFY_AUDIENCE,
					user.getId().toString(), properties.getJwt().getExpirationMillis(),
					SecurityUtils.mapOf("email", user.getEmail()));

			// make the link
			String verifyLink = properties.getApplicationUrl()
				+ "/users/" + user.getId() + "/verification?code=" + verificationCode;

			// send the mail
			sendVerificationMail(user, verifyLink);

			log.debug("Verification mail to " + user.getEmail() + " queued.");
			
		} catch (Throwable e) {
			// In case of exception, just log the error and keep silent
			log.error(ExceptionUtils.getStackTrace(e));
		}
	}	

	
	/**
	 * Sends verification mail to a unverified user.
	 * Override this method if you're using a different MailData
	 */
	protected void sendVerificationMail(final U user, String verifyLink) {
		
		// send the mail
		mailSender.send(SpringMailData.of(user.getEmail(),
			SpringExceptionUtils.getMessage("com.bpwizard.spring.verifySubject"),
			SpringExceptionUtils.getMessage(
				"com.bpwizard.spring.verifyEmail",	verifyLink)));
	}	

	
	/**
	 * Resends verification mail to the user.
	 */
	public Mono<Void> resendVerificationMail(ID userId) {
		
		return findUserById(userId)
			.zipWith(ReactiveUtils.currentUser())
			.doOnNext(this::ensureEditable)
			.map(Tuple2::getT1)
			.doOnNext(this::resendVerificationMail).then();
	}

	
	protected void ensureEditable(Tuple2<U, Optional<UserDto>> tuple) {
		SecurityUtils.ensureAuthority(
				tuple.getT1().hasPermission(tuple.getT2().orElse(null),
				UserUtils.Permission.EDIT),
				"com.bpwizard.spring.notGoodAdminOrSameUser");
	}

	
	/**
	 * Resends verification mail to the user.
	 */
	protected void resendVerificationMail(U user) {

		// must be unverified
		SpringExceptionUtils.validate(user.getRoles().contains(UserUtils.Role.UNVERIFIED),
				"com.bpwizard.spring.alreadyVerified").go();	

		// send the verification mail
		sendVerificationMail(user);
	}


	public Mono<UserDto> verifyUser(ID userId, Mono<MultiValueMap<String, String>> formData) {
		
		log.debug("Verifying user ...");

		return Mono.zip(findUserById(userId), formData)
				.map(this::verifyUser)
				.flatMap(userRepository::save)
				.map(AbstractMongoUser::toUserDto);
	}

	
	public U verifyUser(Tuple2<U, MultiValueMap<String,String>> tuple) {
		
		log.debug("Verifying user ...");
		
		U user = tuple.getT1();
		String verificationCode = tuple.getT2().getFirst("code");

		SpringExceptionUtils.validate(StringUtils.isNotBlank(verificationCode),
				"com.bpwizard.spring.blank", "code").go();

		// ensure that he is unverified
		SpringExceptionUtils.validate(user.hasRole(UserUtils.Role.UNVERIFIED),
				"com.bpwizard.spring.alreadyVerified").go();	
		
		JWTClaimsSet claims = greenTokenService.parseToken(
				verificationCode, GreenTokenService.VERIFY_AUDIENCE, user.getCredentialsUpdatedMillis());
		
		SecurityUtils.ensureAuthority(
				claims.getSubject().equals(user.getId().toString()) &&
				claims.getClaim("email").equals(user.getEmail()),
				"com.bpwizard.spring.wrong.verificationCode");
		
		user.getRoles().remove(UserUtils.Role.UNVERIFIED); // make him verified
		user.setCredentialsUpdatedMillis(System.currentTimeMillis());
		
		return user;
	}

	
	public Mono<Void> forgotPassword(Mono<MultiValueMap<String, String>> formData) {
		
		return formData.map(data -> {			
			String email = data.getFirst("email");
			SpringExceptionUtils.validate(StringUtils.isNotBlank(email),
					"com.bpwizard.spring.blank", "email").go();
			return email;
		}).flatMap(this::findUserByEmail)
				.doOnSuccess(this::mailForgotPasswordLink)
				.then();
	}

	
	/**
	 * Mails the forgot password link.
	 * 
	 * @param user
	 */
	public void mailForgotPasswordLink(U user) {
		
		log.debug("Mailing forgot password link to user: " + user);

		String forgotPasswordCode = greenTokenService.createToken(
				GreenTokenService.FORGOT_PASSWORD_AUDIENCE,
				user.getEmail(), properties.getJwt().getExpirationMillis());

		// make the link
		String forgotPasswordLink =	properties.getApplicationUrl()
			    + "/reset-password?code=" + forgotPasswordCode;
		
		mailForgotPasswordLink(user, forgotPasswordLink);
		
		log.debug("Forgot password link mail queued.");
	}

	
	/**
	 * Mails the forgot password link.
	 * 
	 * Override this method if you're using a different MailData
	 */
	public void mailForgotPasswordLink(U user, String forgotPasswordLink) {
		
		// send the mail
		mailSender.send(SpringMailData.of(user.getEmail(),
				SpringExceptionUtils.getMessage("com.bpwizard.spring.forgotPasswordSubject"),
				SpringExceptionUtils.getMessage("com.bpwizard.spring.forgotPasswordEmail",
					forgotPasswordLink)));
	}

		
	public Mono<UserDto> resetPassword(Mono<ResetPasswordForm> resetPasswordForm) {
		
		return resetPasswordForm.map(form -> {
			
			log.debug("Resetting password ...");

			JWTClaimsSet claims = greenTokenService.parseToken(form.getCode(),
					GreenTokenService.FORGOT_PASSWORD_AUDIENCE);
			
			String email = claims.getSubject();
			
			return Tuples.of(email, claims, form.getNewPassword());
		})
		.flatMap(tuple -> Mono.zip(
				findUserByEmail(tuple.getT1()),
				Mono.just(tuple.getT2()),
				Mono.just(tuple.getT3()))
		)
		.map(this::resetPassword)
		.flatMap(userRepository::save)
		.map(AbstractMongoUser::toUserDto);
	}
	
	
	public U resetPassword(Tuple3<U, JWTClaimsSet, String> tuple) {
		
		log.debug("Resetting password ...");

		U user = tuple.getT1();
		JWTClaimsSet claims = tuple.getT2();
		String newPassword = tuple.getT3();
		
		ReactiveServiceUtils.ensureCredentialsUpToDate(claims, user);
		
		// sets the password
		user.setPassword(passwordEncoder.encode(newPassword));
		user.setCredentialsUpdatedMillis(System.currentTimeMillis());
		
		log.debug("Password reset.");
		
		return user;
	}

	/**
	 * returns the current user and a new authorization token in the response
	 */
	public Mono<UserDto> userWithToken(Mono<UserDto> userDto,
			ServerHttpResponse response, long expirationMillis) {

		return userDto.doOnNext(user -> {
			log.debug("Adding auth header for " + user.getUsername());
			addAuthHeader(response, user, expirationMillis);
		});
	}

	
	protected void addAuthHeader(ServerHttpResponse response, UserDto userDto, long expirationMillis) {
		
		log.debug("Adding auth header for " + userDto.getUsername());
		
		response.getHeaders().add(SecurityUtils.TOKEN_RESPONSE_HEADER_NAME, SecurityUtils.TOKEN_PREFIX +
				blueTokenService.createToken(BlueTokenService.AUTH_AUDIENCE, userDto.getUsername(), expirationMillis));
	}


	public Mono<U> fetchUserByEmail(Mono<MultiValueMap<String, String>> formData) {
		
		// fetch the user
		return formData
				.map(data -> {
					
					String email = data.getFirst("email");
					
					SpringExceptionUtils.validate(StringUtils.isNotBlank(email),
							"com.bpwizard.spring.blank", "email").go();
					
					return email;
				})			
				.flatMap(this::findUserByEmail)
				.zipWith(ReactiveUtils.currentUser())
				.doOnNext(this::hideConfidentialFields)
				.map(Tuple2::getT1);
	}


	public Mono<U> fetchUserById(ID userId) {
		// fetch the user
		return findUserById(userId)
				.zipWith(ReactiveUtils.currentUser())
				.doOnNext(this::hideConfidentialFields)
				.map(Tuple2::getT1);
	}

	
	public Mono<UserDto> updateUser(ID userId, Mono<String> patch) {
		
		return Mono.zip(findUserById(userId), ReactiveUtils.currentUser(), patch)
			.doOnNext(this::ensureEditable)
			.map((Tuple3<U, Optional<UserDto>, String> tuple3) ->
				this.updateUser(tuple3.getT1(), tuple3.getT2(), tuple3.getT3()))
			.flatMap(userRepository::save)
			.map(user -> {
				UserDto userDto = user.toUserDto();
				userDto.setPassword(null);
				return userDto;
			});
	}


	protected U updateUser(U user, Optional<UserDto> currentUser, String patch) {
		
		log.debug("Updating user: " + user);

		U updatedUser = ReactiveUtils.applyPatch(user, patch); // create a patched form
		SpringExceptionUtils.validateBean("updatedUser", updatedUser, UserUtils.UpdateValidation.class).go();
		MongoUtils.ensureCorrectVersion(user, updatedUser);
		
		updateUserFields(user, updatedUser, currentUser.get());

		log.debug("Updated user: " + user);
		return user;
	}
	
	
	/**
	 * Updates the fields of the users. Override this if you have more fields.
	 */
	protected void updateUserFields(U user, U updatedUser, UserDto currentUser) {

		log.debug("Updating user fields for user: " + user);

		// Another good admin must be logged in to edit roles
		if (currentUser.isGoodAdmin() &&
		   !currentUser.getId().equals(user.getId().toString())) { 
			
			log.debug("Updating roles for user: " + user);

			// update the roles
			
			if (user.getRoles().equals(updatedUser.getRoles())) // roles are same
				return;
			
			if (updatedUser.hasRole(UserUtils.Role.UNVERIFIED)) {
				
				if (!user.hasRole(UserUtils.Role.UNVERIFIED)) {

					makeUnverified(user); // make user unverified
				}
			} else {
				
				if (user.hasRole(UserUtils.Role.UNVERIFIED))
					user.getRoles().remove(UserUtils.Role.UNVERIFIED); // make user verified
			}
			
			user.setRoles(updatedUser.getRoles());
			user.setCredentialsUpdatedMillis(System.currentTimeMillis());
		}
	}

	
	public Mono<U> findUserByEmail(String email) {
		return userRepository
				.findByEmail(email)
				.switchIfEmpty(ReactiveUtils.notFoundMono());
	}
	

	public Mono<U> findUserById(ID id) {
		return userRepository
				.findById(id)
				.switchIfEmpty(ReactiveUtils.notFoundMono());
	}

	
	/**
	 * Hides the confidential fields before sending to client
	 */
	protected void hideConfidentialFields(Tuple2<U,Optional<UserDto>> tuple) {
		
		U user = tuple.getT1();
		
		user.eraseCredentials();
		
		if (!user.hasPermission(tuple.getT2().orElse(null), UserUtils.Permission.EDIT))
			user.setEmail(null);
		
		log.debug("Hid confidential fields for user: " + user);
	}


	public Mono<UserDto> changePassword(ID userId, Mono<ChangePasswordForm> changePasswordForm) {
		
		return Mono.zip(findUserById(userId), ReactiveUtils.currentUser())
				.doOnNext(this::ensureEditable)
				.flatMap(tuple -> Mono.zip(
						Mono.just(tuple.getT1()),
						findUserById(toId(tuple.getT2().get().getId())),
						changePasswordForm)
				.doOnNext(this::changePassword))
				.map(Tuple2::getT1)
				.flatMap(userRepository::save)
				.map(AbstractMongoUser::toUserDto);
	}
	
	protected void changePassword(Tuple3<U,U,ChangePasswordForm> tuple) {
		
		U user = tuple.getT1();
		U loggedIn = tuple.getT2();
		ChangePasswordForm changePasswordForm = tuple.getT3();
		
		log.debug("Changing password for user: " + user);
		
		String oldPassword = loggedIn.getPassword();

		SpringExceptionUtils.validateField("changePasswordForm.oldPassword",
			passwordEncoder.matches(changePasswordForm.getOldPassword(),
					oldPassword),
			"com.bpwizard.spring.wrong.password").go();
		
		// sets the password
		user.setPassword(passwordEncoder.encode(changePasswordForm.getPassword()));
		user.setCredentialsUpdatedMillis(System.currentTimeMillis());
		log.debug("Changed password for user: " + user);
	}


	public Mono<Void> requestEmailChange(ID userId, Mono<EmailForm> emailForm) {
		
		return Mono.zip(findUserById(userId), ReactiveUtils.currentUser())
				.doOnNext(this::ensureEditable)
				.flatMap(tuple -> Mono.zip(
						Mono.just(tuple.getT1()),
						findUserById(toId(tuple.getT2().get().getId())),
						emailForm)
				.doOnNext(this::requestEmailChange))
				.map(Tuple2::getT1)
				.flatMap(userRepository::save)
				.doOnNext(this::mailChangeEmailLink)
				.then();
	}
	
	protected abstract ID toId(String id);

	protected void requestEmailChange(Tuple3<U,U,EmailForm> tuple) {
		
		U user = tuple.getT1();
		U loggedIn = tuple.getT2();
		EmailForm emailForm = tuple.getT3();
		
		log.debug("Requesting email change: " + user);
		
		// checks
		SpringExceptionUtils.validateField("emailFormMono.password",
			passwordEncoder.matches(emailForm.getPassword(),
									user.getPassword()),
			"com.bpwizard.spring.wrong.password").go();

		// preserves the new email id
		user.setNewEmail(emailForm.getNewEmail());

		log.debug("Requested email change: " + user);		
	}
	
	
	/**
	 * Mails the change-email verification link to the user.
	 */
	protected void mailChangeEmailLink(U user) {
		
		String changeEmailCode = greenTokenService.createToken(
				GreenTokenService.CHANGE_EMAIL_AUDIENCE,
				user.getId().toString(), properties.getJwt().getExpirationMillis(),
				SecurityUtils.mapOf("newEmail", user.getNewEmail()));
		
		try {
			
			log.debug("Mailing change email link to user: " + user);

			// make the link
			String changeEmailLink = properties.getApplicationUrl()
				    + "/users/" + user.getId()
					+ "/change-email?code=" + changeEmailCode;
			
			// mail it
			mailChangeEmailLink(user, changeEmailLink);
			
			log.debug("Change email link mail queued.");
			
		} catch (Throwable e) {
			// In case of exception, just log the error and keep silent			
			log.error(ExceptionUtils.getStackTrace(e));
		}
	}
	
	
	/**
	 * Mails the change-email verification link to the user.
	 * 
	 * Override this method if you're using a different MailData
	 */
	protected void mailChangeEmailLink(U user, String changeEmailLink) {
		
		mailSender.send(SpringMailData.of(user.getNewEmail(),
				SpringExceptionUtils.getMessage(
				"com.bpwizard.spring.changeEmailSubject"),
				SpringExceptionUtils.getMessage(
				"com.bpwizard.spring.changeEmailEmail",
				 changeEmailLink)));
	}


	@PreAuthorize("isAuthenticated()")
	public Mono<UserDto> changeEmail(ID userId, Mono<MultiValueMap<String, String>> formData) {
		
		log.debug("Changing email of current user ...");
		
		return ReactiveUtils.currentUser()
			.doOnNext(currentUser -> {				
				SpringExceptionUtils.validate(userId.equals(toId(currentUser.get().getId())),
						"com.bpwizard.spring.wrong.login").go();	
			})
			.then(Mono.zip(findUserById(userId), formData))
			.map(this::validateChangeEmail)
			.flatMap(user -> Mono.zip(Mono.just(user),
					userRepository
						.findByEmail(user.getNewEmail())
						.map(Optional::of)
						.defaultIfEmpty(Optional.empty())
					))
			.map(this::changeEmail)
			.flatMap(userRepository::save)
			.map(AbstractMongoUser::toUserDto);
	}

	
	protected U validateChangeEmail(Tuple2<U, MultiValueMap<String, String>> tuple) {
		U user = tuple.getT1();
		String code = tuple.getT2().getFirst("code");
				
		SpringExceptionUtils.validate(StringUtils.isNotBlank(code),
				"com.bpwizard.spring.blank", "code").go();

		SpringExceptionUtils.validate(StringUtils.isNotBlank(user.getNewEmail()),
				"com.bpwizard.spring.blank.newEmail").go();
		
		JWTClaimsSet claims = greenTokenService.parseToken(code,
				GreenTokenService.CHANGE_EMAIL_AUDIENCE,
				user.getCredentialsUpdatedMillis());
		
		SecurityUtils.ensureAuthority(
				claims.getSubject().equals(user.getId().toString()) &&
				claims.getClaim("newEmail").equals(user.getNewEmail()),
				"com.bpwizard.spring.wrong.changeEmailCode");
		
		return user;		
	}

	
	protected U changeEmail(Tuple2<U, Optional<U>> tuple) {
		
		U user = tuple.getT1();
		Optional<U> anotherUserWithSameEmail = tuple.getT2();
		
		// Ensure that the email would be unique 
		SpringExceptionUtils.validate(!anotherUserWithSameEmail.isPresent(),
				"com.bpwizard.spring.duplicate.email").go();	
		
		// update the fields
		user.setEmail(user.getNewEmail());
		user.setNewEmail(null);
		//user.setChangeEmailCode(null);
		user.setCredentialsUpdatedMillis(System.currentTimeMillis());
		
		// make the user verified if he is not
		if (user.hasRole(UserUtils.Role.UNVERIFIED))
			user.getRoles().remove(UserUtils.Role.UNVERIFIED);
		
		return user;
	}


	@PreAuthorize("isAuthenticated()")
	public Mono<Map<String, String>> fetchNewToken(ServerWebExchange exchange) {
		
		return Mono.zip(ReactiveUtils.currentUser(), exchange.getFormData()).map(tuple -> {
				
			UserDto currentUser = (UserDto) tuple.getT1().get();
			
			String username = tuple.getT2().getFirst("username");
			if (StringUtils.isBlank(username))
				username = currentUser.getUsername();
			
			long expirationMillis = getExpirationMillis(tuple.getT2());
			
			SecurityUtils.ensureAuthority(currentUser.getUsername().equals(username) ||
					currentUser.isGoodAdmin(), "com.bpwizard.spring.notGoodAdminOrSameUser");
			
			return Collections.singletonMap("token", SecurityUtils.TOKEN_PREFIX +
					blueTokenService.createToken(blueTokenService.AUTH_AUDIENCE, username, expirationMillis));			
		});
	}
	
	
	@PreAuthorize("isAuthenticated()")
	public Mono<Map<String, String>> fetchFullToken(String authHeader) {

		SecurityUtils.ensureCredentials(blueTokenService.parseClaim(authHeader.substring(SecurityUtils.TOKEN_PREFIX_LENGTH),
				BlueTokenService.USER_CLAIM) == null,	"com.bpwizard.spring.fullTokenNotAllowed");
		
		return ReactiveUtils.currentUser().map(optionalUser -> {
			
			UserDto currentUser = optionalUser.get();
			
			Map<String, Object> claimMap = Collections.singletonMap(BlueTokenService.USER_CLAIM,
					SecurityUtils.serialize(currentUser)); // Not serializing converts it to a JsonNode
			
			Map<String, String> tokenMap = Collections.singletonMap("token", SecurityUtils.TOKEN_PREFIX +
					blueTokenService.createToken(BlueTokenService.AUTH_AUDIENCE, currentUser.getUsername(),
						Long.valueOf(properties.getJwt().getShortLivedMillis()),
						claimMap));
			
			return tokenMap;
		});
	}


	public long getExpirationMillis(MultiValueMap<String, String> formData) {
		
		long expirationMillis = properties.getJwt().getExpirationMillis();
		String expirationMillisStr = formData.getFirst("expirationMillis");
		if (StringUtils.isNotBlank(expirationMillisStr))
			expirationMillis = Long.parseLong(expirationMillisStr);
		
		return expirationMillis;
	}
}
