package com.readytalk.makrut.db.handlers;

import static com.google.common.base.Preconditions.checkArgument;

import javax.annotation.WillNotClose;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.ResultSetHandler;

/**
 * Returns a single result (the first one) from the ResultSet.  If the ResultSet is empty,
 * then it will throw an exception.
 *
 */
public class DelegatingSingleResultHandler<T> implements ResultSetHandler<T> {

	private final ResultSetRowHandler<T> delegate;

	public DelegatingSingleResultHandler(final ResultSetRowHandler<T> delegate) {
		this.delegate = delegate;
	}

	/**
	 * Processes a ResultSet.
	 *
	 * @param rs The ResultSet to process the first row from.
	 * @return The first row, processed by the delegate, from the ResultSet.
	 * @throws SQLException If the underlying ResultSet throws an exception.
	 * @throws IllegalArgumentException If the ResultSet is empty.
	 */
	@Override
	public T handle(@WillNotClose final ResultSet rs) throws SQLException {
		checkArgument(rs.next(), "ResultSet did not return a value!");

		return delegate.handle(rs);
	}
}
