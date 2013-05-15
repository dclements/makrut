package com.readytalk.makrut;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.Timer;
import com.google.common.base.Optional;
import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.readytalk.makrut.strategy.PushbackStrategy;
import com.readytalk.makrut.strategy.RetryStrategy;
import com.readytalk.makrut.util.CacheWrapper;
import com.readytalk.makrut.util.CallableUtils;
import com.readytalk.makrut.util.CallableUtilsFactory;
import com.readytalk.makrut.util.FutureUtils;
import com.readytalk.makrut.util.FutureUtilsFactory;
import com.readytalk.makrut.util.MakrutCommandWrapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class MakrutExecutorBuilderTest {

	@Mock
	private ListeningExecutorService executorService;

	@Mock
	private Timer timer;

	@Mock
	private Cache<Callable<?>, Object> cache;

	@Mock
	private RetryStrategy retry;

	@Mock
	private ListeningScheduledExecutorService retryService;

	@Mock
	private Semaphore sem;

	@Mock
	private CallableUtils callUtils;

	@Mock
	private CallableUtilsFactory callableUtilsFactory;

	@Mock
	private FutureUtilsFactory futureUtilsFactory;

	@Mock
	private Ticker ticker;

	@Mock
	private FutureUtils futureUtils;

	@Mock
	private PushbackStrategy pushback;

	@Mock
	private Callable<Object> callable;

	@Mock
	private ListenableFuture<Object> future;

	private MakrutExecutorBuilder builder;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		when(callUtils.addTimeLimit(any(Callable.class), anyLong(), any(TimeUnit.class))).thenReturn(callable);
		when(callUtils.timeExecution(any(Callable.class))).thenReturn(callable);
		when(callUtils.withBlockingCache(any(Callable.class), any(Callable.class),
				any(CacheWrapper.class))).thenReturn(
				callable);
		when(callUtils.withSemaphore(any(Callable.class), any(Semaphore.class))).thenReturn(callable);

		when(futureUtils.addRetry(any(ListeningScheduledExecutorService.class), any(RetryStrategy.class),
				any(Optional.class), any(ListenableFuture.class), any(MakrutCommandWrapper.class))).thenReturn(future);

		when(executorService.submit(any(Callable.class))).thenReturn(future);

		when(callableUtilsFactory.create(any(Callable.class))).thenReturn(callUtils);
		when(futureUtilsFactory.create(any(Callable.class))).thenReturn(futureUtils);

		builder = new MakrutExecutorBuilder(callableUtilsFactory, futureUtilsFactory);
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	@SuppressWarnings("unchecked")
	public void build_WhenAllPresent_EnsuresOrderingWhenNeeded() {
		builder.withExecutorService(executorService);
		builder.timeIndividualCalls();
		builder.withIndividualTimeLimit(1000L, TimeUnit.MILLISECONDS);
		builder.withBlockingCache(cache);
		builder.withRetry(retry, retryService);
		builder.withPushback(pushback);
		builder.withSemaphore(sem);
		builder.withTicker(ticker);
		builder.withFallbackCache(cache);

		builder.meterIndividualCalls();

		MakrutExecutor executor = builder.build();

		executor.submit(callable);

		InOrder order = Mockito.inOrder(callUtils, futureUtils);

		order.verify(callUtils).withSemaphore(any(Callable.class), eq(sem));
		order.verify(callUtils).withBlockingCache(any(Callable.class), any(Callable.class), any(CacheWrapper.class));
		order.verify(callUtils).timeExecution(any(Callable.class));
		order.verify(callUtils).addTimeLimit(any(Callable.class), anyLong(), any(TimeUnit.class));
		order.verify(callUtils).populateCacheWithResult(eq(callable), any(Callable.class), any(CacheWrapper.class));

		order.verify(futureUtils)
				.addRetry(eq(retryService), eq(retry), eq(Optional.of(pushback)), eq(future),
						any(MakrutCommandWrapper.class));

		order.verify(futureUtils).withFallbackCache(eq(callable), eq(future), any(CacheWrapper.class));

		verify(callUtils).meterExecution(any(Callable.class));
	}
}
