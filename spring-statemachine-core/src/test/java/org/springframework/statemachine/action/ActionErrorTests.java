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
package org.springframework.statemachine.action;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.AbstractStateMachineTests;
import org.springframework.statemachine.ObjectStateMachine;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

public class ActionErrorTests extends AbstractStateMachineTests {

    @Override
    protected AnnotationConfigApplicationContext buildContext() {
        return new AnnotationConfigApplicationContext();
    }

    @Test
    public void testActionExceptionNotCausingStateChange() {
        context.register(Config1.class);
        context.refresh();
        assertTrue(context.containsBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE));
        @SuppressWarnings("unchecked")
        ObjectStateMachine<TestStates,TestEvents> machine =
                context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
        machine.start();
        // error in transition should not cause transition and should
        // not propagate error into a caller.
        machine.sendEvent(TestEvents.E1);
        assertThat(machine.getState().getIds(), contains(TestStates.S1));
    }

    @Configuration
    @EnableStateMachine
    static class Config1 extends EnumStateMachineConfigurerAdapter<TestStates, TestEvents> {

        @Override
        public void configure(StateMachineStateConfigurer<TestStates, TestEvents> states) throws Exception {
            states
                    .withStates()
                    .initial(TestStates.S1)
                    .state(TestStates.S1)
                    .state(TestStates.S2);
        }

        @Override
        public void configure(StateMachineTransitionConfigurer<TestStates, TestEvents> transitions) throws Exception {
            transitions
                    .withExternal()
                    .source(TestStates.S1)
                    .target(TestStates.S2)
                    .event(TestEvents.E1)
                    .action(testAction1());
        }

        @Bean
        public TestCountAction testAction1() {
            return new TestCountAction();
        }

    }

    private static class TestCountAction implements Action<TestStates, TestEvents> {

        @Override
        public void execute(StateContext<TestStates, TestEvents> context) {
            throw new RuntimeException();
        }

    }

}