package org.springframework.data.search.solr;

import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.springframework.dao.DataAccessException;
import org.springframework.data.search.InvalidQueryException;
import org.springframework.data.search.SearchServerException;
import org.springframework.data.search.UncategorizedSearchException;
import org.springframework.data.search.core.SearchExceptionTranslator;

public class SolrExceptionTranslator extends SearchExceptionTranslator {

	@Override
	public DataAccessException translateExceptionIfPossible(RuntimeException ex) {

		if (ex.getCause() instanceof SolrServerException) {
			SolrServerException solrServerException = (SolrServerException) ex.getCause();
			if (solrServerException.getCause() instanceof SolrException) {
				SolrException solrException = (SolrException) solrServerException.getCause();
				if (solrException.getCause() instanceof ParseException) {
					return new InvalidQueryException(((ParseException) solrException.getCause()).getMessage(), solrException.getCause());
				} else {
					ErrorCode errorCode = SolrException.ErrorCode.getErrorCode(solrException.code());
					switch (errorCode) {
					case NOT_FOUND:
					case FORBIDDEN:
					case SERVICE_UNAVAILABLE:
					case SERVER_ERROR:
						return new SearchServerException(solrException.getMessage(), solrException);
					case BAD_REQUEST:
						return new InvalidQueryException(solrException.getMessage(), solrException);
					case UNAUTHORIZED:
					case UNKNOWN:
						return new UncategorizedSearchException(solrException.getMessage(), solrException);
					default:
						break;
					}
				}
			}
		}

		return super.translateExceptionIfPossible(ex);
	}

}
