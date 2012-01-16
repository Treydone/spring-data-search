package org.springframework.search;

import java.util.List;

public interface SearchOperations {

	QueryResponse query(String query);
	
	QueryResponse query(String query, Object[] params);

	<T> T query(String query, QueryResponseExtractor<T> qre);

	<T> T query(String query, Object[] params, QueryResponseExtractor<T> qre);

	<T> List<T> query(String query, DocMapper<T> qre);
	
	<T> List<T> query(String query, Class<T> clazz);

	<T> List<T> query(String query, Object[] params, DocMapper<T> qre);
	
	<T> List<T> query(String query, Object[] params, Class<T> clazz);

	void add(Document document);
	
	void addBean(Object bean);
	
	void add(Document... documents);
	
	void add(List<Document> documents);
	
	void addBeans(List<Object> beans);
	
	void addBeans(Object... beans);
	
	void deleteById(String id);
	
	void deleteById(List<String> ids);
	
	void deleteByQuery(String query);

	void update(String query);

	void updateInBatch(String query);
	
	boolean isAlive();
	
}
