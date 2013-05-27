package com.readytalk.makrut.db.strategy;

/**
 * Retry strategies that operate off of SQLException types.
 */
public final class DBRetryStrategies {

	private DBRetryStrategies() {

	}

	public static RetryConnection retryConnection() {
		return new RetryConnection();
	}

	public static RetryRecoverable retryRecoverable() {
		return new RetryRecoverable();
	}

	public static RetryTransient retryTransient() {
		return new RetryTransient();
	}

}
