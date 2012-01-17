package org.springframework.search.core.solr;

import org.apache.solr.client.solrj.SolrServer;


public interface SolrOperations {

	/**
	 * Performs an explicit commit, causing pending documents to be committed for indexing
	 */
	void commit();
	
	/**
	 * @return the used solrServer
	 */
	SolrServer getSolrServer();
}
