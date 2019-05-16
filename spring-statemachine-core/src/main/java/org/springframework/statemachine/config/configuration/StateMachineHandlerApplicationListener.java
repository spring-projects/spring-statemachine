/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.statemachine.config.configuration;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Spring {@link ApplicationListener} which hooks to {@code ContextRefreshedEvent}
 * and tracks when was a last time context was refreshed.
 *
 * @author Janne Valkealahti
 *
 */
public class StateMachineHandlerApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

	public final static String BEAN_NAME = "stateMachineHandlerApplicationListener";
	private Long lastRefreshTime = null;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		lastRefreshTime = System.currentTimeMillis();
	}

	/**
	 * Gets the last refresh time.
	 *
	 * @return the last refresh time or {@code NULL} if not yet refreshed.
	 */
	public Long getLastRefreshTime() {
		return lastRefreshTime;
	}
}
