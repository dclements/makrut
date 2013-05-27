package com.readytalk.makrut.db.strategy;

import javax.annotation.Nonnegative;
import javax.annotation.concurrent.Immutable;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLTransientConnectionException;

import com.readytalk.makrut.strategy.RetryStrategy;

/**
 * Retries both SQLNonTransientConnectionExceptions and SQLTransientConnectionExceptions.
 *
 * This should only be used with some form of increasing backoff strategy.
 */
@Immutable
public class RetryConnection implements RetryStrategy {

	@Override
	public boolean shouldRetry(@Nonnegative final int lastExecutionCount,
			@Nonnegative final long timeSinceStartMillis,
			final Exception ex) {
		return ex instanceof SQLNonTransientConnectionException || ex instanceof SQLTransientConnectionException;
	}
}

