package org.springframework.data.search.compass;

import org.compass.core.CompassHits;
import org.springframework.data.search.core.AbstractQueryResponse;

public class CompassQueryResponse extends AbstractQueryResponse {

	private CompassHits nativeResponse;
	
	public CompassHits getNativeResponse() {
		return nativeResponse;
	}
	
	public void setNativeResponse(CompassHits results) {
		this.nativeResponse = results;
	}
	
}
