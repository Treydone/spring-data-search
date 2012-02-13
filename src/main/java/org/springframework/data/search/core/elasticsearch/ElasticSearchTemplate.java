package org.springframework.data.search.core.elasticsearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.action.admin.cluster.ping.single.SinglePingRequest;
import org.elasticsearch.action.admin.cluster.ping.single.SinglePingResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.action.bulk.BulkRequestBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.springframework.data.search.Document;
import org.springframework.data.search.DocumentNotFoundException;
import org.springframework.data.search.InvalidDocumentException;
import org.springframework.data.search.QueryResponse;
import org.springframework.data.search.SearchTemplate;
import org.springframework.util.StringUtils;

public class ElasticSearchTemplate extends SearchTemplate implements ElasticSearchOperations {

	private static final String DEFAULT = "default";

	private String indexName;

	private Node node;

	private ObjectMapper objectMapper;

	public ElasticSearchTemplate(Node node) {
		this(node, DEFAULT);
	}

	public ElasticSearchTemplate(Node node, String indexName) {
		super();
		this.indexName = indexName;
		this.node = node;
		setExceptionTranslator(new ElasticSearchExceptionTranslator());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		if (objectMapper == null) {
			objectMapper = new ObjectMapper();
		}

	}

	@Override
	protected Document buildNewDocument() {
		return new ElasticSearchDocument(new HashMap<String, Object>());
	}

	@Override
	public QueryResponse query(String query) {

		Client client = node.client();
		SearchRequest request = Requests.searchRequest();

		request.source(query);
		request.indices(indexName);
		request.searchType(SearchType.QUERY_AND_FETCH);

		SearchResponse response;
		try {
			response = client.search(request).actionGet();
		} catch (RuntimeException e) {
			throw potentiallyConvertCheckedException(e);
		}
		client.close();

		ElasticSearchQueryResponse queryResponse = new ElasticSearchQueryResponse();

		// Setting the native response...
		queryResponse.setNativeResponse(response);
		
		//... the elapsed time...
		queryResponse.setElapsedTime(response.getTookInMillis());

		// ... and the docs...
		List<ElasticSearchDocument> documents = new ArrayList<ElasticSearchDocument>((int) response.getHits().getTotalHits());
		for (SearchHit hit : response.getHits()) {
			if (hit != null) {
				try {
					ElasticSearchDocument document = objectMapper.readValue(hit.source(), ElasticSearchDocument.class);
					documents.add(new ElasticSearchDocument(document));
				} catch (Exception e) {
					throw new InvalidDocumentException("The retrieved document " + hit.source() + " is invalid", e);
				}
			}
		}
		queryResponse.setDocuments(documents);

		return queryResponse;
	}

	@Override
	public boolean isAlive() {
		SinglePingResponse singlePingResponse;
		try {
			singlePingResponse = node.client().admin().cluster().ping(new SinglePingRequest()).get();
			return singlePingResponse != null;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public String add(Document document) {
		Client client = node.client();
		String id = null;
		try {
			IndexResponse indexResponse = client.index(buildIndexRequestForDocument(document)).actionGet();
			id = indexResponse.getId();
		} catch (RuntimeException e) {
			throw potentiallyConvertCheckedException(e);
		}
		client.close();
		return id;
	}

	@Override
	public List<String> add(List<Document> documents) {
		Client client = node.client();

		BulkRequestBuilder bulkRequest = client.prepareBulk();
		for (Document document : documents) {
				IndexRequest request = buildIndexRequestForDocument(document);
				bulkRequest.add(request);
		}
		
		BulkResponse bulkResponse;
		try {
			bulkResponse = bulkRequest.execute().actionGet();
		} catch (RuntimeException e) {
			throw potentiallyConvertCheckedException(e);
		}
		
		client.close();

		List<String> ids = new ArrayList<String>(bulkResponse.items().length);

		for (BulkItemResponse bulkItemResponse : bulkResponse.items()) {
			if (bulkItemResponse.response() instanceof IndexResponse) {
				ids.add(((IndexResponse) bulkItemResponse.response()).id());
			}
		}
		return ids;
	}

	protected Document extractDocumentFromObject(Object bean) {
		Document document = super.extractDocumentFromObject(bean);
		((ElasticSearchDocument) document).setType(bean.getClass().getName().toLowerCase());
		return document;
	}

	private IndexRequest buildIndexRequestForDocument(Document document) {
		String json;
		try {
			json = objectMapper.writeValueAsString(document);
		} catch (Exception e) {
			throw new InvalidDocumentException("The document " + document + " is invalid", e);
		}
		IndexRequest request = Requests.indexRequest(indexName).source(json);
		String type = DEFAULT;
		if (document instanceof ElasticSearchDocument) {
			String docType = ((ElasticSearchDocument) document).getType();
			if (StringUtils.hasText(docType)) {
				type = docType;
			}

			String id = ((ElasticSearchDocument) document).getId();
			if (StringUtils.hasText(id)) {
				request.id(id);
			}
		}
		request.type(type);
		return request;
	}

	@Override
	public void deleteById(String id) {
		Client client = node.client();
		DeleteResponse response;
		try {
			response = client.delete(Requests.deleteRequest(indexName).id(id)).actionGet();
		} catch (RuntimeException e) {
			throw potentiallyConvertCheckedException(e);
		}
		throwExceptionIfNotFound(id, response);
		client.close();
	}

	private void throwExceptionIfNotFound(String id, DeleteResponse response) {
		if (response != null && response.isNotFound()) {
			throw new DocumentNotFoundException("Document with id:" + id + " is not found!");
		}
	}

	@Override
	public void deleteById(List<String> ids) {
		Client client = node.client();

		BulkRequestBuilder bulkRequest = client.prepareBulk();
		for (String id : ids) {
			bulkRequest.add(Requests.deleteRequest(indexName).id(id));
		}

		BulkResponse bulkResponse;
		try {
			bulkResponse = bulkRequest.execute().actionGet();
		} catch (RuntimeException e) {
			throw potentiallyConvertCheckedException(e);
		}

		if (bulkResponse != null) {
			int i = 0;
			for (BulkItemResponse bulkItemResponse : bulkResponse.items()) {
				if (bulkItemResponse.response() instanceof DeleteResponse) {
					throwExceptionIfNotFound(ids.get(i), ((DeleteResponse) bulkItemResponse.response()));
				}
				i++;
			}
		}

		client.close();
	}

	@Override
	public void deleteByQuery(String query) {
		node.client().deleteByQuery(Requests.deleteByQueryRequest(indexName).query(query)).actionGet();
	}

	public void deleteAll() {
		deleteByQuery(new MatchAllQueryBuilder().toString());
	}

	@Override
	public void refresh() {
		Client client = node.client();
		client.admin().indices().refresh(Requests.refreshRequest(indexName)).actionGet();
		client.close();
	}

	@Override
	public void update(String query) {

	}

	@Override
	public void updateInBatch(String query) {

	}

	public String getIndexName() {
		return indexName;
	}

	public Node getNode() {
		return node;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
}
