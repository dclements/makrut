package com.readytalk.makrut.db.handlers;

import static com.google.common.base.Preconditions.checkArgument;

import javax.annotation.WillNotClose;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.ResultSetHandler;

public class DelegatingSingleResultHandler<T> implements ResultSetHandler<T> {

	private final ResultSetRowHandler<T> delegate;

	public DelegatingSingleResultHandler(final ResultSetRowHandler<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public T handle(@WillNotClose final ResultSet rs) throws SQLException {
		checkArgument(rs.next(), "ResultSet did not return a value!");

		return delegate.handle(rs);
	}
}
