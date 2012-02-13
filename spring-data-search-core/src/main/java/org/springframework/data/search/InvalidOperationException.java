package org.springframework.data.search;

@SuppressWarnings("serial")
public class InvalidOperationException extends SearchException {

	public InvalidOperationException(String message) {
		super(message);
	}

	public InvalidOperationException(String message, Throwable cause) {
		super(message, cause);
	}

}
