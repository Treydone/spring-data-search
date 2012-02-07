package org.springframework.search.core.solr;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.client.solrj.impl.StreamingUpdateSolrServer;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.search.Document;
import org.springframework.search.InvalidQueryException;
import org.springframework.search.QueryResponse;
import org.springframework.search.SearchException;
import org.springframework.search.SearchTemplate;

/**
 * @author Vincent Devillers
 */
public class SolrTemplate extends SearchTemplate implements SolrOperations, InitializingBean {

	private SolrServer searchServer;

	private StreamingUpdateSolrServer indexServer = null;

	private boolean autoCommit = true;

	private boolean allowStreaming = false;

	private int queueSize;

	private int threadCount;

	public SolrTemplate(String solrServerUrl) throws MalformedURLException {
		this(new CommonsHttpSolrServer(solrServerUrl));
	}

	public SolrTemplate(String... solrServerUrls) throws MalformedURLException {
		this(new LBHttpSolrServer(solrServerUrls));
	}

	public SolrTemplate(SolrServer searchServer) {
		super();
		this.searchServer = searchServer;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (allowStreaming) {
			if (searchServer instanceof CommonsHttpSolrServer) {
				indexServer = new StreamingUpdateSolrServer(((CommonsHttpSolrServer) searchServer).getBaseURL(), queueSize, threadCount);
			} else {
				throw new IllegalArgumentException("Cannot allow streaming update on solr server other than CommonsHttpSolrServer instance!");
			}
		}

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
			solrQueryResponse = searchServer.query(solrQuery);
			queryResponse.setNativeResponse(solrQueryResponse);
		} catch (SolrServerException e) {
			if (e.getRootCause() instanceof ParseException) {
				throw new InvalidQueryException(query, e.getRootCause());
			}
			throw new SearchException(query, e);
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
			pingResponse = searchServer.ping();
			return pingResponse.getStatus() == 0;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public String add(Document document) {
		org.apache.solr.common.SolrDocument solrDocument = new org.apache.solr.common.SolrDocument();
		solrDocument.putAll(document);
		// TODO id document
		String id = null;
		try {
			UpdateResponse updateResponse = addDocument(solrDocument);
			if (isAutoCommit()) {
				commit();
			}
		} catch (Exception e) {
			// TODO
		}
		return id;
	}

	@Override
	public List<String> add(List<Document> documents) {
		org.apache.solr.common.SolrDocument solrDocument;
		for (Document document : documents) {
			solrDocument = new org.apache.solr.common.SolrDocument();
			solrDocument.putAll(document);
			try {
				UpdateResponse updateResponse = addDocument(solrDocument);
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
		// TODO
		return null;
	}

	@Override
	public void deleteById(String id) {
		try {
			UpdateResponse updateResponse;
			if (allowStreaming) {
				updateResponse = indexServer.deleteById(id);
			} else {
				updateResponse = searchServer.deleteById(id);
			}
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
			UpdateResponse updateResponse;
			if (allowStreaming) {
				updateResponse = indexServer.deleteById(ids);
			} else {
				updateResponse = searchServer.deleteById(ids);
			}
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
			UpdateResponse updateResponse;
			if (allowStreaming) {
				updateResponse = indexServer.deleteByQuery(query);
			} else {
				updateResponse = searchServer.deleteByQuery(query);
			}
			if (isAutoCommit()) {
				commit();
			}
		} catch (Exception e) {
			// TODO
		}
	}

	@Override
	public void deleteAll() {
		deleteByQuery("*:*");
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
			searchServer.commit();
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

	@Override
	public SolrServer getSolrServer() {
		return searchServer;
	}

	@Override
	public void refresh() {
		try {
			searchServer.optimize();
		} catch (Exception e) {
			// TODO
		}
	}

	private UpdateResponse addDocument(org.apache.solr.common.SolrDocument solrDocument) throws SolrServerException, IOException {
		UpdateResponse updateResponse;
		if (allowStreaming) {
			updateResponse = indexServer.add(ClientUtils.toSolrInputDocument(solrDocument));
		} else {
			updateResponse = searchServer.add(ClientUtils.toSolrInputDocument(solrDocument));
		}
		return updateResponse;
	}

	public void setSearchServer(SolrServer searchServer) {
		this.searchServer = searchServer;
	}

	public void setAllowStreaming(boolean allowStreaming) {
		this.allowStreaming = allowStreaming;
	}

	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

}
