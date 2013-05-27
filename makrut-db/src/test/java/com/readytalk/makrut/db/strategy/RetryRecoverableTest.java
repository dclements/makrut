package com.readytalk.makrut.db.strategy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.SQLRecoverableException;
import java.sql.SQLTransientException;

import org.junit.Test;

public class RetryRecoverableTest {

	@Test
	public void shouldRetry_RecoverableException_ReturnsTrue() {
		assertTrue(DBRetryStrategies.retryRecoverable().shouldRetry(100, 1000L, new SQLRecoverableException()));
	}

	@Test
	public void shouldRetry_NonRecoverableException_ReturnsFalse() {
		assertFalse(DBRetryStrategies.retryRecoverable().shouldRetry(100, 1000L, new SQLTransientException()));
	}
}
