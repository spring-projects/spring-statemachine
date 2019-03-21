/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.statemachine.config;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

public class MachineTypedTests extends AbstractStateMachineTests {

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Test
	public void testAutowireMachineByTypeNameMatches() {
		context.register(Config1.class, Config2.class, MyBean1Config.class);
		context.refresh();

		MyBean1 myBean1 = context.getBean(MyBean1.class);
		assertThat(myBean1.machine1, notNullValue());
		assertThat(myBean1.machine2, notNullValue());
		assertThat(myBean1.machine1, not(sameInstance(myBean1.machine2)));
	}

	@Test
	public void testAutowireMachineByTypeNameNotMatches() {
		context.register(Config1.class, Config2.class, MyBean2Config.class);
		context.refresh();

		MyBean2 myBean2 = context.getBean(MyBean2.class);
		assertThat(myBean2.someMachine1, notNullValue());
		assertThat(myBean2.someMachine2, notNullValue());
		assertThat(myBean2.someMachine1, not(sameInstance(myBean2.someMachine2)));
	}

	@Test
	public void testAutowireMachineFactoryByTypeNameMatches() {
		context.register(Config3.class, Config4.class, MyBean3Config.class);
		context.refresh();

		MyBean3 myBean3 = context.getBean(MyBean3.class);
		assertThat(myBean3.machinefactory1, notNullValue());
		assertThat(myBean3.machinefactory2, notNullValue());
		assertThat(myBean3.machinefactory1, not(sameInstance(myBean3.machinefactory2)));
	}

	@Test
	public void testAutowireMachineFactoryByTypeNameNotMatches() {
		context.register(Config3.class, Config4.class, MyBean4Config.class);
		context.refresh();

		MyBean4 myBean4 = context.getBean(MyBean4.class);
		assertThat(myBean4.someMachineFactory1, notNullValue());
		assertThat(myBean4.someMachineFactory2, notNullValue());
		assertThat(myBean4.someMachineFactory1, not(sameInstance(myBean4.someMachineFactory2)));
	}

	@Test
	public void testAutowireMachineFactoryByNewBaseTypeNameNotMatches() {
		context.register(Config5.class, Config6.class, MyBean5Config.class);
		context.refresh();

		MyBean5 myBean5 = context.getBean(MyBean5.class);
		assertThat(myBean5.someMachineFactory3, notNullValue());
		assertThat(myBean5.someMachineFactory4, notNullValue());
		assertThat(myBean5.someMachineFactory3, not(sameInstance(myBean5.someMachineFactory4)));
	}

	@Test
	public void testAutowireMachineByNewBaseTypeNameNotMatches() {
		context.register(Config7.class, Config8.class, MyBean6Config.class);
		context.refresh();

		MyBean6 myBean6 = context.getBean(MyBean6.class);
		assertThat(myBean6.someMachine3, notNullValue());
		assertThat(myBean6.someMachine4, notNullValue());
		assertThat(myBean6.someMachine3, not(sameInstance(myBean6.someMachine4)));
	}

	@Configuration
	public static class MyBean1Config {

		@Bean
		public MyBean1 myBean1() {
			return new MyBean1();
		}
	}

	@Configuration
	public static class MyBean2Config {

		@Bean
		public MyBean2 myBean2() {
			return new MyBean2();
		}
	}

	@Configuration
	public static class MyBean3Config {

		@Bean
		public MyBean3 myBean3() {
			return new MyBean3();
		}
	}

	@Configuration
	public static class MyBean4Config {

		@Bean
		public MyBean4 myBean4() {
			return new MyBean4();
		}
	}

	@Configuration
	public static class MyBean5Config {

		@Bean
		public MyBean5 myBean5() {
			return new MyBean5();
		}
	}

	@Configuration
	public static class MyBean6Config {

		@Bean
		public MyBean6 myBean6() {
			return new MyBean6();
		}
	}

	public static class MyBean1 {

		@Autowired
		StateMachine<MyTestStates1, MyTestEvents1> machine1;

		@Autowired
		StateMachine<MyTestStates2, MyTestEvents2> machine2;
	}

	public static class MyBean2 {

		@Autowired
		StateMachine<MyTestStates1, MyTestEvents1> someMachine1;

		@Autowired
		StateMachine<MyTestStates2, MyTestEvents2> someMachine2;
	}

	public static class MyBean3 {

		@Autowired
		StateMachineFactory<MyTestStates1, MyTestEvents1> machinefactory1;

		@Autowired
		StateMachineFactory<MyTestStates2, MyTestEvents2> machinefactory2;
	}

	public static class MyBean4 {

		@Autowired
		StateMachineFactory<MyTestStates1, MyTestEvents1> someMachineFactory1;

		@Autowired
		StateMachineFactory<MyTestStates2, MyTestEvents2> someMachineFactory2;
	}

	public static class MyBean5 {

		@Autowired
		StateMachineFactory<MyTestStates1, MyTestEvents1> someMachineFactory3;

		@Autowired
		StateMachineFactory<MyTestStates2, MyTestEvents2> someMachineFactory4;
	}

	public static class MyBean6 {

		@Autowired
		StateMachine<MyTestStates1, MyTestEvents1> someMachine3;

		@Autowired
		StateMachine<MyTestStates2, MyTestEvents2> someMachine4;
	}

	@Configuration
	@EnableStateMachine(name = "machine1")
	public static class Config1 extends EnumStateMachineConfigurerAdapter<MyTestStates1, MyTestEvents1> {

