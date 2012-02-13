package org.springframework.data.search;

import org.springframework.dao.UncategorizedDataAccessException;

public class UncategorizedSearchException extends UncategorizedDataAccessException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5195852142889313504L;

	public UncategorizedSearchException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
