package com.readytalk.makrut.db.handlers;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class DelegatingResultSetHandlers {

	private DelegatingResultSetHandlers() {

	}

	public static <T> DelegatingListHandler<T> listHandler(final ResultSetRowHandler<T> delegate) {
		return new DelegatingListHandler<T>(delegate);
	}

	public static <T> DelegatingOptionalSingleResultHandler<T> optionalSingleResultHandler(
			final ResultSetRowHandler<T> delegate) {
		return new DelegatingOptionalSingleResultHandler<T>(delegate);
	}

	public static <T> DelegatingSetHandler<T> setHandler(final ResultSetRowHandler<T> delegate) {
		return new DelegatingSetHandler<T>(delegate);
	}

	public static <T> DelegatingSingleResultHandler<T> singleResultHandler(final ResultSetRowHandler<T> delegate) {
		return new DelegatingSingleResultHandler<T>(delegate);
	}
}
