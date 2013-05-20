package com.readytalk.makrut.db;

import javax.annotation.Nullable;
import javax.annotation.WillClose;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides Connection objects from a DataSource.
 */
@ThreadSafe
public class DataSourceConnectionSupplier implements ConnectionSupplier {
	private static final Logger LOG = LoggerFactory.getLogger(DataSourceConnectionSupplier.class);

	private final DataSource source;

	@Inject
	public DataSourceConnectionSupplier(final DataSource source) {
		this.source = source;
	}

	@Override
	public void close(@WillClose @Nullable final Connection conn) throws SQLException {
		DbUtils.close(conn);
	}

	@Override
	public void closeQuietly(@WillClose @Nullable final Connection conn) {
		try {
			DbUtils.close(conn);
		} catch (Exception ex) {
			LOG.warn("Exception while closing connection.", ex);
		}

	}

	@Override
	public Connection getConnection() throws SQLException {
		return source.getConnection();
	}

	@Override
	public Transaction beginTransaction() throws SQLException {

		final Connection conn = this.getConnection();

		conn.setAutoCommit(false);

		return new DBTransaction(this, conn);
	}
}
