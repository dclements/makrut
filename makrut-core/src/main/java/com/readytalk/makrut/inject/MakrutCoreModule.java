package com.readytalk.makrut.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.readytalk.makrut.MakrutExecutorBuilder;
import com.readytalk.makrut.util.CallableUtils;
import com.readytalk.makrut.util.FutureUtils;

public class MakrutCoreModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(CallableUtils.class).in(Singleton.class);
		bind(FutureUtils.class).in(Singleton.class);
		bind(MakrutExecutorBuilder.class);
	}
}
