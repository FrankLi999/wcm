/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.controller;

import javax.annotation.PostConstruct;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.rest.dto.authorization.AuthorizationDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author Daniel Meyer
 *
 */
public abstract class AbstractAuthorizedRestResource extends AbstractRestProcessEngineAware {


	@Autowired
	protected AuthorizationService authorizationService;
	
	@PostConstruct
	public void afterPropertySet() {
		this.authorizationService = this.processEngine.getAuthorizationService();
	}
	
	protected boolean isAuthorized(Permission permission, Resource resource, String resourceId) {
		if (!processEngine.getProcessEngineConfiguration().isAuthorizationEnabled()) {
			// if authorization is disabled everyone is authorized
			return true;
		}

		final IdentityService identityService = processEngine.getIdentityService();
		final AuthorizationService authorizationService = processEngine.getAuthorizationService();

		Authentication authentication = identityService.getCurrentAuthentication();
		if (authentication == null) {
			return true;

		} else {
			return authorizationService.isUserAuthorized(authentication.getUserId(), authentication.getGroupIds(),
					permission, resource, resourceId);
		}
	}
	
	public AuthorizationDto doGetAuthorization(@PathVariable("resourceId") String resourceId) {
		Authorization dbAuthorization = getDbAuthorization(resourceId);
		return AuthorizationDto.fromAuthorization(dbAuthorization, processEngine.getProcessEngineConfiguration());
	}
	
	protected abstract Resource getResource();

	protected boolean isAuthorized(Permission permission, String resourceId) {
		return isAuthorized(permission, this.getResource(), resourceId);
	}
	
	protected Authorization getDbAuthorization(String resourceId) {
		Authorization dbAuthorization = authorizationService.createAuthorizationQuery().authorizationId(resourceId)
				.singleResult();

		if (dbAuthorization == null) {
			throw new InvalidRequestException(HttpStatus.NOT_FOUND,
					"Authorization with id " + resourceId + " does not exist.");

		} else {
			return dbAuthorization;

		}
	}
}