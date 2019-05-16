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
package demo.ordershipping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StateMachineController {

	private final static String[] CUSTOMERS = new String[] { "customer1", "customer2", "customer3" };
	private final static String[] ORDERS = new String[] { "order1", "order2", "order3" };
	private final static String[] EVENTS = new String[] { "PLACE_ORDER", "RECEIVE_PAYMENT" };
	private final static String[] GUIDES = new String[] { "makeProdPlan", "produce", "payment" };

	@Autowired
	private StateMachineFactory<String, String> stateMachineFactory;

	@Autowired
	private StateMachineLogListener listener;

	private final Map<String, StateMachine<String, String>> machines = new HashMap<>();

	@RequestMapping("/")
	public String home() {
		return "redirect:/state";
	}

	@RequestMapping("/state")
	public String feedAndGetStates(
			@RequestParam(value = "action", required = false) String action,
			@RequestParam(value = "customer", required = false) String customer,
			@RequestParam(value = "order", required = false) String order,
			@RequestParam(value = "event", required = false) String event,
			@RequestParam(value = "guide", required = false) List<String> guides,
			Model model) throws Exception {
		StateMachine<String, String> stateMachine = null;
		if (StringUtils.hasText(customer) && StringUtils.hasText(order)) {
			stateMachine = getMachine(customer + ":" + order);
		}

		if (stateMachine != null && StringUtils.hasText(event) && ObjectUtils.nullSafeEquals(action, "event")) {
			Map<String , Object> headers = new HashMap<>();
			if (ObjectUtils.nullSafeEquals(event, EVENTS[0])) {
				headers.put("customer", customer);
				headers.put("order", order);
			}
			if (guides != null) {
				if (guides.contains("makeProdPlan")) {
					headers.put("makeProdPlan", true);
				}
				if (guides.contains("produce")) {
					headers.put("produce", true);
				}
				if (guides.contains("payment")) {
					headers.put("payment", true);
				}
			}
			stateMachine.sendEvent(MessageBuilder.createMessage(event, new MessageHeaders(headers)));
		}

		model.addAttribute("allIds", machines.keySet());
		model.addAttribute("allCustomers", CUSTOMERS);
		model.addAttribute("allOrders", ORDERS);
		model.addAttribute("allTypes", EVENTS);
		model.addAttribute("allGuides", GUIDES);
		model.addAttribute("machines", machines.values());
		model.addAttribute("messages", createMessages(listener.getMessages()));
		return "states";
	}

	private synchronized StateMachine<String, String> getMachine(String id) {
		StateMachine<String,String> machine = machines.get(id);
		if (machine == null) {
			machine = stateMachineFactory.getStateMachine(id);
			machines.put(id, machine);
		}
		return machine;
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
