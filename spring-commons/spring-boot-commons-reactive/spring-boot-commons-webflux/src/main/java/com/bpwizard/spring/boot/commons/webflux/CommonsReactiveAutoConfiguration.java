package com.bpwizard.spring.boot.commons.webflux;

import java.io.Serializable;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.AbstractSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.reactive.CorsConfigurationSource;

import com.bpwizard.spring.boot.commons.CommonsAutoConfiguration;
import com.bpwizard.spring.boot.commons.SpringProperties;
import com.bpwizard.spring.boot.commons.exceptions.ErrorResponseComposer;
import com.bpwizard.spring.boot.commons.exceptions.util.SpringExceptionUtils;
import com.bpwizard.spring.boot.commons.security.JSONWebSignatureService;
import com.bpwizard.spring.boot.commons.webflux.exceptions.SpringReactiveErrorAttributes;
import com.bpwizard.spring.boot.commons.webflux.exceptions.handlers.VersionExceptionHandler;
import com.bpwizard.spring.boot.commons.webflux.security.SpringCommonsReactiveSecurityConfig;
import com.bpwizard.spring.boot.commons.webflux.security.SpringCorsConfigurationSource;
import com.bpwizard.spring.boot.commons.webflux.security.SpringReactiveAuditorAware;
import com.bpwizard.spring.boot.commons.webflux.util.ReactiveUtils;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Configuration
@EnableReactiveMethodSecurity
@AutoConfigureBefore({
	// WebFluxAutoConfiguration.EnableWebFluxConfiguration.class,
	WebFluxAutoConfiguration.class,
	ErrorWebFluxAutoConfiguration.class,
	ReactiveSecurityAutoConfiguration.class,
	CommonsAutoConfiguration.class})
@ComponentScan(basePackageClasses=VersionExceptionHandler.class)
public class CommonsReactiveAutoConfiguration {
	
	private static final Logger logger = LoggerFactory.getLogger(CommonsReactiveAutoConfiguration.class);
	
	public CommonsReactiveAutoConfiguration() {
		logger.info("Created");
	}
	
	
	/**
	 * Configures an Error Attributes if missing
	 */	
	@Bean
	@ConditionalOnMissingBean(ErrorAttributes.class)
	public <T extends Throwable>
	ErrorAttributes errorAttributes(ErrorResponseComposer<T> errorResponseComposer) {
		
		logger.info("Configuring SpringErrorAttributes");       
		return new SpringReactiveErrorAttributes<T>(errorResponseComposer);
	}

	
	@Bean
	@ConditionalOnMissingBean(SpringCommonsReactiveSecurityConfig.class)
	public SpringCommonsReactiveSecurityConfig springReactiveSecurityConfig(JSONWebSignatureService jwsTokenService) {
		
		logger.info("Configuring SpringCommonsReactiveSecurityConfig ...");
		return new SpringCommonsReactiveSecurityConfig(jwsTokenService);
	}
	
	
	/**
	 * Configures SecurityWebFilterChain if missing
	 */
	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(
			ServerHttpSecurity http,
			SpringCommonsReactiveSecurityConfig securityConfig,
			AbstractSecurityExpressionHandler<?> expressionHandler,
			PermissionEvaluator permissionEvaluator) {
		
		logger.info("Configuring SecurityWebFilterChain ...");
		expressionHandler.setPermissionEvaluator(permissionEvaluator);
		return securityConfig.springSecurityFilterChain(http);
	}
	
	
	/**
	 * Configures SpringCorsConfig if missing and bpw.cors.allowed-origins is provided
	 */
	@Bean
	@ConditionalOnProperty(name="bpw.cors.allowed-origins")
	@ConditionalOnMissingBean(CorsConfigurationSource.class)
	public SpringCorsConfigurationSource corsConfigurationSource(SpringProperties properties) {
		
		logger.info("Configuring SpringCorsConfigurationSource");       
		return new SpringCorsConfigurationSource(properties);		
	}

	
	@Bean
	public SimpleModule objectIdModule() {
		
		SimpleModule module = new SimpleModule();
		module.addSerializer(ObjectId.class, new ToStringSerializer());
		
		return module;
	}
	
	
	/**
	 * Configures an Auditor Aware if missing
	 */	
	@Bean
	@ConditionalOnMissingBean(AuditorAware.class)
	public <ID extends Serializable>
	AuditorAware<ID> auditorAware() {
		
		logger.info("Configuring SpringAuditorAware");       
		return new SpringReactiveAuditorAware<ID>();
	}


	/**
	 * Configures ReactiveUtils
	 */
	@Bean
	public ReactiveUtils reactiveUtils(SpringExceptionUtils lexUtils) {

		logger.info("Configuring LecrUtils");
		return new ReactiveUtils();
	}
}
