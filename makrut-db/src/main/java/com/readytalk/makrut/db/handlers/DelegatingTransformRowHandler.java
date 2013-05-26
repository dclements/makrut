package com.readytalk.makrut.db.handlers;

import javax.annotation.WillNotClose;
import javax.annotation.concurrent.Immutable;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.common.base.Function;

@Immutable
public class DelegatingTransformRowHandler<I, O> implements ResultSetRowHandler<O> {

	private final ResultSetRowHandler<I> delegate;
	private final Function<I, O> transform;

	public DelegatingTransformRowHandler(final ResultSetRowHandler<I> delegate, final Function<I, O> transform) {
		this.delegate = delegate;
		this.transform = transform;
	}

	@Override
	public O handle(@WillNotClose final ResultSet rs) throws SQLException {
		return transform.apply(delegate.handle(rs));
	}
}
