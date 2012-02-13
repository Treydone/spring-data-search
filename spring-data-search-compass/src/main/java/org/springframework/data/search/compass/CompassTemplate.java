package org.springframework.data.search.compass;

import java.util.ArrayList;
import java.util.List;

import org.compass.core.Compass;
import org.compass.core.CompassHit;
import org.compass.core.CompassHits;
import org.springframework.data.search.Document;
import org.springframework.data.search.QueryResponse;
import org.springframework.data.search.SearchTemplate;

public class CompassTemplate extends SearchTemplate {

	private final org.compass.core.CompassTemplate compassTemplate;

	public CompassTemplate(Compass compass) {
		this(new org.compass.core.CompassTemplate(compass));
	}

	public CompassTemplate(org.compass.core.CompassTemplate compassTemplate) {
		super();
		this.compassTemplate = compassTemplate;
	}

	@Override
	public QueryResponse query(String query) {

		long startTime = System.currentTimeMillis();
		CompassHits results = compassTemplate.find(query);
		long endTime = System.currentTimeMillis();

		CompassQueryResponse queryResponse = new CompassQueryResponse();

		// Setting the native response...
		queryResponse.setNativeResponse(results);

		// ... the elapsed time...
		queryResponse.setElapsedTime(endTime - startTime);

		List<CompassDocument> documents = new ArrayList<CompassDocument>(results.getLength());

		for (CompassHit compassHit : results) {
			CompassDocument document = new CompassDocument(compassHit.getResource());
			documents.add(document);
		}

		queryResponse.setDocuments(documents);
		return queryResponse;
	}

	@Override
	public String add(Document document) {
		compassTemplate.create(document);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteById(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteById(List<String> ids) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteByQuery(String query) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteAll() {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(String query) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateInBatch(String query) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isAlive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub

	}

	@Override
	protected Document buildNewDocument() {
		// TODO Auto-generated method stub
		return null;
	}

}
