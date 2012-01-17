package org.springframework.search;

import java.util.List;

public interface QueryResponse {

	List<? extends Document> getDocuments();
	
	Object getNativeResponse();
}
