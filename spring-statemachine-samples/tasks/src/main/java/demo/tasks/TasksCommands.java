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
package demo.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;

@Command
public class TasksCommands {

	@Autowired
	private Tasks tasks;

	@Command(command = "tasks run", description = "Run tasks")
	public void run() {
		tasks.run();
	}

	@Command(command = "tasks list", description = "List tasks")
	public String list() {
		return tasks.toString();
	}

	@Command(command = "tasks fix", description = "Fix tasks")
	public void fix() {
		tasks.fix();
	}

	@Command(command = "tasks fail", description = "Fail task")
	public void fail(@Option(longNames = {"", "task"}, description = "Task id") String task) {
		tasks.fail(task);
	}

}
