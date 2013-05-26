package com.readytalk.makrut.db.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;

import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DelegatingListHandlerTest {

	private final Object[] objects = new Object[]{ new Object(), new Object(), new Object() };

	@Mock
	private ResultSetRowHandler<Object> rowHandler;

	@Mock
	private ResultSet rs;

	private DelegatingListHandler<Object> handler;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		handler = new DelegatingListHandler<Object>(rowHandler);
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	 public void handle_EmptyResultSet_ReturnsEmptyList() throws Exception {
		when(rs.next()).thenReturn(false);

		assertTrue(handler.handle(rs).isEmpty());
	}

	@Test
	public void handle_AnyResultSet_DoesNotClose() throws Exception {
		when(rs.next()).thenReturn(false);

		handler.handle(rs);

		verify(rs, never()).close();
	}

	@Test
	public void handle_FullResultSet_RetunsItemsInList() throws Exception {
		when(rs.next()).thenReturn(true, true, true, false);
		when(rowHandler.handle(eq(rs))).thenReturn(objects[0], objects[1], objects[2]);

		assertEquals(ImmutableList.copyOf(objects), handler.handle(rs));
	}


}
