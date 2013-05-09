package com.readytalk.makrut.strategy;

import javax.annotation.Nonnegative;
import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.TimeUnit;

@ThreadSafe
public interface PushbackStrategy {
	/**
	 * The amount of time to wait before retrying.
	 *
	 * @param lastExecutionCount The number of times the call has been attempted.
	 * @param lastPushback The previous amount of time waited before the last execution.
	 * @param unit The TimeUnit to use.
	 *
	 * @return The next suggested amount of time to wait.
	 */
	long nextWaitPeriod(@Nonnegative int lastExecutionCount, @Nonnegative long lastPushback, TimeUnit unit);
}
