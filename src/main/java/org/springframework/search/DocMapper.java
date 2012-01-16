package org.springframework.search;

public interface DocMapper<T> {

	T docMap(Document doc);
}
