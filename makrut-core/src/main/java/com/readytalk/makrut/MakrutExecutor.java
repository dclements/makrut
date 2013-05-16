package com.readytalk.makrut;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.Callable;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Executes a Callable statement in an implementation-specific manner.
 */
@ThreadSafe
public interface MakrutExecutor {
	/**
	 * Executes the given Callable.
	 *
	 * @param input The Callable to execute.
	 *
	 * @return A ListenableFuture for when the Callable has been run.
	 */
	<T, V extends Callable<T>> ListenableFuture<T> submit(V input);
}
