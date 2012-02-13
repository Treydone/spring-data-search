package org.springframework.data.search.core.solr;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.data.search.core.AbstractQueryResponse;

public class SolrQueryResponse extends AbstractQueryResponse {

	private QueryResponse nativeResponse;
	
	public QueryResponse getNativeResponse() {
		return nativeResponse;
	}
	
	public void setNativeResponse(QueryResponse nativeResponse) {
		this.nativeResponse = nativeResponse;
	}
	
}
