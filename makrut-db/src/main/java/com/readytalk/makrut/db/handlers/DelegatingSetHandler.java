package com.readytalk.makrut.db.handlers;

import javax.annotation.WillNotClose;
import javax.annotation.concurrent.Immutable;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.dbutils.ResultSetHandler;

@Immutable
public class DelegatingSetHandler<T> implements ResultSetHandler<ImmutableSet<T>> {
	private final ResultSetRowHandler<T> delegate;

	public DelegatingSetHandler(final ResultSetRowHandler<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public ImmutableSet<T> handle(@WillNotClose final ResultSet rs) throws SQLException {
		ImmutableSet.Builder<T> builder = ImmutableSet.builder();

		while (rs.next()) {
			builder.add(delegate.handle(rs));
		}

		return builder.build();
	}
}
