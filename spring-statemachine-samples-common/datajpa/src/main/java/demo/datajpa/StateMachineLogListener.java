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
package demo.datajpa;

import java.util.LinkedList;
import java.util.List;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateContext.Stage;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;

public class StateMachineLogListener extends StateMachineListenerAdapter<String, String> {

	private final LinkedList<String> messages = new LinkedList<String>();

	public List<String> getMessages() {
		return messages;
	}

	public void resetMessages() {
		messages.clear();
	}

	@Override
	public void stateContext(StateContext<String, String> stateContext) {
		if (stateContext.getStage() == Stage.STATE_ENTRY) {
			messages.addFirst("Enter " + stateContext.getTarget().getId());
		} else if (stateContext.getStage() == Stage.STATE_EXIT) {
			messages.addFirst("Exit " + stateContext.getSource().getId());
		} else if (stateContext.getStage() == Stage.STATEMACHINE_START) {
			messages.addLast("Machine started");
		} else if (stateContext.getStage() == Stage.STATEMACHINE_STOP) {
			messages.addFirst("Machine stopped");
		}
	}
}
