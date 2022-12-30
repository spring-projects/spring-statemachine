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
package demo.eventservice;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.Scanner;

import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.target.CommonsPool2TargetSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.data.redis.RedisStateMachineContextRepository;
import org.springframework.statemachine.data.redis.RedisStateMachinePersister;
import org.springframework.statemachine.persist.RepositoryStateMachinePersist;

@Configuration
public class StateMachineConfig {

//tag::snippetA[]
	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
	public ProxyFactoryBean stateMachine() {
		ProxyFactoryBean pfb = new ProxyFactoryBean();
		pfb.setTargetSource(poolTargetSource());
		return pfb;
	}
//end::snippetA[]

//tag::snippetB[]
	@Bean
	public CommonsPool2TargetSource poolTargetSource() {
		CommonsPool2TargetSource pool = new CommonsPool2TargetSource();
		pool.setMaxSize(3);
		pool.setTargetBeanName("stateMachineTarget");
		return pool;
	}
//end::snippetB[]

//tag::snippetC[]
	@Bean(name = "stateMachineTarget")
	@Scope(scopeName="prototype")
	public StateMachine<States, Events> stateMachineTarget() throws Exception {
		Builder<States, Events> builder = StateMachineBuilder.<States, Events>builder();

		builder.configureConfiguration()
			.withConfiguration()
				.autoStartup(true);

		builder.configureStates()
			.withStates()
				.initial(States.HOME)
				.states(EnumSet.allOf(States.class));

		builder.configureTransitions()
			.withInternal()
				.source(States.ITEMS).event(Events.ADD)
				.action(addAction())
				.and()
			.withInternal()
				.source(States.CART).event(Events.DEL)
				.action(delAction())
				.and()
			.withInternal()
				.source(States.PAYMENT).event(Events.PAY)
				.action(payAction())
				.and()
			.withExternal()
				.source(States.HOME).target(States.ITEMS)
				.action(pageviewAction())
				.event(Events.VIEW_I)
				.and()
			.withExternal()
				.source(States.CART).target(States.ITEMS)
				.action(pageviewAction())
				.event(Events.VIEW_I)
				.and()
			.withExternal()
				.source(States.ITEMS).target(States.CART)
				.action(pageviewAction())
				.event(Events.VIEW_C)
				.and()
			.withExternal()
				.source(States.PAYMENT).target(States.CART)
				.action(pageviewAction())
				.event(Events.VIEW_C)
				.and()
			.withExternal()
				.source(States.CART).target(States.PAYMENT)
				.action(pageviewAction())
				.event(Events.VIEW_P)
				.and()
			.withExternal()
				.source(States.ITEMS).target(States.HOME)
				.action(resetAction())
				.event(Events.RESET)
				.and()
			.withExternal()
				.source(States.CART).target(States.HOME)
				.action(resetAction())
				.event(Events.RESET)
				.and()
			.withExternal()
				.source(States.PAYMENT).target(States.HOME)
				.action(resetAction())
				.event(Events.RESET);

		return builder.build();
	}
//end::snippetC[]

	@Bean
	public Action<States, Events> pageviewAction() {
		return new Action<States, Events>() {

			@Override
			public void execute(StateContext<States, Events> context) {
				String variable = context.getTarget().getId().toString();
				Integer count = context.getExtendedState().get(variable, Integer.class);
				if (count == null) {
					context.getExtendedState().getVariables().put(variable, 1);
				} else {
					context.getExtendedState().getVariables().put(variable, (count + 1));
				}
			}
		};
	}

	@Bean
	public Action<States, Events> addAction() {
		return new Action<States, Events>() {

			@Override
			public void execute(StateContext<States, Events> context) {
				Integer count = context.getExtendedState().get("COUNT", Integer.class);
				if (count == null) {
					context.getExtendedState().getVariables().put("COUNT", 1);
				} else {
					context.getExtendedState().getVariables().put("COUNT", (count + 1));
				}
			}
		};
	}

	@Bean
	public Action<States, Events> delAction() {
		return new Action<States, Events>() {

			@Override
			public void execute(StateContext<States, Events> context) {
				Integer count = context.getExtendedState().get("COUNT", Integer.class);
				if (count != null && count > 0) {
					context.getExtendedState().getVariables().put("COUNT", (count - 1));
				}
			}
		};
	}

	@Bean
	public Action<States, Events> payAction() {
		return new Action<States, Events>() {

			@Override
			public void execute(StateContext<States, Events> context) {
				context.getExtendedState().getVariables().put("PAYED", true);
			}
		};
	}

	@Bean
	public Action<States, Events> resetAction() {
		return new Action<States, Events>() {

			@Override
			public void execute(StateContext<States, Events> context) {
				context.getExtendedState().getVariables().clear();
			}
		};
	}

//tag::snippetD[]
	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		return new JedisConnectionFactory();
	}

	@Bean
	public StateMachinePersist<States, Events, String> stateMachinePersist(RedisConnectionFactory connectionFactory) {
		RedisStateMachineContextRepository<States, Events> repository =
				new RedisStateMachineContextRepository<States, Events>(connectionFactory);
		return new RepositoryStateMachinePersist<States, Events>(repository);
	}

	@Bean
	public RedisStateMachinePersister<States, Events> redisStateMachinePersister(
			StateMachinePersist<States, Events, String> stateMachinePersist) {
		return new RedisStateMachinePersister<States, Events>(stateMachinePersist);
	}
//end::snippetD[]

	@Bean
	public String stateChartModel() throws IOException {
		ClassPathResource model = new ClassPathResource("statechartmodel.txt");
		InputStream inputStream = model.getInputStream();
		Scanner scanner = new Scanner(inputStream);
		String content = scanner.useDelimiter("\\Z").next();
		scanner.close();
		return content;
	}

	public enum States {
		HOME,
		ITEMS,
		CART,
		PAYMENT
	}

	public enum Events {
		VIEW_I,
		VIEW_C,
		VIEW_P,
		RESET,
		ADD,
		DEL,
		PAY
	}
}
