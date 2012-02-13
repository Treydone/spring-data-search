package org.springframework.data.search;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.search.annotation.Indexed;
import org.springframework.data.search.core.DocMapperQueryResponseExtractor;
import org.springframework.data.search.core.IndexedFieldDocMapper;
import org.springframework.data.search.core.QueryBuilder;
import org.springframework.data.search.core.SearchExceptionTranslator;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public abstract class SearchTemplate implements SearchOperations, InitializingBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchTemplate.class);

	protected abstract Document buildNewDocument();

	private SearchExceptionTranslator exceptionTranslator = new SearchExceptionTranslator();

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(getExceptionTranslator(), "Exception translator cannot be null!");
	}

	@Override
	public QueryResponse query(String query, Object[] params) {
		query = QueryBuilder.resolveParams(query, params);
		return query(query);
	}

	@Override
	public <T> T query(String query, QueryResponseExtractor<T> qre) {
		return query(query, null, qre);
	}

	@Override
	public <T> List<T> query(String query, DocMapper<T> qre) {
		return query(query, null, new DocMapperQueryResponseExtractor<T>(qre));
	}

	@Override
	public <T> T query(String query, Object[] params, QueryResponseExtractor<T> qre) {
		Assert.notNull(query, "Query must not be null");
		Assert.notNull(qre, "QueryResponseExtractor must not be null");
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Executing query [" + query + "]");
		}
		QueryResponse response = query(query, params);
		return qre.extractData(response);
	}

	@Override
	public <T> List<T> query(String query, Object[] params, DocMapper<T> qre) {
		return query(query, params, new DocMapperQueryResponseExtractor<T>(qre));
	}

	@Override
	public List<String> add(Document... documents) {
		return add(Arrays.asList(documents));
	}

	@Override
	public List<String> add(List<Document> documents) {
		List<String> ids = new ArrayList<String>(documents.size());
		for (Document document : documents) {
			ids.add(add(document));
		}
		return ids;
	}

	@Override
	public String addBean(Object bean) {
		Document document = extractDocumentFromObject(bean);
		return add(document);
	}

	@Override
	public <T> List<T> query(String query, Class<T> clazz) {
		return query(query, new IndexedFieldDocMapper<T>(clazz));
	}

	@Override
	public <T> List<T> query(String query, Object[] params, Class<T> clazz) {
		return query(query, params, new IndexedFieldDocMapper<T>(clazz));
	}

	protected Document extractDocumentFromObject(Object bean) {
		Map<String, Object> results = new HashMap<String, Object>();
		Field[] fields = bean.getClass().getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			Indexed annotation = field.getAnnotation(Indexed.class);
			if (annotation != null) {
				try {
					String fieldName = annotation.fieldName();
					if (!StringUtils.hasText(fieldName)) {
						fieldName = field.getName();
					}
					results.put(fieldName, field.get(bean));
				} catch (Exception e) {
					// TODO
				}
			}
		}

		if (results.isEmpty()) {
			throw new InvalidDocumentException("The document has no indexed field!");
		}

		Document document = buildNewDocument();
		document.putAll(results);

		return document;
	}

	@Override
	public List<String> addBeans(Object... beans) {
		return addBeans(Arrays.asList(beans));
	}

	@Override
	public List<String> addBeans(List<Object> beans) {
		List<Document> documents = new ArrayList<Document>(beans.size());

		for (Object bean : beans) {
			Document document = extractDocumentFromObject(bean);
			documents.add(document);
		}

		return add(documents);
	}

	protected RuntimeException potentiallyConvertCheckedException(RuntimeException ex) {
		RuntimeException resolved = getExceptionTranslator().translateExceptionIfPossible(ex);
		return resolved == null ? ex : resolved;
	}
	
	public SearchExceptionTranslator getExceptionTranslator() {
		return exceptionTranslator;
	}

	public void setExceptionTranslator(SearchExceptionTranslator exceptionTranslator) {
		this.exceptionTranslator = exceptionTranslator;
	}
}
