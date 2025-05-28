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

import demo.BasicCommand;
import demo.Command;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TasksCommands {

	@Autowired
	private Tasks tasks;

	@Bean
	public Command run() {
		return new BasicCommand("run", "Run tasks") {
			@Override
			public String execute(String[] args) {
				tasks.run();
				return "Tasks started";
			}
		};
	}

	@Bean
	public Command list() {
		return new BasicCommand("list", "List tasks") {
			@Override
			public String execute(String[] args) {
				return tasks.toString();
			}
		};
	}

	@Bean
	public Command fix() {
		return new BasicCommand("fix", "Fix tasks") {
			@Override
			public String execute(String[] args) {
				tasks.fix();
				return "Tasks fixed";
			}
		};
	}

	@Bean
	public Command fail() {
		return new BasicCommand("fail [taskId]", "Fail task with [taskId]") {
			@Override
			public String execute(String[] args) {
				String taskId = args[0];
				tasks.fail(taskId);
				return "Task " + taskId + " failed";
			}
		};
	}

}
