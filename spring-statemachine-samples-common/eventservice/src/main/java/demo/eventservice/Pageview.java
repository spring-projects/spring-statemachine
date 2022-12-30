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
package demo.eventservice;

import demo.eventservice.StateMachineConfig.Events;

public class Pageview {

	private String user;
	private Events id;

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public Events getId() {
		return id;
	}

	public void setId(Events id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Pageview [user=" + user + ", id=" + id + "]";
	}

}
