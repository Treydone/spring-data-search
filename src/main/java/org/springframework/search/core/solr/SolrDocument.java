package org.springframework.search.core.solr;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.springframework.search.core.AbstractDocument;

public class SolrDocument extends AbstractDocument {

	private final org.apache.solr.common.SolrDocument solrDocument;

	public SolrDocument(org.apache.solr.common.SolrDocument solrDocument) {
		this.solrDocument = solrDocument;
	}

	public boolean containsKey(Object key) {
		return solrDocument.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return solrDocument.containsValue(value);
	}

	public Set<Entry<String, Object>> entrySet() {
		return solrDocument.entrySet();
	}

	public Object get(Object key) {
		return solrDocument.get(key);
	}

	public boolean isEmpty() {
		return solrDocument.isEmpty();
	}

	public Set<String> keySet() {
		return solrDocument.keySet();
	}

	public int size() {
		return solrDocument.size();
	}

	public Collection<Object> values() {
		return solrDocument.values();
	}

	@Override
	public Object put(String key, Object value) {
		return solrDocument.put(key, value);
	}

	@Override
	public Object remove(Object key) {
		return solrDocument.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		solrDocument.putAll(m);
	}

	@Override
	public void clear() {
		solrDocument.clear();
	}

}
