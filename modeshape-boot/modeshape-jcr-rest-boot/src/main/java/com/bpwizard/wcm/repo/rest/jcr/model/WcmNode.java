package com.bpwizard.wcm.repo.rest.jcr.model;

public class WcmNode implements HasWcmAuthority {
	private String repository;
	private String workspace;
	private String id;
    private String wcmPath;
    private String name;
    private String title;
    private String nodeType;    
    private String lastModified;
    private String owner;
    private String status;
    private WcmAuthority wcmAuthority;
    
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

	public String getWcmPath() {
		return wcmPath;
	}
	
	public void setWcmPath(String wcmPath) {
		this.wcmPath = wcmPath;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getNodeType() {
		return nodeType;
	}
	
	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}
	
	public String getLastModified() {
		return lastModified;
	}
	
	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}

	public WcmAuthority getWcmAuthority() {
		return wcmAuthority;
	}

	public void setWcmAuthority(WcmAuthority wcmAuthority) {
		this.wcmAuthority = wcmAuthority;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "WcmNode [repository=" + repository + ", workspace=" + workspace + ", id=" + id + ", wcmPath=" + wcmPath + ", name="
				+ name + ", title=" + title + ", nodeType=" + nodeType + ", lastModified=" + lastModified + ", owner="
				+ owner + ", status=" + status + ", wcmAuthority=" + wcmAuthority + "]";
	}
}
