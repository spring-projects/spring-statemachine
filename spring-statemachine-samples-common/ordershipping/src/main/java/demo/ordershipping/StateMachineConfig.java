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
package demo.ordershipping;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.uml.UmlStateMachineModelFactory;
import org.springframework.util.StringUtils;

@Configuration
public class StateMachineConfig {

	@Bean
	public StateMachineLogListener stateMachineLogListener() {
		return new StateMachineLogListener();
	}

	@Configuration
	@EnableStateMachineFactory
	public static class Config extends StateMachineConfigurerAdapter<String, String> {

		@Autowired
		private StateMachineLogListener stateMachineLogListener;

		@Override
		public void configure(StateMachineConfigurationConfigurer<String, String> config)
				throws Exception {
			config
				.withConfiguration()
					.autoStartup(true)
					.listener(stateMachineLogListener);
		}

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model)
				throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new UmlStateMachineModelFactory("classpath:ordershipping.uml");
		}

		@Bean
		public Action<String, String> entryReceiveOrder() {
			return (context) -> {
				String customer = context.getMessageHeaders().get("customer", String.class);
				if (StringUtils.hasText(customer)) {
					context.getExtendedState().getVariables().put("customer", customer);
				}
				String order = context.getMessageHeaders().get("order", String.class);
				if (StringUtils.hasText(order)) {
					context.getExtendedState().getVariables().put("order", order);
				}
			};
		}

		@Bean
		public Action<String, String> entrySendReminder() {
			return (context) -> {
				System.out.println("REMIND");
			};
		}

		@Bean
		public Action<String, String> entryHandleOrder() {
			return (context) -> {
				if (context.getMessageHeaders().containsKey("makeProdPlan")) {
					context.getExtendedState().getVariables().put("makeProdPlan", true);
				}
				if (context.getMessageHeaders().containsKey("produce")) {
					context.getExtendedState().getVariables().put("produce", true);
				}
			};
		}

		@Bean
		public Guard<String, String> orderOk() {
			return (context) -> {
				Map<Object, Object> variables = context.getExtendedState().getVariables();
				return variables.containsKey("customer") && variables.containsKey("order");
			};
		}

		@Bean
		public Guard<String, String> paymentOk() {
			return (context) -> {
				return context.getMessageHeaders().containsKey("payment");
			};
		}

		@Bean
		public Guard<String, String> makeProdPlan() {
			return (context) -> {
				return context.getExtendedState().getVariables().containsKey("makeProdPlan");
			};
		}

		@Bean
		public Guard<String, String> produce() {
			return (context) -> {
				return context.getExtendedState().getVariables().containsKey("produce");
			};
		}
	}
}
