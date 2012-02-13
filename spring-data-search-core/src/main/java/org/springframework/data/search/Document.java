package org.springframework.data.search;

import java.util.Map;

public interface Document extends Map<String, Object> {

	Object getNativeDocument();
	
	Float getScore();
}
