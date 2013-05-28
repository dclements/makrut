package com.readytalk.makrut.db.handlers;

import javax.annotation.WillNotClose;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

/**
 * Converts a row into a map, with the column labels as keys. This is different from the DbUtils MapHandler,
 * which maps the column names as keys and ignores labeling in the SQL query.
 *
 * Null safe, returning Optional.absent() for null values.
 */
public class OptionalMapRowHandler<V> implements ResultSetRowHandler<ImmutableMap<String, Optional<V>>> {
	@Override
	@SuppressWarnings("unchecked")
	public ImmutableMap<String, Optional<V>> handle(@WillNotClose final ResultSet rs) throws SQLException {
		ImmutableMap.Builder<String, Optional<V>> retval = ImmutableMap.builder();

		ResultSetMetaData metaData = rs.getMetaData();

		final int columnCount = metaData.getColumnCount();

		for (int i = 1; i <= columnCount; i++) {
			retval.put(metaData.getColumnLabel(i), Optional.<V>fromNullable((V) rs.getObject(i)));
		}

		return retval.build();
	}
}
