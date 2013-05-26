package com.readytalk.makrut.db.command;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;

import com.google.common.base.Optional;
import com.readytalk.makrut.MakrutExecutor;
import com.readytalk.makrut.db.ConnectionSupplier;
import com.readytalk.makrut.db.command.Update;
import org.apache.commons.dbutils.QueryRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UpdateTest {
	@Mock
	private QueryRunner runner;

	@Mock
	private MakrutExecutor executor;

	@Mock
	private ConnectionSupplier transaction;

	@Mock
	private Connection conn;

	private Update update;

	@Before
	public void setUp() throws Exception {

		MockitoAnnotations.initMocks(this);

		update = new Update(executor, runner, Optional.<String>absent(), transaction,
				"select * from test where id = ? and val = ?",
				1, 1);
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void call_WhenCalled_ExecutesSQLOnRunner() throws Exception {
		when(transaction.getConnection()).thenReturn(conn);
		when(runner.update(any(Connection.class), anyString(), any(Object.class), anyVararg())).thenReturn(2);

		assertEquals(2, update.call().intValue());
	}

	@Test
	public void call_WhenCalled_DelegatesCloseToSupplier() throws Exception {
		when(transaction.getConnection()).thenReturn(conn);
		when(runner.update(any(Connection.class), anyString(), any(Object.class), anyVararg())).thenReturn(2);

		update.call();

		verify(transaction).close(conn);
	}
}
