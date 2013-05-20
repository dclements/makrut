package com.readytalk.makrut.db.command;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;

import com.google.common.base.Optional;
import com.readytalk.makrut.MakrutExecutor;
import com.readytalk.makrut.db.ConnectionSupplier;
import com.readytalk.makrut.db.command.Query;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class QueryTest {

	private final Object obj = new Object();

	@Mock
	private QueryRunner runner;

	@Mock
	private MakrutExecutor executor;

	@Mock
	private ConnectionSupplier transaction;

	@Mock
	private Connection conn;

	@Mock
	private ResultSetHandler<Object> handler;

	private Query<Object> query;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		query = new Query<Object>(executor, runner, Optional.<String>absent(), transaction, handler,
				"select * from test where id > ? and val like ? ", 1, "%");
	}

	@After
	public void tearDown() {

	}

	@Test
	@SuppressWarnings("unchecked")
	public void call_WithArguments_ExecutesSQLOnRunner() throws Exception {
		when(transaction.getConnection()).thenReturn(conn);
		when(runner.query(any(Connection.class), anyString(), any(ResultSetHandler.class), any(Object.class),
				anyVararg())).thenReturn(obj);

		assertEquals(obj, query.call());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void call_WithoutArguments_ExecutesSQLOnRunner() throws Exception {
		when(transaction.getConnection()).thenReturn(conn);
		when(runner.query(any(Connection.class), anyString(), any(ResultSetHandler.class), anyVararg())).thenReturn(obj);

		Query<Object> query2 = new Query<Object>(executor, runner, Optional.<String>absent(), transaction, handler,
				"select * from test where id > 0 and val like '%'");

		assertEquals(obj, query2.call());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void call_WhenCalled_DelegatesCloseToSupplier() throws Exception {
		when(transaction.getConnection()).thenReturn(conn);
		when(runner.query(any(Connection.class), anyString(), any(ResultSetHandler.class), any(Object.class),
				anyVararg())).thenReturn(obj);

		query.call();

		verify(transaction).close(conn);
	}


}
