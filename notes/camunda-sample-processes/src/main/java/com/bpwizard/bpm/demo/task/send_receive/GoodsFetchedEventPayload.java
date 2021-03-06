package com.bpwizard.bpm.demo.task.send_receive;

public class GoodsFetchedEventPayload {

	private String refId;
	private String pickId;

	public String getRefId() {
		return refId;
	}

	public GoodsFetchedEventPayload setRefId(String refId) {
		this.refId = refId;
		return this;
	}

	public String getPickId() {
		return pickId;
	}

	public GoodsFetchedEventPayload setPickId(String pickId) {
		this.pickId = pickId;
		return this;
	}

	@Override
	public String toString() {
		return "GoodsFetchedEventPayload [refId=" + refId + ", pickId=" + pickId + "]";
	}
}
