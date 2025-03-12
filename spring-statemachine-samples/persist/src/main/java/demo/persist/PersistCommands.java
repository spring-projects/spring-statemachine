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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;

@Command
public class PersistCommands {

	@Autowired
	private Persist persist;

	@Command(command = "persist db", description = "List entries from db")
	public String listDbEntries() {
		return persist.listDbEntries();
	}

	@Command(command = "persist process", description = "Process order")
	public void process(@Option(longNames = {"", "id"}, description = "Order id") int order) {
		persist.change(order, "PROCESS");
	}

	@Command(command = "persist send", description = "Send order")
	public void send(@Option(longNames = {"", "id"}, description = "Order id") int order) {
		persist.change(order, "SEND");
	}

	@Command(command = "persist deliver", description = "Deliver order")
	public void deliver(@Option(longNames = {"", "id"}, description = "Order id") int order) {
		persist.change(order, "DELIVER");
	}

}
