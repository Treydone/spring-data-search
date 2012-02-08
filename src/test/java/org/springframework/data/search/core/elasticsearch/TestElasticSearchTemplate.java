package org.springframework.data.search.core.elasticsearch;

import static junit.framework.Assert.assertEquals;
import static org.elasticsearch.client.Requests.clusterHealthRequest;
import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;
import static org.elasticsearch.common.unit.TimeValue.timeValueSeconds;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.logging.slf4j.Slf4jESLoggerFactory;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.data.search.DocMapper;
import org.springframework.data.search.Document;
import org.springframework.data.search.DocumentMappingException;
import org.springframework.data.search.InvalidDocumentException;
import org.springframework.data.search.InvalidParamsException;
import org.springframework.data.search.InvalidQueryException;
import org.springframework.data.search.QueryResponse;
import org.springframework.data.search.core.AnythingToBeIndexedBean;
import org.springframework.data.search.core.DummyBean;
import org.springframework.data.search.core.EmptyBean;
import org.springframework.data.search.core.SimpleDocument;
import org.springframework.data.search.core.WronglyTypedBean;

public class TestElasticSearchTemplate {

	private final static File indexerDataDir = new File("indexer-tests");

	private static Node node;

	private static ElasticSearchTemplate searchOperations;

	@BeforeClass
	public static void beforeClass() throws Exception {
		FileUtils.deleteQuietly(indexerDataDir);
		ESLoggerFactory.setDefaultFactory(new Slf4jESLoggerFactory());
		System.out.println("Starting Elastic");
		node = NodeBuilder.nodeBuilder().local(true).settings(settingsBuilder().put("path.data", indexerDataDir.getAbsolutePath()).put("http.port", 9200).build()).node();
		node.start();

		node.client().admin().cluster().health(clusterHealthRequest().waitForYellowStatus().timeout(timeValueSeconds(60))).actionGet();

		System.out.println("Started Elastic");

		searchOperations = new ElasticSearchTemplate(node);
		searchOperations.afterPropertiesSet();

		// Create index
		node.client().admin().indices().create(Requests.createIndexRequest(searchOperations.getIndexName())).actionGet();
		node.client().admin().indices().refresh(Requests.refreshRequest(searchOperations.getIndexName())).actionGet();
	}

	@AfterClass
	public static void afterClass() {
		node.close();
		FileUtils.deleteQuietly(indexerDataDir);
	}

	@After
	public void afterEachTest() {
		searchOperations.deleteAll();
	}

	@Test
	public void addDocumentAndCheckResult() {

		Document document = new SimpleDocument();
		document.put("id", "123");
		document.put("name", "toto");

		searchOperations.add(document);
		searchOperations.refresh();

		String query = "{\"query\" : {\"field\" : { \"id\" : \"123\"}}}";

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
		searchOperations.refresh();

		String query = "{\"query\" : {\"field\" : { \"id\" : \"123\"}}}";

		QueryResponse response1 = searchOperations.query(query);
		assertNotNull(response1);
		assertNotNull(response1.getDocuments());

		assertEquals(1, response1.getDocuments().size());
		assertEquals("toto", response1.getDocuments().get(0).get("name"));


		query = "{\"query\" : {\"field\" : { \"id\" : \"124\"}}}";
		QueryResponse response2 = searchOperations.query(query);
		assertNotNull(response2);
		assertNotNull(response2.getDocuments());

		assertEquals(1, response2.getDocuments().size());
		assertEquals("tata", response2.getDocuments().get(0).get("name"));

	}

	@Test
	public void addDummyBeanAndCheckResult() {

		Date today = new Date();
		DummyBean bean = new DummyBean("1234", today, "dummy name");

		searchOperations.addBean(bean);
		searchOperations.refresh();

		QueryResponse response = searchOperations.query("{\"query\" : {\"field\" : { \"id\" : \"1234\"}}}");
		assertNotNull(response);
		assertNotNull(response.getDocuments());

		assertEquals(1, response.getDocuments().size());
		assertEquals("dummy name", response.getDocuments().get(0).get("name"));
		assertEquals("1234", response.getDocuments().get(0).get("id"));
		assertEquals(today.getTime(), response.getDocuments().get(0).get("last_modified"));
	}

	@Test
	public void addDummyBeansAndCheckResult() {

		Date today = new Date();
		DummyBean bean1 = new DummyBean("1234", today, "dummy name");
		DummyBean bean2 = new DummyBean("2345", today, "dummy name 2");

		searchOperations.addBeans(bean1, bean2);
		searchOperations.refresh();

		QueryResponse response = searchOperations.query("{\"sort\" : [{ \"id\" : {\"order\" : \"asc\"} }], \"query\" : {\"match_all\" : { }}}");
		assertNotNull(response);
		assertNotNull(response.getDocuments());

		assertEquals(2, response.getDocuments().size());

		assertEquals("dummy name", response.getDocuments().get(0).get("name"));
		assertEquals("1234", response.getDocuments().get(0).get("id"));
		assertEquals(today.getTime(), response.getDocuments().get(0).get("last_modified"));

		assertEquals("dummy name 2", response.getDocuments().get(1).get("name"));
		assertEquals("2345", response.getDocuments().get(1).get("id"));
		assertEquals(today.getTime(), response.getDocuments().get(1).get("last_modified"));
	}

