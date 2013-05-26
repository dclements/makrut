package com.readytalk.makrut.db;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.sql.Connection;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DBTransactionTest {

	@Rule
	public final ExpectedException thrown = ExpectedException.none();

	@Mock
	private ConnectionSupplier supplier;

	@Mock
	private Connection conn;

	private DBTransaction transaction;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		transaction = new DBTransaction(supplier, conn);
	}

	@After
	public void tearDown() {

	}

	@Test
	public void close_WhenClosed_ThrowsISE() throws Exception {
		thrown.expect(IllegalStateException.class);

		transaction.close();

		transaction.close();
	}

	@Test
	public void close_WhenOpen_ClosesConnectionWithSupplier() throws Exception {
		transaction.close();

		verify(supplier).close(Matchers.eq(conn));
	}

	@Test
	public void commit_WhenClosed_ThrowsISE() throws Exception {
		thrown.expect(IllegalStateException.class);

		transaction.close();

		transaction.commit();
	}

	@Test
	public void commit_WhenOpen_CommitsConnection() throws Exception {
		transaction.commit();

		verify(conn).commit();
	}

	@Test
	public void rollback_WhenClosed_ThrowsISE() throws Exception {
		thrown.expect(IllegalStateException.class);

		transaction.close();

		transaction.rollback();
	}

	@Test
	public void rollback_WhenOpen_RollsbackConnection() throws Exception {
		transaction.rollback();

		verify(conn).rollback();
	}

	@Test
	public void get_WhenClosed_ThrowsISE() throws Exception {
		thrown.expect(IllegalStateException.class);

		transaction.close();

		transaction.getConnection();
	}

	@Test
	public void get_WhenOpen_ReturnsConnection() throws Exception {

		assertEquals(conn, transaction.getConnection());
	}

	@Test
	public void closeConnection_WhenAlreadyClosed_ThrowsISE() throws Exception {
		thrown.expect(IllegalStateException.class);

		transaction.close();

		transaction.close(conn);
	}

	@Test
	public void closeConnection_WhenNotClosed_DoesNotCloseConnection() throws Exception {
		transaction.close(conn);

		verify(supplier, never()).close(conn);
		verify(supplier, never()).closeQuietly(conn);
		verify(conn, never()).close();
	}

	@Test
	public void closeQuietlyConnection_WhenAlreadyClosed_ThrowsISE() throws Exception {
		thrown.expect(IllegalStateException.class);

		transaction.close();

		transaction.closeQuietly(conn);
	}

	@Test
	public void closeQuietlyConnection_WhenNotClosed_DoesNotCloseConnection() throws Exception {
		transaction.closeQuietly(conn);

		verify(supplier, never()).close(conn);
		verify(supplier, never()).closeQuietly(conn);
		verify(conn, never()).close();
	}

	@Test
	public void beginTransaction_OnTransaction_Unsupported() throws Exception {
		thrown.expect(UnsupportedOperationException.class);

		transaction.beginTransaction();
	}

}
