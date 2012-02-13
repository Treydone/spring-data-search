package org.springframework.data.search.core.elasticsearch;

import org.elasticsearch.action.search.SearchResponse;
import org.springframework.data.search.core.AbstractQueryResponse;

public class ElasticSearchQueryResponse extends AbstractQueryResponse {

	private SearchResponse nativeResponse;
	
	public SearchResponse getNativeResponse() {
		return nativeResponse;
	}
	
	public void setNativeResponse(SearchResponse nativeResponse) {
		this.nativeResponse = nativeResponse;
	}
}
