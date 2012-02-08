package org.springframework.data.search.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.data.search.InvalidParamsException;
import org.springframework.data.search.core.QueryBuilder;

public class TestQueryBuilder {

	@Test(expected = InvalidParamsException.class)
	public void searchWithNoEnoughParams() {
		QueryBuilder.resolveParams("id:{id} and name:{name}", new Object[] { 123 });
	}

	@Test(expected = InvalidParamsException.class)
	public void searchWithToMuchParams() {
		QueryBuilder.resolveParams("id:{id}", new Object[] { 123, "toto" });
	}

	@Test
	public void searchWithOneParam() {
		String result = QueryBuilder.resolveParams("id:{id}", new Object[] { 123 });
		assertEquals("id:123", result);
	}

	@Test
	public void searchWithManyParams() {
		String result = QueryBuilder.resolveParams("id:{id} and name:{name} or else", new Object[] { 123, "toto" });
		assertEquals("id:123 and name:toto or else", result);
	}
	
	@Test
	public void jsonSearchWithManyParams() {
		String result = QueryBuilder.resolveParams("{\"query\" : {\"field\" : { \"id\" : \"{id}\"}}}", new Object[] { 123 });
		assertEquals("{\"query\" : {\"field\" : { \"id\" : \"123\"}}}", result);
	}
}
