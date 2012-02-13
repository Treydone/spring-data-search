package org.springframework.data.search.elasticsearch;

import org.elasticsearch.ElasticSearchException;
import org.springframework.dao.DataAccessException;
import org.springframework.data.search.InvalidQueryException;
import org.springframework.data.search.core.SearchExceptionTranslator;

public class ElasticSearchExceptionTranslator extends SearchExceptionTranslator {

	@Override
	public DataAccessException translateExceptionIfPossible(RuntimeException ex) {

		if (ex instanceof ElasticSearchException) {
			ElasticSearchException elasticSearchException = (ElasticSearchException) ex;
			if (elasticSearchException.getDetailedMessage().contains("Failed to parse source")) {
				throw new InvalidQueryException("", elasticSearchException.unwrapCause());
			}
		}
		return super.translateExceptionIfPossible(ex);
	}

}
