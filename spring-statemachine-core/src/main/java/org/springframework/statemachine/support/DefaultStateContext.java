/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.support;

import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;

public class DefaultStateContext implements StateContext {
	
	private final MessageHeaders messageHeaders;
	
	private final ExtendedState extendedState;

	public DefaultStateContext(MessageHeaders messageHeaders, ExtendedState extendedState) {
		this.messageHeaders = messageHeaders;
		this.extendedState = extendedState;
	}

	@Override
	public MessageHeaders getMessageHeaders() {
		return messageHeaders;
	}

	@Override
	public ExtendedState getExtendedState() {
		return extendedState;
	}

}
