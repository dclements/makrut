package com.readytalk.makrut;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

@SuppressWarnings("ObjectEqualsNull")
public class MakrutCommandTest {

	private static final String NAME1 = "name";
	private static final String NAME2 = "different_name";

	private final Object obj = new Object();

	@Test
	public void equals_Equivalent_ReturnsTrue() {
		MakrutCommand<Object> command = new MakrutCommand<Object>(NAME1, 1, 2, 3) {
			@Override
			public Object call() throws Exception {
				return obj;
			}
		};

		assertTrue(command.equals(command));
	}

	@Test
	public void equals_Null_ReturnsFalse() {
		MakrutCommand<Object> command = new MakrutCommand<Object>(NAME1, 1, 2, 3) {
			@Override
			public Object call() throws Exception {
				return obj;
			}
		};

		assertFalse(command.equals(null));
	}

	@Test
	public void equals_ArbitraryObject_ReturnsFalse() {
		MakrutCommand<Object> command = new MakrutCommand<Object>(NAME1) {
			@Override
			public Object call() throws Exception {
				return obj;
			}
		};

		assertFalse(command.equals(obj));
	}

	@Test
	public void equals_SameParametersAndName_ReturnsTrue() {
		MakrutCommand<Object> command1 = new MakrutCommand<Object>(NAME1, 1, "", obj) {
			@Override
			public Object call() throws Exception {
				return obj;
			}
		};

		MakrutCommand<Object> command2 = new MakrutCommand<Object>(NAME1, 1, "", obj) {
			@Override
			public Object call() throws Exception {
				return obj;
			}
		};

		assertTrue(command1.equals(command2));
	}

	@Test
	public void equals_SameParametersAndDifferentName_ReturnsFalse() {
		MakrutCommand<Object> command1 = new MakrutCommand<Object>(NAME1, 1, "", obj) {
			@Override
			public Object call() throws Exception {
				return obj;
			}
		};

		MakrutCommand<Object> command2 = new MakrutCommand<Object>(NAME2, 1, "", obj) {
			@Override
			public Object call() throws Exception {
				return obj;
			}
		};

		assertFalse(command1.equals(command2));
	}

	@Test
	public void equals_DifferentParametersAndSameName_ReturnsFalse() {
		MakrutCommand<Object> command1 = new MakrutCommand<Object>(NAME1, 1, "", obj) {
			@Override
			public Object call() throws Exception {
				return obj;
			}
		};

		MakrutCommand<Object> command2 = new MakrutCommand<Object>(NAME1, 2, "", obj) {
			@Override
			public Object call() throws Exception {
				return obj;
			}
		};

		assertFalse(command1.equals(command2));
	}

	@Test
	public void hashCode_MultipleCalls_SameValue() {
		MakrutCommand<Object> command = new MakrutCommand<Object>(NAME1, 1, 2, 3) {
			@Override
			public Object call() throws Exception {
				return obj;
			}
		};

		assertEquals(command.hashCode(), command.hashCode());
	}

	@Test
	public void hashCode_SameParametersAndName_SameValues() {
		MakrutCommand<Object> command1 = new MakrutCommand<Object>(NAME1, 1, "", obj) {
			@Override
			public Object call() throws Exception {
				return obj;
			}
		};

		MakrutCommand<Object> command2 = new MakrutCommand<Object>(NAME1, 1, "", obj) {
			@Override
			public Object call() throws Exception {
				return obj;
			}
		};

		assertEquals(command1.hashCode(), command2.hashCode());
	}

	@Test
	public void hashCode_DifferentNames_DifferentValues() {
		MakrutCommand<Object> command1 = new MakrutCommand<Object>(NAME1, 1, "", obj) {
			@Override
			public Object call() throws Exception {
				return obj;
			}
		};

		MakrutCommand<Object> command2 = new MakrutCommand<Object>(NAME2, 1, "", obj) {
			@Override
			public Object call() throws Exception {
				return obj;
			}
		};

		assertNotEquals(command1.hashCode(), command2.hashCode());
	}

	@Test
	public void hashCode_DifferentArgs_DifferentValues() {
		MakrutCommand<Object> command1 = new MakrutCommand<Object>(NAME1, 2, "", obj) {
			@Override
			public Object call() throws Exception {
				return obj;
			}
		};

		MakrutCommand<Object> command2 = new MakrutCommand<Object>(NAME1, 1, "", obj) {
			@Override
			public Object call() throws Exception {
				return obj;
			}
		};

		assertNotEquals(command1.hashCode(), command2.hashCode());
	}
}
