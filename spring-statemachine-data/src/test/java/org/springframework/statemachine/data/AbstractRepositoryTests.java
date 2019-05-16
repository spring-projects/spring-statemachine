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
package org.springframework.statemachine.data;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.data.support.StateMachineJackson2RepositoryPopulatorFactoryBean;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.util.ObjectUtils;

/**
 * Base repository config tests.
 *
 * @author Janne Valkealahti
 */
public abstract class AbstractRepositoryTests {

	protected AnnotationConfigApplicationContext context;

	@Before
	public void setup() {
		cleanInternal();
		context = buildContext();
	}

	@After
	public void clean() {
		if (context != null) {
			context.close();
		}
		context = null;
	}

	protected void cleanInternal() {
	}

	protected Class<?>[] getRegisteredClasses() {
		return new Class<?>[0];
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine2() throws Exception {
		context.register(getRegisteredClasses());
		context.register(Config2.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S1").and()
					.step().sendEvent("E1").expectStates("S2").and()
					.step().sendEvent("E2").expectStates("S3").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine3() throws Exception {
		context.register(getRegisteredClasses());
		context.register(Config3.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S1").and()
					.step().sendEvent("E1").expectStates("S2", "S20").and()
					.step().sendEvent("E2").expectStates("S2", "S21").and()
					.step().sendEvent("E3").expectStates("S1").and()
					.step().sendEvent("E4").expectStates("S2", "S21").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine4() throws Exception {
		context.register(getRegisteredClasses());
		context.register(Config4.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S1").and()
					.step().sendEvent("E1").expectStates("S2", "S20").and()
					.step().sendEvent("E2").expectStates("S2", "S21").and()
					.step().sendEvent("E3").expectStates("S1").and()
					.step().sendEvent("E4").expectStates("S2", "S21").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine5() throws Exception {
		context.register(getRegisteredClasses());
		context.register(Config5.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S10", "S20").and()
					.step().sendEvent("E1").expectStates("S11", "S21").and()
					.step().sendEvent("E2").expectStates("S10", "S21").and()
					.step().sendEvent("E3").expectStates("S10", "S20").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine6First() throws Exception {
		context.register(getRegisteredClasses());
		context.register(Config6.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("SI").and()
					.step().sendEvent(MessageBuilder.withPayload("E1").setHeader("choice", "s30").build()).expectStates("S30").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine6Then1() throws Exception {
		context.register(getRegisteredClasses());
		context.register(Config6.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("SI").and()
					.step().sendEvent(MessageBuilder.withPayload("E1").setHeader("choice", "s31").build()).expectStates("S31").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine6Then2() throws Exception {
		context.register(getRegisteredClasses());
		context.register(Config6.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("SI").and()
					.step().sendEvent(MessageBuilder.withPayload("E1").setHeader("choice", "s32").build()).expectStates("S32").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine6Last() throws Exception {
		context.register(getRegisteredClasses());
		context.register(Config6.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("SI").and()
					.step().sendEvent(MessageBuilder.withPayload("E1").build()).expectStates("S33").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine8First() throws Exception {
		context.register(getRegisteredClasses());
		context.register(Config8.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("SI").and()
					.step().sendEvent(MessageBuilder.withPayload("E1").setHeader("junction", "s30").build()).expectStates("S30").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine8Then1() throws Exception {
		context.register(getRegisteredClasses());
		context.register(Config8.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("SI").and()
					.step().sendEvent(MessageBuilder.withPayload("E1").setHeader("junction", "s31").build()).expectStates("S31").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine8Then2() throws Exception {
		context.register(getRegisteredClasses());
		context.register(Config8.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("SI").and()
					.step().sendEvent(MessageBuilder.withPayload("E1").setHeader("junction", "s32").build()).expectStates("S32").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine8Last() throws Exception {
		context.register(getRegisteredClasses());
		context.register(Config8.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("SI").and()
					.step().sendEvent(MessageBuilder.withPayload("E1").build()).expectStates("S33").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine9() throws Exception {
		context.register(getRegisteredClasses());
		context.register(Config9.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S1").and()
					.step().sendEvent("ENTRY1").expectStates("S2", "S22").and()
					.step().sendEvent("EXIT1").expectStates("S4").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine10() throws Exception {
		context.register(getRegisteredClasses());
		context.register(Config10.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("SI").and()
					.step().sendEvent("E1").expectStates("S2", "S21", "S31").and()
					.build();
		plan.test();

		State<String, String> endState = null;
		Iterator<State<String, String>> iterator = stateMachine.getStates().iterator();
		while (iterator.hasNext()) {
			State<String, String> next = iterator.next();
			if (next.getId().equals("SF")) {
				endState = next;
				break;
			}
		}
		assertThat(endState, notNullValue());
		assertThat(endState.getPseudoState(), notNullValue());
		assertThat(endState.getPseudoState().getKind(), is(PseudoStateKind.END));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine11() throws Exception {
		context.register(getRegisteredClasses());
		context.register(Config11.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("SI").and()
					.step().sendEvent("E1").expectStates("S2", "S20", "S30").and()
					.step().sendEvent("E2").expectStates("S2", "S21", "S30").and()
					.step().sendEvent("E3").expectStates("S4").and()
					.step().sendEvent("E4").expectStates("SI").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine12() throws Exception {
		context.register(getRegisteredClasses());
		context.register(Config12.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("READY").and()
					.step().sendEvent("E3").expectStates("S3").and()
					.step().sendEvent("E1").expectStates("S3").and()
					.step().sendEvent("E6").expectStates("S1").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine13() throws Exception {
		context.register(getRegisteredClasses());
		context.register(Config13.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S1").and()
					.step().sendEvent("E1").expectStates("S2", "S21").and()
					.step().sendEvent("E3").expectStates("S2", "S22").and()
					.step().sendEvent("E2").expectStates("S3").and()
					.build();
		plan.test();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMachine14() throws Exception {
		context.register(getRegisteredClasses());
		context.register(Config14.class, FactoryConfig.class);
		context.refresh();
		StateMachineFactory<String, String> stateMachineFactory = context.getBean(StateMachineFactory.class);
		StateMachine<String, String> stateMachine = stateMachineFactory.getStateMachine();

		StateMachineTestPlan<String, String> plan =
				StateMachineTestPlanBuilder.<String, String>builder()
					.stateMachine(stateMachine)
					.step().expectStates("S1").expectVariable("foo", 0).and()
					.step().sendEvent("E1").expectStates("S2").and()
					.build();
		plan.test();
	}

	@Configuration
	public static class Config2 {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data2.json")});
			return factoryBean;
		}
	}

	@Configuration
	public static class Config3 {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data3.json")});
			return factoryBean;
		}
	}

	@Configuration
	public static class Config4 {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data4.json")});
			return factoryBean;
		}
	}

	@Configuration
	public static class Config5 {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data5.json")});
			return factoryBean;
		}
	}

	@Configuration
	public static class Config6 {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data6.json")});
			return factoryBean;
		}

		@Bean
		public Guard<String, String> s30Guard() {
			return new ChoiceGuard("s30");
		}

		@Bean
		public Guard<String, String> s31Guard() {
			return new ChoiceGuard("s31");
		}

		@Bean
		public Guard<String, String> s32Guard() {
			return new ChoiceGuard("s32");
		}

	}

	@Configuration
	public static class Config7 {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data7.json")});
			return factoryBean;
		}
	}

	@Configuration
	public static class Config8 {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data8.json")});
			return factoryBean;
		}

		@Bean
		public Guard<String, String> s30Guard() {
			return new JunctionGuard("s30");
		}

		@Bean
		public Guard<String, String> s31Guard() {
			return new JunctionGuard("s31");
		}

		@Bean
		public Guard<String, String> s32Guard() {
			return new JunctionGuard("s32");
		}

	}

	@Configuration
	public static class Config9 {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data9.json")});
			return factoryBean;
		}
	}

	@Configuration
	public static class Config10 {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data10.json")});
			return factoryBean;
		}
	}

	@Configuration
	public static class Config11 {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data11.json")});
			return factoryBean;
		}
	}

	@Configuration
	public static class Config12 {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data12.json")});
			return factoryBean;
		}
	}

	@Configuration
	public static class Config13 {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data13.json")});
			return factoryBean;
		}
	}

	@Configuration
	public static class Config14 {

		@Bean
		public StateMachineJackson2RepositoryPopulatorFactoryBean jackson2RepositoryPopulatorFactoryBean() {
			StateMachineJackson2RepositoryPopulatorFactoryBean factoryBean = new StateMachineJackson2RepositoryPopulatorFactoryBean();
			factoryBean.setResources(new Resource[]{new ClassPathResource("data14.json")});
			return factoryBean;
		}
	}

	@Configuration
	@EnableStateMachineFactory
	public static class FactoryConfig extends StateMachineConfigurerAdapter<String, String> {

		@Autowired
		private StateRepository<? extends RepositoryState> stateRepository;

		@Autowired
		private TransitionRepository<? extends RepositoryTransition> transitionRepository;

		@Override
		public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
			model
				.withModel()
					.factory(modelFactory());
		}

		@Bean
		public StateMachineModelFactory<String, String> modelFactory() {
			return new RepositoryStateMachineModelFactory(stateRepository, transitionRepository);
		}
	}

	private static class ChoiceGuard implements Guard<String, String> {

		private final String match;

		public ChoiceGuard(String match) {
			this.match = match;
		}

		@Override
		public boolean evaluate(StateContext<String, String> context) {
			return ObjectUtils.nullSafeEquals(match, context.getMessageHeaders().get("choice", String.class));
		}
	}

	private static class JunctionGuard implements Guard<String, String> {

		private final String match;

		public JunctionGuard(String match) {
			this.match = match;
		}

		@Override
		public boolean evaluate(StateContext<String, String> context) {
			return ObjectUtils.nullSafeEquals(match, context.getMessageHeaders().get("junction", String.class));
		}
	}

	/**
	 * Builds the context.
	 *
	 * @return the annotation config application context
	 */
	protected AnnotationConfigApplicationContext buildContext() {
		return null;
	}
}
