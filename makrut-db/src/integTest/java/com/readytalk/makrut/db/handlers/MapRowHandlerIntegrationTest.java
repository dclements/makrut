package com.readytalk.makrut.db.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.ExecutionException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.readytalk.makrut.db.BasicTestDataFramework;
import com.readytalk.makrut.db.MakrutDBFactory;
import com.readytalk.makrut.db.command.Query;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MapRowHandlerIntegrationTest extends BasicTestDataFramework {

	private MakrutDBFactory makrutFactory;

	@Before
	public void setUp() throws Exception {
		super.setUp();

		makrutFactory = injector.getInstance(MakrutDBFactory.class);
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void handle_StandardResult_ReturnsMappingInUpperCase() throws Exception {

		ImmutableList<ImmutableMap<String, Object>> expected = ImmutableList.<ImmutableMap<String, Object>>builder()
				.add(ImmutableMap.<String, Object>of("ID", 1, "VAL", "test"))
				.build();

		Query<ImmutableList<ImmutableMap<String, Object>>> query = makrutFactory.query(
				new DelegatingListHandler<ImmutableMap<String, Object>>(new MapRowHandler<Object>()),
				"select id, val from test where id = 1");


		ImmutableList<ImmutableMap<String, Object>> result = query.submitAndGet();

		assertEquals(expected, result);
	}

	@Test
	public void handle_RenamedColumn_ReturnsMappingWithNewColumnName() throws Exception {
		// Note that this behavior is different from the DbUtils MapHandler.

		ImmutableList<ImmutableMap<String, Object>> expected = ImmutableList.<ImmutableMap<String, Object>>builder()
				.add(ImmutableMap.<String, Object>of("ID", 1, "VALUE", "test"))
				.build();

		Query<ImmutableList<ImmutableMap<String, Object>>> query = makrutFactory.query(
				new DelegatingListHandler<ImmutableMap<String, Object>>(new MapRowHandler<Object>()),
				"select id, val as value from test where id = 1");


		ImmutableList<ImmutableMap<String, Object>> result = query.submitAndGet();

		assertEquals(expected, result);
	}

	@Test
	public void handle_NullValue_ThrowsNPE() throws Exception {

		Query<ImmutableList<ImmutableMap<String, Object>>> query = makrutFactory.query(
				new DelegatingListHandler<ImmutableMap<String, Object>>(new MapRowHandler<Object>()),
				"select id, null as value from test where id = 1");

		try {
			query.submitAndGet();
			fail("Expected exception.");
		} catch (ExecutionException ex) {
			assertTrue(ex.getCause() instanceof NullPointerException);
		}
	}
}
