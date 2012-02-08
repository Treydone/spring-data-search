package org.springframework.data.search;

public interface QueryResponseExtractor<T> {

	T extractData(QueryResponse response);

}
