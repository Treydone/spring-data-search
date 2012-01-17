package org.springframework.search.core.solr;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.search.Document;
import org.springframework.search.InvalidQueryException;
import org.springframework.search.QueryResponse;
import org.springframework.search.SearchTemplate;

public class SolrTemplate extends SearchTemplate implements SolrOperations {

	private SolrServer solrServer;

	private boolean autoCommit = true;

	public SolrTemplate(SolrServer solrServer) {
		super();
		this.solrServer = solrServer;
	}

	@Override
	protected Document buildNewDocument() {
		return new SolrDocument(new org.apache.solr.common.SolrDocument());
	}

	@Override
	public QueryResponse query(String query) {

		SolrQueryResponse queryResponse = new SolrQueryResponse();

		SolrQuery solrQuery = new SolrQuery(query);
		org.apache.solr.client.solrj.response.QueryResponse solrQueryResponse = null;
		try {
			solrQueryResponse = solrServer.query(solrQuery);
		} catch (SolrServerException e) {
			if (e.getRootCause() instanceof ParseException) {
				throw new InvalidQueryException(query, e.getRootCause());
			}
		}
		if (solrQueryResponse != null) {
			SolrDocumentList results = solrQueryResponse.getResults();
			List<SolrDocument> documents = new ArrayList<SolrDocument>(results.size());
			for (org.apache.solr.common.SolrDocument solrDocument : results) {
				documents.add(new SolrDocument(solrDocument));
			}
			queryResponse.setDocuments(documents);
		}

		return queryResponse;
	}

	@Override
	public boolean isAlive() {
		try {
			SolrPingResponse pingResponse;
			pingResponse = solrServer.ping();
			return pingResponse.getStatus() == 0;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void add(Document document) {
		org.apache.solr.common.SolrDocument solrDocument = new org.apache.solr.common.SolrDocument();
		solrDocument.putAll(document);
		try {
			solrServer.add(ClientUtils.toSolrInputDocument(solrDocument));
			if (isAutoCommit()) {
				commit();
			}
		} catch (Exception e) {
			// TODO
		}
	}

	@Override
	public void add(List<Document> documents) {
		org.apache.solr.common.SolrDocument solrDocument;
		for (Document document : documents) {
			solrDocument = new org.apache.solr.common.SolrDocument();
			solrDocument.putAll(document);
			try {
				solrServer.add(ClientUtils.toSolrInputDocument(solrDocument));
			} catch (Exception e) {
				// TODO
			}
		}
		try {
			if (isAutoCommit()) {
				commit();
			}
		} catch (Exception e) {
			// TODO
		}
	}

	@Override
	public void deleteById(String id) {
		try {
			solrServer.deleteById(id);
			if (isAutoCommit()) {
				commit();
			}
		} catch (Exception e) {
			// TODO
		}
	}

	@Override
	public void deleteById(List<String> ids) {
		try {
			solrServer.deleteById(ids);
			if (isAutoCommit()) {
				commit();
			}
		} catch (Exception e) {
			// TODO
		}

	}

	@Override
	public void deleteByQuery(String query) {
		try {
			solrServer.deleteByQuery(query);
			if (isAutoCommit()) {
				commit();
			}
		} catch (Exception e) {
			// TODO
		}
	}

	@Override
	public void update(String query) {

	}

	@Override
	public void updateInBatch(String query) {

	}

	@Override
	public void commit() {
		try {
			solrServer.commit();
		} catch (Exception e) {
			// TODO
		}
	}

	public boolean isAutoCommit() {
		return autoCommit;
	}

	public void setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

}
