package org.springframework.data.search;

import org.springframework.dao.DataAccessException;


@SuppressWarnings("serial")
public class SearchException extends DataAccessException {

	public SearchException(String message) {
		super(message);
	}

	public SearchException(String message, Throwable cause) {
		super(message, cause);
	}

}
