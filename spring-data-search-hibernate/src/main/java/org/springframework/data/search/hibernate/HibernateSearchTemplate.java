package org.springframework.data.search.hibernate;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.springframework.data.search.Document;
import org.springframework.data.search.InvalidOperationException;
import org.springframework.data.search.QueryResponse;
import org.springframework.data.search.SearchTemplate;
import org.springframework.data.search.core.IndexedFieldDocMapper;
import org.springframework.data.search.core.QueryBuilder;

public class HibernateSearchTemplate extends SearchTemplate implements HibernateSearchOperations {

	private static final Version LUCENE_VERSION = Version.LUCENE_35;

	private final FullTextEntityManager fullTextEntityManager;

	private final QueryParser queryParser;

	public HibernateSearchTemplate(EntityManager entityManager) {
		this(Search.getFullTextEntityManager(entityManager), new QueryParser(LUCENE_VERSION, null, new SimpleAnalyzer(LUCENE_VERSION)));
	}

	public HibernateSearchTemplate(FullTextEntityManager fullTextEntityManager, QueryParser queryParser) {
		super();
		this.fullTextEntityManager = fullTextEntityManager;
		this.queryParser = queryParser;
	}

	@Override
	public QueryResponse query(String query) {
		Query fullTextQuery;
		List result;
		try {
			fullTextQuery = fullTextEntityManager.createFullTextQuery(queryParser.parse(query));
			result = fullTextQuery.getResultList();
		} catch (Exception e) {
			throw potentiallyConvertCheckedException(new RuntimeException(e.getCause()));
		}
		return null;
	}

	@Override
	public <T> List<T> query(String query, Object[] params, Class<T> clazz) {
		Query fullTextQuery;
		try {
			fullTextQuery = fullTextEntityManager.createFullTextQuery(queryParser.parse(QueryBuilder.resolveParams(query, params)), clazz);
			return fullTextQuery.getResultList();
		} catch (Exception e) {
			throw potentiallyConvertCheckedException(new RuntimeException(e.getCause()));
		}
	}

	@Override
	public String addBean(Object bean) {
		fullTextEntityManager.persist(bean);
		// TODO return id
		return null;
	}

	@Override
	public List<String> addBeans(List<Object> beans) {
		fullTextEntityManager.persist(beans);
		// TODO return id
		return null;
	}

	@Override
	public List<String> add(List<Document> documents) {
		throw new InvalidOperationException("");
	}

	@Override
	public String add(Document document) {
		throw new InvalidOperationException("");
	}

	@Override
	public void deleteById(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteById(List<String> ids) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteByQuery(String query) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteAll() {
		// TODO Auto-generated method stub
	}

	@Override
	public void update(String query) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateInBatch(String query) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isAlive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub

	}

	@Override
	protected Document buildNewDocument() {
		// TODO Auto-generated method stub
		return null;
	}

}
