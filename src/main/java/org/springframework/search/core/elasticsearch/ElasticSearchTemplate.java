package org.springframework.search.core.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.action.admin.cluster.ping.single.SinglePingRequest;
import org.elasticsearch.action.admin.cluster.ping.single.SinglePingResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.action.bulk.BulkRequestBuilder;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.search.SearchHit;
import org.springframework.search.Document;
import org.springframework.search.QueryResponse;
import org.springframework.search.SearchTemplate;

public class ElasticSearchTemplate extends SearchTemplate implements ElasticSearchOperations {

	private static final String DEFAULT = "default";
	private Client client;

	public ElasticSearchTemplate(Client client) {
		super();
		this.client = client;
	}

	@Override
	protected Document buildNewDocument() {
		return new ElasticSearchDocument(new HashMap<String, Object>());
	}

	@Override
	public QueryResponse query(String query) {

		ElasticSearchQueryResponse queryResponse = new ElasticSearchQueryResponse();
		SearchResponse response = client.prepareSearch(DEFAULT).setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setQuery(query).setFrom(0).setSize(60).setExplain(true).execute().actionGet();

		queryResponse.setNativeResponse(response);
		List<ElasticSearchDocument> documents = new ArrayList<ElasticSearchDocument>((int) response.getHits().getTotalHits());
		for (SearchHit hit : response.getHits()) {
			Map<String, Object> source = hit.getSource();
			if (hit != null) {
				documents.add(new ElasticSearchDocument(source));
			}
		}

		return queryResponse;
	}

	@Override
	public boolean isAlive() {
		SinglePingResponse singlePingResponse;
		try {
			singlePingResponse = client.admin().cluster().ping(new SinglePingRequest()).get();
			return singlePingResponse != null;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void add(Document document) {
		try {
			XContentBuilder object = buildJsonFromDocument(document);
			IndexResponse response = client.prepareIndex(DEFAULT, DEFAULT, "1").setSource(object).execute().actionGet();
		} catch (Exception e) {
		}
	}

	private XContentBuilder buildJsonFromDocument(Document document) throws IOException {
		XContentBuilder object = XContentFactory.jsonBuilder().startObject();
		for (Entry<String, Object> entry : document.entrySet()) {
			object.field(entry.getKey(), entry.getValue());
		}
		object.endObject();
		return object;
	}

	@Override
	public void add(List<Document> documents) {
		BulkRequestBuilder bulkRequest = client.prepareBulk();
		for (Document document : documents) {
			try {
				bulkRequest.add(client.prepareIndex(DEFAULT, DEFAULT, "1").setSource(buildJsonFromDocument(document)));
			} catch (Exception e) {
			}

		}
		BulkResponse bulkResponse = bulkRequest.execute().actionGet();
	}

	@Override
	public void deleteById(String id) {
		DeleteResponse response = client.prepareDelete(DEFAULT, DEFAULT, "1").execute().actionGet();
	}

	@Override
	public void deleteById(List<String> ids) {
		BulkRequestBuilder bulkRequest = client.prepareBulk();
		for (String string : ids) {
			bulkRequest.add(client.prepareDelete(DEFAULT, DEFAULT, "1"));
		}
		BulkResponse bulkResponse = bulkRequest.execute().actionGet();
	}

	@Override
	public void deleteByQuery(String query) {
		DeleteByQueryRequest request = new DeleteByQueryRequest();
		request.query(query);
		client.deleteByQuery(request).actionGet();
	}

	@Override
	public void update(String query) {

	}

	@Override
	public void updateInBatch(String query) {

	}

}
