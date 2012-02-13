package org.springframework.data.search.compass;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.compass.core.Resource;
import org.springframework.data.search.core.AbstractDocument;

public class CompassDocument extends AbstractDocument {

	private final Resource resource;

	private Float score;

	public CompassDocument(Resource resource) {
		this.resource = resource;
	}

	public boolean containsKey(Object key) {
		return resource.getObject((String) key) != null;
	}

	public Object get(Object key) {
		return resource.getObject((String) key);
	}

	public Float getScore() {
		return score;
	}

	public void setScore(Float score) {
		this.score = score;
	}

	@Override
	public Resource getNativeDocument() {
		return resource;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object put(String key, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object remove(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<String> keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Object> values() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}
}
