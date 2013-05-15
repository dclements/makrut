package com.readytalk.makrut.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import com.readytalk.makrut.strategy.PushbackStrategy;
import com.readytalk.makrut.strategy.RetryStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class FutureUtilsTest {

	private final Object obj = new Object();

	private ListeningScheduledExecutorService executorService;

	@Mock
	private MakrutCommandWrapper<Object> command;

	@Mock
	private PushbackStrategy pushbackStrategy;

	@Mock
	private RetryStrategy retryStrategy;

	@Mock
	private CacheWrapper cache;

	@Mock
	private Callable<Object> callable;

	private FutureUtils utils;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		executorService = MoreExecutors.listeningDecorator(new ScheduledThreadPoolExecutor(1));

		utils = new FutureUtils(new MetricRegistry(), callable);
	}

	@After
	public void tearDown() throws Exception {
		executorService.shutdownNow();

	}

	@Test
	public void addRetry_OnFailureWithError_SkipsRetry() throws Exception {
		SettableFuture<Object> value = SettableFuture.create();

		when(retryStrategy.shouldRetry(anyInt(), anyLong(), any(Exception.class))).thenReturn(true, false);
		when(command.call()).thenReturn(obj);

		ListenableFuture<Object> withRetry = utils.addRetry(executorService, retryStrategy,
				Optional.of(pushbackStrategy), value, command);


		Error th = new AssertionError();

		try {
			value.setException(th);
		} catch (Error er) {
			// Working around that settable futures immediately throw errors.
		}

		try {
			withRetry.get();
			fail("Expected exception.");
		} catch (ExecutionException ex) {
			assertEquals(th, ex.getCause());
		}

		verify(retryStrategy, never()).shouldRetry(anyInt(), anyLong(), any(Exception.class));

	}

	@Test
	public void addRetry_OnFailure_TriggersRetry() throws Exception {
		SettableFuture<Object> value = SettableFuture.create();

		when(retryStrategy.shouldRetry(anyInt(), anyLong(), any(Exception.class))).thenReturn(true, false);
		when(command.call()).thenReturn(obj);

		ListenableFuture<Object> withRetry = utils.addRetry(executorService, retryStrategy,
				Optional.of(pushbackStrategy), value, command);

		value.setException(new Exception());

		assertEquals(obj, withRetry.get());

		verify(retryStrategy).shouldRetry(anyInt(), anyLong(), any(Exception.class));

	}

	@Test
	public void addRetry_OnFailure_PushesBack() throws Exception {
		SettableFuture<Object> value = SettableFuture.create();

		when(retryStrategy.shouldRetry(anyInt(), anyLong(), any(Exception.class))).thenReturn(true, false);
		when(command.call()).thenReturn(obj);

		ListenableFuture<Object> withRetry = utils.addRetry(executorService, retryStrategy,
				Optional.of(pushbackStrategy), value, command);


		value.setException(new Exception());

		assertEquals(obj, withRetry.get());

		verify(command).getAndSetNextPushback(eq(pushbackStrategy));

	}

	@Test
	public void addRetry_OnMultipleFailure_CanTriggerMultipleRetries() throws Exception {
		SettableFuture<Object> value = SettableFuture.create();

		when(retryStrategy.shouldRetry(anyInt(), anyLong(), any(Exception.class))).thenReturn(true, true, false);
		when(command.call()).thenThrow(new RuntimeException()).thenReturn(obj);

		ListenableFuture<Object> withRetry = utils.addRetry(executorService, retryStrategy,
				Optional.of(pushbackStrategy), value, command);

		value.setException(new Exception());

		assertEquals(obj, withRetry.get());
		verify(retryStrategy, times(2)).shouldRetry(anyInt(), anyLong(), any(Exception.class));

	}

	@Test
	public void addRetry_OnFailureWithNegativeRetry_ReturnsException() throws Exception {
		SettableFuture<Object> value = SettableFuture.create();

		when(retryStrategy.shouldRetry(anyInt(), anyLong(), any(Exception.class))).thenReturn(false);

		ListenableFuture<Object> withRetry = utils.addRetry(executorService, retryStrategy,
				Optional.of(pushbackStrategy), value, command);

		Exception testEx = new Exception();
		value.setException(testEx);

		try {
			withRetry.get();
			fail("Expected failure.");
		} catch (Exception ex) {
			assertEquals(testEx, Throwables.getRootCause(ex));
			verify(retryStrategy).shouldRetry(anyInt(), anyLong(), any(Exception.class));
		}
	}

	@Test
	public void withFallbackCache_OnFailureWhenPresent_ReplacesValue() throws Exception {
		when(cache.getOptional(callable)).thenReturn(Optional.of(obj));

		SettableFuture<Object> settable = SettableFuture.create();

		ListenableFuture<Object> future = utils.withFallbackCache(callable, settable, cache);

		settable.setException(new Exception());

		assertEquals(obj, future.get());
	}

	@Test
	public void withFallbackCache_OnFailureWhenAbsent_ReturnsFailedFuture() throws Exception {
		when(cache.getOptional(callable)).thenReturn(Optional.absent());

		final Exception ex = new Exception();

		SettableFuture<Object> settable = SettableFuture.create();

		ListenableFuture<Object> future = utils.withFallbackCache(callable, settable, cache);

		settable.setException(ex);

		try {
			future.get();
			fail("Expected exception.");
		} catch (ExecutionException ee) {
			assertEquals(ex, ee.getCause());

		}
	}

	@Test
	public void withFallbackCache_OnSuccess_DoesNotRun() throws Exception {
		when(cache.getOptional(callable)).thenReturn(Optional.of(obj));

		SettableFuture<Object> settable = SettableFuture.create();

		ListenableFuture<Object> future = utils.withFallbackCache(callable, settable, cache);

		settable.set(obj);

		assertEquals(obj, future.get());

		verify(cache, never()).getOptional(eq(callable));
	}

	@Test
	public void withFallbackCache_IfCancelled_RemainsCancelled() throws Exception {
		when(cache.getOptional(callable)).thenReturn(Optional.of(obj));

		SettableFuture<Object> settable = SettableFuture.create();

		ListenableFuture<Object> future = utils.withFallbackCache(callable, settable, cache);

		settable.cancel(true);

		assertTrue(future.isCancelled());
	}
}
