package org.springframework.data.search.core.solr;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
import org.springframework.dao.DataAccessException;
import org.springframework.data.search.Document;
import org.springframework.data.search.QueryResponse;
import org.springframework.data.search.SearchTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author Vincent Devillers
 */
public class SolrTemplate extends SearchTemplate implements SolrOperations {

	private static final String DEFAULT_DOCUMENT_ID_FIELD = "id";
	private SolrServer searchServer;

	private StreamingUpdateSolrServer indexServer = null;

	private boolean autoCommit = true;

	private boolean allowStreaming = false;

	private int queueSize;

	private int threadCount;

	private String documentIdField = DEFAULT_DOCUMENT_ID_FIELD;

	private boolean autoGenerateIdField = true;

	public SolrTemplate(String solrServerUrl) throws MalformedURLException {
		this(new CommonsHttpSolrServer(solrServerUrl));
	}

	public SolrTemplate(String... solrServerUrls) throws MalformedURLException {
		this(new LBHttpSolrServer(solrServerUrls));
	}

	public SolrTemplate(SolrServer searchServer) {
		super();
		this.searchServer = searchServer;
		setExceptionTranslator(new SolrExceptionTranslator());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();

		if (allowStreaming) {
			if (searchServer instanceof CommonsHttpSolrServer) {
				indexServer = new StreamingUpdateSolrServer(((CommonsHttpSolrServer) searchServer).getBaseURL(), queueSize, threadCount);
			} else {
				throw new IllegalArgumentException("Cannot allow streaming update on solr server other than CommonsHttpSolrServer instance!");
			}
		}

		Assert.hasText(documentIdField, "The document id field have to be setted");
	}

	@Override
	protected Document buildNewDocument() {
		return new SolrDocument(new org.apache.solr.common.SolrDocument());
	}

	@Override
	public QueryResponse query(String query) throws DataAccessException {

		SolrQueryResponse queryResponse = new SolrQueryResponse();

		SolrQuery solrQuery = new SolrQuery(query);
		org.apache.solr.client.solrj.response.QueryResponse solrQueryResponse = null;
		try {
			solrQueryResponse = searchServer.query(solrQuery);
			queryResponse.setNativeResponse(solrQueryResponse);
		} catch (SolrServerException e) {
			throw potentiallyConvertCheckedException(new RuntimeException(e.getCause()));
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
		String id = addIdToDocumentIfEnabled(document);
		org.apache.solr.common.SolrDocument solrDocument = new org.apache.solr.common.SolrDocument();
		solrDocument.putAll(document);
		try {
			addDocument(solrDocument);
			if (isAutoCommit()) {
				commit();
			}
		} catch (Exception e) {
			throw potentiallyConvertCheckedException(new RuntimeException(e.getCause()));
		}
		return id;
	}

	private String addIdToDocumentIfEnabled(Document document) {
		String id = String.valueOf(document.get(documentIdField));
		if (!StringUtils.hasText(id) && autoGenerateIdField) {
			id = UUID.randomUUID().toString();
			document.put(documentIdField, id);
		}
		return id;
	}

	@Override
	public List<String> add(List<Document> documents) {
		org.apache.solr.common.SolrDocument solrDocument;
		List<String> ids = new ArrayList<String>(documents.size());

		for (Document document : documents) {
			ids.add(addIdToDocumentIfEnabled(document));
			solrDocument = new org.apache.solr.common.SolrDocument();
			solrDocument.putAll(document);
			try {
				addDocument(solrDocument);
			} catch (Exception e) {
				throw potentiallyConvertCheckedException(new RuntimeException(e.getCause()));
			}
		}

		try {
			if (isAutoCommit()) {
				commit();
			}
		} catch (Exception e) {
			throw potentiallyConvertCheckedException(new RuntimeException(e.getCause()));
		}
		return ids;
	}

	@Override
	public void deleteById(String id) {
		try {
			if (allowStreaming) {
				indexServer.deleteById(id);
			} else {
				searchServer.deleteById(id);
			}
			if (isAutoCommit()) {
				commit();
			}
		} catch (Exception e) {
			throw potentiallyConvertCheckedException(new RuntimeException(e.getCause()));
		}
	}

	@Override
	public void deleteById(List<String> ids) {
		try {
			if (allowStreaming) {
				indexServer.deleteById(ids);
			} else {
				searchServer.deleteById(ids);
			}
			if (isAutoCommit()) {
				commit();
			}
		} catch (Exception e) {
			throw potentiallyConvertCheckedException(new RuntimeException(e.getCause()));
		}

	}

	@Override
	public void deleteByQuery(String query) {
		try {
			if (allowStreaming) {
				indexServer.deleteByQuery(query);
			} else {
				searchServer.deleteByQuery(query);
			}
			if (isAutoCommit()) {
				commit();
			}
		} catch (Exception e) {
			throw potentiallyConvertCheckedException(new RuntimeException(e.getCause()));
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
			if (allowStreaming) {
				indexServer.commit();
			}
		} catch (Exception e) {
			throw potentiallyConvertCheckedException(new RuntimeException(e.getCause()));
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
			if (allowStreaming) {
				indexServer.commit();
			}
		} catch (Exception e) {
			throw potentiallyConvertCheckedException(new RuntimeException(e.getCause()));
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

	public void setDocumentIdField(String documentIdField) {
		this.documentIdField = documentIdField;
	}

	public void setAutoGenerateIdField(boolean autoGenerateIdField) {
		this.autoGenerateIdField = autoGenerateIdField;
	}
}
