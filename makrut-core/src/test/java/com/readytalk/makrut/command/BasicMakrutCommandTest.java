package com.readytalk.makrut.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.Futures;
import com.readytalk.makrut.MakrutExecutor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@SuppressWarnings("unchecked")
public class BasicMakrutCommandTest {

	private final Object obj = new Object();

	private final Object obj2 = new Object();

	@Mock
	private MakrutExecutor executor;

	@Mock
	private Callable<Object> callable;

	@Mock
	private Callable<Object> fallback;

	private BasicMakrutCommand<Object> command;

	BasicMakrutCommand<Object> commandWithFallback;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		command = new BasicMakrutCommand<Object>(executor, "") {
			@Override
			public Object doCall() throws Exception {
				return callable.call();
			}
		};

		commandWithFallback = new BasicMakrutCommand<Object>(executor, "") {
			@Override
			public Object doCall() throws Exception {
				return callable.call();
			}

			@Override
			public Optional<Callable<Object>> doFallback() {
				return Optional.of(fallback);
			}
		};

	}

	@After
	public void tearDown() {

	}

	@Test
	public void submit_WhenCalled_SendsToExecutor() throws Exception {
		when(executor.submit(any(Callable.class))).thenReturn(Futures.immediateFuture(obj));

		assertEquals(obj, command.submit().get());
	}

	@Test
	public void submit_OnExceptionWithNoFallback_SendsException() throws Exception {
		Exception expected = new SQLException();
		when(executor.submit(any(Callable.class))).thenReturn(Futures.immediateFailedFuture(expected));

		try {
			command.submitAndGet();
			fail("Expected exception.");
		} catch (ExecutionException ex) {
			assertEquals(expected, ex.getCause());
		}
	}

	@Test
	public void submit_OnExceptionWithFallback_ProccessesFallback() throws Exception {
		when(fallback.call()).thenReturn(obj2);
		when(executor.submit(any(Callable.class))).thenReturn(Futures.immediateFailedFuture(new Exception()));

		assertEquals(obj2, commandWithFallback.submitAndGet());
	}

	@Test
	public void submit_OnErrorWithFallback_ThrowsError() throws Exception {
		when(executor.submit(any(Callable.class))).thenReturn(Futures.immediateFailedFuture(new Error()));

		try {
			commandWithFallback.submitAndGet();
			fail("Expected error.");
		} catch (Error er) {
			//Expected.
		}
	}

	@Test
	public void submit_OnErrorWithFallbackThatThrowsException_ThrowsOriginalException() throws Exception {
		Exception expected = new SQLException();
		when(executor.submit(any(Callable.class))).thenReturn(Futures.immediateFailedFuture(expected));
		when(fallback.call()).thenThrow(new Exception());

		try {
			commandWithFallback.submitAndGet();
			fail("Expected error.");
		} catch (ExecutionException ex) {
			assertEquals(expected, ex.getCause());
		}
	}

	@Test
	public void submit_OnCancellationWithFallback_ReturnsCancelledFuture() throws Exception {
		when(executor.submit(any(Callable.class))).thenReturn(Futures.immediateCancelledFuture());

		assertTrue(commandWithFallback.submit().isCancelled());
	}

	@Test
	public void submitAndGet_WhenCalled_SendsToExecutor() throws Exception {
		when(executor.submit(any(Callable.class))).thenReturn(Futures.immediateFuture(obj));

		assertEquals(obj, command.submitAndGet());
	}

	@Test
	public void call_WhenCalled_Delegates() throws Exception {
		when(callable.call()).thenReturn(obj);

		assertEquals(obj, command.call());
	}
}
