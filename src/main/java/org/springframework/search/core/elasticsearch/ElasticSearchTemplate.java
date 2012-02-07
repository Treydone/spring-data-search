package org.springframework.search.core.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.ElasticSearchException;
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
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.action.bulk.BulkRequestBuilder;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.search.Document;
import org.springframework.search.DocumentNotFoundException;
import org.springframework.search.Failure;
import org.springframework.search.InvalidQueryException;
import org.springframework.search.QueryResponse;
import org.springframework.search.SearchException;
import org.springframework.search.SearchTemplate;
import org.springframework.search.core.SimpleFailure;
import org.springframework.util.StringUtils;

public class ElasticSearchTemplate extends SearchTemplate implements ElasticSearchOperations, InitializingBean {

	private static final String DEFAULT = "default";

	private String indexName = DEFAULT;

	private Node node;

	private ObjectMapper objectMapper;

	public ElasticSearchTemplate(Node node) {
		super();
		this.node = node;
	}

	public ElasticSearchTemplate(Node node, String indexName) {
		super();
		this.indexName = indexName;
		this.node = node;
	}

	@Override
	public void afterPropertiesSet() {
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
		} catch (ElasticSearchException e) {
			if (e.getDetailedMessage().contains("Failed to parse source")) {
				throw new InvalidQueryException(query, e.unwrapCause());
			}
			throw new SearchException(query, e);
		}
		
		ElasticSearchQueryResponse queryResponse = new ElasticSearchQueryResponse();
		client.close();

		// Setting the native response...
		queryResponse.setNativeResponse(response);

		// ...the failures...
		ShardSearchFailure[] shardsFailures = response.getShardFailures();
		List<Failure> failures = new ArrayList<Failure>(shardsFailures.length);

		for (ShardSearchFailure shardSearchFailure : shardsFailures) {
			failures.add(new SimpleFailure("Failure on index '" + shardSearchFailure.index() + "' due to '" + shardSearchFailure.reason() + "'"));
		}
		queryResponse.setFailures(failures);

		// ... and the docs...
		List<ElasticSearchDocument> documents = new ArrayList<ElasticSearchDocument>((int) response.getHits().getTotalHits());
		for (SearchHit hit : response.getHits()) {
			if (hit != null) {
				try {
					documents.add(new ElasticSearchDocument(objectMapper.readValue(hit.source(), ElasticSearchDocument.class)));
				} catch (Exception e) {
					throw new SearchException("Unable to parse the response!", e);
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
		} catch (Exception e) {
			throw new SearchException("Unable to add the document!", e);
		}
		client.close();
		return id;
	}

	@Override
	public List<String> add(List<Document> documents) {
		Client client = node.client();

		BulkRequestBuilder bulkRequest = client.prepareBulk();
		for (Document document : documents) {
			try {
				IndexRequest request = buildIndexRequestForDocument(document);
				bulkRequest.add(request);
			} catch (Exception e) {
				throw new SearchException("Unable to add the document!", e);
			}
		}
		BulkResponse bulkResponse = bulkRequest.execute().actionGet();
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

	private IndexRequest buildIndexRequestForDocument(Document document) throws IOException {
		IndexRequest request = Requests.indexRequest(indexName).source(buildJsonFromDocument(document));
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

	private String buildJsonFromDocument(Document document) throws IOException {
		return objectMapper.writeValueAsString(document);
	}

	@Override
	public void deleteById(String id) {
		Client client = node.client();
		DeleteResponse response = client.delete(Requests.deleteRequest(indexName).id(id)).actionGet();
		throwExceptionIfNotFound(id, response);
		client.close();
	}

	private void throwExceptionIfNotFound(String id, DeleteResponse response) {
		if (response.isNotFound()) {
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
		BulkResponse bulkResponse = bulkRequest.execute().actionGet();
		int i = 0;
		for (BulkItemResponse bulkItemResponse : bulkResponse.items()) {
			if (bulkItemResponse.response() instanceof DeleteResponse) {
				throwExceptionIfNotFound(ids.get(i), ((DeleteResponse) bulkItemResponse.response()));
			}
			i++;
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
