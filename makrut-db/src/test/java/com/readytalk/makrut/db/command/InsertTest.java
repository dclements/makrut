package com.readytalk.makrut.db.command;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import com.google.common.base.Optional;
import com.readytalk.makrut.MakrutExecutor;
import com.readytalk.makrut.db.ConnectionSupplier;
import com.readytalk.makrut.db.command.Insert;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class InsertTest {

	private static final String SQL = "select * from test where id > ? and val like ?";

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
	private PreparedStatement stmt;

	@Mock
	private ResultSet rs;

	@Mock
	private ResultSetHandler<Object> handler;

	private Insert<Object> insert;

	@Before
	@SuppressFBWarnings({ "OBL_UNSATISFIED_OBLIGATION" })
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(conn.prepareStatement(anyString(), anyInt())).thenReturn(stmt);
		when(stmt.getGeneratedKeys()).thenReturn(rs);
		when(handler.handle(eq(rs))).thenReturn(obj);

		insert = new Insert<Object>(executor, runner, Optional.<String>absent(), transaction, handler, SQL, 1, "%");
	}

	@After
	public void tearDown() {

	}

	@Test
	public void call_WhenCalled_ReturnsResultOfGeneratedKeys() throws Exception {
		when(transaction.getConnection()).thenReturn(conn);

		assertEquals(obj, insert.call());

		verify(conn).prepareStatement(eq(SQL), eq(Statement.RETURN_GENERATED_KEYS));
	}

	@Test
	@SuppressFBWarnings({ "ODR_OPEN_DATABASE_RESOURCE", "OBL_UNSATISFIED_OBLIGATION" })
	public void call_WhenCalled_ClosesInOrder() throws Exception {
		when(transaction.getConnection()).thenReturn(conn);

		insert.call();

		InOrder order = inOrder(rs, stmt, transaction);

		order.verify(rs).close();
		order.verify(stmt).close();
		order.verify(transaction).close(conn);
	}
}
