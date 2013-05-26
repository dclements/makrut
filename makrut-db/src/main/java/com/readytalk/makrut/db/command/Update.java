package com.readytalk.makrut.db.command;

import javax.annotation.concurrent.ThreadSafe;
import java.sql.Connection;
import java.sql.SQLException;

import com.google.common.base.Optional;
import com.readytalk.makrut.MakrutExecutor;
import com.readytalk.makrut.db.ConnectionSupplier;
import org.apache.commons.dbutils.QueryRunner;

/**
 * A database update or insert where a number is returned.
 */
@ThreadSafe
public class Update extends AbstractDatabaseCommand<Integer> {

	private final ConnectionSupplier connectionSupplier;
	private final QueryRunner runner;
	private final String sql;
	private final Object[] params;

	public Update(final MakrutExecutor executor,
			final QueryRunner runner,
			final Optional<String> name,
			final ConnectionSupplier connectionSupplier,
			final String sql,
			final Object... params) {
		super(executor, name.or("makrut") + ".db.update", sql, params);

		this.runner = runner;

		this.connectionSupplier = connectionSupplier;
		this.sql = sql;
		this.params = params;
	}

	@Override
	public Integer doCall() throws SQLException {
		Connection conn = connectionSupplier.getConnection();
		try {
			return runner.update(conn, sql, params);
		} finally {
			connectionSupplier.close(conn);
		}
	}
}
