package com.readytalk.makrut.util;

import java.util.concurrent.Callable;

/**
 * A factory for creating the toolchain for augmenting callables.
 */
public interface CallableUtilsFactory {
	CallableUtils create(Callable<?> input);
}
