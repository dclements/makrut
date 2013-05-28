package com.readytalk.makrut.db.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MapRowHandlerTest {

	private static final Integer ID_VALUE = 1;
	private static final String VAL_VALUE = "value";

	@Mock
	private ResultSet rs;

	@Mock
	private ResultSetMetaData rsmd;

	private MapRowHandler<Object> handler;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(rs.getMetaData()).thenReturn(rsmd);

		when(rsmd.getColumnLabel(1)).thenReturn("id");
		when(rsmd.getColumnLabel(2)).thenReturn("val");

		when(rs.getObject(1)).thenReturn(ID_VALUE);
		when(rs.getObject(2)).thenReturn(VAL_VALUE);

		when(rsmd.getColumnCount()).thenReturn(2);

		handler = new MapRowHandler<Object>();
	}

	@After
	public void tearDown() {

	}

	@Test
	public void handle_MultipleColumns_ConvertsToMapWithTypes() throws Exception {
		ImmutableMap<String, Object> result = handler.handle(rs);

		assertEquals(ID_VALUE, result.get("id"));
		assertEquals(VAL_VALUE, result.get("val"));
	}

	@Test
	public void handle_AnyResultSet_DoesNotClose() throws Exception {
		handler.handle(rs);

		verify(rs, never()).close();
	}

	@Test
	public void handle_WithNull_ThrowsNPE() throws Exception {
		when(rs.getObject(2)).thenReturn(null);

		try {
			handler.handle(rs);
			fail("Expected exception.");
		} catch (NullPointerException ex) {
			//Expected
		}
	}
}
