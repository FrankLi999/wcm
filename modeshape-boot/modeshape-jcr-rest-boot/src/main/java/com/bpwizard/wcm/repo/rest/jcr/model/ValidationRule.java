package com.bpwizard.wcm.repo.rest.jcr.model;

import java.io.Serializable;

import org.modeshape.jcr.api.JcrConstants;
import org.springframework.util.StringUtils;

import com.bpwizard.wcm.repo.rest.JsonUtils;
import com.bpwizard.wcm.repo.rest.modeshape.model.HasName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ValidationRule implements HasName, Serializable, Comparable<ValidationRule> {

	private static final long serialVersionUID = 1L;

	private String repository;
	private String workspace;
	private String library;
	
	private String name;
	private String title;
	private String description;
	private String type;
	private String rule;
	
	public String getRepository() {
		return repository;
	}
	
	public void setRepository(String repository) {
		this.repository = repository;
	}
	
	public String getWorkspace() {
		return workspace;
	}
	
	public void setWorkspace(String workspace) {
		this.workspace = workspace;
	}
	
	public String getLibrary() {
		return library;
	}
	
	public void setLibrary(String library) {
		this.library = library;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getRule() {
		return rule;
	}
	
	public void setRule(String rule) {
		this.rule = rule;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public JsonNode toJson() {
		
		ObjectNode jsonNode = JsonUtils.createObjectNode();
		jsonNode.put(JcrConstants.JCR_PRIMARY_TYPE, "bpw:validationRule");
		
		if (StringUtils.hasText(this.getTitle())) {
			jsonNode.put("bpw:title", this.getTitle());
		}
		
		if (StringUtils.hasText(this.getDescription())) {
			jsonNode.put("bpw:description", this.getDescription());
		}
		
		jsonNode.put("bpw:name", this.getName());
		String type = StringUtils.hasLength(this.getType())? this.getType() : "regex";
		jsonNode.put("bpw:type", type);		
		jsonNode.put("bpw:rule", this.getRule());
				
		return jsonNode;
	}

	@Override
	public String toString() {
		return "ValidationRule [repository=" + repository + ", workspace=" + workspace + ", library=" + library
				+ ", name=" + name + ", title=" + title + ", description=" + description + ", type=" + type + ", rule="
				+ rule + "]";
	}
	
	@Override
	public int compareTo(ValidationRule o) {
		int result = 1;
		if (o != null) {
		    result = this.name.compareTo(o.getName());
		    if (result == 0) {
		    	result = this.library.compareTo(o.getLibrary());
		    }
		}
		return result;
	}
}