package org.springframework.search;

@SuppressWarnings("serial")
public class InvalidDocumentException extends SearchException {

	public InvalidDocumentException(String message) {
		super(message);
	}

	public InvalidDocumentException(String message, Throwable cause) {
		super(message, cause);
	}

}
