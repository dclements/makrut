package com.readytalk.makrut.db;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DataSourceConnectionSupplierTest {

	@Mock
	private DataSource dataSource;

	@Mock
	private Connection conn;

	private DataSourceConnectionSupplier supplier;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		supplier = new DataSourceConnectionSupplier(dataSource);
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void get_WhenCalled_RetrievesFromSource() throws Exception {
		when(dataSource.getConnection()).thenReturn(conn);


		assertEquals(conn, supplier.getConnection());
	}

	@Test
	public void get_WhenDSException_Rethrows() throws Exception {
		SQLException sqle = new SQLException("08000");


		when(dataSource.getConnection()).thenThrow(sqle);

		try {
			supplier.getConnection();
		} catch (SQLException ex) {
			assertEquals(sqle, ex);
		}
	}

	@Test
	public void close_OnNormalConnection_ClosesConnection() throws Exception {
		supplier.close(conn);

		verify(conn).close();
	}

	@Test
	public void close_OnNull_DoesNothing() throws Exception {
		supplier.close(null);

		verifyZeroInteractions(conn, dataSource);
	}

	@Test
	public void closeQuietly_OnNull_DoesNothing() {
		supplier.closeQuietly(null);

		verifyZeroInteractions(conn, dataSource);
	}

	@Test
	public void closeQuietly_OnNormalConnection_ClosesConnection() throws Exception {
		supplier.closeQuietly(conn);

		verify(conn).close();
	}

	@Test
	public void closeQuietly_WhenException_SwallowsException() throws Exception {
		doThrow(new SQLException()).when(conn).close();

		supplier.closeQuietly(conn);

		verify(conn).close();
	}


}
