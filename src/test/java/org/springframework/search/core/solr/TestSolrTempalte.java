package org.springframework.search.core.solr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.search.DocMapper;
import org.springframework.search.Document;
import org.springframework.search.InvalidDocumentException;
import org.springframework.search.QueryResponse;
import org.springframework.search.SearchOperations;
import org.springframework.search.core.AnythingToBeIndexedBean;
import org.springframework.search.core.DummyBean;
import org.springframework.search.core.EmptyBean;
import org.springframework.search.core.SimpleDocument;
import org.xml.sax.SAXException;

public class TestSolrTempalte {

	private static EmbeddedSolrServer embeddedSolrServer;

	private SearchOperations searchOperations;

	@BeforeClass
	public static void beforeClass() throws IOException, ParserConfigurationException, SAXException {
		File configFile = new ClassPathResource("solr/solr.xml").getFile();
		CoreContainer coreContainer = new CoreContainer();
		coreContainer.load(configFile.getParentFile().getAbsolutePath(), configFile);
		embeddedSolrServer = new EmbeddedSolrServer(coreContainer, "default");
	}

	@Before
	public void beforeEachTest() throws SolrServerException, IOException {
		searchOperations = new SolrTemplate(embeddedSolrServer);
		searchOperations.deleteByQuery("*:*");
	}

	@Test
	public void addDocumentAndCheckResult() {

		Document document = new SimpleDocument();
		document.put("id", "123");
		document.put("name", "toto");

		searchOperations.add(document);

		QueryResponse response = searchOperations.query("id:123");
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

	@Test
	public void addDummyBeanAndCheckResult() {

		Date today = new Date();
		DummyBean bean = new DummyBean(1234, today, "dummy name");

		searchOperations.addBean(bean);

		QueryResponse response = searchOperations.query("id:1234");
		assertNotNull(response);
		assertNotNull(response.getDocuments());

		assertEquals(1, response.getDocuments().size());
		assertEquals("dummy name", response.getDocuments().get(0).get("name"));
		assertEquals("1234", response.getDocuments().get(0).get("id"));
		assertEquals(today, response.getDocuments().get(0).get("last_modified"));
	}

	@Test
	public void addDummyBeansAndCheckResult() {

		Date today = new Date();
		DummyBean bean1 = new DummyBean(1234, today, "dummy name");
		DummyBean bean2 = new DummyBean(2345, today, "dummy name 2");

		searchOperations.addBeans(bean1, bean2);

		QueryResponse response = searchOperations.query("id:*");
		assertNotNull(response);
		assertNotNull(response.getDocuments());

		assertEquals(2, response.getDocuments().size());

		assertEquals("dummy name", response.getDocuments().get(0).get("name"));
		assertEquals("1234", response.getDocuments().get(0).get("id"));
		assertEquals(today, response.getDocuments().get(0).get("last_modified"));

		assertEquals("dummy name 2", response.getDocuments().get(1).get("name"));
		assertEquals("2345", response.getDocuments().get(1).get("id"));
		assertEquals(today, response.getDocuments().get(1).get("last_modified"));
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
		DummyBean bean1 = new DummyBean(1234, today, "dummy name");
		DummyBean bean2 = new DummyBean(2345, today, "dummy name 2");

		searchOperations.addBeans(bean1, bean2);

		List<DummyBean> beans = searchOperations.query("id:1234", new DocMapper<DummyBean>() {

			@Override
			public DummyBean docMap(Document doc) {
				return new DummyBean(Integer.valueOf((String) doc.get("id")), (Date) doc.get("last_modified"), (String) doc.get("name"));
			}

		});

		assertNotNull(beans);
		assertEquals(1, beans.size());
		assertEquals(bean1, beans.get(0));

	}

	@Test
	public void findAndMapADocumentWithAClass() {

		Date today = new Date();
		DummyBean bean1 = new DummyBean(1234, today, "dummy name");
		DummyBean bean2 = new DummyBean(2345, today, "dummy name 2");

		searchOperations.addBeans(bean1, bean2);

		List<DummyBean> beans = searchOperations.query("id:1234", DummyBean.class);

		assertNotNull(beans);
		assertEquals(1, beans.size());
		assertEquals(bean1, beans.get(0));

	}
}
