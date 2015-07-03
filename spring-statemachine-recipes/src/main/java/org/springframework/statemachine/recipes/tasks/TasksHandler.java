/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.recipes.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineException;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.ensemble.StateMachinePersist;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.AbstractCompositeListener;
import org.springframework.statemachine.recipes.support.RunnableAction;
import org.springframework.statemachine.support.tree.Tree;
import org.springframework.statemachine.support.tree.Tree.Node;
import org.springframework.statemachine.support.tree.TreeTraverser;

/**
 * {@code TasksHandler} is a recipe for executing {@link Runnable} tasks
 * using a state machine logic.
 *
 *
 *
 * @author Janne Valkealahti
 *
 */
public class TasksHandler {

	private final static Log log = LogFactory.getLog(TasksHandler.class);

	public final static String STATE_READY = "READY";
	public final static String STATE_FORK = "FORK";
	public final static String STATE_TASKS = "TASKS";
	public final static String STATE_JOIN = "JOIN";
	public final static String STATE_CHOICE = "CHOICE";
	public final static String STATE_ERROR = "ERROR";
	public final static String STATE_AUTOMATIC = "AUTOMATIC";
	public final static String STATE_MANUAL = "MANUAL";

	public final static String STATE_TASKS_PREFIX = "TASK_";
	public final static String STATE_TASKS_INITIAL_POSTFIX = "_INITIAL";

	public final static String EVENT_RUN = "RUN";
	public final static String EVENT_FALLBACK = "FALLBACK";
	public final static String EVENT_CONTINUE = "CONTINUE";
	public final static String EVENT_FIX = "FIX";

	private StateMachine<String, String> stateMachine;
	private final CompositeTasksListener listener = new CompositeTasksListener();

	/**
	 * Instantiates a new tasks handler. Intentionally private instantiation
	 * meant to be called from builder.
	 *
	 * @param tasks the wrapped tasks
	 */
	private TasksHandler(List<TaskWrapper> tasks) {
		try {
			this.stateMachine = buildStateMachine(tasks);
		} catch (Exception e) {
			throw new StateMachineException("Error building state machine from tasks", e);
		}
	}

	public void runTasks() {
		stateMachine.sendEvent(EVENT_RUN);
	}

	public void continueFromError() {
		stateMachine.sendEvent(EVENT_CONTINUE);
	}

	public void fixCurrentProblems() {
		stateMachine.sendEvent(EVENT_FIX);
	}

	/**
	 * Adds the tasks listener.
	 *
	 * @param listener the listener
	 */
	public void addTasksListener(TasksListener listener) {
		this.listener.register(listener);
	}

	/**
	 * Removes the tasks listener.
	 *
	 * @param listener the listener
	 */
	public void removeTasksListener(TasksListener listener) {
		this.listener.unregister(listener);
	}

	/**
	 * Gets the internal state machine used by executing tasks.
	 *
	 * @return the state machine
	 */
	public StateMachine<String, String> getStateMachine() {
		return stateMachine;
	}

