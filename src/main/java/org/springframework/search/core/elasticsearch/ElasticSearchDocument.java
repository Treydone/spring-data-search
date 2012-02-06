package org.springframework.search.core.elasticsearch;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.springframework.search.core.AbstractDocument;

public class ElasticSearchDocument extends AbstractDocument {

	private final Map<String, Object> source;

	private String type;

	private String id;

	public ElasticSearchDocument(Map<String, Object> source) {
		this.source = source;
	}

	public boolean containsKey(Object key) {
		return source.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return source.containsValue(value);
	}

	public Set<Entry<String, Object>> entrySet() {
		return source.entrySet();
	}

	public Object get(Object key) {
		return source.get(key);
	}

	public boolean isEmpty() {
		return source.isEmpty();
	}

	public Set<String> keySet() {
		return source.keySet();
	}

	public int size() {
		return source.size();
	}

	public Collection<Object> values() {
		return source.values();
	}

	@Override
	public Object put(String key, Object value) {
		return source.put(key, value);
	}

	@Override
	public Object remove(Object key) {
		return source.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		source.putAll(m);
	}

	@Override
	public void clear() {
		source.clear();
	}

	@Override
	public Object getNativeDocument() {
		return source;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
