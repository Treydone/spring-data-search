package org.springframework.search;

import org.springframework.core.NestedRuntimeException;

@SuppressWarnings("serial")
public class SearchException extends NestedRuntimeException {

	public SearchException(String message) {
		super(message);
	}

	public SearchException(String message, Throwable cause) {
		super(message, cause);
	}

}