		@Override
		public void configure(StateMachineStateConfigurer<MyTestStates1, MyTestEvents1> states) throws Exception {
			states
				.withStates()
					.initial(MyTestStates1.S1)
					.state(MyTestStates1.S2);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<MyTestStates1, MyTestEvents1> transitions) throws Exception {
			transitions
				.withExternal()
					.source(MyTestStates1.S1).target(MyTestStates1.S2)
					.event(MyTestEvents1.E1);
		}
	}

	@Configuration
	@EnableStateMachine(name = "machine2")
	public static class Config2 extends EnumStateMachineConfigurerAdapter<MyTestStates2, MyTestEvents2> {

		@Override
		public void configure(StateMachineStateConfigurer<MyTestStates2, MyTestEvents2> states) throws Exception {
			states
				.withStates()
					.initial(MyTestStates2.S1)
					.state(MyTestStates2.S2);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<MyTestStates2, MyTestEvents2> transitions) throws Exception {
			transitions
				.withExternal()
					.source(MyTestStates2.S1).target(MyTestStates2.S2)
					.event(MyTestEvents2.E1);
		}
	}

	@Configuration
	@EnableStateMachineFactory(name = "machinefactory1")
	public static class Config3 extends EnumStateMachineConfigurerAdapter<MyTestStates1, MyTestEvents1> {

		@Override
		public void configure(StateMachineStateConfigurer<MyTestStates1, MyTestEvents1> states) throws Exception {
			states
				.withStates()
					.initial(MyTestStates1.S1)
					.state(MyTestStates1.S2);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<MyTestStates1, MyTestEvents1> transitions) throws Exception {
			transitions
				.withExternal()
					.source(MyTestStates1.S1).target(MyTestStates1.S2)
					.event(MyTestEvents1.E1);
		}
	}

	@Configuration
	@EnableStateMachineFactory(name = "machinefactory2")
	public static class Config4 extends EnumStateMachineConfigurerAdapter<MyTestStates2, MyTestEvents2> {

		@Override
		public void configure(StateMachineStateConfigurer<MyTestStates2, MyTestEvents2> states) throws Exception {
			states
				.withStates()
					.initial(MyTestStates2.S1)
					.state(MyTestStates2.S2);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<MyTestStates2, MyTestEvents2> transitions) throws Exception {
			transitions
				.withExternal()
					.source(MyTestStates2.S1).target(MyTestStates2.S2)
					.event(MyTestEvents2.E1);
		}
	}

	@Configuration
	@EnableStateMachineFactory(name = "machinefactory3")
	public static class Config5 extends MyNewBaseAdapter1 {

		@Override
		public void configure(StateMachineStateConfigurer<MyTestStates1, MyTestEvents1> states) throws Exception {
			states
				.withStates()
					.initial(MyTestStates1.S1)
					.state(MyTestStates1.S2);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<MyTestStates1, MyTestEvents1> transitions) throws Exception {
			transitions
				.withExternal()
					.source(MyTestStates1.S1).target(MyTestStates1.S2)
					.event(MyTestEvents1.E1);
		}
	}

	@Configuration
	@EnableStateMachineFactory(name = "machinefactory4")
	public static class Config6 extends MyNewBaseAdapter2 {

		@Override
		public void configure(StateMachineStateConfigurer<MyTestStates2, MyTestEvents2> states) throws Exception {
			states
				.withStates()
					.initial(MyTestStates2.S1)
					.state(MyTestStates2.S2);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<MyTestStates2, MyTestEvents2> transitions) throws Exception {
			transitions
				.withExternal()
					.source(MyTestStates2.S1).target(MyTestStates2.S2)
					.event(MyTestEvents2.E1);
		}
	}

	@Configuration
	@EnableStateMachine(name = "machine3")
	public static class Config7 extends MyNewBaseAdapter1 {

		@Override
		public void configure(StateMachineStateConfigurer<MyTestStates1, MyTestEvents1> states) throws Exception {
			states
				.withStates()
					.initial(MyTestStates1.S1)
					.state(MyTestStates1.S2);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<MyTestStates1, MyTestEvents1> transitions) throws Exception {
			transitions
				.withExternal()
					.source(MyTestStates1.S1).target(MyTestStates1.S2)
					.event(MyTestEvents1.E1);
		}
	}

	@Configuration
	@EnableStateMachine(name = "machine4")
	public static class Config8 extends MyNewBaseAdapter2 {

		@Override
		public void configure(StateMachineStateConfigurer<MyTestStates2, MyTestEvents2> states) throws Exception {
			states
				.withStates()
					.initial(MyTestStates2.S1)
					.state(MyTestStates2.S2);
		}

		@Override
		public void configure(StateMachineTransitionConfigurer<MyTestStates2, MyTestEvents2> transitions) throws Exception {
			transitions
				.withExternal()
					.source(MyTestStates2.S1).target(MyTestStates2.S2)
					.event(MyTestEvents2.E1);
		}
	}

	public static class MyNewBaseAdapter1 extends EnumStateMachineConfigurerAdapter<MyTestStates1, MyTestEvents1> {
	}

	public static class MyNewBaseAdapter2 extends EnumStateMachineConfigurerAdapter<MyTestStates2, MyTestEvents2> {
	}

	public enum MyTestStates1 {
		S1, S2;
	}

	public enum MyTestEvents1 {
		E1;
	}

	public enum MyTestStates2 {
		S1, S2;
	}

	public enum MyTestEvents2 {
		E1;
	}
}
