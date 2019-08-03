/*
 * Copyright 2015-2019 the original author or authors.
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
package org.springframework.statemachine.support;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.Assert;

import reactor.core.publisher.Mono;

/**
 * Convenient base class for object which needs spring task scheduler, task
 * executor and life cycle handling.
 *
 * @author Janne Valkealahti
 *
 */
public abstract class LifecycleObjectSupport
		/*extends ReactiveLifecycleManager*/
		implements InitializingBean, DisposableBean, SmartLifecycle, BeanFactoryAware, StateMachineReactiveLifecycle {

	private static final Log log = LogFactory.getLog(LifecycleObjectSupport.class);

	// fields for lifecycle
	private volatile boolean autoStartup = false;
	private volatile int phase = 0;

	// to access bean factory
	private volatile BeanFactory beanFactory;

	// protect InitializingBean for single call
	private final AtomicBoolean afterPropertiesSetCalled = new AtomicBoolean(false);

	private final ReactiveLifecycleManager reactiveLifecycleManager;

	public LifecycleObjectSupport() {
		this.reactiveLifecycleManager = new ReactiveLifecycleManager(
				() -> doPreStartReactively(),
				() -> doPreStopReactively(),
				() -> doPostStartReactively(),
				() -> doPostStopReactively()
				);
		this.reactiveLifecycleManager.setOwner(this);
	}

	@Override
	public final void afterPropertiesSet() {
		try {
			if (afterPropertiesSetCalled.compareAndSet(false, true)) {
				this.onInit();
			} else {
				log.debug("afterPropertiesSet() is already called, not calling onInit()");
			}
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}
			throw new BeanInitializationException("failed to initialize", e);
		}
	}

	@Override
	public final void destroy() throws Exception {
		log.info("destroy called");
		doDestroy();
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		Assert.notNull(beanFactory, "beanFactory must not be null");
		if(log.isTraceEnabled()) {
			log.trace("Setting bean factory: " + beanFactory + " for " + this);
		}
		this.beanFactory = beanFactory;
	}

	@Override
	public final boolean isAutoStartup() {
		return this.autoStartup;
	}

	@Override
	public final int getPhase() {
		return this.phase;
	}

	@Override
	public void start() {
		startReactively().block();
	}

	@Override
	public void stop() {
		stopReactively().block();
	}

	@Override
	public Mono<Void> startReactively() {
		log.debug("startReactively " + this + " with rlm " + this.reactiveLifecycleManager);
		return this.reactiveLifecycleManager.startReactively();
	}

	@Override
	public Mono<Void> stopReactively() {
		log.debug("stopReactively " + this + " with rlm " + this.reactiveLifecycleManager);
		return this.reactiveLifecycleManager.stopReactively();
	}

	@Override
	public boolean isRunning() {
		return this.reactiveLifecycleManager.isRunning();
	}

	/**
	 * Sets the auto startup.
	 *
	 * @param autoStartup the new auto startup
	 * @see SmartLifecycle
	 */
	public void setAutoStartup(boolean autoStartup) {
		this.autoStartup = autoStartup;
	}

	/**
	 * Sets the phase.
	 *
	 * @param phase the new phase
	 * @see SmartLifecycle
	 */
	public void setPhase(int phase) {
		this.phase = phase;
	}

	/**
	 * Gets the {@link BeanFactory} for this instance.
	 *
	 * @return the bean factory.
	 */
	protected final BeanFactory getBeanFactory() {
		return beanFactory;
	}

	/**
	 * Subclasses may implement this for initialization logic. Called during the
	 * {@link InitializingBean} phase.
	 *
	 * @throws Exception exception
	 */
	protected void onInit() throws Exception {}

	/**
	 * Subclasses may implement this for destroy logic.
	 */
	protected void doDestroy() {};

	/**
	 * Subclasses may implement this for pre start logic.
	 *
	 * @return the mono for completion
	 */
	protected Mono<Void> doPreStartReactively() {
		return Mono.empty();
	}

	/**
	 * Subclasses may implement this for pre stop logic.
	 *
	 * @return the mono for completion
	 */
	protected Mono<Void> doPreStopReactively() {
		return Mono.empty();
	}

	/**
	 * Subclasses may implement this for post start logic.
	 *
	 * @return the mono for completion
	 */
	protected Mono<Void> doPostStartReactively() {
		return Mono.empty();
	}

	/**
	 * Subclasses may implement this for post stop logic.
	 *
	 * @return the mono for completion
	 */
	protected Mono<Void> doPostStopReactively() {
		return Mono.empty();
	}
}
