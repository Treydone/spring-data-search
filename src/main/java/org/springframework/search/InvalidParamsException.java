package org.springframework.search;

public class InvalidParamsException extends SearchException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4350812455697611819L;

	public InvalidParamsException(String message) {
		super(message);
	}

	public InvalidParamsException(String message, Throwable cause) {
		super(message, cause);
	}

}
