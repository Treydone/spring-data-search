package org.springframework.search;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.search.annotation.Indexed;
import org.springframework.search.core.DocMapperQueryResponseExtractor;
import org.springframework.search.core.IndexedFieldDocMapper;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public abstract class SearchTemplate implements SearchOperations {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchTemplate.class);

	/** Captures URI template variable names. */
	private static final Pattern NAMES_PATTERN = Pattern.compile("\\{([^/]+?)\\}");

	protected abstract Document buildNewDocument();

	@Override
	public QueryResponse query(String query, Object[] params) {
		if (!ArrayUtils.isEmpty(params)) {
			Iterator<Object> iterator = Arrays.asList(params).iterator();
			Matcher matcher = NAMES_PATTERN.matcher(query);

			int i=0;
			StringBuffer sb = new StringBuffer(query.length());
			while (matcher.find()) {
				i++;
				if (params.length < i) {
					throw new InvalidParamsException("Some parameters are missing:" + Arrays.toString(params));
				}
				Object variableValue = iterator.next();
				String variableValueString = getVariableValueAsString(variableValue);
				String replacement = Matcher.quoteReplacement(variableValueString);
				matcher.appendReplacement(sb, replacement);
			}
			if (params.length > i) {
				throw new InvalidParamsException("Too much parameters for this query!" + Arrays.toString(params));
			}
			query = sb.toString();
		}
		return query(query);
	}

	private static String getVariableValueAsString(Object variableValue) {
		return variableValue != null ? variableValue.toString() : "";
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
}
