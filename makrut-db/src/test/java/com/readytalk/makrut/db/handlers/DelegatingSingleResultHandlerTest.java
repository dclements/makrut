package com.readytalk.makrut.db.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DelegatingSingleResultHandlerTest {

	private final Object obj = new Object();

	@Mock
	private ResultSet rs;

	@Mock
	private ResultSetRowHandler<Object> rowHandler;

	private DelegatingSingleResultHandler<Object> handler;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		handler = new DelegatingSingleResultHandler<Object>(rowHandler);

	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void handle_EmptyResultSet_ThrowsIAE() throws Exception {
		when(rs.next()).thenReturn(false);

		try {
			handler.handle(rs);

			fail("Exception expected.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}

		verify(rs).next();
	}

	@Test
	public void handle_AnyResultSet_DoesNotCloseResultSet() throws Exception {
		when(rs.next()).thenReturn(true, false);
		when(rowHandler.handle(eq(rs))).thenReturn(obj);

		handler.handle(rs);

		verify(rs, never()).close();
	}

	@Test
	public void handle_ResultSetWithValue_ReturnsProcessedValue() throws Exception {
		when(rs.next()).thenReturn(true, false);
		when(rowHandler.handle(eq(rs))).thenReturn(obj);

		assertEquals(obj, handler.handle(rs));
	}
}
