package org.springframework.data.search;

@SuppressWarnings("serial")
public class DocumentMappingException extends SearchException {

	public DocumentMappingException(String message) {
		super(message);
	}

	public DocumentMappingException(String message, Throwable cause) {
		super(message, cause);
	}

}
