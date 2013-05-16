package com.readytalk.makrut.strategy;

import static com.google.common.base.Preconditions.checkArgument;

import javax.annotation.Nonnegative;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.google.common.annotations.VisibleForTesting;

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

	public static BackoffStrategy exponentialIncrease(@Nonnegative final long firstTime,
			final TimeUnit backoffUnit,
			final long multiple) {

		checkArgument(firstTime > 0, "First time must be greater than for exponential backoff.");
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

	public static BackoffStrategy applyUniformRandom(final BackoffStrategy strategy,
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

	public static BackoffStrategy applyGaussianRandom(final BackoffStrategy strategy, @Nonnegative final long stddev) {
		checkArgument(stddev > 0L, "Standard deviation must be a positive value.");

		return new BackoffStrategy() {
			@Override
			public long nextWaitPeriod(@Nonnegative final int lastExecutionCount,
					@Nonnegative final long lastBackoff,
					final TimeUnit unit) {
				return Math.max(0L,
						Math.round(RANDOM.get().nextGaussian() * stddev) + strategy.nextWaitPeriod(lastExecutionCount,
								lastBackoff, unit));
			}
		};
	}

}
