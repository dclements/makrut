package com.readytalk.makrut.db.handlers;

import javax.annotation.WillNotClose;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.common.base.Optional;
import org.apache.commons.dbutils.ResultSetHandler;

public class DelegatingOptionalSingleResultHandler<T> implements ResultSetHandler<Optional<T>> {
	private final ResultSetRowHandler<T> delegate;

	public DelegatingOptionalSingleResultHandler(final ResultSetRowHandler<T> delegate) {
		this.delegate = delegate;
	}


	@Override
	public Optional<T> handle(@WillNotClose final ResultSet rs) throws SQLException {
		if (!rs.next()) {
			return Optional.absent();
		}

		return Optional.of(delegate.handle(rs));
	}
}
