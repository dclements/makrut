package com.readytalk.makrut.db;

import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.readytalk.makrut.db.inject.MakrutDBDefaultModule;
import com.readytalk.makrut.inject.MakrutCoreModule;
import org.apache.commons.dbutils.QueryRunner;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;

public abstract class BasicTestDataFramework extends HSQLDBFramework {

	private final ListeningScheduledExecutorService executorService = MoreExecutors.listeningDecorator(
			new ScheduledThreadPoolExecutor(2));

	protected Injector injector = Guice.createInjector(new MakrutCoreModule());
	protected final QueryRunner runner = new QueryRunner();


	protected final List<Map<String, Object>> dataSet = ImmutableList.<Map<String, Object>>builder()
			.add(ImmutableMap.<String, Object>of("ID", 1, "VAL", "test"))
			.add(ImmutableMap.<String, Object>of("ID", 2, "VAL", "test2"))
			.add(ImmutableMap.<String, Object>of("ID", 3, "VAL", "test3"))
			.build();

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		injector = Guice.createInjector(new MakrutCoreModule(),
				new MakrutDBDefaultModule(executorService, dataSource()));
	}

	@Override
	protected IDataSet dataSet() throws DataSetException {

		return new FlatXmlDataSetBuilder().build(Resources.getResource("basic_test/basic_test.xml"));
	}

	@Override
	protected void buildTablesImpl(final Connection conn) throws Exception {
		InputSupplier<InputStreamReader> iss = Resources.newReaderSupplier(
				Resources.getResource("basic_test/basic_test.hsql.sql"), Charset.forName("utf-8"));

		runner.update(conn, CharStreams.toString(iss));
	}

	@Override
	protected void dropTablesImpl(final Connection conn) throws Exception {
		runner.update(conn, "drop table if exists test");

	}
}
