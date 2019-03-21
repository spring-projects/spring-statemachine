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

import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.statemachine.transition.Transition;

/**
 * Votes if any {@link ConfigAttribute#getAttribute()} starts with a prefix indicating
 * that it is a transition source or target. The default prefixes strings are
 * <Code>TRANSITION_SOURCE</code> and <Code>TRANSITION_TARGET</code>, but
 * those may be overridden to any value. It may also be set to empty, which means that
 * essentially any attribute will be voted on. As described further below, the effect
 * of an empty prefix may not be quite desirable.
 * <p>
 * All comparisons and prefixes are case sensitive.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class TransitionVoter<S, E> implements AccessDecisionVoter<Transition<S, E>> {

	private String transitionSourcePrefix = "TRANSITION_SOURCE_";
	private String transitionTargetPrefix = "TRANSITION_TARGET_";

	@Override
	public boolean supports(ConfigAttribute attribute) {
		if ((attribute.getAttribute() != null) && (attribute.getAttribute().startsWith(getTransitionSourcePrefix())
				|| attribute.getAttribute().startsWith(getTransitionTargetPrefix()))) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return Transition.class.isAssignableFrom(clazz);
	}

	@Override
	public int vote(Authentication authentication, Transition<S, E> transition, Collection<ConfigAttribute> attributes) {
		int result = ACCESS_ABSTAIN;
		if (authentication == null) {
			return result;
		}

		S source = transition.getSource().getId();
		S target = transition.getTarget().getId();

		for (ConfigAttribute attribute : attributes) {
			if (this.supports(attribute)) {
				result = ACCESS_DENIED;
				String attr = attribute.getAttribute();
				if (attr.startsWith(getTransitionSourcePrefix())
						&& attr.equals(getTransitionSourcePrefix() + source.toString())) {
					return ACCESS_GRANTED;
				} else if (attr.startsWith(getTransitionTargetPrefix())
						&& attr.equals(getTransitionTargetPrefix() + target.toString())) {
					return ACCESS_GRANTED;
				}
			}
		}
		return result;
	}

	/**
	 * Gets the transition source prefix.
	 *
	 * @return the transition source prefix
	 */
	public String getTransitionSourcePrefix() {
		return transitionSourcePrefix;
	}

	/**
	 * Allows the default transition source prefix of <code>TRANSITION_SOURCE_</code> to
	 * be overridden. May be set to an empty value, although this is usually not desirable.
	 *
	 * @param transitionSourcePrefix the new transition source prefix
	 */
	public void setTransitionSourcePrefix(String transitionSourcePrefix) {
		this.transitionSourcePrefix = transitionSourcePrefix;
	}

	/**
	 * Gets the transition target prefix.
	 *
	 * @return the transition target prefix
	 */
	public String getTransitionTargetPrefix() {
		return transitionTargetPrefix;
	}

	/**
	 * Allows the default transition target prefix of <code>TRANSITION_TARGET_</code> to
	 * be overridden. May be set to an empty value, although this is usually not desirable.
	 *
	 * @param transitionTargetPrefix the new transition source prefix
	 */
	public void setTransitionTargetPrefix(String transitionTargetPrefix) {
		this.transitionTargetPrefix = transitionTargetPrefix;
	}

}
