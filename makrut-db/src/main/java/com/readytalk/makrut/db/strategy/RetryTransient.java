package com.readytalk.makrut.db.strategy;

import javax.annotation.Nonnegative;
import javax.annotation.concurrent.Immutable;
import java.sql.SQLTransientException;

import com.readytalk.makrut.strategy.RetryStrategy;

/**
 * Retries all SQLTransientExceptions.
 */
@Immutable
public class RetryTransient implements RetryStrategy {

	@Override
	public boolean shouldRetry(@Nonnegative final int lastExecutionCount,
			@Nonnegative final long timeSinceStartMillis,
			final Exception ex) {
		return ex instanceof SQLTransientException;
	}
}
