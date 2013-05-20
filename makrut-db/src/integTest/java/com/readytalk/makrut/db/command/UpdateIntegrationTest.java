package com.readytalk.makrut.db.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

import com.readytalk.makrut.db.BasicTestDataFramework;
import com.readytalk.makrut.db.MakrutDBFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;


public class UpdateIntegrationTest extends BasicTestDataFramework {

	private MakrutDBFactory makrutFactory;


	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		super.setUp();

		makrutFactory = injector.getInstance(MakrutDBFactory.class);
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void update_OnNoParams_ReturnsCountOfUpdatedRows() throws Exception {
		Update update = makrutFactory.update("update test set val = 'test1' where id = 1");

		assertEquals(1, update.submitAndGet().intValue());
	}

	@Test
	public void update_OnUpdateOverRangeWithParams_ReturnsCountOfUpdatedRows() throws Exception {
		Update update = makrutFactory.update("update test set val = 'test1' where id > ?", 1);

		assertEquals(2, update.submitAndGet().intValue());
	}

	@Test
	public void update_OnUpdateWithNoRows_ReturnsZero() throws Exception {
		Update update = makrutFactory.update("update test set val = 'test1' where id < ?", 0);

		assertEquals(0, update.submitAndGet().intValue());
	}

	@Test
	public void update_WhenUpdateIsInvalid_ThrowsWrappedSQLException() throws Exception {
		Update update = makrutFactory.update("update test set val = null where id = 1");

		try {
			update.submitAndGet();
		} catch (ExecutionException ex) {
			assertTrue("Not an instanceof SQLException.", ex.getCause() instanceof SQLException);
		}
	}
}
