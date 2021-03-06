package org.camunda.bpm.engine.rest.sub.repository.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.rest.DecisionDefinitionRestService;
import org.camunda.bpm.engine.rest.dto.HistoryTimeToLiveDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.dmn.EvaluateDecisionDto;
import org.camunda.bpm.engine.rest.dto.repository.DecisionDefinitionDiagramDto;
import org.camunda.bpm.engine.rest.dto.repository.DecisionDefinitionDto;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.sub.repository.service.DecisionDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(DecisionDefinitionRestService.PATH + "/key/{key}")
public class DecisionDefinitionByKeyRestController {
	
	
	@Autowired
	DecisionDefinitionService decisionDefinitionService;
	
	@GetMapping(path="/", produces=MediaType.APPLICATION_JSON_VALUE)
	public DecisionDefinitionDto getDecisionDefinition(@PathVariable("key") String decisionDefinitionKey) {
		return this.decisionDefinitionService.getDecisionDefinition(this.getDecisionDefinitionId(decisionDefinitionKey));
	}

	@GetMapping(path="/xml", produces=MediaType.APPLICATION_JSON_VALUE)
	public DecisionDefinitionDiagramDto getDecisionDefinitionDmnXml(@PathVariable("key") String decisionDefinitionKey) {
		return this.decisionDefinitionService.getDecisionDefinitionDmnXml(this.getDecisionDefinitionId(decisionDefinitionKey));
	}

	@GetMapping(path="/diagram", produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getDecisionDefinitionDiagram(@PathVariable("key") String decisionDefinitionKey) {
		return this.decisionDefinitionService.getDecisionDefinitionDiagram(this.getDecisionDefinitionId(decisionDefinitionKey));
	}

	@PostMapping(path="/evaluate", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	public List<Map<String, VariableValueDto>> evaluateDecision(@PathVariable("key") String decisionDefinitionKey, HttpServletRequest request, @RequestBody EvaluateDecisionDto parameters) {
		return this.decisionDefinitionService.evaluateDecision(this.getDecisionDefinitionId(decisionDefinitionKey), request, parameters);
	}

	@PutMapping(path="/history-time-to-live", consumes=MediaType.APPLICATION_JSON_VALUE)
	public void updateHistoryTimeToLive(@PathVariable("decisionDefinitionKey") String decisionDefinitionKey, @RequestBody HistoryTimeToLiveDto historyTimeToLiveDto) {
		this.decisionDefinitionService.updateHistoryTimeToLive(this.getDecisionDefinitionId(decisionDefinitionKey), historyTimeToLiveDto);
	}
	
	protected String getDecisionDefinitionId(String decisionDefinitionKey) {
		DecisionDefinition decisionDefinition = this.decisionDefinitionService.getDecisionDefinitionByKey(decisionDefinitionKey);

		if (decisionDefinition == null) {
			String errorMessage = String.format("No matching decision definition with key: %s and no tenant-id",
					decisionDefinitionKey);
			throw new RestException(HttpStatus.NOT_FOUND, errorMessage);

		} 
		return decisionDefinition.getId();
		
	}
}
