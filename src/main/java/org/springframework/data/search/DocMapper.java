package org.springframework.data.search;

public interface DocMapper<T> {

	T docMap(Document doc);
}
