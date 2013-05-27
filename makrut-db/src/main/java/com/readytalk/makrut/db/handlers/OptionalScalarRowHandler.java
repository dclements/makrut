package com.readytalk.makrut.db.handlers;

import javax.annotation.WillNotClose;
import javax.annotation.concurrent.Immutable;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.common.base.Optional;

/**
 * Returns an optional scalar value from the first column of a ResultSet.
 *
 * If the object is null then it returns Optional.absent().
 */
@Immutable
public class OptionalScalarRowHandler<T> implements ResultSetRowHandler<Optional<T>> {
	@Override
	@SuppressWarnings("unchecked")
	public Optional<T> handle(@WillNotClose final ResultSet rs) throws SQLException {

		return Optional.fromNullable((T) rs.getObject(1));
	}
}
