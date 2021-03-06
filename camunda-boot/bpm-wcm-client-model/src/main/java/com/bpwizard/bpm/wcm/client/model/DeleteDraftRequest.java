package com.bpwizard.bpm.wcm.client.model;

public class DeleteDraftRequest {
	private String contentId;
	private String workflow;

	public static DeleteDraftRequest createDeleteDraftRequest(String contentId, String workflow) {
		DeleteDraftRequest deleteDraftRequest = new DeleteDraftRequest();
		deleteDraftRequest.setContentId(contentId);
		deleteDraftRequest.setWorkflow(workflow);
		return deleteDraftRequest;
	}
	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public String getWorkflow() {
		return workflow;
	}

	public void setWorkflow(String workflow) {
		this.workflow = workflow;
	}

	@Override
	public String toString() {
		return "DeleteDraftRequest [contentId=" + contentId + ", workflow=" + workflow + "]";
	}
}
