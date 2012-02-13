package org.springframework.data.search.core;

import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.data.search.InvalidParamsException;

public abstract class QueryBuilder {

	private static final Pattern NAMES_PATTERN = Pattern.compile("\\{\\w+\\}");

	private static String getVariableValueAsString(Object variableValue) {
		return variableValue != null ? variableValue.toString() : "";
	}

	public static String resolveParams(String query, Object[] params) {
		if (!ArrayUtils.isEmpty(params)) {
			Iterator<Object> iterator = Arrays.asList(params).iterator();
			Matcher matcher = NAMES_PATTERN.matcher(query);

			int i = 0;
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
			matcher.appendTail(sb);
			
			if (params.length > i) {
				throw new InvalidParamsException("Too much parameters for this query!" + Arrays.toString(params));
			}
			query = sb.toString();
		}
		return query;
	}
}
