package com.bpwizard.spring.boot.commons.jpa;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.AbstractPersistable;

import com.bpwizard.spring.boot.commons.security.PermissionEvaluatorEntity;
import com.bpwizard.spring.boot.commons.security.UserDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Base class for all entities.
 * 
 * @author Sanjay Patel
  */
@MappedSuperclass
@Getter @Setter
@JsonIgnoreProperties({ "createdById", "lastModifiedById", "createdDate", "lastModifiedDate", "new" })
public class SpringEntity<ID extends Serializable> extends AbstractPersistable<ID> implements PermissionEvaluatorEntity {

	private static final long serialVersionUID = -8151190931948396443L;
	
	@CreatedBy
	private ID createdById;
	
	@CreatedDate
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate;
	
	@LastModifiedBy
	private ID lastModifiedById;
	
	@LastModifiedDate
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastModifiedDate;
	
	@Version
	private Long version;
	
	/**
	 * Whether the given user has the given permission for
	 * this entity. Override this method where you need.
	 */
	@Override
	public boolean hasPermission(UserDto user, String permission) {
		return false;
	}
	
}
