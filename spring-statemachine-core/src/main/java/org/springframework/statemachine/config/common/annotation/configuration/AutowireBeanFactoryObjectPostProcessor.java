/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.config.common.annotation.configuration;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.Lifecycle;
import org.springframework.context.SmartLifecycle;
import org.springframework.statemachine.config.common.annotation.ObjectPostProcessor;
import org.springframework.util.Assert;

/**
 * Post processor handling lifecycle methods of
 * POJOs created from builder/configurer.
 *
 * @author Janne Valkealahti
 *
 */
final class AutowireBeanFactoryObjectPostProcessor implements ObjectPostProcessor<Object>, DisposableBean, SmartLifecycle {

	private final static Log log = LogFactory.getLog(AutowireBeanFactoryObjectPostProcessor.class);

	private final AutowireCapableBeanFactory autowireBeanFactory;
	private final List<DisposableBean> disposableBeans = new ArrayList<DisposableBean>();
	private final List<Lifecycle> lifecycleBeans = new ArrayList<Lifecycle>();

	private boolean running;

	/**
	 * Instantiates a new autowire bean factory object post processor.
	 *
	 * @param autowireBeanFactory the autowire bean factory
	 */
	public AutowireBeanFactoryObjectPostProcessor(AutowireCapableBeanFactory autowireBeanFactory) {
		Assert.notNull(autowireBeanFactory, "autowireBeanFactory cannot be null");
		this.autowireBeanFactory = autowireBeanFactory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T postProcess(T object) {
		T result = (T) autowireBeanFactory.initializeBean(object, null);
		if(result instanceof DisposableBean) {
			disposableBeans.add((DisposableBean) result);
		}
		if(result instanceof Lifecycle) {
			lifecycleBeans.add((Lifecycle) result);
		}
		return result;
	}

	@Override
	public void destroy() throws Exception {
		for(DisposableBean disposable : disposableBeans) {
			try {
				disposable.destroy();
			} catch(Exception error) {
				log.error(error);
			}
		}
	}

	@Override
	public void start() {
		running = true;
		for (Lifecycle bean : lifecycleBeans) {
			bean.start();
		}
	}

	@Override
	public void stop() {
		for (Lifecycle bean : lifecycleBeans) {
			bean.stop();
		}
		running = false;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public int getPhase() {
		return 0;
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public void stop(Runnable callback) {
		stop();
		callback.run();
	}

}
