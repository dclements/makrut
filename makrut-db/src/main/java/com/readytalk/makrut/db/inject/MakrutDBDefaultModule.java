package com.readytalk.makrut.db.inject;

import javax.sql.DataSource;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.readytalk.makrut.MakrutExecutor;
import com.readytalk.makrut.MakrutExecutorBuilder;
import com.readytalk.makrut.db.ConnectionSupplier;
import com.readytalk.makrut.db.DataSourceConnectionSupplier;
import com.readytalk.makrut.db.MakrutDBFactory;
import org.apache.commons.dbutils.QueryRunner;

public class MakrutDBDefaultModule extends PrivateModule {

	private final ListeningExecutorService executorService;
	private final DataSource dataSource;

	public MakrutDBDefaultModule(final ListeningExecutorService executorService, final DataSource dataSource) {
		this.executorService = executorService;
		this.dataSource = dataSource;

	}

	@Override
	protected void configure() {
		bind(QueryRunner.class).toInstance(new QueryRunner());
		bind(DataSource.class).toInstance(dataSource);
		bind(ConnectionSupplier.class).to(DataSourceConnectionSupplier.class);

		bind(MakrutDBFactory.class).in(Singleton.class);
		expose(MakrutDBFactory.class);
	}

	@Provides
	@Singleton
	public MakrutExecutor executor(final MakrutExecutorBuilder builder) {
		return builder.withExecutorService(executorService).build();
	}

}
