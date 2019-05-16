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
package org.springframework.statemachine.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Spring Boot {@link ConfigurationProperties} for <em>spring.statemachine</em>.
 *
 * @author Janne Valkealahti
 *
 */
@ConfigurationProperties(value = "spring.statemachine")
public class StateMachineProperties {

	private StateMachineMonitoringProperties monitor;

	public StateMachineMonitoringProperties getMonitor() {
		return monitor;
	}

	public void setMonitor(StateMachineMonitoringProperties monitor) {
		this.monitor = monitor;
	}

	/**
	 * Config props for "spring.statemachine.monitor".
	 */
	public static class StateMachineMonitoringProperties {
		private boolean enabled = false;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	}
}
