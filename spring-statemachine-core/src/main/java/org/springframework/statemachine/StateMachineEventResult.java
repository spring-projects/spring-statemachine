/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.statemachine;

import org.springframework.messaging.Message;
import org.springframework.statemachine.region.Region;

/**
 * Interface defining a result for sending an event to a statemachine.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public interface StateMachineEventResult<S, E> {

	/**
	 * Gets the region.
	 *
	 * @return the region
	 */
	Region<S, E> getRegion();

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	Message<E> getMessage();

	/**
	 * Gets the result type.
	 *
	 * @return the result type
	 */
	ResultType getResultType();

	/**
	 * Enumeration of a result type indicating whether a region accepted, denied or
	 * deferred an event.
	 */
	public enum ResultType {
		ACCEPTED,
		DENIED,
		DEFERRED
	}

	/**
	 * Create a {@link StateMachineEventResult} from a {@link Region}, {@link Message} and a {@link ResultType}.
	 *
	 * @param <S> the type of state
	 * @param <E> the type of event
	 * @param region the region
	 * @param message the message
	 * @param resultType the result type
	 * @return the state machine event result
	 */
	public static <S, E> StateMachineEventResult<S, E> from(Region<S, E> region, Message<E> message, ResultType resultType) {
		return new DefaultStateMachineEventResult<>(region, message, resultType);
	}

	static class DefaultStateMachineEventResult<S, E> implements StateMachineEventResult<S, E> {

		private final Region<S, E> region;
		private final Message<E> message;
		private final ResultType resultType;

		DefaultStateMachineEventResult(Region<S, E> region, Message<E> message, ResultType resultType) {
			this.region = region;
			this.message = message;
			this.resultType = resultType;
		}

		@Override
		public Region<S, E> getRegion() {
			return region;
		}

		@Override
		public Message<E> getMessage() {
			return message;
		}

		@Override
		public ResultType getResultType() {
			return resultType;
		}

		@Override
		public String toString() {
			return "DefaultStateMachineEventResult [region=" + region + ", message=" + message + ", resultType="
					+ resultType + "]";
		}
	}
}
