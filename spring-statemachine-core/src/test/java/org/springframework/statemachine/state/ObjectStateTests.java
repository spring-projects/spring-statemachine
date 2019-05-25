/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.statemachine.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.ReactiveAction;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class ObjectStateTests {

	@Test
	public void testEntrySingleAction() {
		TestAction action = new TestAction();
		ObjectState<String, String> state = new ObjectState<>("TEST", null, Arrays.asList(action), null, null, null,
				null, null);
		StepVerifier.create(state.entry(null))
			.expectComplete()
			.verify();
		assertThat(action.count).hasValue(1);
	}

	@Test
	public void testEntryMultiActions() {
		TestAction action1 = new TestAction();
		TestAction action2 = new TestAction();
		ObjectState<String, String> state = new ObjectState<>("TEST", null, Arrays.asList(action1, action2), null, null,
				null, null, null);
		StepVerifier.create(state.entry(null))
			.expectComplete()
			.verify();
		assertThat(action1.count).hasValue(1);
		assertThat(action2.count).hasValue(1);
	}

	@Test
	public void testExitSingle() {
		TestAction action = new TestAction();
		ObjectState<String, String> state = new ObjectState<>("TEST", null, null, Arrays.asList(action), null, null,
				null, null);
		StepVerifier.create(state.exit(null))
			.expectComplete()
			.verify();
		assertThat(action.count).hasValue(1);
	}

	@Test
	public void testExitMultiActions() {
		TestAction action1 = new TestAction();
		TestAction action2 = new TestAction();
		ObjectState<String, String> state = new ObjectState<>("TEST", null, null, Arrays.asList(action1, action2), null,
				null, null, null);
		StepVerifier.create(state.exit(null))
			.expectComplete()
			.verify();
		assertThat(action1.count).hasValue(1);
		assertThat(action2.count).hasValue(1);
	}

	@Test
	public void testStateAction() {
		TestAction action = new TestAction();
		ObjectState<String, String> state = new ObjectState<>("TEST", null, null, null, Arrays.asList(action), null,
				null, null);
		StepVerifier.create(state.entry(null))
			.expectComplete()
			.verify();
		await().untilAtomic(action.count, is(1));
	}

	@Test
	public void testEntrySingleActionBlocks() {
		TestBlockingAction action = new TestBlockingAction();
		ObjectState<String, String> state = new ObjectState<>("TEST", null, Arrays.asList(action), null, null, null,
				null, null);
		StepVerifier.create(state.entry(null))
			.expectComplete()
			.verify();
		assertThat(action.countBefore).hasValue(1);
		assertThat(action.countInterrupt).hasValue(0);
		assertThat(action.countAfter).hasValue(1);
	}

	@Test
	public void testStateActionBlocks() {
		TestBlockingAction action = new TestBlockingAction();
		ObjectState<String, String> state = new ObjectState<>("TEST", null, null, null, Arrays.asList(action), null,
				null, null);
		StepVerifier.create(state.entry(null))
			.expectComplete()
			.verify();
		await().untilAtomic(action.countAfter, is(1));
		assertThat(action.countBefore).hasValue(1);
		assertThat(action.countInterrupt).hasValue(0);
	}

	@Test
	public void testStateMultiActionBlocks() {
		TestBlockingAction action1 = new TestBlockingAction();
		TestBlockingAction action2 = new TestBlockingAction();
		ObjectState<String, String> state = new ObjectState<>("TEST", null, null, null, Arrays.asList(action1, action2),
				null, null, null);
		StepVerifier.create(state.entry(null))
			.expectComplete()
			.verify();
		await().untilAtomic(action1.countAfter, is(1));
		assertThat(action1.countBefore).hasValue(1);
		assertThat(action1.countInterrupt).hasValue(0);
		await().untilAtomic(action2.countAfter, is(1));
		assertThat(action2.countBefore).hasValue(1);
		assertThat(action2.countInterrupt).hasValue(0);
	}

	private static class TestAction implements ReactiveAction<String, String> {

		AtomicInteger count = new AtomicInteger();

		@Override
		public Mono<Void> apply(StateContext<String, String> context) {
			return Mono.<Void>empty().doOnSuccess(d -> {
				count.incrementAndGet();
			});
		}
	}

	private static class TestBlockingAction implements ReactiveAction<String, String> {

		AtomicInteger countBefore = new AtomicInteger();
		AtomicInteger countAfter = new AtomicInteger();
		AtomicInteger countInterrupt = new AtomicInteger();

		@Override
		public Mono<Void> apply(StateContext<String, String> context) {
			return Mono.fromRunnable(() -> {
				countBefore.incrementAndGet();
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					countInterrupt.incrementAndGet();
				}
				countAfter.incrementAndGet();
			});
		}
	}
}
