package com.readytalk.makrut.strategy;

import static com.google.common.base.Preconditions.checkArgument;

import javax.annotation.Nonnegative;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.google.common.annotations.VisibleForTesting;

/**
 * Default, common strategies for providing backoff values.   This is the amount of time that makrut will wait before
 * attempting to retry.
 */
public final class BackoffStrategies {

	@VisibleForTesting
	protected static final ThreadLocal<Random> RANDOM = new ThreadLocal<Random>() {

		@Override
		protected Random initialValue() {
			return new Random();
		}
	};

	private static final double HALF_SCALE = 0.5d;
	private static final double FULL_SCALE = 1.0d;


	private BackoffStrategies() {

	}

	/**
	 * A constant valued backoff.  Will always return same value.
	 *
	 * @param time The amount of time to wait between
	 * @param backoffUnit The time unit for the backoff.
	 */
	public static BackoffStrategy constantValue(@Nonnegative final long time, final TimeUnit backoffUnit) {
		checkArgument(time > 0, "Constant backoff must be greater than zero.");
		return new BackoffStrategy() {
			@Override
			public long nextWaitPeriod(@Nonnegative final int _lastExecutionCount,
					@Nonnegative final long _lastBackoff,
					final TimeUnit unit) {
				return unit.convert(time, backoffUnit);
			}
		};
	}

	/**
	 * Increases by a constant amount each time, e.g., 100, 150, 200, 250...
	 *
	 * @param firstTime The first backoff (100 in the example).
	 * @param increaseTime The amount to increase by each time (
	 * @param backoffUnit The time unit for the backoff.
	 */
	public static BackoffStrategy linearIncrease(@Nonnegative final long firstTime,
			@Nonnegative final long increaseTime,
			final TimeUnit backoffUnit) {
		checkArgument(firstTime >= 0, "First time argument must be greater than or equal to zero.");
		checkArgument(increaseTime > 0, "Further time arguments must be greater than zero.");

		return new BackoffStrategy() {
			@Override
			public long nextWaitPeriod(@Nonnegative final int lastExecutionCount,
					@Nonnegative final long lastBackoff,
					final TimeUnit unit) {
				if (lastExecutionCount == 1) {
					return unit.convert(firstTime, backoffUnit);
				}

				return unit.convert(increaseTime, backoffUnit) + lastBackoff;
			}
		};
	}

	/**
	 * An exponentially increasing backoff, e.g., 5, 10, 20, 40, 80...
	 *
	 * @param firstTime The amount of time for the first backoff (5 in the example).
	 * @param backoffUnit The time unit for the backoff.
	 * @param multiple The scale factor (2 in the example).
	 */
	public static BackoffStrategy exponentialIncrease(@Nonnegative final long firstTime,
			final TimeUnit backoffUnit,
			final long multiple) {

		checkArgument(firstTime > 0, "First time must be greater than zero for exponential backoff.");
		checkArgument(multiple > 0, "Multiple must be greater than zero.");
		return new BackoffStrategy() {
			@Override
			public long nextWaitPeriod(@Nonnegative final int lastExecutionCount,
					@Nonnegative final long lastBackoff,
					final TimeUnit unit) {
				if (lastExecutionCount == 1) {
					return unit.convert(firstTime, backoffUnit);
				}

				return multiple * lastBackoff;
			}
		};
	}

	/**
	 * Caps the amount of time that a strategy will backoff for, e.g., 5, 10, 20, 40, 50, 50,
	 * 50 for an exponential backoff (5, 2) with a max of 50.
	 *
	 * @param strategy The strategy to decorate.
	 * @param time The maximum amount of time.
	 * @param backoffUnit The time unit for the maximum.
	 */
	public static BackoffStrategy applyMax(final BackoffStrategy strategy,
			@Nonnegative final long time,
			final TimeUnit backoffUnit) {
		checkArgument(time > 0, "Max time must be positive.");

		return new BackoffStrategy() {
			@Override
			public long nextWaitPeriod(@Nonnegative final int lastExecutionCount,
					@Nonnegative final long lastBackoff,
					final TimeUnit unit) {
				return Math.min(strategy.nextWaitPeriod(lastExecutionCount, lastBackoff, unit),
						unit.convert(time, backoffUnit));
			}
		};
	}

	/**
	 * Scales the result of another strategy between, at most, 0.5 and 1.5 times the value based on an adjusted
	 * uniform random value.
	 *
	 * @param strategy The strategy to decorate.
	 * @param scaleFactor The amount to scale the result by, limiting the range. A scaleFactor of 0.5 will result in a
	 * multiple between 0.75 and 1.25.  One of zero is identically equivalent to multiplying by 1.0.
	 */
	public static BackoffStrategy applyUniformRandomScale(final BackoffStrategy strategy,
			@Nonnegative final double scaleFactor) {
		checkArgument(scaleFactor >= 0.0d, "scaleFactor must be greater than or equal to zero.");
		checkArgument(scaleFactor <= FULL_SCALE, "scaleFactor must be less than or equal to one.");

		return new BackoffStrategy() {
			@Override
			public long nextWaitPeriod(@Nonnegative final int lastExecutionCount,
					@Nonnegative final long lastBackoff,
					final TimeUnit unit) {
				double scale = FULL_SCALE + (scaleFactor * (RANDOM.get().nextDouble() - HALF_SCALE));

				return Math.round(strategy.nextWaitPeriod(lastExecutionCount, lastBackoff, unit) * scale);
			}
		};
	}

	/**
	 * Applies a uniform random amount of time, where min <= time < max, on top of another strategy.
	 *
	 * @param strategy The strategy to decorate.
	 * @param min The minimum amount of time.
	 * @param max The maximum amount of time.
	 * @param rangeUnit The unit of the time added to the strategy.
	 */
	public static BackoffStrategy applyUniformRandomRange(final BackoffStrategy strategy,
			@Nonnegative final int min,
			@Nonnegative final int max,
			final TimeUnit rangeUnit) {
		checkArgument(max >= 0, "max must be greater than zero.");
		checkArgument(min >= 0, "min must be greater than or equal to zero.");
		checkArgument(max > min, "max must be greater than min.");

		return new BackoffStrategy() {
			@Override
			public long nextWaitPeriod(@Nonnegative final int lastExecutionCount,
					@Nonnegative final long lastBackoff,
					final TimeUnit unit) {
				int value = RANDOM.get().nextInt(max - min) + min;

				return strategy.nextWaitPeriod(lastExecutionCount, lastBackoff, unit) + unit.convert(value, rangeUnit);
			}
		};
	}


	/**
	 * Treats the amount of time as a bounded gaussian random variable, using the result of the strategy as the mean
	 * and a provided standard deviation.  It is advised to use this mostly on larger values relative to the standard
	 * deviation.
	 *
	 * @param strategy The strategy to decorate.
	 * @param stddev The standard deviation of the random variate.
	 */
	public static BackoffStrategy applyGaussianRandom(final BackoffStrategy strategy, @Nonnegative final long stddev) {
		checkArgument(stddev > 0L, "Standard deviation must be a positive value.");

		return new BackoffStrategy() {
			@Override
			public long nextWaitPeriod(@Nonnegative final int lastExecutionCount,
					@Nonnegative final long lastBackoff,
					final TimeUnit unit) {
				Random random = RANDOM.get();

				long retval;

				do {
					retval = Math.round(random.nextGaussian() * stddev) + strategy.nextWaitPeriod(lastExecutionCount,
							lastBackoff, unit);
				} while (retval < 0L);

				return retval;
			}
		};
	}

}
