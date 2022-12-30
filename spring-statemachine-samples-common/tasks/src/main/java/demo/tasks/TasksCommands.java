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
package demo.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

@Component
public class TasksCommands implements CommandMarker {

	@Autowired
	private Tasks tasks;

	@CliCommand(value = "tasks run", help = "Run tasks")
	public void run() {
		tasks.run();
	}

	@CliCommand(value = "tasks list", help = "List tasks")
	public String list() {
		return tasks.toString();
	}

	@CliCommand(value = "tasks fix", help = "Fix tasks")
	public void fix() {
		tasks.fix();
	}

	@CliCommand(value = "tasks fail", help = "Fail task")
	public void fail(@CliOption(key = {"", "task"}, help = "Task id") String task) {
		tasks.fail(task);
	}

}