	@Test(expected = InvalidDocumentException.class)
	public void addEmptyBeanAndCheckResult() {

		EmptyBean bean = new EmptyBean();
		searchOperations.addBean(bean);

	}

	@Test(expected = InvalidDocumentException.class)
	public void addEmptyBeansAndCheckResult() {

		EmptyBean bean1 = new EmptyBean();
		EmptyBean bean2 = new EmptyBean();

		searchOperations.addBeans(bean1, bean2);
	}

	@Test(expected = InvalidDocumentException.class)
	public void addAnythingToBeIndexedBeanAndCheckResult() {

		AnythingToBeIndexedBean bean = new AnythingToBeIndexedBean();
		searchOperations.addBean(bean);

	}

	@Test(expected = InvalidDocumentException.class)
	public void addAnythingToBeIndexedBeansAndCheckResult() {

		AnythingToBeIndexedBean bean1 = new AnythingToBeIndexedBean();
		AnythingToBeIndexedBean bean2 = new AnythingToBeIndexedBean();

		searchOperations.addBeans(bean1, bean2);
	}

	@Test
	public void findAndMapADocumentWithAMapper() {

		Date today = new Date();
		DummyBean bean1 = new DummyBean("1234", today, "dummy name");
		DummyBean bean2 = new DummyBean("2345", today, "dummy name 2");

		searchOperations.addBeans(bean1, bean2);
		searchOperations.refresh();

		List<DummyBean> beans = searchOperations.query("{\"query\" : {\"field\" : { \"id\" : \"1234\"}}}", new DocMapper<DummyBean>() {

			@Override
			public DummyBean docMap(Document doc) {
				return new DummyBean((String) doc.get("id"), (Date) doc.get("last_modified"), (String) doc.get("name"));
			}

		});

		assertNotNull(beans);
		assertEquals(1, beans.size());
		assertEquals(bean1, beans.get(0));

	}

	@Test
	public void findAndMapADocumentWithAClass() {

		Date today = new Date();
		DummyBean bean1 = new DummyBean("1234", today, "dummy name");
		DummyBean bean2 = new DummyBean("2345", today, "dummy name 2");

		searchOperations.addBeans(bean1, bean2);
		searchOperations.refresh();

		List<DummyBean> beans = searchOperations.query("{\"query\" : {\"field\" : { \"id\" : \"1234\"}}}", DummyBean.class);

		assertNotNull(beans);
		assertEquals(1, beans.size());
		assertEquals(bean1, beans.get(0));

	}

	@Test(expected = DocumentMappingException.class)
	public void findAndMapADocumentWithAClassNotCorretlyTyped() {

		Date today = new Date();
		WronglyTypedBean bean1 = new WronglyTypedBean(1234, today);
		WronglyTypedBean bean2 = new WronglyTypedBean(2345, today);

		searchOperations.addBeans(bean1, bean2);

		searchOperations.query("{\"query\" : {\"field\" : { \"id\" : \"1234\"}}}", WronglyTypedBean.class);

	}

	@Test
	public void searchWithManyParams() {

		Document document = new SimpleDocument();
		document.put("id", "123");
		document.put("name", "toto");

		searchOperations.add(document);
		searchOperations.refresh();

		QueryResponse response = searchOperations.query("{\"query\" : {\"field\" : { \"id\" : \"{id}\", \"name\" : \"{name}\"}}}", new Object[] { 123, "toto" });
		assertNotNull(response);
		assertNotNull(response.getDocuments());

		assertEquals(1, response.getDocuments().size());
		assertEquals("toto", response.getDocuments().get(0).get("name"));
	}

	@Test
	public void searchWithOneParam() {

		Document document = new SimpleDocument();
		document.put("id", "123");
		document.put("name", "toto");

		searchOperations.add(document);
		searchOperations.refresh();

		QueryResponse response = searchOperations.query("{\"query\" : {\"field\" : { \"id\" : \"{id}\"}}}", new Object[] { 123 });
		assertNotNull(response);
		assertNotNull(response.getDocuments());

		assertEquals(1, response.getDocuments().size());
		assertEquals("toto", response.getDocuments().get(0).get("name"));
	}

	@Test(expected = InvalidQueryException.class)
	public void searchWithInvalidQuery() {

		searchOperations.query("<$'(-/*");

	}

	@Test(expected = InvalidParamsException.class)
	public void searchWithNoEnoughParams() {

		searchOperations.query("{\"query\" : {\"field\" : { \"id\" : \"{id}\", \"name\" : \"{name}\"}}}", new Object[] { 123 });

	}

	@Test(expected = InvalidParamsException.class)
	public void searchWithToMuchParams() {

		searchOperations.query("{\"query\" : {\"field\" : { \"id\" : \"{id}\"}}}", new Object[] { 123, "toto" });

	}
}
