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
package org.camunda.bpm.engine.rest.sub.runtime.controller;

import java.util.List;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.rest.sub.VariableResource;
import org.camunda.bpm.engine.rest.sub.impl.AbstractVariablesService;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.springframework.stereotype.Service;

/**
 *
 * @author Roman Smirnov
 *
 */
@Service
public class CaseExecutionVariablesService extends AbstractVariablesService implements VariableResource {

//	public CaseExecutionVariablesService(ProcessEngine engine, String resourceId, ObjectMapper objectMapper) {
//		super(engine, resourceId, objectMapper);
//	}

	protected VariableMap getVariableEntities(String resourceId, boolean deserializeValues) {
		CaseService caseService = engine.getCaseService();
		return caseService.getVariablesTyped(resourceId, deserializeValues);
	}

	protected void updateVariableEntities(String resourceId, VariableMap variables, List<String> deletions) {
		CaseService caseService = engine.getCaseService();
		caseService.withCaseExecution(resourceId).setVariables(variables).removeVariables(deletions).execute();
	}

	protected void removeVariableEntity(String resourceId, String variableKey) {
		CaseService caseService = engine.getCaseService();
		caseService.withCaseExecution(resourceId).removeVariable(variableKey).execute();
	}

	protected String getResourceTypeName() {
		return "case execution";
	}

	protected TypedValue getVariableEntity(String resourceId, String variableKey, boolean deserializeValue) {
		CaseService caseService = engine.getCaseService();
		return caseService.getVariableTyped(resourceId, variableKey, deserializeValue);
	}

	protected void setVariableEntity(String resourceId, String variableKey, TypedValue variableValue) {
		CaseService caseService = engine.getCaseService();
		caseService.withCaseExecution(resourceId).setVariable(variableKey, variableValue).execute();
	}

}
