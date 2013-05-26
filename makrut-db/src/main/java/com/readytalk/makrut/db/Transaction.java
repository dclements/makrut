package com.readytalk.makrut.db;

import javax.annotation.concurrent.ThreadSafe;
import java.sql.SQLException;

/**
 * An active transaction.
 */
@ThreadSafe
public interface Transaction extends ConnectionSupplier {
	void commit() throws SQLException;
	void rollback() throws SQLException;

	void close() throws SQLException;
}
