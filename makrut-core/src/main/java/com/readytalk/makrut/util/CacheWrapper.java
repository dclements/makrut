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

	@SuppressWarnings("unchecked")
	public <T> T get(final Callable<T> key, final Callable<T> valueLoader) throws ExecutionException {
		return (T) cache.get(key, valueLoader);
	}

	@SuppressWarnings("unchecked")
	public <T> Optional<T> getIfPresent(final Callable<T> key) {
		return Optional.fromNullable((T) cache.getIfPresent(key));
	}

	public void put(final Callable<?> key, final Object value) {
		cache.put(key, value);
	}


}
