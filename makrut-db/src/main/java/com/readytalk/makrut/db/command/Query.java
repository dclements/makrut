package com.readytalk.makrut.db.command;

import java.sql.Connection;
import java.sql.SQLException;

import com.google.common.base.Optional;
import com.readytalk.makrut.MakrutExecutor;
import com.readytalk.makrut.db.ConnectionSupplier;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 * A query with some sort of return.
 *
 * @param <T> The return type.
 */
public class Query<T> extends AbstractDatabaseCommand<T> {

	private final ConnectionSupplier connectionSupplier;
	private final QueryRunner runner;
	private final ResultSetHandler<T> handler;
	private final String sql;
	private final Object[] params;

	public Query(final MakrutExecutor executor,
			final QueryRunner runner,
			final Optional<String> name,
			final ConnectionSupplier connectionSupplier,
			final ResultSetHandler<T> handler,
			final String sql,
			final Object... params) {
		super(executor, name.or("makrut") + ".db.update", sql, params);

		this.runner = runner;

		this.connectionSupplier = connectionSupplier;
		this.handler = handler;
		this.sql = sql;
		this.params = params;
	}

	@Override
	public T doCall() throws SQLException {
		Connection conn = connectionSupplier.getConnection();
		try {
			return runner.query(conn, sql, handler, params);
		} finally {
			connectionSupplier.close(conn);
		}
	}
}
