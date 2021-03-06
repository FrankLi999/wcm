package com.bpwizard.spring.boot.commons.reactive.demo;

import static com.bpwizard.spring.boot.commons.reactive.demo.MyTestUtils.ADMIN_ID;
import static com.bpwizard.spring.boot.commons.reactive.demo.MyTestUtils.BLOCKED_ADMIN_ID;
import static com.bpwizard.spring.boot.commons.reactive.demo.MyTestUtils.CLIENT;
import static com.bpwizard.spring.boot.commons.reactive.demo.MyTestUtils.TOKENS;
import static com.bpwizard.spring.boot.commons.reactive.demo.MyTestUtils.UNVERIFIED_ADMIN_ID;
import static com.bpwizard.spring.boot.commons.reactive.demo.MyTestUtils.UNVERIFIED_USER_ID;
import static com.bpwizard.spring.boot.commons.reactive.demo.MyTestUtils.USER_ID;
import static com.bpwizard.spring.boot.commons.reactive.demo.controllers.MyController.BASE_URI;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

public class ResendVerificationMailTests extends AbstractTests {

	@Test
	public void testResendVerificationMail() {
		
		resendVerificationMail(UNVERIFIED_USER_ID, UNVERIFIED_USER_ID)
			.expectStatus().isNoContent();
		
		verify(mailSender).send(any());
	}


	@Test
	public void testAdminResendVerificationMailOtherUser() {
		
		resendVerificationMail(UNVERIFIED_USER_ID, ADMIN_ID)
			.expectStatus().isNoContent();
	}
	
	
	@Test
	public void testBadAdminResendVerificationMailOtherUser() {
		
		resendVerificationMail(UNVERIFIED_USER_ID, UNVERIFIED_ADMIN_ID)
			.expectStatus().isForbidden();

		resendVerificationMail(UNVERIFIED_USER_ID, BLOCKED_ADMIN_ID)
		.expectStatus().isForbidden();
		
		verify(mailSender, never()).send(any());
	}
	
	
	@Test
	public void testResendVerificationMailUnauthenticated() {
		
		CLIENT.post().uri(BASE_URI + "/users/{id}/resend-verification-mail", UNVERIFIED_USER_ID)
			.exchange()
			.expectStatus().isForbidden();

		verify(mailSender, never()).send(any());
	}

	
	@Test
	public void testResendVerificationMailAlreadyVerified() {
		
		resendVerificationMail(USER_ID, USER_ID)
			.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
		
		verify(mailSender, never()).send(any());
	}

	
	@Test
	public void testResendVerificationMailOtherUser() {
		
		resendVerificationMail(UNVERIFIED_USER_ID, USER_ID)
			.expectStatus().isForbidden();

		verify(mailSender, never()).send(any());
	}

	
	@Test
	public void testResendVerificationMailNonExistingUser() {
		
		resendVerificationMail(ObjectId.get(), ADMIN_ID)
			.expectStatus().isNotFound();
		
		verify(mailSender, never()).send(any());
	}

	
	private ResponseSpec resendVerificationMail(ObjectId userId, ObjectId loggedInId) {
		
		return CLIENT.post().uri(BASE_URI + "/users/{id}/resend-verification-mail", userId)
			.header(HttpHeaders.AUTHORIZATION, TOKENS.get(loggedInId))
			.exchange();

	}
}
