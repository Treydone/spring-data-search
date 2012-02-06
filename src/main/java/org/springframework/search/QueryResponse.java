package org.springframework.search;

import java.util.List;

public interface QueryResponse {

	List<? extends Document> getDocuments();
	
	List<? extends Failure> getFailures();
	
	Object getNativeResponse();
}
