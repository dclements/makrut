package com.readytalk.makrut.db.command;

import javax.annotation.concurrent.ThreadSafe;
import java.sql.SQLException;

import com.readytalk.makrut.MakrutExecutor;
import com.readytalk.makrut.command.BasicMakrutCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * A command that will be executed against the database.
 *
 * @param <T> The return type for the command.
 */
@ThreadSafe
public abstract class AbstractDatabaseCommand<T> extends BasicMakrutCommand<T> {
	private static final Marker SQL_LOGGING = MarkerFactory.getMarker("sql");

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final String sql;

	public AbstractDatabaseCommand(final MakrutExecutor executor,
			final String commandName,
			final String sql,
			final Object... args) {
		super(executor, commandName, prepend(sql, args));

		this.sql = sql;
	}

	@Override
	public T call() throws SQLException {
		logger.debug(SQL_LOGGING, "SQL: {}", sql);
		return doCall();
	}

	private static Object[] prepend(final Object value, final Object... values) {
		Object[] retval = new Object[values.length + 1];

		if (values.length > 0) {
			System.arraycopy(values, 0, retval, 1, values.length);
		}

		retval[0] = value;

		return retval;
	}

	/**
	 * The actual call that will be processed.
	 */
	protected abstract T doCall() throws SQLException;
}
