/*
 * Copyright 2015-2025 the original author or authors.
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
package demo.persist;

import demo.BasicCommand;
import demo.Command;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PersistCommands {

	@Autowired
	private Persist persist;

	@Bean
	public Command list() {
		return new BasicCommand("list", "List entries from db") {
			@Override
			public String execute(String[] args) {
				return persist.listDbEntries();
			}
		};
	}

	@Bean
	public Command process() {
		return new BasicCommand("process [orderId]", "Process order with [orderId]") {
			@Override
			public String execute(String[] args) {
				int order = Integer.parseInt(args[0]);
				persist.change(order, "PROCESS");
				return "Order " + order + " processed";
			}
		};
	}

	@Bean
	public Command send() {
		return new BasicCommand("send [orderId]", "Send order with [orderId]") {
			@Override
			public String execute(String[] args) {
				int order = Integer.parseInt(args[0]);
				persist.change(order, "SEND");
				return "Order " + order + " sent";
			}
		};
	}

	@Bean
	public Command deliver() {
		return new BasicCommand("deliver [orderId]", "Deliver order with [orderId]") {
			@Override
			public String execute(String[] args) {
				int order = Integer.parseInt(args[0]);
				persist.change(order, "DELIVER");
				return "Order " + order + " delivered";
			}
		};
	}

}
