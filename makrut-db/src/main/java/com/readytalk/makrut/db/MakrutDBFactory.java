package com.readytalk.makrut.db;

import javax.annotation.concurrent.ThreadSafe;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.readytalk.makrut.MakrutExecutor;
import com.readytalk.makrut.db.command.Insert;
import com.readytalk.makrut.db.command.Query;
import com.readytalk.makrut.db.command.Update;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

@ThreadSafe
public class MakrutDBFactory {
	private final MakrutExecutor executor;
	private final ConnectionSupplier supplier;
	private final QueryRunner runner;

	@Inject
	public MakrutDBFactory(final QueryRunner runner, final MakrutExecutor executor,
			final ConnectionSupplier supplier) {
		this.runner = runner;
		this.executor = executor;
		this.supplier = supplier;

	}

	public Update update(final String sql, final Object... params) {
		return new Update(executor, runner, Optional.<String>absent(), supplier, sql, params);
	}

	public <K> Insert<K> insert(final ResultSetHandler<K> handler, final String sql, final Object... params) {
		return new Insert<K>(executor, runner, Optional.<String>absent(), supplier, handler, sql, params);
	}

	public <K> Query<K> query(final ResultSetHandler<K> handler, final String sql, final Object... params) {
		return new Query<K>(executor, runner, Optional.<String>absent(), supplier, handler, sql, params);
	}
}
