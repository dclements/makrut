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

public class ScalarRowHandlerTest {
	private final Object obj = new Object();

	@Mock
	private ResultSet rs;

	private ScalarRowHandler<Object> handler;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		handler = RowHandlers.scalarRowHandler();
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void handle_ResultSetWithObject_ReturnsObject() throws Exception {
		when(rs.getObject(eq(1))).thenReturn(obj);

		assertEquals(obj, handler.handle(rs));
	}

	@Test
	public void handle_AnyResultSet_DoesNotClose() throws Exception {
		when(rs.getObject(eq(1))).thenReturn(obj);

		handler.handle(rs);

		verify(rs, never()).close();
	}

	@Test
	public void handle_ResultSetWithNull_ThrowsNPE() throws Exception {
		when(rs.getObject(eq(1))).thenReturn(null);


		try {
			handler.handle(rs);
			fail("Expected NPE.");
		} catch (NullPointerException npe) {
			// Expected
		}
		verify(rs).getObject(eq(1));
	}
}
