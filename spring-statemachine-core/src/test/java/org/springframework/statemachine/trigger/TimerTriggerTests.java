/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.statemachine.trigger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.statemachine.AbstractStateMachineTests;

public class TimerTriggerTests extends AbstractStateMachineTests {

	@Test
	public void testListenerEvents() throws Exception {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(BaseConfig.class, Config1.class);

		final CountDownLatch latch = new CountDownLatch(2);
		@SuppressWarnings("rawtypes")
		TimerTrigger timerTrigger = ctx.getBean(TimerTrigger.class);
		timerTrigger.addTriggerListener(new TriggerListener() {

			@Override
			public void triggered() {
				latch.countDown();
			}
		});
		timerTrigger.afterPropertiesSet();
		timerTrigger.start();

		assertThat(latch.await(1, TimeUnit.SECONDS), is(true));
		ctx.close();
	}

	static class Config1 {

		@Bean
		public TimerTrigger<TestStates, TestEvents> timerTrigger() {
			return new TimerTrigger<TestStates, TestEvents>(100);
		}
	}

}
