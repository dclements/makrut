package com.readytalk.makrut.db.strategy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLTransientException;

import org.junit.Test;

public class RetryTransientTest {

	@Test
	public void shouldRetry_TransientException_ReturnsTrue() {
		assertTrue(DBRetryStrategies.retryTransient().shouldRetry(100, 1000L, new SQLTransientException()));
	}

	@Test
	public void shouldRetry_NonTransientException_ReturnsFalse() {
		assertFalse(
				DBRetryStrategies.retryTransient().shouldRetry(100, 1000L, new SQLNonTransientConnectionException()));
	}
}
