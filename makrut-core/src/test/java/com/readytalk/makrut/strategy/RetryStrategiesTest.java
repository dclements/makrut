package com.readytalk.makrut.strategy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Predicate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class RetryStrategiesTest {

	private final Exception ex = new SQLException();

	@Mock
	private RetryStrategy strategy1;

	@Mock
	private RetryStrategy strategy2;

	@Mock
	private Predicate<Exception> pred;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@After
	public void tearDown() {

	}

	@Test
	public void alwaysRetry_OnInput_ReturnsTrue() {
		assertTrue(RetryStrategies.alwaysRetry().shouldRetry(10, 10L, new Exception()));
	}

	@Test
	public void neverRetry_OnInput_ReturnsFalse() {
		assertFalse(RetryStrategies.neverRetry().shouldRetry(10, 10L, new Exception()));
	}

	@Test
	public void and_OnAllTrue_ReturnsTrue() {
		when(strategy1.shouldRetry(anyInt(), anyLong(), any(Exception.class))).thenReturn(true);
		when(strategy2.shouldRetry(anyInt(), anyLong(), any(Exception.class))).thenReturn(true);

		assertTrue(RetryStrategies.and(strategy1, strategy2).shouldRetry(10, 10L, ex));
	}

	@Test
	public void and_OnAnyFalse_ReturnsFalse() {
		when(strategy1.shouldRetry(anyInt(), anyLong(), any(Exception.class))).thenReturn(true);
		when(strategy2.shouldRetry(anyInt(), anyLong(), any(Exception.class))).thenReturn(false);

		assertFalse(RetryStrategies.and(strategy1, strategy2).shouldRetry(10, 10L, ex));
	}

	@Test
	public void and_OnFalseValue_ShortCircuits() {
		when(strategy1.shouldRetry(anyInt(), anyLong(), any(Exception.class))).thenReturn(false);
		when(strategy2.shouldRetry(anyInt(), anyLong(), any(Exception.class))).thenThrow(
				new RuntimeException("Did not short circuit."));

		assertFalse(RetryStrategies.and(strategy1, strategy2).shouldRetry(10, 10L, ex));
	}

	@Test
	public void or_OnAnyTrue_ReturnsTrue() {
		when(strategy1.shouldRetry(anyInt(), anyLong(), any(Exception.class))).thenReturn(false);
		when(strategy2.shouldRetry(anyInt(), anyLong(), any(Exception.class))).thenReturn(true);

		assertTrue(RetryStrategies.or(strategy1, strategy2).shouldRetry(10, 10L, ex));
	}

	@Test
	public void or_OnAllFalse_ReturnsFalse() {
		when(strategy1.shouldRetry(anyInt(), anyLong(), any(Exception.class))).thenReturn(false);
		when(strategy2.shouldRetry(anyInt(), anyLong(), any(Exception.class))).thenReturn(false);

		assertFalse(RetryStrategies.or(strategy1, strategy2).shouldRetry(10, 10L, ex));
	}

	@Test
	public void or_OnAnyTrue_ShortCircuits() {
		when(strategy1.shouldRetry(anyInt(), anyLong(), any(Exception.class))).thenReturn(true);
		when(strategy2.shouldRetry(anyInt(), anyLong(), any(Exception.class))).thenThrow(
				new RuntimeException("Did not short circuit."));

		assertTrue(RetryStrategies.or(strategy1, strategy2).shouldRetry(10, 10L, ex));
	}

	@Test
	public void not_OnTrue_ReturnsFalse() {
		when(strategy1.shouldRetry(anyInt(), anyLong(), any(Exception.class))).thenReturn(true);

		assertFalse(RetryStrategies.not(strategy1).shouldRetry(10, 10L, ex));
	}

	@Test
	public void not_OnFalse_ReturnsTrue() {
		when(strategy1.shouldRetry(anyInt(), anyLong(), any(Exception.class))).thenReturn(true);

		assertFalse(RetryStrategies.not(strategy1).shouldRetry(10, 10L, ex));
	}

	@Test
	public void forExceptionPredicate_OnTrue_ReturnsTrue() {
		when(pred.apply(any(Exception.class))).thenReturn(true);

		assertTrue(RetryStrategies.forExceptionPredicate(pred).shouldRetry(10, 10L, ex));
	}

	@Test
	public void forExceptionPredicate_OnTrue_ReturnsTrue_OnFalse_ReturnsFalse() {
		when(pred.apply(any(Exception.class))).thenReturn(false);

		assertFalse(RetryStrategies.forExceptionPredicate(pred).shouldRetry(10, 10L, ex));
	}

	@Test
	public void underTimeLimit_WhenUnder_ReturnsTrue() {

		assertTrue(RetryStrategies.underTimeLimit(1L, TimeUnit.SECONDS).shouldRetry(10, 999L, ex));
	}

	@Test
	public void underTimeLimit_WhenEqual_ReturnsFalse() {

		assertFalse(RetryStrategies.underTimeLimit(1L, TimeUnit.SECONDS).shouldRetry(10, 1000L, ex));
	}

	@Test
	public void underTimeLimit_WhenOver_ReturnsFalse() {

		assertFalse(RetryStrategies.underTimeLimit(1L, TimeUnit.SECONDS).shouldRetry(10, 1001L, ex));
	}

	@Test
	public void allowNumberOfAttempts_WhenOver_ReturnsFalse() {

		assertFalse(RetryStrategies.allowNumberOfAttempts(3).shouldRetry(4, 10L, ex));
	}

	@Test
	public void allowNumberOfAttempts_WhenUnder_ReturnsTrue() {

		assertTrue(RetryStrategies.allowNumberOfAttempts(3).shouldRetry(2, 10L, ex));
	}

	@Test
	public void allowNumberOfAttempts_WhenEquals_ReturnsFalse() {

		assertFalse(RetryStrategies.allowNumberOfAttempts(3).shouldRetry(3, 10L, ex));
	}


}
