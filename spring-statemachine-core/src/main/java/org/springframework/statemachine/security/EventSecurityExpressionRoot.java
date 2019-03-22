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
package org.springframework.statemachine.security;

import org.springframework.messaging.Message;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;
import org.springframework.util.ObjectUtils;

/**
 * The {@link SecurityExpressionRoot} used for {@link Message} expressions.
 *
 * @author Rob Winch
 * @author Janne Valkealahti
 */
public class EventSecurityExpressionRoot extends SecurityExpressionRoot {

	public final Message<?> message;

	/**
	 * Instantiates a new event security expression root.
	 *
	 * @param authentication the authentication
	 * @param message the message
	 */
	public EventSecurityExpressionRoot(Authentication authentication, Message<?> message) {
		super(authentication);
		this.message = message;
	}

	public final boolean hasEvent(Object source) {
		return ObjectUtils.nullSafeEquals(source, message.getPayload());
	}

}
