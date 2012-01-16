package org.springframework.search;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.search.annotation.Indexed;
import org.springframework.search.core.DocMapperQueryResponseExtractor;
import org.springframework.util.Assert;

public abstract class SearchTemplate implements SearchOperations {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SearchTemplate.class);

	/** Captures URI template variable names. */
	private static final Pattern NAMES_PATTERN = Pattern.compile("\\{([^/]+?)\\}");

	protected abstract Document buildNewDocument();

	@Override
	public QueryResponse query(String query, Object[] params) {
		Iterator<Object> iterator = Arrays.asList(params).iterator();
		Matcher matcher = NAMES_PATTERN.matcher(query);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			Object variableValue = iterator.next();
			String variableValueString = getVariableValueAsString(variableValue);
			String replacement = Matcher.quoteReplacement(variableValueString);
			matcher.appendReplacement(sb, replacement);
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
	public void add(List<Document> documents) {
		for (Document document : documents) {
			add(document);
		}
	}

	@Override
	public void addBean(Object bean) {
		Document document = buildNewDocument();
		extractDocumentFromObject(bean, document);
		add(document);
	}

	private void extractDocumentFromObject(Object bean, Document document) {
		Field[] fields = bean.getClass().getFields();
		for (Field field : fields) {
			if (field.getAnnotation(Indexed.class) != null) {
				try {
					document.put(field.getName(), field.get(bean));
				} catch (Exception e) {
					// TODO
				}
			}
		}
	}

	@Override
	public void addBeans(List<Object> beans) {
		List<Document> documents = new ArrayList<Document>(beans.size());

		for (Object bean : beans) {
			Document document = buildNewDocument();
			extractDocumentFromObject(bean, document);
			documents.add(document);
		}

		add(documents);
	}
}
