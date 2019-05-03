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
package org.springframework.statemachine.security;

import java.util.Collection;

import org.springframework.messaging.Message;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

/**
 * Votes if any {@link ConfigAttribute#getAttribute()} starts with a prefix indicating
 * that it is an event. The default prefix is <Code>EVENT</code>, but
 * it may be overridden to any value. It may also be set to empty, which means that
 * essentially any attribute will be voted on. As described further below, the effect
 * of an empty prefix may not be quite desirable.
 * <p>
 * All comparisons and prefixes are case sensitive.
 *
 * @author Janne Valkealahti
 *
 * @param <T> the message type
 */
public class EventVoter<T> implements AccessDecisionVoter<Message<T>>{

	private String eventPrefix = "EVENT_";

	@Override
	public boolean supports(ConfigAttribute attribute) {
		if ((attribute.getAttribute() != null) && attribute.getAttribute().startsWith(getEventPrefix())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return Message.class.isAssignableFrom(clazz);
	}

	@Override
	public int vote(Authentication authentication, Message<T> event, Collection<ConfigAttribute> attributes) {
		int result = ACCESS_ABSTAIN;
		if (authentication == null) {
			return result;
		}

		T e = event.getPayload();

		for (ConfigAttribute attribute : attributes) {
			if (this.supports(attribute)) {
				result = ACCESS_DENIED;
				String attr = attribute.getAttribute();
				if (attr.startsWith(getEventPrefix()) && attr.equals(getEventPrefix() + e.toString())) {
					return ACCESS_GRANTED;
				}
			}
		}
		return result;
	}

	/**
	 * Gets the event prefix.
	 *
	 * @return the event prefix
	 */
	public String getEventPrefix() {
		return eventPrefix;
	}

}
