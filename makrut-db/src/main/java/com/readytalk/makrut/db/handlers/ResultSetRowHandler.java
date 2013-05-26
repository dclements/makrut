package com.readytalk.makrut.db.handlers;

import javax.annotation.WillNotClose;
import javax.annotation.concurrent.Immutable;
import java.sql.ResultSet;
import java.sql.SQLException;

@Immutable
public interface ResultSetRowHandler<T> {
	/**
	 * Handle a single row from a ResultSet without forwarding the iterator.
	 *
	 * @param rs The result set, forwarded to the appropriate row.
	 *
	 * @return The processed value, should never be null (may provide a collection or other object that can contain
	 *         nulls, however).
	 *
	 * @throws SQLException If the underlying ResultSet throws a SQLException.
	 */
	T handle(@WillNotClose ResultSet rs) throws SQLException;
}
