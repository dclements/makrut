package com.readytalk.makrut.db.handlers;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Function;

@Immutable
public final class RowHandlers {
	private RowHandlers() {

	}

	public static <V> MapRowHandler<V> mapRowHandler() {
		return new MapRowHandler<V>();
	}

	public static <V> OptionalMapRowHandler<V> optionalMapRowHandler() {
		return new OptionalMapRowHandler<V>();
	}

	public static <V> ScalarRowHandler<V> scalarRowHandler() {
		return new ScalarRowHandler<V>();
	}

	public static <V> OptionalScalarRowHandler<V> optionalScalarRowHandler() {
		return new OptionalScalarRowHandler<V>();
	}

	public static <I, O> TransformRowHandler<I, O> transformRowHandler(final ResultSetRowHandler<I> delegate,
			final Function<I, O> transform) {
		return new TransformRowHandler<I, O>(delegate, transform);
	}

}
