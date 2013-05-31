package com.readytalk.makrut.strategy;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

import javax.annotation.Nonnegative;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(Enclosed.class)
public class BackoffStrategiesTest {

	@RunWith(Parameterized.class)
	public static class MainBackoffStrategies {
		private final int lastExecutionCount;
		private final long lastBackoff;
		private final TimeUnit unit;

		@Parameters
		public static Collection<Object[]> data() {
			return Arrays.asList(new Object[][]{ { 1, 0L, TimeUnit.SECONDS }, { 2, 1L, TimeUnit.SECONDS },
					{ 3, 10L, TimeUnit.SECONDS }, { 4, 15L, TimeUnit.SECONDS }, { 5, 100L, TimeUnit.SECONDS },
					{ 6, 1000L, TimeUnit.SECONDS }, });

		}

		public MainBackoffStrategies(@Nonnegative final int lastExecutionCount,
				@Nonnegative final long lastBackoff,
				final TimeUnit unit) {
			this.lastExecutionCount = lastExecutionCount;
			this.lastBackoff = lastBackoff;
			this.unit = unit;
		}

		@Before
		public void setUp() throws Exception {

		}

		@After
		public void tearDown() throws Exception {

		}

		@Test
		public void constantValue_WithAnyParams_AlwaysReturnsSame() {
			assertEquals(3L, BackoffStrategies.constantValue(3L, TimeUnit.SECONDS)
					.nextWaitPeriod(lastExecutionCount, lastBackoff, unit));
		}

		@Test
		public void linearIncrease_WhenFirstIteration_ProvidesFirstValue() {

			assertEquals(11L,
					BackoffStrategies.linearIncrease(11L, 10L, TimeUnit.SECONDS).nextWaitPeriod(1, lastBackoff, unit));
		}

		@Test
		public void linearIncrease_WhenLaterIterations_LinearlyIncrements() {
			Assume.assumeThat(lastExecutionCount, is(not(equalTo(1))));

			assertEquals(lastBackoff + 10L, BackoffStrategies.linearIncrease(11L, 10L, unit)
					.nextWaitPeriod(lastExecutionCount, lastBackoff, unit));
		}

		@Test
		public void exponentialIncrease_WhenFirstIteration_ProvidesFirstValue() {

			assertEquals(11L, BackoffStrategies.exponentialIncrease(11L, TimeUnit.MILLISECONDS, 2L)
					.nextWaitPeriod(1, lastBackoff, TimeUnit.MILLISECONDS));
		}

		@Test
		public void exponentialIncrease_WhenLaterIterations_LinearlyIncrements() {
			Assume.assumeThat(lastExecutionCount, is(not(equalTo(1))));

			assertEquals(lastBackoff * 3L, BackoffStrategies.exponentialIncrease(10L, TimeUnit.MILLISECONDS, 3L)
					.nextWaitPeriod(lastExecutionCount, lastBackoff, unit));
		}
	}

	public static class MainBackoffStrategiesExceptions {
		@Rule
		public final ExpectedException thrown = ExpectedException.none();

		@Before
		public void setUp() {
			MockitoAnnotations.initMocks(this);
		}

		@After
		public void tearDown() {

		}

		@Test
		public void constantValue_EqualZero_ThrowsIAE() {
			thrown.expect(IllegalArgumentException.class);

			BackoffStrategies.constantValue(0L, TimeUnit.DAYS);
		}

		@Test
		@SuppressFBWarnings("TQ_NEVER_VALUE_USED_WHERE_ALWAYS_REQUIRED")
		public void linearIncrease_NegativeFirstValue_ThrowsIAE() {
			thrown.expect(IllegalArgumentException.class);

			BackoffStrategies.linearIncrease(-1L, 100L, TimeUnit.DAYS);
		}

		@Test
		public void linearIncrease_ZeroIncreases_ThrowsIAE() {
			thrown.expect(IllegalArgumentException.class);

			BackoffStrategies.linearIncrease(100L, 0L, TimeUnit.DAYS);
		}

		@Test
		public void exponentialIncrease_ZeroFirstValue_ThrowsIAE() {
			thrown.expect(IllegalArgumentException.class);

			BackoffStrategies.exponentialIncrease(0L, TimeUnit.SECONDS, 3L);
		}

		@Test
		public void exponentialIncrease_ZeroMultiple_ThrowsIAE() {
			thrown.expect(IllegalArgumentException.class);

			BackoffStrategies.exponentialIncrease(100L, TimeUnit.SECONDS, 0L);
		}

	}

	public static class MetaStrategies {
		@Rule
		public final ExpectedException thrown = ExpectedException.none();

		@Mock
		private BackoffStrategy strategy;

		@Before
		public void setUp() {
			MockitoAnnotations.initMocks(this);
		}

		@After
		public void tearDown() {

		}

		@Test
		public void applyMax_WhenUnder_ReturnsValue() {
			when(strategy.nextWaitPeriod(anyInt(), anyLong(), any(TimeUnit.class))).thenReturn(100L);

			assertEquals(100L, BackoffStrategies.applyMax(strategy, 1L, TimeUnit.SECONDS)
					.nextWaitPeriod(1, 10L, TimeUnit.MILLISECONDS));
		}

