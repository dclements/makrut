package com.readytalk.makrut.db.handlers;

import static org.junit.Assert.assertEquals;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.readytalk.makrut.db.BasicTestDataFramework;
import com.readytalk.makrut.db.MakrutDBFactory;
import com.readytalk.makrut.db.command.Query;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
public class OptionalMapRowHandlerIntegrationTest extends BasicTestDataFramework {

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

		ImmutableList<ImmutableMap<String, Optional<?>>> expected
				= ImmutableList.<ImmutableMap<String, Optional<?>>>builder()
				.add(ImmutableMap.<String, Optional<?>>of("ID", Optional.of(1), "VAL", Optional.of("test")))
				.build();


		Query<ImmutableList<ImmutableMap<String, Optional<Object>>>> query = makrutFactory.query(
				DelegatingResultSetHandlers.listHandler(RowHandlers.optionalMapRowHandler()),
				"select id, val from test where id = 1");


		ImmutableList<ImmutableMap<String, Optional<Object>>> result = query.submitAndGet();

		assertEquals(expected, result);
	}

	@Test
	public void handle_RenamedColumn_ReturnsMappingWithNewColumnName() throws Exception {
		// Note that this behavior is different from the DbUtils MapHandler.

		ImmutableList<ImmutableMap<String, Optional<?>>> expected
				= ImmutableList.<ImmutableMap<String, Optional<?>>>builder()
				.add(ImmutableMap.<String, Optional<?>>of("ID", Optional.of(1), "VALUE", Optional.of("test")))
				.build();

		Query<ImmutableList<ImmutableMap<String, Optional<Object>>>> query = makrutFactory.query(
				DelegatingResultSetHandlers.listHandler(RowHandlers.optionalMapRowHandler()),
				"select id, val as value from test where id = 1");


		ImmutableList<ImmutableMap<String, Optional<Object>>> result = query.submitAndGet();

		assertEquals(expected, result);
	}

	@Test
	public void handle_NullValue_UsesAbsent() throws Exception {

		ImmutableList<ImmutableMap<String, Optional<?>>> expected
				= ImmutableList.<ImmutableMap<String, Optional<?>>>builder()
				.add(ImmutableMap.<String, Optional<?>>of("ID", Optional.of(1), "VAL", Optional.absent()))
				.build();

		Query<ImmutableList<ImmutableMap<String, Optional<Object>>>> query = makrutFactory.query(
				DelegatingResultSetHandlers.listHandler(RowHandlers.optionalMapRowHandler()),
				"select id, null as val from test where id = 1");


		ImmutableList<ImmutableMap<String, Optional<Object>>> result = query.submitAndGet();

		assertEquals(expected, result);
	}
}
