/*
 * Copyright 2019-2020 the original author or authors.
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
package org.springframework.statemachine.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

public class LifecycleObjectSupportTests {

	@Test
	public void testBlocking() {
		LifecycleObjectSupport support = new NoopLifecycleObjectSupport();
		assertThat(support.isRunning()).isFalse();
		support.start();
		assertThat(support.isRunning()).isTrue();
		support.stop();
		assertThat(support.isRunning()).isFalse();
	}

	@Test
	public void testReactive() {
		LifecycleObjectSupport support = new NoopLifecycleObjectSupport();
		StepVerifier.create(support.startReactively()).expectComplete().verify();
		assertThat(support.isRunning()).isTrue();
		StepVerifier.create(support.stopReactively()).expectComplete().verify();
		assertThat(support.isRunning()).isFalse();
	}

	private static class NoopLifecycleObjectSupport extends LifecycleObjectSupport {

	}
}
