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
package org.camunda.bpm.engine.rest.history.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricIncidentQuery;
import org.camunda.bpm.engine.rest.controller.AbstractRestProcessEngineAware;
import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricIncidentDto;
import org.camunda.bpm.engine.rest.dto.history.HistoricIncidentQueryDto;
import org.camunda.bpm.engine.rest.history.HistoricIncidentRestService;
import org.camunda.bpm.engine.rest.history.HistoryRestService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Roman Smirnov
 *
 */
@RestController(value="historicIncidentApi")
@RequestMapping(HistoryRestService.PATH + HistoricIncidentRestService.PATH)
public class HistoricIncidentRestController extends AbstractRestProcessEngineAware implements HistoricIncidentRestService {

	@Override
	@GetMapping(path="/", produces=MediaType.APPLICATION_JSON_VALUE)
	public List<HistoricIncidentDto> getHistoricIncidents(
			HttpServletRequest request, 
			@RequestParam("firstResult") Integer firstResult,
			@RequestParam("maxResults") Integer maxResults) {
		HistoricIncidentQueryDto queryDto = new HistoricIncidentQueryDto(this.getObjectMapper(), request.getParameterMap());
		HistoricIncidentQuery query = queryDto.toQuery(processEngine);

		List<HistoricIncident> queryResult;
		if (firstResult != null || maxResults != null) {
			queryResult = executePaginatedQuery(query, firstResult, maxResults);
		} else {
			queryResult = query.list();
		}

		List<HistoricIncidentDto> result = new ArrayList<HistoricIncidentDto>();
		for (HistoricIncident historicIncident : queryResult) {
			HistoricIncidentDto dto = HistoricIncidentDto.fromHistoricIncident(historicIncident);
			result.add(dto);
		}

		return result;
	}

	@Override
	@GetMapping(path="/count", produces=MediaType.APPLICATION_JSON_VALUE)
	public CountResultDto getHistoricIncidentsCount(HttpServletRequest request) {
		HistoricIncidentQueryDto queryDto = new HistoricIncidentQueryDto(this.getObjectMapper(), request.getParameterMap());
		HistoricIncidentQuery query = queryDto.toQuery(processEngine);

		long count = query.count();
		CountResultDto result = new CountResultDto();
		result.setCount(count);

		return result;
	}

	private List<HistoricIncident> executePaginatedQuery(HistoricIncidentQuery query, Integer firstResult,
			Integer maxResults) {
		if (firstResult == null) {
			firstResult = 0;
		}
		if (maxResults == null) {
			maxResults = Integer.MAX_VALUE;
		}
		return query.listPage(firstResult, maxResults);
	}

}
