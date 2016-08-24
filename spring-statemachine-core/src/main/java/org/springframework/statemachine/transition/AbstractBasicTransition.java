package org.springframework.statemachine.transition;

import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.action.Actions;
import org.springframework.statemachine.state.State;

import java.util.Arrays;
import java.util.Collection;

public abstract class AbstractBasicTransition<S, E> {
	protected final State<S, E> target;
	protected final Collection<Action<S, E>> actions;
	protected final Action<S, E> errorAction;

	/**
	 * Instantiates a new initial transition.
	 *
	 * @param target the initial target state
	 */
	public AbstractBasicTransition(State<S, E> target) {
		this(target, null, null);
	}

	/**
	 * Instantiates a new initial transition.
	 *
	 * @param target the initial target state
	 * @param action the initial action
	 */
	public AbstractBasicTransition(State<S, E> target, Action<S, E> action) {
		this(target, action == null ? null : Arrays.asList(action), null);
	}

	/**
	 * Instantiates a new initial transition.
	 *
	 * @param target the initial target state
	 * @param actions the initial actions
	 */
	public AbstractBasicTransition(State<S, E> target, Collection<Action<S, E>> actions,
			Action<S, E> errorAction) {
		this.target = target;
		this.actions = actions;
		this.errorAction = errorAction == null ? Actions.<S, E> emptyAction()
				: errorAction;
	}

	public State<S, E> getTarget() {
		return target;
	}

	public Collection<Action<S, E>> getActions() {
		return actions;
	}

	public Action<S, E> getErrorAction() {
		return errorAction;
	}

	protected final void executeAllActions(StateContext<S, E> context) {
		if (actions == null) {
		    return;
		}

		for (Action<S, E> action : actions) {
			try {
				action.execute(context);
			}
			catch (Exception exception) {
				errorAction.execute(context); // notify something wrong is happening in
												// Actions execution.
				throw exception;
			}
		}
	}
}
