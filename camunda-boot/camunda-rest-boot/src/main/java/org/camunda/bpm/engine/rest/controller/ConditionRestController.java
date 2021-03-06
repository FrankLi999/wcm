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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.rest.ConditionRestService;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.condition.EvaluationConditionDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.runtime.ConditionEvaluationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.VariableMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController(value="conditionApi")
@RequestMapping(ConditionRestService.PATH)
public class ConditionRestController extends AbstractRestProcessEngineAware implements ConditionRestService {

	@Override
	@PostMapping(path="/", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public List<ProcessInstanceDto> evaluateCondition(@RequestBody EvaluationConditionDto conditionDto) {
		if (conditionDto.getTenantId() != null && conditionDto.isWithoutTenantId()) {
			throw new InvalidRequestException(HttpStatus.BAD_REQUEST,
					"Parameter 'tenantId' cannot be used together with parameter 'withoutTenantId'.");
		}
		ConditionEvaluationBuilder builder = createConditionEvaluationBuilder(conditionDto);
		List<ProcessInstance> processInstances = builder.evaluateStartConditions();

		List<ProcessInstanceDto> result = new ArrayList<ProcessInstanceDto>();
		for (ProcessInstance processInstance : processInstances) {
			result.add(ProcessInstanceDto.fromProcessInstance(processInstance));
		}
		return result;
	}

	protected ConditionEvaluationBuilder createConditionEvaluationBuilder(EvaluationConditionDto conditionDto) {
		RuntimeService runtimeService = processEngine.getRuntimeService();

		VariableMap variables = VariableValueDto.toMap(conditionDto.getVariables(), processEngine, this.getObjectMapper());

		ConditionEvaluationBuilder builder = runtimeService.createConditionEvaluation();

		if (variables != null && !variables.isEmpty()) {
			builder.setVariables(variables);
		}

		if (conditionDto.getBusinessKey() != null) {
			builder.processInstanceBusinessKey(conditionDto.getBusinessKey());
		}

		if (conditionDto.getProcessDefinitionId() != null) {
			builder.processDefinitionId(conditionDto.getProcessDefinitionId());
		}

		if (conditionDto.getTenantId() != null) {
			builder.tenantId(conditionDto.getTenantId());
		} else if (conditionDto.isWithoutTenantId()) {
			builder.withoutTenantId();
		}

		return builder;
	}
}
