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
package demo.persist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

@Component
public class PersistCommands implements CommandMarker {

	@Autowired
	private Persist persist;

	@CliCommand(value = "persist db", help = "List entries from db")
	public String listDbEntries() {
		return persist.listDbEntries();
	}

	@CliCommand(value = "persist process", help = "Process order")
	public void process(@CliOption(key = {"", "id"}, help = "Order id") int order) {
		persist.change(order, "PROCESS");
	}

	@CliCommand(value = "persist send", help = "Send order")
	public void send(@CliOption(key = {"", "id"}, help = "Order id") int order) {
		persist.change(order, "SEND");
	}

	@CliCommand(value = "persist deliver", help = "Deliver order")
	public void deliver(@CliOption(key = {"", "id"}, help = "Order id") int order) {
		persist.change(order, "DELIVER");
	}

}
