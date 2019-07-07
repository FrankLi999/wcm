package com.bpwizard.spring.boot.commons.exceptions.handlers;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;

import com.bpwizard.spring.boot.commons.exceptions.ErrorResponse;
import com.bpwizard.spring.boot.commons.exceptions.SpringFieldError;
import com.bpwizard.spring.boot.commons.exceptions.util.SpringExceptionUtils;

/**
 * Extend this to code an exception handler
 */
public abstract class AbstractExceptionHandler<T extends Throwable> {
	
	protected final Logger log = LogManager.getLogger(this.getClass());
	
	private Class<?> exceptionClass;
	
	public AbstractExceptionHandler(Class<?> exceptionClass) {
		this.exceptionClass = exceptionClass;
	}

	public Class<?> getExceptionClass() {
		return exceptionClass;
	}
	
	protected String getExceptionId(T ex) {
		return SpringExceptionUtils.getExceptionId(ex);
	}

	protected String getMessage(T ex) {
		return ex.getMessage();
	}
	
	protected HttpStatus getStatus(T ex) {
		return null;
	}
	
	protected Collection<SpringFieldError> getErrors(T ex) {
		return null;
	}

	public ErrorResponse getErrorResponse(T ex) {
    	
		ErrorResponse errorResponse = new ErrorResponse();
		
		errorResponse.setExceptionId(getExceptionId(ex));
		errorResponse.setMessage(getMessage(ex));
		
		HttpStatus status = getStatus(ex);
		if (status != null) {
			errorResponse.setStatus(status.value());
			errorResponse.setError(status.getReasonPhrase());
		}
		
		errorResponse.setErrors(getErrors(ex));
		return errorResponse;
	}
}