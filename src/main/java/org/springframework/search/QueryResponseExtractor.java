package org.springframework.search;

public interface QueryResponseExtractor<T> {

	T extractData(QueryResponse response);

}
