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
package demo.datajpamultipersist;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.data.RepositoryTransition;
import org.springframework.statemachine.data.TransitionRepository;
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StateMachineController {

	public final static String MACHINE_ID_1 = "datajpamultipersist1";
	public final static String MACHINE_ID_2 = "datajpamultipersist2";
	public final static String MACHINE_ID_2R1 = "datajpamultipersist2#R1";
	public final static String MACHINE_ID_2R2 = "datajpamultipersist2#R2";
	private final static String[] MACHINES = new String[] { MACHINE_ID_1, MACHINE_ID_2 };
	private final StateMachineLogListener listener = new StateMachineLogListener();

	@Autowired
	private StateMachineService<String, String> stateMachineService;

	@Autowired
	private StateMachinePersist<String, String, String> stateMachinePersist;

	@Autowired
	private TransitionRepository<? extends RepositoryTransition> transitionRepository;

	private StateMachine<String, String> currentStateMachine;

	@RequestMapping("/")
	public String home() {
		return "redirect:/state";
	}

	@RequestMapping("/state")
	public String feedAndGetStates(
			@RequestParam(value = "events", required = false) List<String> events,
			@RequestParam(value = "machine", required = false, defaultValue = MACHINE_ID_1) String machine,
			Model model) throws Exception {


		StateMachine<String, String> stateMachine = getStateMachine(machine);
		if (events != null) {
			for (String event : events) {
				stateMachine.sendEvent(event);
			}
		}

		StringBuilder contextBuf = new StringBuilder();

		StateMachineContext<String, String> stateMachineContext = stateMachinePersist.read(machine);
		if (stateMachineContext != null) {
			contextBuf.append(stateMachineContext.toString());
		}
		if (ObjectUtils.nullSafeEquals(machine, MACHINE_ID_2)) {
			stateMachineContext = stateMachinePersist.read(MACHINE_ID_2R1);
			if (stateMachineContext != null) {
				contextBuf.append("\n---\n");
				contextBuf.append(stateMachineContext.toString());
			}
			stateMachineContext = stateMachinePersist.read(MACHINE_ID_2R2);
			if (stateMachineContext != null) {
				contextBuf.append("\n---\n");
				contextBuf.append(stateMachineContext.toString());
			}
		}

		model.addAttribute("allMachines", MACHINES);
		model.addAttribute("machine", machine);
		model.addAttribute("currentMachine", currentStateMachine);
		model.addAttribute("allEvents", getEvents());
		model.addAttribute("messages", createMessages(listener.getMessages()));
		model.addAttribute("context", contextBuf.toString());
		return "states";
	}

	private synchronized StateMachine<String, String> getStateMachine(String machineId) throws Exception {
		listener.resetMessages();
		if (currentStateMachine == null) {
			currentStateMachine = stateMachineService.acquireStateMachine(machineId, false);
			currentStateMachine.addStateListener(listener);
			currentStateMachine.start();
		} else if (!ObjectUtils.nullSafeEquals(currentStateMachine.getId(), machineId)) {
			stateMachineService.releaseStateMachine(currentStateMachine.getId());
			currentStateMachine.stop();
			currentStateMachine = stateMachineService.acquireStateMachine(machineId, false);
			currentStateMachine.addStateListener(listener);
			currentStateMachine.start();
		}
		return currentStateMachine;
	}

	private String[] getEvents() {
		List<String> events = new ArrayList<>();
		for (RepositoryTransition t : transitionRepository.findAll()) {
			events.add(t.getEvent());
		}
		return events.toArray(new String[0]);
	}

	private String createMessages(List<String> messages) {
		StringBuilder buf = new StringBuilder();
		for (String message : messages) {
			buf.append(message);
			buf.append("\n");
		}
		return buf.toString();
	}

}
