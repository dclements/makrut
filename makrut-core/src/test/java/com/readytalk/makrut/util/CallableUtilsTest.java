package com.readytalk.makrut.util;

import static com.codahale.metrics.MetricRegistry.name;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedTimeoutException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CallableUtilsTest {

	@Rule
	public final ExpectedException thrown = ExpectedException.none();

	@Mock
	private Callable<Object> testcallable;

	@Mock
	private Callable<Object> key;

	@Mock
	private Semaphore sem;

	private final Cache<Callable<?>, Object> cache = CacheBuilder.newBuilder().build();

	private final CacheWrapper wrapper = new CacheWrapper(cache);

	private final Object obj = new Object();
	private final MetricRegistry metrics = new MetricRegistry();


	private Timer timer;
	private CallableUtils utils;


	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		utils = new CallableUtils(metrics, testcallable);

		timer = metrics.timer(name(testcallable.getClass(), "call", "duration"));
	}

	@After
	public void tearDown() {

	}

	@Test
	public void addTimeLimit_OnCallable_LimitsTimeForExecution() throws Exception {
		thrown.expect(UncheckedTimeoutException.class);

		Callable<Object> callable = new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				Thread.sleep(10L);
				return obj;
			}
		};

		callable = utils.addTimeLimit(callable, 5L, TimeUnit.MICROSECONDS);


		callable.call();
	}


	@Test
	@SuppressFBWarnings({ "TQ_NEVER_VALUE_USED_WHERE_ALWAYS_REQUIRED" })
	public void addTimeLimit_OnNegativeTime_ThrowsException() {
		thrown.expect(IllegalArgumentException.class);

		utils.addTimeLimit(testcallable, -1L, TimeUnit.MILLISECONDS);
	}

	@Test
	public void addTimeLimit_OnZeroTime_ThrowsException() {
		thrown.expect(IllegalArgumentException.class);

		utils.addTimeLimit(testcallable, 0L, TimeUnit.MILLISECONDS);
	}

	@Test
	@SuppressFBWarnings({ "NP_NONNULL_PARAM_VIOLATION" })
	public void addTimeLimit_OnNullCallable_ThrowsException() {
		thrown.expect(NullPointerException.class);

		utils.addTimeLimit(null, 500L, TimeUnit.MILLISECONDS);
	}

	@Test
	@SuppressFBWarnings({ "NP_NONNULL_PARAM_VIOLATION" })
	public void addTimeLimit_OnNullTimeUnit_ThrowsException() {
		thrown.expect(NullPointerException.class);

		utils.addTimeLimit(testcallable, 500L, null);
	}

	@Test
	public void timeExecution_WhenRun_UpdatesTimerWithValue() throws Exception {
		Callable<Object> callable = new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				Thread.sleep(10L);
				return obj;
			}
		};

		callable = utils.timeExecution(callable);

		callable.call();

		assertEquals(1, timer.getCount());
	}

	@Test
	@SuppressFBWarnings({ "NP_NONNULL_PARAM_VIOLATION" })
	public void timeExecution_OnNullCallable_ThrowsException() {
		thrown.expect(NullPointerException.class);

		utils.timeExecution(null);
	}

	@Test
	public void withSemaphore_OnCall_UsesSemaphore() throws Exception {

		InOrder inOrder = inOrder(sem);

		utils.withSemaphore(testcallable, sem).call();


		inOrder.verify(sem).acquire();
		inOrder.verify(sem).release();
	}

	@Test
	public void withBlockingCache_OnEmpty_Calls() throws Exception {
		when(testcallable.call()).thenReturn(obj);

		assertEquals(obj, utils.withBlockingCache(testcallable, testcallable, wrapper).call());

		verify(testcallable).call();
	}

	@Test
	public void withBlockingCache_OnPresent_ReturnsValue() throws Exception {
		cache.put(testcallable, obj);

		assertEquals(obj, utils.withBlockingCache(testcallable, testcallable, wrapper).call());

		verify(testcallable, never()).call();
	}

	@Test
	public void populateCacheWithResult_WhenCalled_ReturnsValue() throws Exception {
		when(testcallable.call()).thenReturn(obj);

		Callable<Object> callable = utils.populateCacheWithResult(key, testcallable, wrapper);

		assertEquals(obj, callable.call());
	}

	@Test
	public void populateCacheWithResult_WhenCalled_PlacesResultInCache() throws Exception {
		when(testcallable.call()).thenReturn(obj);

		Callable<Object> callable = utils.populateCacheWithResult(key, testcallable, wrapper);

		callable.call();

		assertEquals(obj, cache.getIfPresent(key));
	}

	@Test
	public void meterExecution_WhenCalledThreeTimes_CountsThreeCalls() throws Exception {
		Meter count = metrics.meter(name(testcallable.getClass(), "call", "count"));

		Callable<Object> callable = utils.meterExecution(testcallable);

		callable.call();
		callable.call();
		callable.call();

		assertEquals(3, count.getCount());
	}

	@Test
	public void meterExecution_WhenCalled_ReturnsValue() throws Exception {
		when(testcallable.call()).thenReturn(obj);

		Callable<Object> callable = utils.meterExecution(testcallable);

		assertEquals(obj, callable.call());
	}

}
