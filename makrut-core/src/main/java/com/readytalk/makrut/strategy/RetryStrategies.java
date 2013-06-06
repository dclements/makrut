package com.readytalk.makrut.strategy;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnegative;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Predicate;

/**
 * Strategies for determining whether to retry.
 */
public final class RetryStrategies {

	private RetryStrategies() {

	}

	/**
	 * Always returns true.
	 */
	public static RetryStrategy alwaysRetry() {
		return new RetryStrategy() {
			@Override
			public boolean shouldRetry(@Nonnegative final int lastExecutionCount,
					@Nonnegative final long timeSinceStartMillis,
					final Exception ex) {
				return true;
			}
		};
	}

	/**
	 * Always returns false.
	 */
	public static RetryStrategy neverRetry() {
		return new RetryStrategy() {
			@Override
			public boolean shouldRetry(@Nonnegative final int lastExecutionCount,
					@Nonnegative final long timeSinceStartMillis,
					final Exception ex) {
				return false;
			}
		};
	}

	/**
	 * Allows a given number of retries.
	 *
	 * @param n The number of retries to allow.
	 */
	public static RetryStrategy allowNumberOfAttempts(@Nonnegative final int n) {
		checkArgument(n > 1, "Retry strategies must allow at least 1 retry and the original execution.");

		return new RetryStrategy() {
			@Override
			public boolean shouldRetry(@Nonnegative final int lastExecutionCount,
					@Nonnegative final long timeSinceStartMillis,
					final Exception ex) {
				return n > lastExecutionCount;
			}
		};
	}

	/**
	 * Allows any number of retries in a given amount of time.
	 *
	 * @param time The amount of time.
	 * @param unit The time unit for the amount of time.
	 */
	public static RetryStrategy underTimeLimit(@Nonnegative final long time, final TimeUnit unit) {
		checkArgument(time > 0, "Retry strategies must allow some amount of time for execution.");
		checkNotNull(unit);

		return new RetryStrategy() {
			@Override
			public boolean shouldRetry(@Nonnegative final int lastExecutionCount,
					@Nonnegative final long timeSinceStartMillis,
					final Exception ex) {
				return TimeUnit.MILLISECONDS.convert(time, unit) > timeSinceStartMillis;
			}
		};
	}

	/**
	 * Allows retries when a predicate returns true.
	 *
	 * @param test The predicate test for the exception.
	 */
	public static RetryStrategy forExceptionPredicate(final Predicate<Exception> test) {
		checkNotNull(test);
		return new RetryStrategy() {
			@Override
			public boolean shouldRetry(@Nonnegative final int lastExecutionCount,
					@Nonnegative final long timeSinceStartMillis,
					final Exception ex) {
				return test.apply(ex);
			}
		};
	}

	/**
	 * Allows retry if and only if all of the strategies return true. Supports short circuiting.
	 *
	 * @param strategies The strategies to test, in order.
	 */
	public static RetryStrategy and(final RetryStrategy... strategies) {
		return new RetryStrategy() {
			@Override
			public boolean shouldRetry(@Nonnegative final int lastExecutionCount,
					@Nonnegative final long timeSinceStartMillis,
					final Exception ex) {
				for (RetryStrategy o : strategies) {
					if (!o.shouldRetry(lastExecutionCount, timeSinceStartMillis, ex)) {
						return false;
					}
				}

				return true;
			}
		};
	}

	/**
	 * Allows retry if and only if any of the strategies return true. Supports short circuiting.
	 *
	 * @param strategies The strategies to test, in order.
	 */
	public static RetryStrategy or(final RetryStrategy... strategies) {
		return new RetryStrategy() {
			@Override
			public boolean shouldRetry(@Nonnegative final int lastExecutionCount,
					@Nonnegative final long timeSinceStartMillis,
					final Exception ex) {
				for (RetryStrategy o : strategies) {
					if (o.shouldRetry(lastExecutionCount, timeSinceStartMillis, ex)) {
						return true;
					}
				}

				return false;
			}
		};
	}

	/**
	 * Allows retry if and only the underlying strategy returns false.
	 *
	 * @param strategy The underlying strategy to invert.
	 */
	public static RetryStrategy not(final RetryStrategy strategy) {
		return new RetryStrategy() {
			@Override
			public boolean shouldRetry(@Nonnegative final int lastExecutionCount,
					@Nonnegative final long timeSinceStartMillis,
					final Exception ex) {
				return !strategy.shouldRetry(lastExecutionCount, timeSinceStartMillis, ex);
			}
		};
	}

}
