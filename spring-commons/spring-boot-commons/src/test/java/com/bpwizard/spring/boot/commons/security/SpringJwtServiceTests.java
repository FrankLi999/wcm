package com.bpwizard.spring.boot.commons.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;

import com.bpwizard.spring.boot.commons.util.SecurityUtils;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;

public class SpringJwtServiceTests {
	
	private static final Logger log = LoggerFactory.getLogger(SpringJwtServiceTests.class);	

	// An aes-128-cbc key generated at https://asecuritysite.com/encryption/keygen (take the "key" field)
	private static final String SECRET1 = "926D96C90030DD58429D2751AC1BDBBC";
	private static final String SECRET2 = "538518AB685B514685DA8055C03DDA63";
		 
	private SpringJweService jweService1;
	private SpringJweService jweService2;
	private SpringJwsService jwsService1;
	private SpringJwsService jwsService2;

	public SpringJwtServiceTests() throws JOSEException {
		
		jweService1 = new SpringJweService(SECRET1);
		jweService2 = new SpringJweService(SECRET2);

		jwsService1 = new SpringJwsService(SECRET1);
		jwsService2 = new SpringJwsService(SECRET2);
	}
	
	@Test
	public void testParseToken() {
		
		testParseToken(jweService1);
		testParseToken(jwsService1);
	}
	
	private void testParseToken(SpringTokenService service) {
		
		log.info("Creating token ..." + service.getClass().getSimpleName());
		String token = service.createToken("auth", "subject", 5000L,
				SecurityUtils.mapOf("username", "abc@example.com"));
		
		log.info("Parsing token ...");
		JWTClaimsSet claims = service.parseToken(token, "auth");
		
		log.info("Parsed token.");
		Assertions.assertEquals("subject", claims.getSubject());
		Assertions.assertEquals("abc@example.com", claims.getClaim("username"));
	}

	@Test
	public void testParseJweTokenWrongAudience() {
		Assertions.assertThrows(BadCredentialsException.class, () -> {
		    testParseTokenWrongAudience(jweService1);
		});
	}
	
	@Test
	public void testParseJwsTokenWrongAudience() {
		Assertions.assertThrows(BadCredentialsException.class, () -> {
		    testParseTokenWrongAudience(jwsService1);
		});			
	}
	

	private void testParseTokenWrongAudience(SpringTokenService service) {
		
		String token = service.createToken("auth", "subject", 5000L);
		service.parseToken(token, "auth2");
	}

	@Test
	public void testParseJweTokenExpired() throws InterruptedException {
		Assertions.assertThrows(BadCredentialsException.class, () -> {
		    testParseTokenExpired(jweService1);
		});
			}
	
	@Test
	public void testParseJwsTokenExpired() throws InterruptedException {
		Assertions.assertThrows(BadCredentialsException.class, () -> {
		    testParseTokenExpired(jwsService1);
		});
	}

	private void testParseTokenExpired(SpringTokenService service) throws InterruptedException {
		
		String token = service.createToken("auth", "subject", 1L);
		Thread.sleep(1L);
		service.parseToken(token, "auth");
	}

	@Test
	public void testParseJweTokenWrongSecret() {
		Assertions.assertThrows(BadCredentialsException.class, () -> {
		    testParseTokenWrongSecret(jweService1, jweService2);
		});		
	}

	@Test
	public void testParseJwsTokenWrongSecret() {
		Assertions.assertThrows(BadCredentialsException.class, () -> {
		    testParseTokenWrongSecret(jwsService1, jwsService2);
		});		
	}

	private void testParseTokenWrongSecret(SpringTokenService service1, SpringTokenService service2) {
		
		String token = service1.createToken("auth", "subject", 5000L);
		service2.parseToken(token, "auth");
	}

	@Test
	public void testParseJweTokenCutoffTime() throws InterruptedException {
		Assertions.assertThrows(BadCredentialsException.class, () -> {
		    testParseTokenCutoffTime(jweService1);
		});		
	}

	@Test
	public void testParseJwsTokenCutoffTime() throws InterruptedException {
		Assertions.assertThrows(BadCredentialsException.class, () -> {
		    testParseTokenCutoffTime(jwsService1);
		});		
	}


	private void testParseTokenCutoffTime(SpringTokenService service) throws InterruptedException {
		
		String token = service.createToken("auth", "subject", 5000L);
		Thread.sleep(1L);				
		service.parseToken(token, "auth", System.currentTimeMillis());
	}
}
