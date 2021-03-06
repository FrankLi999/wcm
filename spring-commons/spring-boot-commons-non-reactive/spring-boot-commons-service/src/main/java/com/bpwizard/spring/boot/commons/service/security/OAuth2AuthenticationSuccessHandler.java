package com.bpwizard.spring.boot.commons.service.security;

import java.io.Serializable;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import com.bpwizard.spring.boot.commons.SpringProperties;
import com.bpwizard.spring.boot.commons.security.JSONWebSignatureService;
import com.bpwizard.spring.boot.commons.security.UserDto;
import com.bpwizard.spring.boot.commons.util.SecurityUtils;
import com.bpwizard.spring.boot.commons.web.util.WebUtils;

import lombok.AllArgsConstructor;

/**
 * Authentication success handler for redirecting the
 * OAuth2 signed in user to a URL with a short lived auth token
 */
@AllArgsConstructor
public class OAuth2AuthenticationSuccessHandler<ID extends Serializable>
	extends SimpleUrlAuthenticationSuccessHandler {
	
	// private static final Logger log = LogManager.getLogger(OAuth2AuthenticationSuccessHandler.class);

	private SpringProperties properties;
	private JSONWebSignatureService jwsTokenService;

//	@Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
//        super.onAuthenticationSuccess(request, response, authentication);
//        this.clearAuthenticationAttributes(request, response);
//    }
	
	@Override
	protected String determineTargetUrl(HttpServletRequest request,
			HttpServletResponse response) {
		
		
		UserDto currentUser = WebUtils.currentUser();
		
		String shortLivedAuthToken = jwsTokenService.createToken(
				JSONWebSignatureService.AUTH_AUDIENCE,
				currentUser.getUsername(),
				(long) properties.getJwt().getShortLivedMillis());

		String targetUrl = WebUtils.fetchCookie(request,
				SecurityUtils.BPW_REDIRECT_URI_COOKIE_PARAM_NAME)
				.map(Cookie::getValue)
			.orElse(properties.getOauth2AuthenticationSuccessUrl());
		
		WebUtils.deleteCookies(request, response,
				SecurityUtils.AUTHORIZATION_REQUEST_COOKIE_NAME,
				SecurityUtils.BPW_REDIRECT_URI_COOKIE_PARAM_NAME);
		
		// return targetUrl + shortLivedAuthToken;
		return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", shortLivedAuthToken)
                .build().toUriString();
	}
}
