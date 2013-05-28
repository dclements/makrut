package com.readytalk.makrut.db.handlers;

import javax.annotation.WillNotClose;
import javax.annotation.concurrent.Immutable;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.common.base.Function;

/**
 * Provides a transform for a given result from a ResultSet.
 *
 * @param <I> The input type, coming out of the delegate.
 * @param <O> The output type, which will be returned to the ResultSetHandler.
 */
@Immutable
public class TransformRowHandler<I, O> implements ResultSetRowHandler<O> {

	private final ResultSetRowHandler<I> delegate;
	private final Function<I, O> transform;

	public TransformRowHandler(final ResultSetRowHandler<I> delegate, final Function<I, O> transform) {
		this.delegate = delegate;
		this.transform = transform;
	}

	@Override
	public O handle(@WillNotClose final ResultSet rs) throws SQLException {
		return transform.apply(delegate.handle(rs));
	}
}
