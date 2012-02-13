package org.springframework.data.search;

public class InvalidQueryException extends SearchException {

	private String query;
	/**
	 * 
	 */
	private static final long serialVersionUID = -4350812455697611819L;

	public InvalidQueryException(String query) {
		super(query);
	}

	public InvalidQueryException(String query, Throwable cause) {
		super(query, cause);
	}

	public String getQuery() {
		return query;
	}

}
