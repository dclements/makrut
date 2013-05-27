package com.readytalk.makrut.db.strategy;

import javax.annotation.Nonnegative;
import javax.annotation.concurrent.Immutable;
import java.sql.SQLRecoverableException;

import com.readytalk.makrut.strategy.RetryStrategy;

/**
 * Retries all recoverable exceptions.  Should only be used in cases where, at a minimum,
 * the connection will be closed and a new one will be retrieved.
 */
@Immutable
public class RetryRecoverable implements RetryStrategy {

	@Override
	public boolean shouldRetry(@Nonnegative final int lastExecutionCount,
			@Nonnegative final long timeSinceStartMillis,
			final Exception ex) {
		return ex instanceof SQLRecoverableException;
	}
}
