package com.readytalk.makrut.db.command;

import static com.readytalk.makrut.db.handlers.DelegatingResultSetHandlers.listHandler;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.readytalk.makrut.db.BasicTestDataFramework;
import com.readytalk.makrut.db.MakrutDBFactory;
import com.readytalk.makrut.db.handlers.DelegatingListHandler;
import com.readytalk.makrut.db.handlers.RowHandlers;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

@SuppressWarnings("AssertEqualsBetweenInconvertibleTypes")
public class QueryIntegrationTest extends BasicTestDataFramework {

	private final DelegatingListHandler<ImmutableMap<String, Object>> listHandler = listHandler(
			RowHandlers.<Object>mapRowHandler());
	private final MapHandler mapHandler = new MapHandler();

	private MakrutDBFactory makrutFactory;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		super.setUp();

		makrutFactory = injector.getInstance(MakrutDBFactory.class);
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}


	@Test
	public void select_OverAllRows_ReturnsAllRows() throws Exception {
		Query<ImmutableList<ImmutableMap<String, Object>>> query = makrutFactory.query(listHandler,
				"select * from test order by id");


		ImmutableList<ImmutableMap<String, Object>> result = query.submitAndGet();

		assertEquals(dataSet, result);
	}

	@Test
	public void select_OverOneRow_ReturnsResult() throws Exception {
		Query<Map<String, Object>> query = makrutFactory.query(mapHandler, "select * from test where id = ?", 1);

		Map<String, Object> result = query.submitAndGet();

		assertEquals(ImmutableMap.<String, Object>of("ID", 1, "VAL", "test"), result);
	}

	@Test
	public void select_WithNoResults_ReturnsNoRows() throws Exception {
		Query<ImmutableList<ImmutableMap<String, Object>>> query = makrutFactory.query(listHandler,
				"select * from test where id < ?", 0);


		ImmutableList<ImmutableMap<String, Object>> result = query.submitAndGet();

		assertThat(result.size(), is(equalTo(0)));
	}

	@Test
	public void select_WhenUpdateIsInvalid_ThrowsWrappedSQLException() throws Exception {
		Query<ImmutableList<ImmutableMap<String, Object>>> query = makrutFactory.query(listHandler, "select");

		try {
			query.submitAndGet();
		} catch (ExecutionException ex) {
			assertTrue("Not an instanceof SQLException.", ex.getCause() instanceof SQLException);
		}
	}
}
