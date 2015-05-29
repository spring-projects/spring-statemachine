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
package demo.tasks;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.annotation.WithStateMachine;

import demo.tasks.Application.Events;
import demo.tasks.Application.States;
import demo.tasks.Application.StatesOnTransition;

@WithStateMachine
public class Tasks {

	private final static Log log = LogFactory.getLog(Tasks.class);

	@Autowired
	private StateMachine<States, Events> stateMachine;

	private final Map<String, Boolean> tasks = new HashMap<String, Boolean>();

	public Tasks() {
		tasks.put("T1", true);
		tasks.put("T2", true);
		tasks.put("T3", true);
	}

	public void run() {
		stateMachine.sendEvent(Events.RUN);
	}

	public void cont() {
		stateMachine.sendEvent(Events.CONTINUE);
	}

	public void fix(String task) {
		if (tasks.containsKey(task)) {
			tasks.put(task, true);
		}
		stateMachine.sendEvent(Events.FIX);
	}

	public void fail(String task) {
		if (tasks.containsKey(task)) {
			tasks.put(task, false);
		}
	}

	@StatesOnTransition(target = States.T1)
	public void taskT1(ExtendedState extendedState) {
		log.info("run task on T1");
		sleep(2000);
		extendedState.getVariables().put("T1", tasks.get("T1"));
		log.info("run task on T1 done");
	}

	@StatesOnTransition(target = States.T2)
	public void taskT2(ExtendedState extendedState) {
		log.info("run task on T2");
		sleep(2000);
		extendedState.getVariables().put("T2", tasks.get("T2"));
		log.info("run task on T2 done");
	}

	@StatesOnTransition(target = States.T3)
	public void taskT3(ExtendedState extendedState) {
		log.info("run task on T3");
		sleep(2000);
		extendedState.getVariables().put("T3", tasks.get("T3"));
		log.info("run task on T3 done");
	}

	@StatesOnTransition(target = States.AUTOMATIC)
	public void automaticFix(ExtendedState extendedState) {
		Map<Object, Object> variables = extendedState.getVariables();
		if (variables.get("T1").equals(false)) {
			variables.put("T1", true);
			tasks.put("T1", true);
		}
	}

	@StatesOnTransition(target = States.MANUAL)
	public void manualFix(ExtendedState extendedState) {
		Map<Object, Object> variables = extendedState.getVariables();
		if (variables.get("T2").equals(false)) {
			variables.put("T2", true);
			tasks.put("T2", true);
		}
	}

	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}

	@Override
	public String toString() {
		return "Tasks " + tasks;
	}

}
