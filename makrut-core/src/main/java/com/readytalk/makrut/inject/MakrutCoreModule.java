package com.readytalk.makrut.inject;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.PrivateModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.readytalk.makrut.MakrutExecutorBuilder;
import com.readytalk.makrut.util.CallableUtils;
import com.readytalk.makrut.util.CallableUtilsFactory;
import com.readytalk.makrut.util.FutureUtils;
import com.readytalk.makrut.util.FutureUtilsFactory;

public class MakrutCoreModule extends PrivateModule {
	private final MetricRegistry metrics;

	public MakrutCoreModule(final MetricRegistry metrics) {
		this.metrics = metrics;
	}

	public MakrutCoreModule() {
		this.metrics = new MetricRegistry();
	}

	@Override
	protected void configure() {
		install(new FactoryModuleBuilder().implement(CallableUtils.class, CallableUtils.class)
				.build(CallableUtilsFactory.class));

		install(new FactoryModuleBuilder().implement(FutureUtils.class, FutureUtils.class)
				.build(FutureUtilsFactory.class));

		bind(MetricRegistry.class).toInstance(metrics);

		bind(MakrutExecutorBuilder.class);
		expose(MakrutExecutorBuilder.class);


	}
}
