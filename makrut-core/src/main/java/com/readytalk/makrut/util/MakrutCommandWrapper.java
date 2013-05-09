package com.readytalk.makrut.util;

import javax.annotation.Nonnegative;
import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.readytalk.makrut.strategy.PushbackStrategy;

/**
 * A wrapped callable object with instrumentation.  Designed to be executed on multiple threads and maintain
 * visibility, but not to be executed on those threads at the same time as being scheduled.
 */
@ThreadSafe
public class MakrutCommandWrapper<T> implements Callable<T> {

	private final Lock timerLock = new ReentrantLock();
	private final AtomicInteger callCount = new AtomicInteger(0);
	private final AtomicLong lastPushbackMillis = new AtomicLong(0);

	private final Callable<T> delegate;
	private final Stopwatch timer;

	public MakrutCommandWrapper(final Callable<T> delegate, final Ticker ticker) {
		this.delegate = delegate;
		this.timer = new Stopwatch(ticker);

	}

	@Override
	public T call() throws Exception {

		timerLock.lock();

		try {
			if (!timer.isRunning()) {
				timer.start();
			}
		} finally {
			timerLock.unlock();
		}

		callCount.incrementAndGet();


		return delegate.call();
	}

	/**
	 * The time elapsed since the MarkutCommand's call method was first run.  This enables tracking how long it has
	 * been since the first (unsuccessful) attempt.
	 *
	 * @param desiredUnit The unit of measurement for the time.
	 *
	 * @return The amount of time passed in the specified unit of measurement.
	 */
	@Nonnegative
	public long timeElapsed(final TimeUnit desiredUnit) {
		timerLock.lock();

		try {
			if (!timer.isRunning()) {
				return 0;
			}

			return timer.elapsed(desiredUnit);
		} finally {
			timerLock.unlock();
		}
	}

	/**
	 * Number of times a method has been called.  Increments prior to the delegate method being called.
	 */
	@Nonnegative
	public int callCount() {
		return callCount.get();
	}

	/**
	 * Gets the next pushback value and records it.
	 *
	 * @param strategy The strategy to use for determining the next pushback size.
	 *
	 * @return The next suggested pushback size.
	 */
	public long getAndSetNextPushback(final PushbackStrategy strategy) {

		long value = strategy.nextWaitPeriod(callCount.get(), lastPushbackMillis.get(), TimeUnit.MILLISECONDS);

		lastPushbackMillis.set(value);

		return value;
	}
}
