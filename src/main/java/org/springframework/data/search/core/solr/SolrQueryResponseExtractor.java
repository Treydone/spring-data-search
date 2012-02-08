package org.springframework.data.search.core.solr;

import java.util.List;

import org.springframework.data.search.QueryResponse;
import org.springframework.data.search.QueryResponseExtractor;

public abstract class SolrQueryResponseExtractor<T> implements QueryResponseExtractor<List<T>> {

	public List<T> extractData(QueryResponse queryResponse) {

		if (queryResponse instanceof SolrQueryResponse) {
			org.apache.solr.client.solrj.response.QueryResponse nativeResponse = (org.apache.solr.client.solrj.response.QueryResponse) queryResponse.getNativeResponse();
			return extractNativeData(nativeResponse);
		}
		return null;

	}

	public abstract List<T> extractNativeData(org.apache.solr.client.solrj.response.QueryResponse nativeResponse);

}
