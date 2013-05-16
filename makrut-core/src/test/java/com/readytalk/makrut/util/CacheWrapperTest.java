package com.readytalk.makrut.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;

import com.google.common.cache.Cache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CacheWrapperTest {
	private final Object obj = new Object();

	@Mock
	private Cache<Callable<?>, Object> cache;

	@Mock
	private Callable<Object> key;

	@Mock
	private Callable<Object> valueLoader;

	private CacheWrapper wrapper;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		wrapper = new CacheWrapper(cache);

	}

	@After
	public void tearDown() {

	}

	@Test
	public void get_WhenCalled_DelegatesToCache() throws Exception {
		wrapper.get(key, valueLoader);

		verify(cache).get(eq(key), eq(valueLoader));
	}

	@Test
	public void getIfPresent_IfPresent_ReturnsValue() throws Exception {
		when(cache.getIfPresent(eq(key))).thenReturn(obj);

		assertEquals(obj, wrapper.getOptional(key).get());
	}

	@Test
	public void getIfPresent_IfAbsent_ReturnsAbsent() throws Exception {

		assertFalse(wrapper.getOptional(key).isPresent());
	}

	@Test
	public void put_WhenCalled_DelegatesToCache() throws Exception {
		wrapper.put(key, valueLoader);

		verify(cache).put(eq(key), eq(valueLoader));
	}
}
