package com.readytalk.makrut.db.strategy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLTransientConnectionException;

import org.junit.Test;

public class RetryConnectionTest {

	@Test
	public void shouldRetry_NonTransientConnectionException_ReturnsTrue() {
		assertTrue(
				DBRetryStrategies.retryConnection().shouldRetry(100, 1000L, new SQLNonTransientConnectionException()));
	}

	@Test
	public void shouldRetry_TransientConnectionException_ReturnsTrue() {
		assertTrue(DBRetryStrategies.retryConnection().shouldRetry(100, 1000L, new SQLTransientConnectionException()));
	}

	@Test
	public void shouldRetry_NonConnectionException_ReturnsFalse() {
		assertFalse(DBRetryStrategies.retryConnection().shouldRetry(100, 1000L, new SQLException()));
	}
}
