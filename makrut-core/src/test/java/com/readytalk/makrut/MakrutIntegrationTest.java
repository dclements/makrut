package com.readytalk.makrut;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.readytalk.makrut.strategy.PushbackStrategy;
import com.readytalk.makrut.strategy.RetryStrategy;
import com.readytalk.makrut.util.CallableUtils;
import com.readytalk.makrut.util.FutureUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MakrutIntegrationTest {

	@Rule
	public final ExpectedException thrown = ExpectedException.none();

	@Rule
	public final Timeout timeout = new Timeout(10000);

	private final ListeningExecutorService executor = MoreExecutors.listeningDecorator(
			MoreExecutors.getExitingScheduledExecutorService(new ScheduledThreadPoolExecutor(2)));

	private final ListeningScheduledExecutorService retryExecutor = spy(MoreExecutors.listeningDecorator(
			MoreExecutors.getExitingScheduledExecutorService(new ScheduledThreadPoolExecutor(2))));

	private final CallableUtils callableUtils = new CallableUtils();
	private final FutureUtils futureUtils = new FutureUtils();

	private final Cache<Callable<?>, Object> cache = CacheBuilder.newBuilder().concurrencyLevel(2).build();

	private final Object obj = new Object();
	private final Object obj2 = new Object();

	@Mock
	private Callable<Object> callable;

	@Mock
	private Callable<Object> callable2;

	@Mock
	private RetryStrategy retryStrategy;

	@Mock
	private PushbackStrategy pushbackStrategy;

	@Mock
	private Ticker ticker;

	private MakrutExecutorBuilder builder;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		builder = new MakrutExecutorBuilder(callableUtils, futureUtils).withExecutorService(executor);
	}

	@Test
	public void build_WithoutExecutor_ThrowsNPException() {
		thrown.expect(NullPointerException.class);

		new MakrutExecutorBuilder(callableUtils, futureUtils).build();
	}

	@Test
	public void basic_OnBasicExecutor_Executes() throws Exception {
		when(callable.call()).thenReturn(obj);

		assertEquals(obj, builder.build().submit(callable).get());
	}

	@Test
	public void withBlockingCache_OnSecondCall_ReturnsCachedValue() throws Exception {
		when(callable.call()).thenReturn(obj);

		MakrutExecutor mexec = builder.withBlockingCache(cache).build();

		assertEquals(obj, mexec.submit(callable).get());
		assertEquals(obj, mexec.submit(callable).get());

		verify(callable, times(1)).call();
	}

	@Test
	public void withBlockingCache_OnSecondCallWithDifferent_ReturnsDifferentValue() throws Exception {
		when(callable.call()).thenReturn(obj);
		when(callable2.call()).thenReturn(obj2);

		MakrutExecutor mexec = builder.withBlockingCache(cache).build();

		assertEquals(obj, mexec.submit(callable).get());
		assertEquals(obj2, mexec.submit(callable2).get());
	}

	@Test
	public void withBlockingCache_OnMultiple_BlocksRepeatedCalls() throws Exception {

		final CountDownLatch latch = new CountDownLatch(1);

		final Callable<Object> passCall = new MakrutCommand<Object>("name", 1, obj) {
			@Override
			public Object call() throws Exception {
				latch.countDown();
				Thread.sleep(100L);
				return obj;
			}
		};

		final Callable<Object> blockedCall = new MakrutCommand<Object>("name", 1, obj) {
			@Override
			public Object call() throws Exception {
				throw new IllegalStateException("Call was not blocked.");
			}
		};

		MakrutExecutor mexec = builder.withBlockingCache(cache).build();

		ListenableFuture<Object> f = mexec.submit(passCall);

		latch.await();

		assertEquals(obj, mexec.submit(blockedCall).get());
		assertEquals(obj, f.get());

	}

	@Test
	public void withBlockingCache_FirstFails_OthersBlockingFail() throws Exception {

		final CountDownLatch exceptionCallLatch = new CountDownLatch(1);
		final CountDownLatch blockingLatch = new CountDownLatch(1);
		final AtomicBoolean hasException = new AtomicBoolean(true);

		final Callable<Object> exceptionCall = new MakrutCommand<Object>("name", 1, obj) {
			@Override
			public Object call() throws Exception {
				exceptionCallLatch.countDown();
				Thread.sleep(100L);
				blockingLatch.await();
				if (hasException.getAndSet(false)) {
					throw new RuntimeException("Expected.");
				}

				return obj;
			}
		};

		MakrutExecutor mexec = builder.withBlockingCache(cache).build();

		ListenableFuture<Object> f = mexec.submit(exceptionCall);

		exceptionCallLatch.await();

		ListenableFuture<Object> fs = mexec.submit(exceptionCall);

		blockingLatch.countDown();

		try {
			fs.get();
			fail("Expected exception to propagate.");
		} catch (ExecutionException ex) {
			//Expected
		}

		try {
			f.get();
			fail("Expected exception.");
		} catch (ExecutionException ex) {
			//Expected
		}
	}

	@Test
	public void withRetry_WithoutFailure_RunsOnce() throws Exception {
		when(callable.call()).thenReturn(obj);

		when(retryStrategy.shouldRetry(anyInt(), anyLong(), any(Exception.class))).thenReturn(true);

		MakrutExecutor mexec = builder.withRetry(retryStrategy, retryExecutor).build();

		assertEquals(obj, mexec.submit(callable).get());

		verify(callable).call();
		verifyZeroInteractions(retryStrategy);
	}

	@Test
	public void withRetry_OnFailureWhenNoMoreRetries_RunsOnce() throws Exception {
		Exception th = new RuntimeException();
		when(callable.call()).thenThrow(th);

		when(retryStrategy.shouldRetry(anyInt(), anyLong(), any(Exception.class))).thenReturn(false);

		MakrutExecutor mexec = builder.withRetry(retryStrategy, retryExecutor).build();

		try {
			mexec.submit(callable).get();
			fail("Expected an exception.");
		} catch (ExecutionException ex) {

		}

		verify(retryStrategy).shouldRetry(eq(1), anyLong(), eq(th));
	}

	@Test
	public void withRetry_OnFailureWhenRetry_RunsAgain() throws Exception {
		Exception th = new RuntimeException();
		when(callable.call()).thenThrow(th).thenReturn(obj);

		when(retryStrategy.shouldRetry(eq(1), anyLong(), any(Exception.class))).thenReturn(true);

		MakrutExecutor mexec = builder.withRetry(retryStrategy, retryExecutor).build();

		assertEquals(obj, mexec.submit(callable).get());

		verify(retryStrategy).shouldRetry(eq(1), anyLong(), eq(th));
		verify(callable, times(2)).call();
	}

	@Test
	public void withRetry_OnFailureWhenRetry_IncrementsCounter() throws Exception {
		Exception th = new RuntimeException();
		when(callable.call()).thenThrow(th, th, th).thenReturn(obj);

		when(retryStrategy.shouldRetry(anyInt(), anyLong(), any(Exception.class))).thenReturn(true, true, true, false);

		MakrutExecutor mexec = builder.withRetry(retryStrategy, retryExecutor).build();

		assertEquals(obj, mexec.submit(callable).get());

		verify(retryStrategy).shouldRetry(eq(1), anyLong(), eq(th));
		verify(retryStrategy).shouldRetry(eq(2), anyLong(), eq(th));
		verify(retryStrategy).shouldRetry(eq(3), anyLong(), eq(th));
	}

	@Test
	public void withRetry_OnFailureWhenRetry_ReportsTime() throws Exception {
		Exception th = new RuntimeException();
		when(callable.call()).thenThrow(th).thenReturn(obj);

		when(retryStrategy.shouldRetry(anyInt(), anyLong(), any(Exception.class))).thenReturn(true, false);

		when(ticker.read()).thenReturn(0L, TimeUnit.NANOSECONDS.convert(5L, TimeUnit.MILLISECONDS));

		MakrutExecutor mexec = builder.withRetry(retryStrategy, retryExecutor).withTicker(ticker).build();

		assertEquals(obj, mexec.submit(callable).get());

		verify(retryStrategy).shouldRetry(anyInt(), eq(5L), eq(th));
	}

	@Test
	public void withRetryAndPushback_OnFailure_PushesBackRuntime() throws Exception {
		Exception th = new RuntimeException();
		when(callable.call()).thenThrow(th).thenReturn(obj);

		when(retryStrategy.shouldRetry(eq(1), anyLong(), any(Exception.class))).thenReturn(true);

		when(pushbackStrategy.nextWaitPeriod(anyInt(), anyLong(), any(TimeUnit.class))).thenReturn(100L);

		MakrutExecutor mexec = builder.withRetry(retryStrategy, retryExecutor).withPushback(pushbackStrategy).build();

		assertEquals(obj, mexec.submit(callable).get());

		verify(retryExecutor).schedule(any(ListenableFutureTask.class), eq(100L), eq(TimeUnit.MILLISECONDS));
		verify(pushbackStrategy).nextWaitPeriod(eq(1), eq(0L), any(TimeUnit.class));
		verify(retryStrategy).shouldRetry(eq(1), anyLong(), eq(th));
		verify(callable, times(2)).call();
	}

	@Test
	public void withSemaphoreAndBlockingCache_OnSecondCall_ReturnsCachedValue() throws Exception {
		final Semaphore sem = new Semaphore(1);

		when(callable.call()).thenReturn(obj, new Object());

		MakrutExecutor mexec = builder.withBlockingCache(cache).withSemaphore(sem).build();

		assertEquals(obj, mexec.submit(callable).get());
		assertEquals(obj, mexec.submit(callable).get());

		verify(callable, times(1)).call();
	}

	@Test
	public void withFallbackCache_OnFailureWithCache_ReturnsCachedValue() throws Exception {
		when(callable.call()).thenReturn(obj).thenThrow(new Exception());

		MakrutExecutor mexec = builder.withFallbackCache(cache).build();

		assertEquals(obj, mexec.submit(callable).get());
		assertEquals(obj, mexec.submit(callable).get());
	}

	@Test
	public void withFallbackCacheAndRetry_OnFailureWithCache_ReturnsCachedValue() throws Exception {
		Exception th = new RuntimeException();
		when(callable.call()).thenReturn(obj).thenThrow(th, th);

		when(retryStrategy.shouldRetry(anyInt(), anyLong(), any(Exception.class))).thenReturn(true, false);

		MakrutExecutor mexec = builder.withRetry(retryStrategy, retryExecutor).withFallbackCache(cache).build();

		assertEquals(obj, mexec.submit(callable).get());
		assertEquals(obj, mexec.submit(callable).get());

		verify(retryStrategy).shouldRetry(eq(1), anyLong(), eq(th));
		verify(callable, times(3)).call();
	}
}
