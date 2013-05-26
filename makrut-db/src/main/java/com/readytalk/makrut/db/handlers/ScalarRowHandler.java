package com.readytalk.makrut.db.handlers;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.WillNotClose;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ScalarRowHandler<T> implements ResultSetRowHandler<T> {
	@Override
	@SuppressWarnings("unchecked")
	public T handle(@WillNotClose final ResultSet rs) throws SQLException {

		T retval = (T) rs.getObject(1);

		return checkNotNull(retval, "Returned value from database was null.");
	}
}
