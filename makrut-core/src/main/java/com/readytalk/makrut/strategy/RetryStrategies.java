package com.readytalk.makrut.strategy;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnegative;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Predicate;

public final class RetryStrategies {

	private RetryStrategies() {

	}

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
