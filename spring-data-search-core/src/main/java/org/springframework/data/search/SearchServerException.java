package org.springframework.data.search;

public class SearchServerException extends SearchException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4350812455697611819L;

	public SearchServerException(String message) {
		super(message);
	}

	public SearchServerException(String message, Throwable cause) {
		super(message, cause);
	}

}
