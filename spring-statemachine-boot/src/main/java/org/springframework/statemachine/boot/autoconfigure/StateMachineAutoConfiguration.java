/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.boot.autoconfigure;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.trace.TraceRepository;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.boot.StateMachineProperties;
import org.springframework.statemachine.boot.support.BootStateMachineMonitor;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Statemachine.
 *
 * @author Janne Valkealahti
 *
 */
@Configuration
@EnableConfigurationProperties({ StateMachineProperties.class })
public class StateMachineAutoConfiguration {

	@Configuration
	@ConditionalOnClass(MeterRegistry.class)
	@ConditionalOnProperty(prefix = "spring.statemachine.monitor", name = "enabled", havingValue = "true", matchIfMissing = true)
	public static class StateMachineMonitoringConfiguration {

		private final MeterRegistry meterRegistry;
		private final TraceRepository traceRepository;

		public StateMachineMonitoringConfiguration(ObjectProvider<MeterRegistry> meterRegistryProvider,
				ObjectProvider<TraceRepository> traceRepositoryProvider) {
			this.meterRegistry = meterRegistryProvider.getIfAvailable();
			this.traceRepository = traceRepositoryProvider.getIfAvailable();
		}

		@Bean
		public BootStateMachineMonitor<?, ?> bootStateMachineMonitor() {
			return new BootStateMachineMonitor<>(meterRegistry, traceRepository);
		}
	}
}