	/**
	 * Gets a new instance of a {@link Builder} which is used to build
	 * an instance of a {@code TasksHandler}.
	 *
	 * @return the tasks handler builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	private StateMachine<String, String> buildStateMachine(List<TaskWrapper> tasks) throws Exception {
		StateMachineBuilder.Builder<String, String> builder = StateMachineBuilder.builder();

		builder.configureConfiguration().withConfiguration()
			.taskExecutor(taskExecutor());

		StateMachineStateConfigurer<String, String> stateMachineStateConfigurer = builder.configureStates();
		StateMachineTransitionConfigurer<String, String> stateMachineTransitionConfigurer = builder.configureTransitions();

		stateMachineStateConfigurer
			.withStates()
				.initial(STATE_READY)
				.fork(STATE_FORK)
				.state(STATE_TASKS, tasksEntryAction(), null)
				.join(STATE_JOIN)
				.choice(STATE_CHOICE)
				.state(STATE_ERROR);

		stateMachineTransitionConfigurer
			.withExternal()
				.source(STATE_READY).target(STATE_FORK).event(EVENT_RUN)
				.and()
			.withFork()
				.source(STATE_FORK).target(STATE_TASKS);

		Iterator<Node<TaskWrapper>> iterator = buildTasksIterator(tasks);
		String parent = null;
		Collection<String> joinStates = new ArrayList<String>();
		while (iterator.hasNext()) {
			Node<TaskWrapper> node = iterator.next();
			if (node.getData() == null) {
				break;
			}
			String initial = STATE_TASKS_PREFIX + node.getData().id.toString() + STATE_TASKS_INITIAL_POSTFIX;
			String task = STATE_TASKS_PREFIX + node.getData().id.toString();
			parent = node.getData().parent != null ? STATE_TASKS_PREFIX + node.getData().parent.toString() : STATE_TASKS;

			stateMachineStateConfigurer
				.withStates()
					.parent(parent)
					.initial(initial)
					.state(task, runnableAction(node.getData().runnable, node.getData().id.toString()), null);

			joinStates.add(task);

			stateMachineTransitionConfigurer
				.withExternal()
					.source(initial)
					.target(task);
		}

		stateMachineStateConfigurer
			.withStates()
				.parent(STATE_ERROR)
				.initial(STATE_AUTOMATIC)
				.state(STATE_AUTOMATIC, automaticAction(), null)
				.state(STATE_MANUAL);

		stateMachineTransitionConfigurer
			.withJoin()
				.sources(joinStates)
				.target(STATE_JOIN)
				.and()
			.withExternal()
				.source(STATE_JOIN).target(STATE_CHOICE)
				.and()
			.withChoice()
				.source(STATE_CHOICE)
				.first(STATE_ERROR, tasksChoiceGuard())
				.last(STATE_READY)
				.and()
			.withExternal()
				.source(STATE_ERROR).target(STATE_READY)
				.event(EVENT_CONTINUE)
				.and()
			.withExternal()
				.source(STATE_AUTOMATIC).target(STATE_MANUAL)
				.event(EVENT_FALLBACK)
				.and()
			.withInternal()
				.source(STATE_MANUAL)
				.action(fixAction())
				.event(EVENT_FIX);

		return builder.build();
	}

	private static TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.afterPropertiesSet();
		taskExecutor.setCorePoolSize(5);
		return taskExecutor;
	}

	private static Iterator<Node<TaskWrapper>> buildTasksIterator(List<TaskWrapper> tasks) {
		Tree<TaskWrapper> tree = new Tree<TaskWrapper>();
		for (TaskWrapper wrapper : tasks) {
			tree.add(wrapper, wrapper.id, wrapper.parent);
		}

		TreeTraverser<Node<TaskWrapper>> traverser = new TreeTraverser<Node<TaskWrapper>>() {
		    @Override
		    public Iterable<Node<TaskWrapper>> children(Node<TaskWrapper> root) {
		        return root.getChildren();
		    }
		};


		Iterable<Node<TaskWrapper>> postOrderTraversal = traverser.postOrderTraversal(tree.getRoot());
		Iterator<Node<TaskWrapper>> iterator = postOrderTraversal.iterator();
		return iterator;
	}

	/**
	 * Builder pattern implementation building a {@link TasksHandler}.
	 */
	public static class Builder {

		private final List<TaskWrapper> tasks = new ArrayList<TaskWrapper>();

		/**
		 * Define a top-level task.
		 *
		 * @param id the id
		 * @param runnable the runnable
		 * @return the builder
		 */
		public Builder task(Object id, Runnable runnable) {
			tasks.add(new TaskWrapper(null, id, runnable));
			return this;
		}

		/**
		 * Define a sub-task with a reference to its parent.
		 *
		 * @param parent the parent
		 * @param id the id
		 * @param runnable the runnable
		 * @return the builder
		 */
		public Builder task(Object parent, Object id, Runnable runnable) {
			tasks.add(new TaskWrapper(parent, id, runnable));
			return this;
		}

		/**
		 * Define a {@link StateMachinePersist} implementation if state machine
		 * should be persisted with state changes.
		 *
		 * @param persist the persist
		 * @return the builder
		 */
		public Builder persist(StateMachinePersist<String, String, Void> persist) {
			return this;
		}

		/**
		 * Builds the {@link TasksHandler}.
		 *
		 * @return the tasks handler
		 */
		public TasksHandler build() {
			return new TasksHandler(tasks);
		}

	}

	private TasksEntryAction tasksEntryAction() {
		return new TasksEntryAction();
	}

	private static LocalRunnableAction runnableAction(Runnable runnable, String id) {
		return new LocalRunnableAction(runnable, id);
	}

	private static Guard<String, String> tasksChoiceGuard() {
		return new Guard<String, String>() {

			@Override
			public boolean evaluate(StateContext<String, String> context) {
				Map<Object, Object> variables = context.getExtendedState().getVariables();
				for (Entry<Object, Object> entry : variables.entrySet()) {
					if (entry.getKey() instanceof String && ((String)entry.getKey()).startsWith(STATE_TASKS_PREFIX)) {
						if (entry.getValue() instanceof Integer) {
							Integer value = (Integer) entry.getValue();
							if (value < 0) {
								if (log.isDebugEnabled()) {
									log.debug("Task id=[" + entry.getKey() + "] has negative execution value, tasksChoiceGuard returns true");
								}
								return true;
							}
						}
					}
				}
				return false;
			}
		};
	}

	private Action<String, String> automaticAction() {
		return new Action<String, String>() {

			@Override
			public void execute(StateContext<String, String> context) {
			}
		};
	}

	private Action<String, String> fixAction() {
		return new Action<String, String>() {

			@Override
			public void execute(StateContext<String, String> context) {
				Map<Object, Object> variables = context.getExtendedState().getVariables();
				for (Entry<Object, Object> entry : variables.entrySet()) {
					if (entry.getKey() instanceof String && ((String)entry.getKey()).startsWith(STATE_TASKS_PREFIX)) {
						if (entry.getValue() instanceof Integer) {
							Integer value = (Integer) entry.getValue();
							if (value < 0) {
								variables.put(entry.getValue(), 0);
							}
						}
					}
				}
			}
		};
	}

