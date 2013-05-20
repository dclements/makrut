package com.readytalk.makrut.db;

import javax.sql.DataSource;
import java.sql.Connection;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.dbunit.DataSourceDatabaseTester;
import org.dbunit.IDatabaseTester;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.junit.Rule;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HSQLDBFramework {

	private static final Logger LOG = LoggerFactory.getLogger(HSQLDBFramework.class);

	private PoolingDataSource dataSource;
	private ConnectionSupplier supplier;
	private IDatabaseTester dbTester;
	@Rule
	public final ExternalResource db = new ExternalResource() {

		private final GenericObjectPool<Connection> pool = new GenericObjectPool<Connection>(null);

		@Override
		protected void before() throws Throwable {
			super.before();

			DriverManagerConnectionFactory factory = new DriverManagerConnectionFactory("jdbc:hsqldb:mem:testdb", "sa",
					"");

			// This sets the pool's factory as part of the constructor.
			@SuppressWarnings("unused") @SuppressFBWarnings("DLS_DEAD_LOCAL_STORE") PoolableConnectionFactory
					poolableConnectionFactory = new PoolableConnectionFactory(factory, pool, null, "select 1", false,
					true);

			dataSource = new PoolingDataSource(pool);

			supplier = new DataSourceConnectionSupplier(dataSource);

		}

		@Override
		protected void after() {
			super.after();

			try {
				pool.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	protected HSQLDBFramework() {
	}

	protected void setUp() throws Exception {
		dbTester = new DataSourceDatabaseTester(dataSource);

		buildTables();

		dbTester.setDataSet(dataSet());

		dbTester.onSetup();
	}

	protected void tearDown() throws Exception {

		try {
			dropTables();
		} catch (Exception ex) {
			LOG.warn("Exception when dropping tables.", ex);
		}

		dbTester.onTearDown();

	}

	protected DataSource dataSource() {
		return dataSource;
	}

	protected ConnectionSupplier supplier() {
		return supplier;
	}

	private void buildTables() throws Exception {
		Connection conn = null;

		try {
			conn = dbTester.getConnection().getConnection();
			buildTablesImpl(conn);
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

	private void dropTables() throws Exception {
		Connection conn = null;

		try {
			conn = dbTester.getConnection().getConnection();
			dropTablesImpl(conn);
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

	protected abstract IDataSet dataSet() throws DataSetException;

	protected abstract void buildTablesImpl(Connection conn) throws Exception;

	protected abstract void dropTablesImpl(Connection conn) throws Exception;
}
