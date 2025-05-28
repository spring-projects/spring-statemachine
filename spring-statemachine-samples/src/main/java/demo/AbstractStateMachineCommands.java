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
package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

@Configuration
public class AbstractStateMachineCommands<S, E> {

	@Autowired
	private StateMachine<S, E> stateMachine;

	protected StateMachine<S, E> getStateMachine() {
		return stateMachine;
	}

	@Bean
	public Command state() {
		return new BasicCommand("state", "Prints current state") {
			public String execute(String[] args) {
				State<S, E> state = stateMachine.getState();
				if (state != null) {
					return StringUtils.collectionToCommaDelimitedString(state.getIds());
				} else {
					return "No state";
				}
			}
		};
	}

	@Bean
	public Command start() {
		return new BasicCommand("start", "Start a state machine") {
			public String execute(String[] args) {
				stateMachine.startReactively().subscribe();
				return "State machine started";
			}
		};
	}

	@Bean
	public Command stop() {
		return new BasicCommand("stop", "Stop a state machine") {
			public String execute(String[] args) {
				stateMachine.stopReactively().subscribe();
				return "State machine stopped";
			}
		};
	}

	@Bean
	public Command print() {
		return new BasicCommand("print", "Print state machine") {
			public String execute(String[] args) throws Exception {
				ClassPathResource model = new ClassPathResource("statechartmodel.txt");
				InputStream inputStream = model.getInputStream();
				Scanner scanner = new Scanner(inputStream);
				String content = scanner.useDelimiter("\\Z").next();
				scanner.close();
				return content;
			}
		};
	}

	@Bean
	public Command variables() {
		return new BasicCommand("variables", "Prints extended state variables") {
			public String execute(String[] args) {
				StringBuilder buf = new StringBuilder();
				Set<Entry<Object, Object>> entrySet = stateMachine.getExtendedState().getVariables().entrySet();
				Iterator<Entry<Object, Object>> iterator = entrySet.iterator();
				if (entrySet.size() > 0) {
					while (iterator.hasNext()) {
						Entry<Object, Object> e = iterator.next();
						buf.append(e.getKey() + "=" + e.getValue());
						if (iterator.hasNext()) {
							buf.append("\n");
						}
					}
				} else {
					buf.append("No variables");
				}
				return buf.toString();
			}
		};
	}
}
