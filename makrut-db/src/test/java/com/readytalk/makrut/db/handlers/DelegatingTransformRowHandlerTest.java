package com.readytalk.makrut.db.handlers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;

import com.google.common.base.Function;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DelegatingTransformRowHandlerTest {

	private final Object obj1 = new Object();
	private final Object obj2 = new Object();

	@Mock
	private ResultSetRowHandler<Object> delegateHandler;

	@Mock
	private Function<Object, Object> func;

	@Mock
	private ResultSet rs;

	private DelegatingTransformRowHandler<Object, Object> handler;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		handler = new DelegatingTransformRowHandler<Object, Object>(delegateHandler, func);

	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void handle_AnyObject_ReturnsTransformedValue() throws Exception {
		when(func.apply(obj1)).thenReturn(obj2);
		when(delegateHandler.handle(Matchers.eq(rs))).thenReturn(obj1);

		assertEquals(obj2, handler.handle(rs));
	}

	@Test
	public void handle_AnyObject_DoesNotClose() throws Exception {
		when(func.apply(obj1)).thenReturn(obj2);
		when(delegateHandler.handle(Matchers.eq(rs))).thenReturn(obj1);

		handler.handle(rs);

		verify(delegateHandler).handle(eq(rs));
		verify(rs, never()).close();
	}
}
