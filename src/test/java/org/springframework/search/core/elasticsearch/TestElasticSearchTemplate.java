package org.springframework.search.core.elasticsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.search.Document;
import org.springframework.search.QueryResponse;
import org.springframework.search.SearchOperations;
import org.springframework.search.core.SimpleDocument;

public class TestElasticSearchTemplate {

	private static Client client;

	private static Node node;

	private SearchOperations searchOperations;

	@BeforeClass
	public static void beforeClass() {
		node = NodeBuilder.nodeBuilder().client(true).node();
		client = node.client();
//		CreateIndexRequest request = new CreateIndexRequest("default");
//		client.admin().indices().create(request).actionGet();
	}

	@Before
	public void beforeEachTest() {
		searchOperations = new ElasticSearchTemplate(client);
	}

	@AfterClass
	public static void afterClass() {
		node.close();
	}

	@Test
	public void addDocumentAndCheckResult() {

		Document document = new SimpleDocument();
		document.put("id", "123");
		document.put("name", "toto");

		searchOperations.add(document);

		String query = "{" + "\"terms\" : {" + "\"id\" : [ \"123\"]," + " \"minimum_match\" : 1" + "}" + "}";

		QueryResponse response = searchOperations.query(query);
		assertNotNull(response);
		assertNotNull(response.getDocuments());

		assertEquals(1, response.getDocuments().size());
		assertEquals("toto", response.getDocuments().get(0).get("name"));
	}

	@Test
	public void addDocumentsAndCheckResult() {

		Document document1 = new SimpleDocument();
		document1.put("id", "123");
		document1.put("name", "toto");

		Document document2 = new SimpleDocument();
		document2.put("id", "124");
		document2.put("name", "tata");

		searchOperations.add(document1, document2);

		QueryResponse response1 = searchOperations.query("id:123");
		assertNotNull(response1);
		assertNotNull(response1.getDocuments());

		assertEquals(1, response1.getDocuments().size());
		assertEquals("toto", response1.getDocuments().get(0).get("name"));

		QueryResponse response2 = searchOperations.query("id:124");
		assertNotNull(response2);
		assertNotNull(response2.getDocuments());

		assertEquals(1, response2.getDocuments().size());
		assertEquals("tata", response2.getDocuments().get(0).get("name"));
	}
}
