package com.readytalk.makrut.util;

import static com.google.inject.internal.util.$Preconditions.checkArgument;
import static com.google.inject.internal.util.$Preconditions.checkNotNull;

import javax.annotation.Nonnegative;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.Timer;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;

/**
 * Utilities for constructing robust Callable objects.
 */
@Immutable
public class CallableUtils {
	private final TimeLimiter limiter = new SimpleTimeLimiter();

	@Inject
	public CallableUtils() {

	}

	/**
	 * Puts a time limit on the execution of the callable, so that it will throw an UncheckedTimeoutException if the
	 * call takes too long.
	 *
	 * @param callable The Callable to proxy.
	 * @param timeLimit The amount of time to take.
	 * @param unit The desired unit for the amount of time.
	 *
	 * @return The proxied Callable.
	 */
	@SuppressWarnings("unchecked")
	public <T> Callable<T> addTimeLimit(final Callable<T> callable,
			@Nonnegative final long timeLimit,
			final TimeUnit unit) {
		checkArgument(timeLimit > 0, "Time limit must be greater than zero.");
		checkNotNull(callable);
		checkNotNull(unit);

		return limiter.newProxy(callable, Callable.class, timeLimit, unit);
	}

	/**
	 * Reports the amount of time that it takes to execute a Callable to a Timer object.
	 *
	 * @param callable The Callable to report on.
	 * @param timer The Timer object which will create the report.
	 *
	 * @return A Callable that will report the duration of every call to call().
	 */
	public <T> Callable<T> timeExecution(final Callable<T> callable, final Timer timer) {
		checkNotNull(callable);
		checkNotNull(timer);

		return new Callable<T>() {

			@Override
			public T call() throws Exception {
				return timer.time(callable);
			}
		};
	}

	/**
	 * Locks access to a callable based on an external semaphore.
	 */
	public <V extends Callable<T>, T> Callable<T> withSemaphore(final V callable, final Semaphore sem) {
		checkNotNull(callable);
		checkNotNull(sem);

		return new Callable<T>() {
			@Override
			public T call() throws Exception {
				sem.acquire();

				try {
					return callable.call();
				} finally {
					sem.release();
				}
			}
		};
	}

	/**
	 * Wraps a call with a blocking cache. If multiple simultaneous calls hit the cache then the leader's result will
	 * be returned to all of the other callers.
	 *
	 * @param key The callable that will be used as a key for the cache.  This should implement good equals and
	 * hashCode methods.
	 * @param load The callable to use to load the value.
	 * @param cache The cache that can be used.  It is suggested that caches used for this purpose be built with a
	 * way of expiring keys and a size limit to prevent excessive resource consumption.
	 * @param <T> The the return value for the cache.
	 *
	 * @return A callable that will block on the cache returning if other callers are requesting a resource.
	 */
	public <T> Callable<T> withBlockingCache(final Callable<T> key, final Callable<T> load, final CacheWrapper cache) {
		return new Callable<T>() {
			@Override
			public T call() throws Exception {
				return cache.get(key, load);
			}
		};
	}

	public <T> Callable<T> populateCacheWithResult(final Callable<T> key,
			final Callable<T> callable,
			final CacheWrapper cache) {
		return new Callable<T>() {

			@Override
			public T call() throws Exception {
				T retval = callable.call();

				cache.put(key, retval);

				return retval;
			}
		};
	}
}