		@Test
		public void applyMax_WhenEqualZero_ThrowsIAE() {
			thrown.expect(IllegalArgumentException.class);

			BackoffStrategies.applyMax(strategy, 0L, TimeUnit.SECONDS);
		}

		@Test
		public void applyMax_WhenOver_ReturnsMaxValue() {
			when(strategy.nextWaitPeriod(anyInt(), anyLong(), any(TimeUnit.class))).thenReturn(1500L);

			assertEquals(1000L, BackoffStrategies.applyMax(strategy, 1L, TimeUnit.SECONDS)
					.nextWaitPeriod(1, 10L, TimeUnit.MILLISECONDS));
		}

		@Test
		public void applyUniformRandomScale_OnMaxScale_ReturnsFullAdjustment() {
			when(strategy.nextWaitPeriod(anyInt(), anyLong(), any(TimeUnit.class))).thenReturn(1500L);

			Random r = new Random(400);

			BackoffStrategies.RANDOM.set(r);

			assertEquals(1900, BackoffStrategies.applyUniformRandomScale(strategy, 1.0)
					.nextWaitPeriod(1, 100L, TimeUnit.MILLISECONDS));

		}

		@Test
		public void applyUniformRandomRange_BetweenValues_ReturnsRandomValue() {
			when(strategy.nextWaitPeriod(anyInt(), anyLong(), any(TimeUnit.class))).thenReturn(1500L);

			Random r = new Random(400);

			BackoffStrategies.RANDOM.set(r);

			assertEquals(1940, BackoffStrategies.applyUniformRandomRange(strategy, 100, 500, TimeUnit.MILLISECONDS)
					.nextWaitPeriod(1, 100L, TimeUnit.MILLISECONDS));

		}

		@Test
		public void applyUniformRandomRange_MaxLessThanMin_ThrowsIAE() {
			thrown.expect(IllegalArgumentException.class);

			BackoffStrategies.applyUniformRandomRange(strategy, 500, 100, TimeUnit.MILLISECONDS);
		}

		@Test
		public void applyUniformRandomRange_MaxEqualsMin_ThrowsIAE() {
			thrown.expect(IllegalArgumentException.class);

			BackoffStrategies.applyUniformRandomRange(strategy, 100, 100, TimeUnit.MILLISECONDS);
		}

		@Test
		@SuppressFBWarnings("TQ_NEVER_VALUE_USED_WHERE_ALWAYS_REQUIRED")
		public void applyUniformRandomRange_MaxLessThanZero_ThrowsIAE() {
			thrown.expect(IllegalArgumentException.class);

			BackoffStrategies.applyUniformRandomRange(strategy, 100, -100, TimeUnit.MILLISECONDS);
		}

		@Test
		public void applyUniformRandomRange_MaxEqualToZero_ThrowsIAE() {
			thrown.expect(IllegalArgumentException.class);

			BackoffStrategies.applyUniformRandomRange(strategy, 100, 0, TimeUnit.MILLISECONDS);
		}

		@Test
		@SuppressFBWarnings("TQ_NEVER_VALUE_USED_WHERE_ALWAYS_REQUIRED")
		public void applyUniformRandomRange_MinLessThanZero_ThrowsIAE() {
			thrown.expect(IllegalArgumentException.class);

			BackoffStrategies.applyUniformRandomRange(strategy, -100, 100, TimeUnit.MILLISECONDS);
		}

		@Test
		public void applyUniformRandom_OnHalfScale_ReturnsHalfAdjustment() {
			when(strategy.nextWaitPeriod(anyInt(), anyLong(), any(TimeUnit.class))).thenReturn(1500L);

			Random r = new Random(400);

			BackoffStrategies.RANDOM.set(r);

			assertEquals(1700, BackoffStrategies.applyUniformRandomScale(strategy, 0.5d)
					.nextWaitPeriod(1, 100L, TimeUnit.MILLISECONDS));

		}

		@Test
		@SuppressFBWarnings("TQ_NEVER_VALUE_USED_WHERE_ALWAYS_REQUIRED")
		public void applyUniformRandom_NegativeScale_ThrowsIAE() {
			thrown.expect(IllegalArgumentException.class);

			BackoffStrategies.applyUniformRandomScale(strategy, -1.0d);

		}

		@Test
		public void applyUniformRandom_OverOneScale_ThrowsIAE() {
			thrown.expect(IllegalArgumentException.class);

			BackoffStrategies.applyUniformRandomScale(strategy, 1.5d);

		}

		@Test
		public void applyGaussianRandom_WhenCalled_ReturnsAdjustedValue() {
			when(strategy.nextWaitPeriod(anyInt(), anyLong(), any(TimeUnit.class))).thenReturn(1500L);

			Random r = new Random(400);

			BackoffStrategies.RANDOM.set(r);

			assertEquals(1574,
					BackoffStrategies.applyGaussianRandom(strategy, 50).nextWaitPeriod(1, 100L,
							TimeUnit.MILLISECONDS));
		}

		@Test
		@SuppressFBWarnings("TQ_NEVER_VALUE_USED_WHERE_ALWAYS_REQUIRED")
		public void applyGaussianRandom_WhenNegativeStdDev_ThrowsIAE() {
			thrown.expect(IllegalArgumentException.class);

			BackoffStrategies.applyGaussianRandom(strategy, -50);

		}
	}

}
