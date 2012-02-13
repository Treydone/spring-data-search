package org.springframework.data.search.core;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.search.DocMapper;
import org.springframework.data.search.Document;
import org.springframework.data.search.QueryResponse;
import org.springframework.data.search.QueryResponseExtractor;
import org.springframework.util.Assert;

public class DocMapperQueryResponseExtractor<T> implements QueryResponseExtractor<List<T>> {

	private final DocMapper<T> docMapper;

	/**
	 * Create a new RowMapperResultSetExtractor.
	 * 
	 * @param docMapper the DocMapper which creates an object for each entry
	 */
	public DocMapperQueryResponseExtractor(DocMapper<T> docMapper) {
		Assert.notNull(docMapper, "DocMapper is required");
		this.docMapper = docMapper;
	}

	public List<T> extractData(QueryResponse response) {
		List<T> results = new ArrayList<T>(response.getDocuments().size());
		for (Document doc : response.getDocuments()) {
			results.add(this.docMapper.docMap(doc));
		}
		return results;
	}

}
