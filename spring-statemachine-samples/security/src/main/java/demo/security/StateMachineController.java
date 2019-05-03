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
package demo.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import demo.security.StateMachineConfig.Events;
import demo.security.StateMachineConfig.States;

@Controller
public class StateMachineController {

	private final static Log log = LogFactory.getLog(StateMachineController.class);

	@Autowired
	private StateMachine<States, Events> stateMachine;

	@Autowired
	@Qualifier("stateChartModel")
	private String stateChartModel;

	@RequestMapping("/")
	public String greeting() {
		return "redirect:/states";
	}

	@RequestMapping("/states")
	public String getStates(@RequestParam(value = "event", required = false) Events event, Model model) {
		if (event != null) {
			try {
				stateMachine.sendEvent(event);
			} catch (Exception e) {
				log.error("Error sendEvent", e);
			}
		}
		model.addAttribute("states", stateMachine.getState().getIds());
		model.addAttribute("stateChartModel", stateChartModel);
		return "states";
	}

}