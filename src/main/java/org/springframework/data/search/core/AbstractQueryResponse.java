package org.springframework.data.search.core;

import java.util.List;

import org.springframework.data.search.Document;
import org.springframework.data.search.QueryResponse;

public abstract class AbstractQueryResponse implements QueryResponse {

	private List<? extends Document> documents;

	private Object nativeResponse;

	@Override
	public List<? extends Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<? extends Document> documents) {
		this.documents = documents;
	}

	public Object getNativeResponse() {
		return nativeResponse;
	}

	public void setNativeResponse(Object nativeResponse) {
		this.nativeResponse = nativeResponse;
	}
}
