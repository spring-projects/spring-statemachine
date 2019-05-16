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
package demo.deploy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StateMachineController {

	private final static String[] EVENTS = new String[] { "DEPLOY", "UNDEPLOY" };
	private final static String[] FAILS = new String[] { "isInstalled", "installedOk", "isRunning", "hasError" };

	@Autowired
	private StateMachine<String, String> stateMachine;

	@Autowired
	private StateMachineLogListener listener;

	@RequestMapping("/")
	public String home() {
		return "redirect:/state";
	}

	@RequestMapping("/state")
	public String feedAndGetState(@RequestParam(value = "event", required = false) String event,
			@RequestParam(value = "fail", required = false) Collection<String> fails, Model model) throws Exception {
		model.addAttribute("allTypes", EVENTS);
		model.addAttribute("failTypes", FAILS);
		if (StringUtils.hasText(event)) {
			Map<String , Object> headers = new HashMap<>();
			if (fails != null) {
				for (String fail : fails) {
					headers.put(fail, true);
				}
			}
			listener.resetMessages();
			stateMachine.sendEvent(MessageBuilder.createMessage(event, new MessageHeaders(headers)));
		}
		model.addAttribute("states", stateMachine.getState().getIds());
		model.addAttribute("messages", listener.getMessages());
		return "states";
	}
}
