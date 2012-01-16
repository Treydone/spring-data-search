package org.springframework.search;

import org.springframework.core.NestedCheckedException;

@SuppressWarnings("serial")
public abstract class SearchException extends NestedCheckedException {

	public SearchException(String message) {
		super(message);
	}

	public SearchException(String message, Throwable cause) {
		super(message, cause);
	}

}
