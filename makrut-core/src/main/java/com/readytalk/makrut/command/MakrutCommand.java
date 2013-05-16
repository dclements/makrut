package com.readytalk.makrut.command;


import javax.annotation.concurrent.Immutable;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A MakrutCommand, usable instead of a Callable to allow for caching.  Caches its own hashCode value for performance
 * reasons, presuming that the underlying arguments are not going to change from underneath it.
 *
 * It is recommended that this structure be used instead of directly using Callables, since it will allow for caching.
 */
@Immutable
public abstract class MakrutCommand<T> implements Callable<T> {
	private static final int HASH_MULTIPLE = 3;

	private final String commandName;
	private final Object[] args;
	private final AtomicInteger cachedHash = new AtomicInteger(0);

	public MakrutCommand(final String commandName, final Object... args) {
		this.commandName = commandName;
		this.args = args;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(final Object o) {
		if (!(o instanceof MakrutCommand)) {
			return false;
		} else if (this == o) {
			return true;
		}

		MakrutCommand<T> cmd = (MakrutCommand<T>) o;

		if (this.hashCode() != o.hashCode()) {
			return false;
		}

		return this.commandName.equals(cmd.commandName) && Arrays.equals(args, cmd.args);
	}

	@Override
	public int hashCode() {
		if (cachedHash.get() == 0) {
			cachedHash.compareAndSet(0, Arrays.hashCode(args) + HASH_MULTIPLE * this.commandName.hashCode());
		}
		return cachedHash.get();
	}
}
