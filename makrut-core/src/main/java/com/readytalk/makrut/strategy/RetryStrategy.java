package com.readytalk.makrut.strategy;

import javax.annotation.Nonnegative;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public interface RetryStrategy {
	/**
	 * Determine whether a retry should be attempted.
	 *
	 * @param lastExecutionCount The number of calls that have been attempted.
	 * @param timeSinceStartMillis The amount of time, in milliseconds, since the first call.
	 * @param ex The exception thrown that is causing the retry.
	 *
	 * @return Whether, according to the strategy, the executor should retry a request.
	 */
	boolean shouldRetry(@Nonnegative int lastExecutionCount, @Nonnegative long timeSinceStartMillis, Exception ex);
}
