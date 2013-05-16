package com.readytalk.makrut.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Ticker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MakrutCommandWrapperTest {

	@Mock
	private Callable<String> callable;

	@Mock
	private Ticker ticker;

	private MakrutCommandWrapper<String> command;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		command = new MakrutCommandWrapper<String>(callable, ticker);

	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void call_WhenCalled_Delegates() throws Exception {
		command.call();

		verify(callable).call();

	}

	@Test
	public void timeElapsed_WhenNotStarted_ReturnsZero() {

		assertEquals(0, command.timeElapsed(TimeUnit.NANOSECONDS));
	}

	@Test
	public void timeElapsed_AfterStarting_CalculatesTimeSinceCall() throws Exception {

		when(ticker.read()).thenReturn(500L);

		command.call();

		when(ticker.read()).thenReturn(1000L);

		assertEquals(500L, command.timeElapsed(TimeUnit.NANOSECONDS));
	}

	@Test
	public void callCount_WhenNotRun_ReturnsZero() {
		assertEquals(0, command.callCount());
	}

	@Test
	public void callCount_OnCall_IncrementsByOne() throws Exception {
		command.call();

		assertEquals(1, command.callCount());
	}

	@Test
	public void callCount_OnRepeatedCalls_Increments() throws Exception {
		command.call();
		command.call();

		assertEquals(2, command.callCount());
	}


}
