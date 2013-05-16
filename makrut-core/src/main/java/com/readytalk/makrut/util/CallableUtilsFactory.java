package com.readytalk.makrut.util;

/**
 * A factory for creating the toolchain for augmenting callables.
 */
public interface CallableUtilsFactory {
	CallableUtils create(String name);
}
