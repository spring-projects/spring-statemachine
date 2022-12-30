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
package org.springframework.statemachine.recipes;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;

public class TestStateMachinePersist implements StateMachinePersist<String, String, Void> {

	public final ArrayList<StateMachineContext<String, String>> contexts = new ArrayList<>();
	public volatile CountDownLatch writeLatch = new CountDownLatch(1);
	private StateMachineContext<String, String> context;

	public TestStateMachinePersist() {
	}

	public TestStateMachinePersist(StateMachineContext<String, String> context) {
		this.context = context;
	}

	@Override
	public void write(StateMachineContext<String, String> context, Void contextObj) throws Exception {
		synchronized (this) {
			contexts.add(context);
		}
		this.context = context;
		writeLatch.countDown();
	}

	@Override
	public StateMachineContext<String, String> read(Void contextObj) throws Exception {
		return context;
	}

	public ArrayList<StateMachineContext<String, String>> getContexts() {
		return contexts;
	}

	public void reset(int c1) {
		synchronized (this) {
			contexts.clear();
			writeLatch = new CountDownLatch(c1);
		}
	}

}
