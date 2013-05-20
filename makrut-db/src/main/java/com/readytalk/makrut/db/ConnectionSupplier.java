package com.readytalk.makrut.db;

import javax.annotation.Nullable;
import javax.annotation.WillClose;
import javax.annotation.concurrent.ThreadSafe;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Supplies database connections and closes them properly.
 */
@ThreadSafe
public interface ConnectionSupplier {

	Connection getConnection() throws SQLException;

	void close(@WillClose @Nullable Connection conn) throws SQLException;

	void closeQuietly(@WillClose @Nullable Connection conn);

	Transaction beginTransaction() throws SQLException;
}
