package org.springframework.search;

import java.util.List;

/**
 * Interface specifying a basic set of JDBC operations. Implemented by {@link SearchTemplate}. Not often used directly, but a useful option to enhance testability, as it can easily be mocked
 * or stubbed.
 * 
 * @author Vincent Devillers
 * @see SearchTemplate
 */
public interface SearchOperations {

	/**
	 * Performs a query to the Solr server
	 * 
	 * @return a response object holding documents.
	 */
	QueryResponse query(String query);

	/**
	 * Performs a query to the Solr server
	 * 
	 * @param params an object holding all the parameters
	 * @return a response object holding documents.
	 */
	QueryResponse query(String query, Object[] params);

	<T> T query(String query, QueryResponseExtractor<T> qre);

	<T> T query(String query, Object[] params, QueryResponseExtractor<T> qre);

	<T> List<T> query(String query, DocMapper<T> qre);

	<T> List<T> query(String query, Class<T> clazz);

	<T> List<T> query(String query, Object[] params, DocMapper<T> qre);

	<T> List<T> query(String query, Object[] params, Class<T> clazz);

	/**
	 * Adds a single document
	 * 
	 * @param document the input document
	 */
	void add(Document document);

	/**
	 * Adds a single bean
	 * 
	 * @param obj the input bean
	 */
	void addBean(Object bean);

	/**
	 * Adds a collection of documents
	 * 
	 * @param docs the collection of documents
	 */
	void add(Document... documents);

	/**
	 * Adds a collection of documents
	 * 
	 * @param docs the collection of documents
	 */
	void add(List<Document> documents);

	/**
	 * Adds a collection of beans
	 * 
	 * @param beans the collection of beans
	 */
	void addBeans(List<Object> beans);

	/**
	 * Adds a collection of beans
	 * 
	 * @param beans the collection of beans
	 */
	void addBeans(Object... beans);

	/**
	 * Deletes a single document by unique ID
	 * 
	 * @param id the ID of the document to delete
	 */
	void deleteById(String id);

	/**
	 * Deletes a list of documents by unique ID
	 * 
	 * @param ids the list of document IDs to delete
	 */
	void deleteById(List<String> ids);

	/**
	 * Deletes documents from the index based on a query
	 * 
	 * @param query the query expressing what documents to delete
	 */
	void deleteByQuery(String query);

	void update(String query);

	void updateInBatch(String query);

	/**
	 * Issues a ping request to check if the server is alive
	 */
	boolean isAlive();

}
