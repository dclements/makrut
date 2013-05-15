package com.readytalk.makrut.util;

import java.util.concurrent.Callable;

public interface FutureUtilsFactory {
	FutureUtils create(Callable<?> input);
}
