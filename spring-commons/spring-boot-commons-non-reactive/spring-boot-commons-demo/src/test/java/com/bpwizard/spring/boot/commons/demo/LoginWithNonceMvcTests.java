package com.bpwizard.spring.boot.commons.demo;

//
//import static org.hamcrest.Matchers.containsString;
//import static org.hamcrest.Matchers.hasItems;
//import static org.hamcrest.Matchers.hasSize;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MvcResult;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.bpwizard.spring.boot.commons.forms.NonceForm;
//import com.bpwizard.spring.boot.commons.security.SpringSecurityConfig;
//import com.bpwizard.spring.boot.commons.util.SecurityUtils;
//import com.bpwizard.spring.boot.commons.demo.entities.User;
//
//public class LoginWithNonceMvcTests extends AbstractMvcTests {
//	
//	private static final String NONCE = SecurityUtils.uid();
//	
//	private NonceForm<Long> form(Long jwtExpirationMillis) {
//		
//		NonceForm<Long> nonceForm = new NonceForm<>();
//		nonceForm.setNonce(NONCE);
//		nonceForm.setUserId(UNVERIFIED_USER_ID);
//		nonceForm.setExpirationMillis(jwtExpirationMillis);
//		
//		return nonceForm;
//	}
//	
//	@Before
//	public void setUp() {
//		
//		User user = userRepository.findById(UNVERIFIED_USER_ID).get();
//		user.setNonce(NONCE);
//		userRepository.save(user);
//	}
//	
//	@Test
//	public void testLoginWithNonce() throws Exception {
//		
//		String token = loginWithNonce(null);
//		
//		User user = userRepository.findById(UNVERIFIED_USER_ID).get();
//		Assert.assertNull(user.getNonce());
//	
//		ensureTokenWorks(token);
//
//		// Retry should fail
//		mvc.perform(post("/api/core/login-with-nonce")
//				.contentType(MediaType.APPLICATION_JSON)
//				.content(SecurityUtils.toJson(form(null))))
//				.andExpect(status().is(401));
//	}
//	
//	@Test
//	public void testLoginWithNonceExpiryOk() throws Exception {
//		
//		String token = loginWithNonce(1000L);
//		ensureTokenWorks(token);
//	}
//	
//	@Test
//	public void testLoginWithNonceExpiryShouldFail() throws Exception {
//		
//		String token = loginWithNonce(100L);
//		Thread.sleep(101L);
//		
//		mvc.perform(get("/api/core/context")
//				.header(SpringSecurityConfig.TOKEN_REQUEST_HEADER_NAME, token))
//				.andExpect(status().is(401));
//	}
//
//	@Test
//	public void testLoginWithNonceInvalidData() throws Exception {
//		
//		mvc.perform(post("/api/core/login-with-nonce")
//				.contentType(MediaType.APPLICATION_JSON)
//				.content(SecurityUtils.toJson(new NonceForm<Long>())))
//				.andExpect(status().is(422))
//				.andExpect(jsonPath("$.errors[*].field").value(hasSize(2)))
//				.andExpect(jsonPath("$.errors[*].field").value(hasItems(
//						"nonce.userId",
//						"nonce.nonce")));
//	}
//
//	@Test
//	public void testLoginWithNonceUnknownUser() throws Exception {
//		
//		NonceForm<Long> form = form(null);
//		form.setUserId(99L);
//		
//		mvc.perform(post("/api/core/login-with-nonce")
//				.contentType(MediaType.APPLICATION_JSON)
//				.content(SecurityUtils.toJson(form)))
//				.andExpect(status().is(404));
//	}
//
//	@Test
//	public void testLoginWithWrongNonce() throws Exception {
//		
//		NonceForm<Long> form = form(null);
//		form.setNonce("wrong-nonce");
//		
//		mvc.perform(post("/api/core/login-with-nonce")
//				.contentType(MediaType.APPLICATION_JSON)
//				.content(SecurityUtils.toJson(form)))
//				.andExpect(status().is(401));
//	}
//
//	private String loginWithNonce(Long jwtExpirationMillis) throws JsonProcessingException, Exception {
//
//        MvcResult result = mvc.perform(post("/api/core/login-with-nonce")
//				.contentType(MediaType.APPLICATION_JSON)
//				.content(SecurityUtils.toJson(form(jwtExpirationMillis))))
//				.andExpect(status().is(200))
//				.andExpect(header().string(SpringSecurityConfig.TOKEN_RESPONSE_HEADER_NAME, containsString(".")))
//				.andExpect(jsonPath("$.id").value(UNVERIFIED_USER_ID))
//                .andReturn();
//
//        return result.getResponse().getHeader(SpringSecurityConfig.TOKEN_RESPONSE_HEADER_NAME);
//	}
//}
