/*
 * Copyright 2016-2020 the original author or authors.
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
package org.springframework.statemachine.uml;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.uml.support.EnumTypeConverter;

public class EnumUmlStateMachineModelFactoryTests extends AbstractUmlTests<EnumUmlStateMachineModelFactoryTests.TestState, EnumUmlStateMachineModelFactoryTests.TestEvent> {

	public static final EnumTypeConverter<TestState> STATE_CONVERTER = new EnumTypeConverter<>(TestState.class);
	public static final EnumTypeConverter<TestEvent> EVENT_CONVERTER = new EnumTypeConverter<>(TestEvent.class);

	public EnumUmlStateMachineModelFactoryTests() {
		super(STATE_CONVERTER, EVENT_CONVERTER);
	}

	@Override
	protected AnnotationConfigApplicationContext buildContext() {
		return new AnnotationConfigApplicationContext();
	}

	@Override
	protected Action<TestState, TestEvent> createLatchAction() {
		return new LatchAction();
	}

	protected void registerConfig2() {
		context.register(Config2.class);
	}

	protected void registerConfig3() {
		context.register(Config3.class);
	}

	protected void registerConfig4() {
		context.register(Config4.class);
	}

	protected void registerConfig5() {
		context.register(Config5.class);
	}

	protected void registerConfig6() {
		context.register(Config6.class);
	}

	protected void registerConfig6MissingName() {
		context.register(Config6MissingName.class);
	}

	protected void registerConfig7() {
		context.register(Config7.class);
	}

	protected void registerConfig20() {
		context.register(Config20.class);
	}

	protected void registerConfig8() {
		context.register(Config8.class);
	}

	protected void registerConfig9() {
		context.register(Config9.class);
	}

	protected void registerConfig10() {
		context.register(Config10.class);
	}

	protected void registerConfig11() {
		context.register(Config11.class);
	}

	protected void registerConfig12() {
		context.register(Config12.class);
	}

	protected void registerConfig13() {
		context.register(Config13.class);
	}

	protected void registerConfig14() {
		context.register(Config14.class);
	}

	protected void registerConfig15() {
		context.register(Config15.class);
	}

	protected void registerConfig16() {
		context.register(Config16.class);
	}

	protected void registerConfig17() {
		context.register(Config17.class);
	}

	protected void registerConfig18() {
		context.register(Config18.class);
	}

	protected void registerConfig19() {
		context.register(Config19.class);
	}

	protected void registerConfig21() {
		context.register(Config21.class);
	}

	protected void registerConfig22() {
		context.register(Config22.class);
	}

	protected void registerConfig23() {
		context.register(Config23.class);
	}

	protected void registerConfig24() {
		context.register(Config24.class);
	}

	protected void registerConfig25() {
		context.register(Config25.class);
	}

	protected void registerConfig26() {
		context.register(Config26.class);
	}

	protected void registerConfig27() {
		context.register(Config27.class);
	}

	protected void registerConfig28() {
		context.register(Config28.class);
	}

	@Configuration
	@EnableStateMachine
	public static class Config2 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-flat.uml");
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>(model, TestState.class, TestEvent.class);
		}

		@Bean
		public Action<TestState, TestEvent> action1() {
			return new LatchAction();
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config3 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-submachine.uml");
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>(model, TestState.class, TestEvent.class);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config4 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-root-regions.uml");
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>(model, TestState.class, TestEvent.class);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config5 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-entryexit.uml");
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>(model, TestState.class, TestEvent.class);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config6 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-choice.uml");
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>(model, TestState.class, TestEvent.class);
		}

		@Bean
		public ChoiceGuard s2Guard() {
			return new ChoiceGuard("s2");
		}

		@Bean
		public ChoiceGuard s3Guard() {
			return new ChoiceGuard("s3");
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config6MissingName extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/missingname-choice.uml");
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>(model, TestState.class, TestEvent.class);
		}

		@Bean
		public ChoiceGuard s2Guard() {
			return new ChoiceGuard("s2");
		}

		@Bean
		public ChoiceGuard s3Guard() {
			return new ChoiceGuard("s3");
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config7 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-forkjoin.uml");
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>(model, TestState.class, TestEvent.class);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config8 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-history-shallow.uml");
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>(model, TestState.class, TestEvent.class);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config9 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-history-deep.uml");
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>(model, TestState.class, TestEvent.class);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config10 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-junction.uml");
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>(model, TestState.class, TestEvent.class);
		}

		@Bean
		public JunctionGuard s5Guard() {
			return new JunctionGuard("s5");
		}

		@Bean
		public JunctionGuard s6Guard() {
			return new JunctionGuard("s6");
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config11 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>("classpath:org/springframework/statemachine/uml/simple-actions.uml", TestState.class, TestEvent.class);
		}

		@Bean
		public LatchAction s1Exit() {
			return new LatchAction();
		}

		@Bean
		public LatchAction s2Entry() {
			return new LatchAction();
		}

		@Bean
		public LatchAction e1Action() {
			return new LatchAction();
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config12 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>("classpath:org/springframework/statemachine/uml/simple-history-default.uml", TestState.class, TestEvent.class);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config13 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>("classpath:org/springframework/statemachine/uml/simple-timers.uml", TestState.class, TestEvent.class);
		}

		@Bean
		public LatchAction s3Entry() {
			return new LatchAction();
		}

		@Bean
		public LatchAction s5Entry() {
			return new LatchAction();
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config14 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>("classpath:org/springframework/statemachine/uml/simple-guards.uml", TestState.class, TestEvent.class);
		}

		@Bean
		public SimpleGuard denyGuard() {
			return new SimpleGuard(false);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config15 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>("classpath:org/springframework/statemachine/uml/initial-actions.uml", TestState.class, TestEvent.class);
		}

		@Bean
		public LatchAction initialAction() {
			return new LatchAction();
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config16 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>("classpath:org/springframework/statemachine/uml/simple-spels.uml", TestState.class, TestEvent.class);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config17 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>("classpath:org/springframework/statemachine/uml/simple-flat-multiple-to-end.uml", TestState.class, TestEvent.class);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config18 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>("classpath:org/springframework/statemachine/uml/simple-flat-multiple-to-end-viachoices.uml", TestState.class, TestEvent.class);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config19 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>("classpath:org/springframework/statemachine/uml/broken-model-shadowentries.uml", TestState.class, TestEvent.class);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config20 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/multijoin-forkjoin.uml");
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>(model, TestState.class, TestEvent.class);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config21 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>("classpath:org/springframework/statemachine/uml/simple-submachineref.uml", TestState.class, TestEvent.class);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config22 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>("classpath:org/springframework/statemachine/uml/simple-state-actions.uml", TestState.class, TestEvent.class);
		}

		@Bean
		public LatchAction e1Action() {
			return new LatchAction();
		}

		@Bean
		public LatchAction e2Action() {
			return new LatchAction();
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config23 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>("classpath:org/springframework/statemachine/uml/simple-localtransition.uml", TestState.class, TestEvent.class);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config24 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/simple-connectionpointref.uml");
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>(model, TestState.class, TestEvent.class);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config25 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/action-with-transition-choice.uml");
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>(model, TestState.class, TestEvent.class);
		}

		@Bean
		public ChoiceGuard s2Guard() {
			return new ChoiceGuard("s2");
		}

		@Bean
		public ChoiceGuard s3Guard() {
			return new ChoiceGuard("s3");
		}

		@Bean
		public ChoiceGuard s5Guard() {
			return new ChoiceGuard("s5");
		}

		@Bean
		public ChoiceGuard choice2Guard() {
			return new ChoiceGuard("choice2");
		}

		@Bean
		public LatchAction s1ToChoice() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choiceToS2() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choiceToS4() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choice1ToChoice2() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choiceToS5() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choiceToS6() {
			return new LatchAction();
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config26 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/action-with-transition-junction.uml");
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>(model, TestState.class, TestEvent.class);
		}

		@Bean
		public ChoiceGuard s2Guard() {
			return new ChoiceGuard("s2");
		}

		@Bean
		public ChoiceGuard s3Guard() {
			return new ChoiceGuard("s3");
		}

		@Bean
		public ChoiceGuard s5Guard() {
			return new ChoiceGuard("s5");
		}

		@Bean
		public ChoiceGuard choice2Guard() {
			return new ChoiceGuard("choice2");
		}

		@Bean
		public LatchAction s1ToChoice() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choiceToS2() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choiceToS4() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choice1ToChoice2() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choiceToS5() {
			return new LatchAction();
		}

		@Bean
		public LatchAction choiceToS6() {
			return new LatchAction();
		}

	}

	@Configuration
	@EnableStateMachine
	public static class Config27 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			Resource model = new ClassPathResource("org/springframework/statemachine/uml/transition-effect-spel.uml");
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>(model, TestState.class, TestEvent.class);
		}
	}

	@Configuration
	@EnableStateMachine
	public static class Config28 extends AbstractConfig<TestState, TestEvent> {

		@Bean
		public StateMachineModelFactory<TestState, TestEvent> modelFactory() {
			Resource mainModel = new ClassPathResource("org/springframework/statemachine/uml/import-main/import-main.uml");
			Resource subModel = new ClassPathResource("org/springframework/statemachine/uml/import-sub/import-sub.uml");
			return new EnumUmlStateMachineModelFactory<TestState, TestEvent>(mainModel, new Resource[] { subModel }, TestState.class, TestEvent.class);
		}
	}

	public static class LatchAction extends AbstractLatchAction<TestState, TestEvent> {
	}

	private static class ChoiceGuard extends AbstractChoiceGuard {

		public ChoiceGuard(String match) {
			super(match);
		}

	}

	private static class SimpleGuard<S, E> extends AbstractSimpleGuard<S, E> {

		public SimpleGuard(boolean deny) {
			super(deny);
		}

	}


	private static class JunctionGuard<S, E> extends AbstractJunctionGuard<S, E> {

		public JunctionGuard(String match) {
			super(match);
		}

	}
	
	static enum TestState {
		SI, SF, SH, S211, S212,
		S1, S2, S3, S4, S5, S6, S7,
		S11, S12, S13, S14,
		S20, S21, S22, S23, S24,
		S30, S31, S32, S33, S34,
		ENTRY, ENTRY1, ENTRY2,
		EXIT, EXIT1, EXIT2,
		FINAL, FINAL1, FINAL2,
		MAIN1, MAIN2,
		JUNCTION, JUNCTION1, JUNCTION2,
		CHOICE, CHOICE1, CHOICE2,
		CHILD1, CHILD2,
		GUARD1, TIMER1,
		// See: testMissingNameChoice(), testConnectionPointReference2 etc.
		// The parser generates names where names are missing,
		// but we need to declare those generated names when using concrete types.
		choice1, initial1, initial2, initial3, ConnectionPointReference2
	}

	static enum TestEvent {
		E1, E2, E3, E4,
		E11, E12, E13, E14,
		E20, E21, E22, E23, E24,
		E30, E31, E32, E33, E34,
		ENTRY, EXIT
	}
}
