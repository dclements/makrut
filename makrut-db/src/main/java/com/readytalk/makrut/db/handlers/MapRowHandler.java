package com.readytalk.makrut.db.handlers;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.WillNotClose;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.google.common.collect.ImmutableMap;

/**
 * Converts a row into a map, with the column labels as keys. This is different from the DbUtils MapHandler,
 * which maps the column names as keys and ignores labeling in the SQL query.
 *
 * If any of the columns are null, it will throw a NullPointerException.
 */
public class MapRowHandler<V> implements ResultSetRowHandler<ImmutableMap<String, V>> {
	@Override
	@SuppressWarnings("unchecked")
	public ImmutableMap<String, V> handle(@WillNotClose final ResultSet rs) throws SQLException {
		ImmutableMap.Builder<String, V> retval = ImmutableMap.builder();

		ResultSetMetaData metaData = rs.getMetaData();

		final int columnCount = metaData.getColumnCount();

		for (int i = 1; i <= columnCount; i++) {
			retval.put(metaData.getColumnLabel(i),
					(V) checkNotNull(rs.getObject(i), "MapRowHandler does not support null valued columns."));
		}

		return retval.build();
	}
}
