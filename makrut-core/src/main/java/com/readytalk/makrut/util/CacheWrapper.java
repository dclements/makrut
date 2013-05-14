package com.readytalk.makrut.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;

/**
 * A wrapper for a Guava cache that stores callables with different return values, casting results based on later get
 * request.
 */
public class CacheWrapper {

	private final Cache<Callable<?>, Object> cache;

	public CacheWrapper(final Cache<Callable<?>, Object> cache) {
		this.cache = cache;
	}

	/**
	 * Follows the standard cache usage pattern: Checks to see if a value is present for a given key,
	 * if the value isn't there it populates the cache using the valueLoader, then it returns the result.
	 *
	 * @param key A Callable representing a method invocation.
	 * @param valueLoader The method for loading a callable.
	 *
	 * @return The latest value in the cache, possibly after populating the cache with that value.
	 *
	 * @throws ExecutionException If valueLoader throws an exception.
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(final Callable<T> key, final Callable<T> valueLoader) throws ExecutionException {
		return (T) cache.get(key, valueLoader);
	}

	/**
	 * Gets an optional result from the cache.
	 *
	 * @param key A Callable representing a method invocation.
	 *
	 * @return The latest value in the cache, or absent() if the cache does not contain a given value.
	 */
	@SuppressWarnings("unchecked")
	public <T> Optional<T> getOptional(final Callable<T> key) {
		return Optional.fromNullable((T) cache.getIfPresent(key));
	}

	/**
	 * Assigns a given value to a given key.
	 *
	 * @param key A Callable representing a method invocation.
	 * @param value The value to associate with the key.
	 */
	public void put(final Callable<?> key, final Object value) {
		cache.put(key, value);
	}


}
