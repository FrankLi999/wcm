package com.bpwizard.wcm.repo.rest.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.bpwizard.wcm.repo.rest.JsonUtils;
import com.bpwizard.wcm.repo.rest.jcr.model.JcrNode;
import com.bpwizard.wcm.repo.rest.jcr.model.RootNodeKeys;
import com.bpwizard.wcm.repo.rest.jcr.model.Syndicator;
import com.bpwizard.wcm.repo.rest.jcr.model.WcmEvent;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class JcrNodeEventRestPublisher {
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private JcrNodeRepository jcrNodeService;
	
	public void syndicateCndTypes(WcmEvent wcmEvent, Syndicator syndicator, String token) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		// headers.setBearerAuth(token);
		headers.set(HttpHeaders.AUTHORIZATION, token);

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		
		body.add("file", this.getTemplFile("cnd_", wcmEvent.getContent()));
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
		String serverUrl = String.format("http://%s:%s/wcm/api/import/%s/%s/nodetypes", syndicator.getCollector().getHost(), syndicator.getCollector().getPort(), syndicator.getRepository(), syndicator.getWorkspace());
		restTemplate.postForEntity(serverUrl, requestEntity, Void.class);
	}
	
	public void syndicate(WcmEvent wcmEvent, Syndicator syndicator, String token, RootNodeKeys rootNodeKeys) throws RepositoryException, IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		// headers.setBearerAuth(token);
		headers.set(HttpHeaders.AUTHORIZATION, token);
		MultiValueMap<String, Object> wcmItemBody = new LinkedMultiValueMap<>();
		List<String> descendantIds = fromWcmEvent(wcmEvent, wcmItemBody);
		for (String id : descendantIds) {
			JcrNode elementNode = this.getJcrNode(rootNodeKeys.getRootNodeKey(), id);
			MultiValueMap<String, Object> elementBody = new LinkedMultiValueMap<>();
			elementBody.add("id", id);
			elementBody.add("timestamp", elementNode.getLastUpdated().getTime());
			elementBody.add("operation", wcmEvent.getOperation().name());	
			elementBody.add("content", this.getTemplFile("element_", elementNode.getContent()));
			elementBody.add("rootNodeKey", rootNodeKeys.getRootNodeKey());
			elementBody.add("jcrSystemKey", rootNodeKeys.getJcrSystemKey());
			String serverUrl = String.format("http://%s:%s/wcm/api/collector/%s/%s/jcrElement", syndicator.getCollector().getHost(), syndicator.getCollector().getPort(), syndicator.getRepository(), syndicator.getWorkspace());
			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(elementBody, headers);
			restTemplate.postForEntity(serverUrl, requestEntity, Void.class);
		}
		JcrNode jcrNode = this.getJcrNode(rootNodeKeys.getRootNodeKey(), wcmEvent.getId());
		wcmItemBody.add("timestamp", jcrNode.getLastUpdated().getTime());
		wcmItemBody.add("content", this.getTemplFile("wcmItem_", jcrNode.getContent()));
		wcmItemBody.add("rootNodeKey", rootNodeKeys.getRootNodeKey());
		wcmItemBody.add("jcrSystemKey", rootNodeKeys.getJcrSystemKey());
		String serverUrl = String.format("http://%s:%s/wcm/api/collector/%s/%s/jcrItem", syndicator.getCollector().getHost(), syndicator.getCollector().getPort(), syndicator.getRepository(), syndicator.getWorkspace());
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>( wcmItemBody, headers);
		restTemplate.postForEntity(serverUrl, requestEntity, Void.class);
	}
	
	private List<String> fromWcmEvent(WcmEvent wcmEvent, MultiValueMap<String, Object> body) throws IOException {
		List<String> descendantIds = new ArrayList<>();
		body.add("id", wcmEvent.getId());
		
		body.add("library", wcmEvent.getLibrary());
		body.add("nodePath", wcmEvent.getNodePath());
		body.add("operation", wcmEvent.getOperation().name());
		body.add("itemType", wcmEvent.getItemType().name());
		
		ObjectNode contentNode = (ObjectNode) JsonUtils.bytesToJsonNode(wcmEvent.getContent());
		ArrayNode descendants = (ArrayNode) contentNode.get("descendants");
		if (descendants != null && descendants.size() > 0) {
			
			for (int i = 0; i < descendants.size(); i++) {
				descendantIds.add(descendants.get(i).textValue());
			}
			body.add("descendants", String.join(",", descendantIds));
		}
		
		ArrayNode removedDescendants = (ArrayNode) contentNode.get("removedDescendants");
		if (removedDescendants != null && removedDescendants.size() > 0) {
			List<String> removedDescendantIds = new ArrayList<>();
			for (int i = 0; i < removedDescendants.size(); i++) {
				removedDescendantIds.add(removedDescendants.get(i).textValue());
			}
			body.add("removedDescendants", String.join(",", removedDescendantIds));
		} else {
			body.add("removedDescendants", "");
		}
		return descendantIds;
	}
	
	public Resource getTemplFile(String prefix, byte[] content) throws IOException {
        Path cndFile = Files.createTempFile(prefix + System.currentTimeMillis(), ".txt");
        Files.write(cndFile, content);
        return new FileSystemResource(cndFile.toFile());
    }
	
	private JcrNode getJcrNode(String rootNodeKey, String nodeId) {
		String nodeKey =  String.format("%s%s", rootNodeKey, nodeId);
		return jcrNodeService.getJcrNode(nodeKey);
	}
}
