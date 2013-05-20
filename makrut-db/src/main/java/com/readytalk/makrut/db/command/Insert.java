package com.readytalk.makrut.db.command;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.google.common.base.Optional;
import com.readytalk.makrut.MakrutExecutor;
import com.readytalk.makrut.db.ConnectionSupplier;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 * Represents an insertion into the database.
 *
 * @param <K> The key type.
 */
public class Insert<K> extends AbstractDatabaseCommand<K> {

	private final ConnectionSupplier connectionSupplier;
	private final QueryRunner runner;
	private final String sql;
	private final ResultSetHandler<K> handler;
	private final Object[] params;

	public Insert(final MakrutExecutor executor,
			final QueryRunner runner,
			final Optional<String> name,
			final ConnectionSupplier connectionSupplier,
			final ResultSetHandler<K> handler,
			final String sql,
			final Object... params) {
		super(executor, name.or("makrut") + ".db.update", sql, params);

		this.runner = runner;

		this.connectionSupplier = connectionSupplier;
		this.sql = sql;
		this.handler = handler;
		this.params = params;
	}

	@Override
	@SuppressWarnings("unchecked")
	public K doCall() throws SQLException {

		K retval = null;

		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			conn = connectionSupplier.getConnection();

			stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

			runner.fillStatement(stmt, params);

			stmt.executeUpdate();

			ResultSet r = null;

			try {
				r = stmt.getGeneratedKeys();

				retval = handler.handle(r);

			} finally {
				DbUtils.close(r);
			}


		} finally {
			DbUtils.close(stmt);

			connectionSupplier.close(conn);
		}

		return retval;
	}
}
