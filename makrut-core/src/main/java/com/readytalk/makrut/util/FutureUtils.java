package com.readytalk.makrut.util;

import static com.codahale.metrics.MetricRegistry.name;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.concurrent.Immutable;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureFallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.readytalk.makrut.strategy.PushbackStrategy;
import com.readytalk.makrut.strategy.RetryStrategy;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Utilities for handling a ListenableFuture's failures.
 */
@Immutable
public class FutureUtils {

	private final MetricRegistry metrics;
	private final Callable<?> input;

	@AssistedInject
	public FutureUtils(final MetricRegistry metrics, @Assisted final Callable<?> input) {
		this.metrics = metrics;
		this.input = input;

	}

	/**
	 * Makes a ListenableFuture retryable, delegating to a given command and placing it on an executor.
	 *
	 * It should be noted that this does not block the original thread and does not hold a resource if a pushback is
	 * being used.
	 *
	 * @param service The Executor to run the fallback on.  It is recommended that this be a limited pool of
	 * some
	 * variety.
	 * @param retryStrategy The strategy that will indicate rather a retry should be attempted.
	 * @param pushbackStrategy How much to defer attempts to retry, allowing for constant-time or exponential backoff
	 * If this is absent then all retries will be scheduled to execute immediately.
	 * @param future The Future to trigger a retry off of if it fails.
	 * @param command The command to run if the future fails.
	 */
	public <T> ListenableFuture<T> addRetry(final ListeningScheduledExecutorService service,
			final RetryStrategy retryStrategy,
			final Optional<PushbackStrategy> pushbackStrategy,
			final ListenableFuture<T> future,
			final MakrutCommandWrapper<T> command) {

		checkNotNull(future);
		checkNotNull(command);

		final Meter retryRate = metrics.meter(name(input.getClass(), "retry", "rate"));

		return Futures.withFallback(future, new FutureFallback<T>() {

			@Override
			@SuppressFBWarnings({ "BC_UNCONFIRMED_CAST" })
			public ListenableFuture<T> create(final Throwable t) throws Exception {

				Exception ex;
				if (t instanceof Error) {
					throw (Error) t;
				} else if (t instanceof Exception) {
					ex = (Exception) t;
				} else {
					ex = new Exception(t);
				}

				retryRate.mark();

				if (retryStrategy.shouldRetry(command.callCount(), command.timeElapsed(TimeUnit.MILLISECONDS), ex)) {
					return addRetry(service, retryStrategy, pushbackStrategy,
							submit(service, pushbackStrategy, command), command);
				} else {
					throw ex;
				}
			}
		}, service);

	}

	private <T> ListenableFuture<T> submit(final ListeningScheduledExecutorService service,
			final Optional<PushbackStrategy> pushbackStrategy,
			final MakrutCommandWrapper<T> command) {

		if (pushbackStrategy.isPresent()) {
			long valueMillis = command.getAndSetNextPushback(pushbackStrategy.get());

			ListenableFutureTask<T> task = ListenableFutureTask.create(command);
			service.schedule(task, valueMillis, TimeUnit.MILLISECONDS);

			return task;
		} else {
			return service.submit(command);
		}
	}

	public <T> ListenableFuture<T> withFallbackCache(final Callable<T> key,
			final ListenableFuture<T> future,
			final CacheWrapper cache) {
		return Futures.withFallback(future, new FutureFallback<T>() {
			@Override
			public ListenableFuture<T> create(final Throwable th) throws Exception {
				Optional<T> value = cache.getOptional(key);

				if (future.isCancelled()) {
					return Futures.immediateCancelledFuture();
				} else if (value.isPresent()) {
					metrics.meter(name(input.getClass(), "fallback", "cache", "hit", "rate"));
					return Futures.immediateFuture(value.get());
				} else {
					metrics.meter(name(input.getClass(), "fallback", "cache", "miss", "rate"));
					return Futures.immediateFailedFuture(th);
				}
			}
		});
	}

}
