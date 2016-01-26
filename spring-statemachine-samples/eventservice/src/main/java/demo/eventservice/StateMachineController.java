/*
 * Copyright 2016 the original author or authors.
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
package demo.eventservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineFunction;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import demo.eventservice.StateMachineConfig.Events;
import demo.eventservice.StateMachineConfig.States;

@Controller
public class StateMachineController {

	@Autowired
	private StateMachine<States, Events> stateMachine;

	@Autowired
	private StateMachinePersist<States, Events, String> stateMachinePersist;

	@Autowired
	private String stateChartModel;

	@RequestMapping("/")
	public String home() {
		return "redirect:/state";
	}

	@RequestMapping("/state")
	public String feedAndGetState(@RequestParam(value = "user", required = false) String user,
			@RequestParam(value = "id", required = false) Events id, Model model) throws Exception {
		model.addAttribute("user", user);
		model.addAttribute("allTypes", Events.values());
		model.addAttribute("stateChartModel", stateChartModel);
		// we may get into this page without a user so
		// do nothing with a state machine
		if (StringUtils.hasText(user)) {
			resetStateMachineFromStore(user);
			if (id != null) {
				feedMachine(user, id);
			}
			model.addAttribute("states", stateMachine.getState().getIds());
			model.addAttribute("extendedState", stateMachine.getExtendedState().getVariables());
		}
		return "states";
	}

	@RequestMapping(value = "/feed",method= RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void feedPageview(@RequestBody(required = true) Pageview event) throws Exception {
		Assert.notNull(event.getUser(), "User must be set");
		Assert.notNull(event.getId(), "Id must be set");
		resetStateMachineFromStore(event.getUser());
		feedMachine(event.getUser(), event.getId());
	}

	private void feedMachine(String user, Events id) throws Exception {
		stateMachine.sendEvent(id);
		stateMachinePersist.write(new DefaultStateMachineContext<States, Events>(stateMachine.getState().getId(), null, null,
				stateMachine.getExtendedState()), "testprefix:" + user);
	}

	private StateMachine<States, Events> resetStateMachineFromStore(String user) throws Exception {
		final StateMachineContext<States, Events> context = stateMachinePersist.read("testprefix:" + user);
		stateMachine.stop();
		stateMachine.getStateMachineAccessor()
			.doWithAllRegions(new StateMachineFunction<StateMachineAccess<States, Events>>() {

			@Override
			public void apply(StateMachineAccess<States, Events> function) {
				function.resetStateMachine(context);
			}
		});
		stateMachine.start();
		return stateMachine;
	}
}