	/**
	 * {@code TasksListener} is a generic interface listening tasks
	 * execution events.
	 */
	public interface TasksListener {

		/**
		 * Called when all DAGs have either never executed or previous
		 * execution was fully successful.
		 */
		void onTasksStarted();

		/**
		 * Called when some of a tasks in DAGs failed to execute and tasks
		 * execution in going to continue.
		 */
		void onTasksContinue();

		/**
		 * Called before tasks is about to be executed.
		 *
		 * @param id the task id
		 */
		void onTaskPreExecute(Object id);

		/**
		 * Called after tasks has been executed regardless if task
		 * execution succeeded or not.
		 *
		 * @param id the task id
		 */
		void onTaskPostExecute(Object id);

		/**
		 * Called when task execution resulter an error of any kind.
		 *
		 * @param id the task id
		 * @param exception the exception
		 */
		void onTaskFailed(Object id, Exception exception);

		/**
		 * Called when all tasks has been executed successfully.
		 */
		void onTasksSuccess();

		/**
		 * Called when after an execution of full DAGs if some of the
		 * tasks executed with an error.
		 */
		void onTasksError();
	}

	private class CompositeTasksListener extends AbstractCompositeListener<TasksListener> implements
		TasksListener {

		@Override
		public void onTasksStarted() {
			for (Iterator<TasksListener> iterator = getListeners().reverse(); iterator.hasNext();) {
				iterator.next().onTasksStarted();
			}
		}

		@Override
		public void onTasksContinue() {
			for (Iterator<TasksListener> iterator = getListeners().reverse(); iterator.hasNext();) {
				iterator.next().onTasksContinue();
			}
		}

		@Override
		public void onTaskPreExecute(Object id) {
			for (Iterator<TasksListener> iterator = getListeners().reverse(); iterator.hasNext();) {
				iterator.next().onTaskPreExecute(id);
			}
		}

		@Override
		public void onTaskPostExecute(Object id) {
			for (Iterator<TasksListener> iterator = getListeners().reverse(); iterator.hasNext();) {
				iterator.next().onTaskPostExecute(id);
			}
		}

		@Override
		public void onTaskFailed(Object id, Exception exception) {
			for (Iterator<TasksListener> iterator = getListeners().reverse(); iterator.hasNext();) {
				iterator.next().onTaskFailed(id, exception);
			}
		}

		@Override
		public void onTasksSuccess() {
			for (Iterator<TasksListener> iterator = getListeners().reverse(); iterator.hasNext();) {
				iterator.next().onTasksSuccess();
			}
		}

		@Override
		public void onTasksError() {
			for (Iterator<TasksListener> iterator = getListeners().reverse(); iterator.hasNext();) {
				iterator.next().onTasksError();
			}
		}

	}

	/**
	 * {@link Action} which is executed when TASKS state is entered.
	 */
	private class TasksEntryAction implements Action<String, String> {

		@Override
		public void execute(StateContext<String, String> context) {
			boolean hasErrors = false;
			Map<Object, Object> variables = context.getExtendedState().getVariables();
			for (Entry<Object, Object> entry : variables.entrySet()) {
				if (entry.getKey() instanceof String && ((String)entry.getKey()).startsWith(STATE_TASKS_PREFIX)) {
					if (entry.getValue() instanceof Integer) {
						Integer value = (Integer) entry.getValue();
						if (value < 0) {
							hasErrors = true;
							break;
						}
					}
				}
			}
			if (hasErrors) {
				listener.onTasksContinue();
			} else {
				listener.onTasksStarted();
			}
		}

	}

	/**
	 * {@link Action} which is execution with every registered {@link Runnable}.
	 */
	private static class LocalRunnableAction extends RunnableAction {

		public LocalRunnableAction(Runnable runnable, String id) {
			super(runnable, id);
		}

		@Override
		protected boolean shouldExecute(StateContext<String, String> context) {
			return super.shouldExecute(context);
		}

		@Override
		protected void onSuccess(StateContext<String, String> context) {
			changeCount(1, context);
		}

		@Override
		protected void onError(StateContext<String, String> context, Exception e) {
			changeCount(-1, context);
		}

		private void changeCount(int delta, StateContext<String, String> context) {
			Map<Object, Object> variables = context.getExtendedState().getVariables();
			Integer count;
			String key = STATE_TASKS_PREFIX + getId();
			if (variables.containsKey(key)) {
				count = (Integer) variables.get(key);
			} else {
				count = 0;
			}
			count =+ delta;
			variables.put(key, count);
		}

	}

	/**
	 * Wrapping a {@link Runnable} with a task identifier and parent if task
	 * is a subtask. If parent is null it indicates that a task is a top-level
	 * task with optional child tasks creating a dag task graph.
	 */
	private static class TaskWrapper {
		final Object parent;
		final Object id;
		final Runnable runnable;

		public TaskWrapper(Object parent, Object id, Runnable runnable) {
			this.parent = parent;
			this.id = id;
			this.runnable = runnable;
		}

	}

}
