package com.readytalk.makrut.db.handlers;

import javax.annotation.WillNotClose;
import javax.annotation.concurrent.Immutable;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.common.collect.ImmutableList;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 * Returns an ImmutableList of results from a ResultSet. If the ResultSet is empty
 * then it will return an empty list.
 */
@Immutable
public class DelegatingListHandler<T> implements ResultSetHandler<ImmutableList<T>> {
	private final ResultSetRowHandler<T> delegate;

	public DelegatingListHandler(final ResultSetRowHandler<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public ImmutableList<T> handle(@WillNotClose final ResultSet rs) throws SQLException {
		ImmutableList.Builder<T> builder = ImmutableList.builder();

		while (rs.next()) {
			builder.add(delegate.handle(rs));
		}

		return builder.build();
	}
}
