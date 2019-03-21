/*
 * Copyright 2015-2019 the original author or authors.
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
package org.springframework.statemachine.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachineContext;

/**
 * Default implementation of a {@link StateMachineContext}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class DefaultStateMachineContext<S, E> implements StateMachineContext<S, E> {

	private final String id;
	private final List<StateMachineContext<S, E>> childs;
	private final List<String> childRefs;
	private final S state;
	private final Map<S, S> historyStates;
	private final E event;
	private final Map<String, Object> eventHeaders;
	private final ExtendedState extendedState;

	/**
	 * Instantiates a new default state machine context.
	 *
	 * @param state the state
	 * @param event the event
	 * @param eventHeaders the event headers
	 * @param extendedState the extended state
	 */
	public DefaultStateMachineContext(S state, E event, Map<String, Object> eventHeaders, ExtendedState extendedState) {
		this(new ArrayList<StateMachineContext<S, E>>(), state, event, eventHeaders, extendedState);
	}

	/**
	 * Instantiates a new default state machine context.
	 *
	 * @param state the state
	 * @param event the event
	 * @param eventHeaders the event headers
	 * @param extendedState the extended state
	 * @param historyStates the history state mappings
	 */
	public DefaultStateMachineContext(S state, E event, Map<String, Object> eventHeaders, ExtendedState extendedState,
			Map<S, S> historyStates) {
		this(new ArrayList<StateMachineContext<S, E>>(), state, event, eventHeaders, extendedState, historyStates);
	}

	/**
	 * Instantiates a new default state machine context.
	 *
	 * @param state the state
	 * @param event the event
	 * @param eventHeaders the event headers
	 * @param extendedState the extended state
	 * @param historyStates the history state mappings
	 * @param id the machine id
	 */
	public DefaultStateMachineContext(S state, E event, Map<String, Object> eventHeaders, ExtendedState extendedState,
			Map<S, S> historyStates, String id) {
		this(new ArrayList<StateMachineContext<S, E>>(), state, event, eventHeaders, extendedState, historyStates, id);
	}

	/**
	 * Instantiates a new default state machine context.
	 *
	 * @param childs the child state machine contexts
	 * @param state the state
	 * @param event the event
	 * @param eventHeaders the event headers
	 * @param extendedState the extended state
	 */
	public DefaultStateMachineContext(List<StateMachineContext<S, E>> childs, S state, E event,
			Map<String, Object> eventHeaders, ExtendedState extendedState) {
		this(childs, state, event, eventHeaders, extendedState, null);
	}

	/**
	 * Instantiates a new default state machine context.
	 *
	 * @param childs the child state machine contexts
	 * @param state the state
	 * @param event the event
	 * @param eventHeaders the event headers
	 * @param extendedState the extended state
	 * @param historyStates the history state mappings
	 */
	public DefaultStateMachineContext(List<StateMachineContext<S, E>> childs, S state, E event,
			Map<String, Object> eventHeaders, ExtendedState extendedState, Map<S, S> historyStates) {
		this(childs, state, event, eventHeaders, extendedState, historyStates, null);
	}

	/**
	 * Instantiates a new default state machine context.
	 *
	 * @param childs the child state machine contexts
	 * @param state the state
	 * @param event the event
	 * @param eventHeaders the event headers
	 * @param extendedState the extended state
	 * @param historyStates the history state mappings
	 * @param id the machine id
	 */
	public DefaultStateMachineContext(List<StateMachineContext<S, E>> childs, S state, E event,
			Map<String, Object> eventHeaders, ExtendedState extendedState, Map<S, S> historyStates, String id) {
		this.childs = childs;
		this.childRefs = null;
		this.state = state;
		this.event = event;
		this.eventHeaders = eventHeaders;
		this.extendedState = extendedState;
		this.historyStates = historyStates != null ? historyStates : new HashMap<S, S>();
		this.id = id;
	}

	/**
	 * Instantiates a new default state machine context.
	 *
	 * @param childRefs the child state machine context refs
	 * @param childs the child state machine contexts
	 * @param state the state
	 * @param event the event
	 * @param eventHeaders the event headers
	 * @param extendedState the extended state
	 * @param historyStates the history state mappings
	 * @param id the machine id
	 */
	public DefaultStateMachineContext(List<String> childRefs, List<StateMachineContext<S, E>> childs, S state, E event,
			Map<String, Object> eventHeaders, ExtendedState extendedState, Map<S, S> historyStates, String id) {
		this.childs = childs;
		this.childRefs = childRefs;
		this.state = state;
		this.event = event;
		this.eventHeaders = eventHeaders;
		this.extendedState = extendedState;
		this.historyStates = historyStates != null ? historyStates : new HashMap<S, S>();
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public List<StateMachineContext<S, E>> getChilds() {
		return childs;
	}

	@Override
	public List<String> getChildReferences() {
		return childRefs;
	}

	@Override
	public S getState() {
		return state;
	}

	@Override
	public Map<S, S> getHistoryStates() {
		return historyStates;
	}

	@Override
	public E getEvent() {
		return event;
	}

	@Override
	public Map<String, Object> getEventHeaders() {
		return eventHeaders;
	}

	@Override
	public ExtendedState getExtendedState() {
		return extendedState;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((childRefs == null) ? 0 : childRefs.hashCode());
		result = prime * result + ((childs == null) ? 0 : childs.hashCode());
		result = prime * result + ((event == null) ? 0 : event.hashCode());
		result = prime * result + ((eventHeaders == null) ? 0 : eventHeaders.hashCode());
		result = prime * result + ((extendedState == null) ? 0 : extendedState.hashCode());
		result = prime * result + ((historyStates == null) ? 0 : historyStates.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		DefaultStateMachineContext<?, ?> other = (DefaultStateMachineContext<?, ?>) obj;
		if (childRefs == null) {
			if (other.childRefs != null) {
				return false;
			}
		} else if (!childRefs.equals(other.childRefs)) {
			return false;
		}
		if (childs == null) {
			if (other.childs != null) {
				return false;
			}
		} else if (!childs.equals(other.childs)) {
			return false;
		}
		if (event == null) {
			if (other.event != null) {
				return false;
			}
		} else if (!event.equals(other.event)) {
			return false;
		}
		if (eventHeaders == null) {
			if (other.eventHeaders != null) {
				return false;
			}
		} else if (!eventHeaders.equals(other.eventHeaders)) {
			return false;
		}
		if (extendedState == null) {
			if (other.extendedState != null) {
				return false;
			}
		} else if (!extendedState.equals(other.extendedState)) {
			return false;
		}
		if (historyStates == null) {
			if (other.historyStates != null) {
				return false;
			}
		} else if (!historyStates.equals(other.historyStates)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (state == null) {
			if (other.state != null) {
				return false;
			}
		} else if (!state.equals(other.state)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "DefaultStateMachineContext [id=" + id + ", childs=" + childs + ", childRefs=" + childRefs + ", state="
				+ state + ", historyStates=" + historyStates + ", event=" + event + ", eventHeaders=" + eventHeaders
				+ ", extendedState=" + extendedState + "]";
	}
}
