package org.springframework.data.search;

@SuppressWarnings("serial")
public class DocumentNotFoundException extends SearchException {

	public DocumentNotFoundException(String message) {
		super(message);
	}

	public DocumentNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
