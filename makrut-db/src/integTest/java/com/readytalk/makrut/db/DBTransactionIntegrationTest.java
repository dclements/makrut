package com.readytalk.makrut.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.readytalk.makrut.MakrutExecutor;
import com.readytalk.makrut.MakrutExecutorBuilder;
import com.readytalk.makrut.db.command.Insert;
import com.readytalk.makrut.db.command.Query;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DBTransactionIntegrationTest extends BasicTestDataFramework {

	private final ListeningScheduledExecutorService executorService = MoreExecutors.listeningDecorator(
			new ScheduledThreadPoolExecutor(2));

	private final ScalarHandler<Number> handler = new ScalarHandler<Number>();

	private MakrutExecutor executor;

	@Before
	public void setUp() throws Exception {
		super.setUp();

		executor = injector.getInstance(MakrutExecutorBuilder.class).withExecutorService(executorService).build();
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}


	@Test
	public void transaction_InsertThenCommit_ShowsResults() throws Exception {
		Transaction tx = supplier().beginTransaction();

		try {

			Insert<Number> command = new Insert<Number>(executor, runner, Optional.<String>absent(), tx, handler,
					"insert into test (id, val) VALUES (4, 'test4')");

			Number key = command.submitAndGet();

			command = new Insert<Number>(executor, runner, Optional.<String>absent(), tx, handler,
					"insert into test (id, val) VALUES (?, 'test5')", key.intValue() + 1);


			command.submitAndGet();

			tx.commit();

			Query<Number> query = new Query<Number>(executor, runner, Optional.<String>absent(), supplier(), handler,
					"select count(*) from test");


			assertEquals(5, query.submitAndGet().intValue());

		} finally {

			tx.close();
		}
	}

	@Test
	public void connection_FromTransaction_DoesNotAutoCommit() throws Exception {
		Transaction tx = supplier().beginTransaction();

		try {
			assertFalse(tx.getConnection().getAutoCommit());
		} finally {
			tx.close();
		}
	}

	@Test
	public void transaction_InsertThenRollback_DoesNotModifyResults() throws Exception {
		Transaction tx = supplier().beginTransaction();

		try {

			Insert<Number> command = new Insert<Number>(executor, runner, Optional.<String>absent(), tx, handler,
					"insert into test (id, val) VALUES (4, 'test4')");

			Number key = command.submitAndGet();

			command = new Insert<Number>(executor, runner, Optional.<String>absent(), tx, handler,
					"insert into test (id, val) VALUES (?, 'test5')", key.intValue() + 1);


			command.submitAndGet();

			tx.rollback();

			Query<Number> query = new Query<Number>(executor, runner, Optional.<String>absent(), supplier(), handler,
					"select count(*) from test");


			assertEquals(3, query.submitAndGet().intValue());

		} finally {

			tx.close();
		}
	}


}
