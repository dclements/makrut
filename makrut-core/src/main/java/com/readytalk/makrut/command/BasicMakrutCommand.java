package com.readytalk.makrut.command;

import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureFallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.readytalk.makrut.MakrutExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A version of the MakrutCommand that wraps submit functionality from the executor.
 */
@ThreadSafe
public abstract class BasicMakrutCommand<T> extends MakrutCommand<T> {
	private static final Logger LOGGER = LoggerFactory.getLogger(BasicMakrutCommand.class);

	private final MakrutExecutor executor;

	public BasicMakrutCommand(final MakrutExecutor executor, final String commandName, final Object... args) {
		super(commandName, args);

		this.executor = executor;
	}

	public Optional<Callable<T>> doFallback() {
		return Optional.absent();
	}

	private ListenableFuture<T> addFallback(final ListenableFuture<T> future, final Callable<T> fallback) {
		return Futures.withFallback(future, new FutureFallback<T>() {
			@Override
			public ListenableFuture<T> create(final Throwable t) throws Exception {

				ListenableFuture<T> retval;

				if (t instanceof CancellationException) {
					retval = Futures.immediateCancelledFuture();
				} else {
					try {
						retval = Futures.immediateFuture(fallback.call());
					} catch (Exception ex) {
						LOGGER.warn("doFallback() threw an exception. This should never happen.", ex);
						retval = Futures.immediateFailedFuture(t);
					}
				}

				return retval;
			}
		});
	}

	public ListenableFuture<T> submit() {
		ListenableFuture<T> retval = this.executor.submit(this);

		Optional<Callable<T>> fallback = this.doFallback();

		if (fallback.isPresent()) {
			retval = addFallback(retval, fallback.get());
		}

		return retval;
	}

	public T submitAndGet() throws ExecutionException, InterruptedException {
		return this.submit().get();
	}
}
