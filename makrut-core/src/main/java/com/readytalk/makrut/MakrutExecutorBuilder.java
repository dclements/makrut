package com.readytalk.makrut;

import static com.codahale.metrics.MetricRegistry.name;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Optional;
import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.readytalk.makrut.strategy.BackoffStrategy;
import com.readytalk.makrut.strategy.RetryStrategy;
import com.readytalk.makrut.util.CacheWrapper;
import com.readytalk.makrut.util.CallableUtils;
import com.readytalk.makrut.util.CallableUtilsFactory;
import com.readytalk.makrut.util.FutureUtils;
import com.readytalk.makrut.util.FutureUtilsFactory;
import com.readytalk.makrut.util.MakrutCommandWrapper;

/**
 * A default builder that makes a few choices around execution ordering.
 *
 * <ol>
 * <li>Semaphore</li>
 * <li>Preliminary caching</li>
 * <li>Call timer</li>
 * <li>Time limit</li>
 * <li>Retry</li>
 * </ol>
 */
@NotThreadSafe
public class MakrutExecutorBuilder {

	private final CallableUtilsFactory callUtilsFactory;
	private final FutureUtilsFactory retryUtilsFactory;

	private ListeningExecutorService primaryPool = null;

	private Ticker callTicker = Ticker.systemTicker();

	private Optional<Semaphore> callSemaphore = Optional.absent();
	private Optional<CacheWrapper> blockingCache = Optional.absent();
	private Optional<Long> individualTimeLimitMillis = Optional.absent();
	private Optional<ListeningScheduledExecutorService> retryPool = Optional.absent();
	private Optional<RetryStrategy> retry = Optional.absent();
	private Optional<BackoffStrategy> backoff = Optional.absent();
	private Optional<CacheWrapper> fallbackCache = Optional.absent();

	private boolean callTimer = false;
	private boolean callMeter = false;

	@Inject
	public MakrutExecutorBuilder(final CallableUtilsFactory callUtilsFactory, final FutureUtilsFactory futureUtils) {
		this.callUtilsFactory = checkNotNull(callUtilsFactory);
		this.retryUtilsFactory = futureUtils;
	}

	public MakrutExecutor build() {
		final ListeningExecutorService pool = checkNotNull(primaryPool,
				"Requires an executor service to be specified.");

		return new MakrutExecutor() {
			@Override
			public <T, V extends Callable<T>> ListenableFuture<T> submit(final V input) {
				final MakrutCommandWrapper<T> command = buildCommand(input);

				return buildFuture(input, command, pool.submit(command));
			}
		};
	}

	private <T, V extends Callable<T>> MakrutCommandWrapper<T> buildCommand(final V input) {
		Callable<T> command = input;

		CallableUtils utils = callUtilsFactory.create(name(input.getClass()));

		if (callSemaphore.isPresent()) {
			command = utils.withSemaphore(command, callSemaphore.get());
		}

		if (blockingCache.isPresent()) {
			command = utils.withBlockingCache(input, command, blockingCache.get());
		}

		if (callTimer) {
			command = utils.timeExecution(command);
		}

		if (callMeter) {
			command = utils.meterExecution(command);
		}

		if (individualTimeLimitMillis.isPresent()) {
			command = utils.addTimeLimit(command, individualTimeLimitMillis.get(), TimeUnit.MILLISECONDS);
		}

		if (fallbackCache.isPresent()) {
			command = utils.populateCacheWithResult(input, command, fallbackCache.get());
		}

		return new MakrutCommandWrapper<T>(command, callTicker);
	}

	private <T> ListenableFuture<T> buildFuture(final Callable<T> input,
			final MakrutCommandWrapper<T> command,
			final ListenableFuture<T> future) {
		ListenableFuture<T> retval = future;

		FutureUtils utils = retryUtilsFactory.create(name(input.getClass()));

		if (fallbackCache.isPresent()) {
			retval = utils.withFallbackCache(input, retval, fallbackCache.get());
		}

		if (retry.isPresent()) {
			checkState(retryPool.isPresent(), "Retry executor service must also be provided.");

			retval = utils.addRetry(retryPool.get(), retry.get(), backoff, retval, command);
		}

		return retval;
	}

	public MakrutExecutorBuilder withExecutorService(final ListeningExecutorService service) {
		checkNotNull(service);

		this.primaryPool = service;

		return this;
	}

	public MakrutExecutorBuilder withIndividualTimeLimit(@Nonnegative final long value, final TimeUnit unit) {
		checkArgument(value > 0, "Time limit must be greater than zero.");

		individualTimeLimitMillis = Optional.of(unit.convert(value, TimeUnit.MILLISECONDS));

		return this;
	}

	public MakrutExecutorBuilder withRetry(final RetryStrategy strategy, final ListeningScheduledExecutorService
			pool) {
		this.retryPool = Optional.of(pool);
		this.retry = Optional.of(strategy);

		return this;
	}

	public MakrutExecutorBuilder withBackoff(@Nullable final BackoffStrategy backoffStrategy) {
		this.backoff = Optional.fromNullable(backoffStrategy);

		return this;
	}

	public MakrutExecutorBuilder timeIndividualCalls() {
		this.callTimer = true;

		return this;
	}

	public MakrutExecutorBuilder meterIndividualCalls() {
		this.callMeter = true;

		return this;
	}

	public MakrutExecutorBuilder withTicker(final Ticker ticker) {
		checkNotNull(ticker);

		this.callTicker = ticker;

		return this;
	}

	public MakrutExecutorBuilder withSemaphore(final Semaphore sem) {
		this.callSemaphore = Optional.of(sem);

		return this;
	}

	public MakrutExecutorBuilder withBlockingCache(final Cache<Callable<?>, Object> cache) {
		blockingCache = Optional.of(new CacheWrapper(cache));

		return this;
	}

	public MakrutExecutorBuilder withFallbackCache(final Cache<Callable<?>, Object> cache) {
		fallbackCache = Optional.of(new CacheWrapper(cache));
		return this;
	}

}
