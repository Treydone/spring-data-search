package org.springframework.search.core;

import org.springframework.search.Failure;

public class SimpleFailure implements Failure {

	private String reason;

	public SimpleFailure(String reason) {
		this.reason = reason;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	@Override
	public String toString() {
		return "Failure [reason=" + reason + "]";
	}
}
