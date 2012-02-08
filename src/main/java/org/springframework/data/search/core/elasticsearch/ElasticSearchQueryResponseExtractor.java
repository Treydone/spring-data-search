package org.springframework.data.search.core.elasticsearch;

import java.util.List;

import org.elasticsearch.action.search.SearchResponse;
import org.springframework.data.search.QueryResponse;
import org.springframework.data.search.QueryResponseExtractor;

public abstract class ElasticSearchQueryResponseExtractor<T> implements QueryResponseExtractor<List<T>> {

	public List<T> extractData(QueryResponse queryResponse) {

		if (queryResponse instanceof SearchResponse) {
			SearchResponse nativeResponse = (SearchResponse) queryResponse.getNativeResponse();
			return extractNativeData(nativeResponse);
		}
		return null;

	}

	public abstract List<T> extractNativeData(SearchResponse nativeResponse);

}
