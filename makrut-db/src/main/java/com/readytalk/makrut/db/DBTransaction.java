package com.readytalk.makrut.db;

import static com.google.common.base.Preconditions.checkState;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Represents a transaction in the database.  Designed for sequential execution that may take place in separate
 * threads, so while it provides for thread visibility (a close in one thread will be represented in another),
 * it doesn't guard against race conditions.
 */
@NotThreadSafe
class DBTransaction implements Transaction {

	private final ConnectionSupplier supplier;
	private volatile Connection conn;

	public DBTransaction(final ConnectionSupplier supplier, final Connection conn) {
		this.supplier = supplier;
		this.conn = conn;
	}

	@Override
	public void commit() throws SQLException {
		checkIsOpen();

		conn.commit();
	}

	@Override
	public void rollback() throws SQLException {
		checkIsOpen();

		conn.rollback();
	}

	@Override
	public void close() throws SQLException {
		checkIsOpen();

		Connection toClose = conn;

		conn = null;

		supplier.close(toClose);
	}

	@Override
	public Connection getConnection() throws SQLException {
		checkIsOpen();

		return conn;
	}

	@Override
	public void close(@Nullable final Connection _conn) throws SQLException {
		checkIsOpen();

	}

	@Override
	public void closeQuietly(@Nullable final Connection _conn) {
		checkIsOpen();

	}

	@Override
	public Transaction beginTransaction() throws SQLException {
		throw new UnsupportedOperationException("Nested transactions not yet supported.");
	}

	private void checkIsOpen() {
		checkState(conn != null, "Connection has already been closed!");
	}
}
