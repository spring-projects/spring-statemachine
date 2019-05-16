/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
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
import org.springframework.messaging.Message;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachineException;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineFunction;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.AbstractCompositeListener;
import org.springframework.statemachine.recipes.support.RunnableAction;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptor;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.support.StateMachineUtils;
import org.springframework.statemachine.support.tree.Tree;
import org.springframework.statemachine.support.tree.Tree.Node;
import org.springframework.statemachine.support.tree.TreeTraverser;
import org.springframework.statemachine.transition.Transition;

/**
 * {@code TasksHandler} is a recipe for executing arbitrary {@link Runnable} tasks
 * using a state machine logic.
 *
 * This recipe supports execution of multiple top-level tasks with a
 * sub-states construct of DAGs.
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
	private final StateMachinePersist<String, String, Void> persist;

	/**
	 * Instantiates a new tasks handler. Intentionally private instantiation
	 * meant to be called from a builder.
	 *
	 * @param tasks the wrapped tasks
	 * @param listener the tasks listener
	 * @param taskExecutor the task executor
	 * @param persist the state machine persist
	 */
	private TasksHandler(List<TaskWrapper> tasks, TasksListener listener, TaskExecutor taskExecutor,
			StateMachinePersist<String, String, Void> persist) {
		this.persist = persist;
		try {
			stateMachine = buildStateMachine(tasks, taskExecutor);
			if (persist != null) {
				final LocalStateMachineInterceptor interceptor = new LocalStateMachineInterceptor(persist);
				stateMachine.getStateMachineAccessor()
					.doWithAllRegions(new StateMachineFunction<StateMachineAccess<String, String>>() {

					@Override
					public void apply(StateMachineAccess<String, String> function) {
						function.addStateMachineInterceptor(interceptor);
					}
				});
			}
		} catch (Exception e) {
			throw new StateMachineException("Error building state machine from tasks", e);
		}
		if (listener != null) {
			addTasksListener(listener);
		}
	}

	/**
	 * Request to execute current tasks logic.
	 */
	public void runTasks() {
		stateMachine.sendEvent(EVENT_RUN);
	}

	/**
	 * Request to continue from an error.
	 */
	public void continueFromError() {
		stateMachine.sendEvent(EVENT_CONTINUE);
	}

	/**
	 * Request to fix current problems.
	 */
	public void fixCurrentProblems() {
		stateMachine.sendEvent(EVENT_FIX);
	}

	/**
	 * Resets state machine states from a backing persistent repository. If
	 * {@link StateMachinePersist} is not set this method doesn't do anything.
	 * {@link StateMachine} is stopped before states are reseted from a persistent
	 * store and started afterwards.
	 */
	public void resetFromPersistStore() {
		if (persist == null) {
			// TODO: should we throw or silently return?
			return;
		}

		final StateMachineContext<String, String> context;
		try {
			context = persist.read(null);
		} catch (Exception e) {
			throw new StateMachineException("Error reading state from persistent store", e);
		}

		stateMachine.stop();
		stateMachine.getStateMachineAccessor()
			.doWithAllRegions(new StateMachineFunction<StateMachineAccess<String, String>>() {

			@Override
			public void apply(StateMachineAccess<String, String> function) {
				function.resetStateMachine(context);
			}
		});
		stateMachine.start();
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

	/**
	 * Mark all extended state variables related to tasks fixed.
	 */
	public void markAllTasksFixed() {
		Map<Object, Object> variables = getStateMachine().getExtendedState().getVariables();
		for (Entry<Object, Object> entry : variables.entrySet()) {
			if (entry.getKey() instanceof String && ((String)entry.getKey()).startsWith(STATE_TASKS_PREFIX)) {
				if (entry.getValue() instanceof Integer) {
					Integer value = (Integer) entry.getValue();
					if (value < 0) {
						variables.put(entry.getKey(), 0);
					}
				}
			}
		}
	}

	private StateMachine<String, String> buildStateMachine(List<TaskWrapper> tasks, TaskExecutor taskExecutor)
			throws Exception {
		StateMachineBuilder.Builder<String, String> builder = StateMachineBuilder.builder();

		int taskCount = topLevelTaskCount(tasks);

		builder.configureConfiguration().withConfiguration()
			.taskExecutor(taskExecutor != null ? taskExecutor : taskExecutor(taskCount));

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

			if (node.getChildren().isEmpty()) {
				joinStates.add(task);
			}

			stateMachineTransitionConfigurer
				.withExternal()
					.state(parent)
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
				.action(continueAction())
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

	private static TaskExecutor taskExecutor(int taskCount) {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.afterPropertiesSet();
		taskExecutor.setCorePoolSize(taskCount);
		return taskExecutor;
	}

	private static int topLevelTaskCount(List<TaskWrapper> tasks) {
		Tree<TaskWrapper> tree = new Tree<TaskWrapper>();
		for (TaskWrapper wrapper : tasks) {
			tree.add(wrapper, wrapper.id, wrapper.parent);
		}
		return tree.getRoot().getChildren().size();
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
		private TasksListener listener;
		private TaskExecutor taskExecutor;
		private StateMachinePersist<String, String, Void> persist;

		/**
		 * Define a top-level task.
		 *
		 * @param id the id
		 * @param runnable the runnable
		 * @return the builder for chaining
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
		 * @return the builder for chaining
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
		 * @return the builder for chaining
		 */
		public Builder persist(StateMachinePersist<String, String, Void> persist) {
			this.persist = persist;
			return this;
		}

		/**
		 * Define a {@link TasksListener} to be registered.
		 *
		 * @param listener the tasks listener
		 * @return the builder for chaining
		 */
		public Builder listener(TasksListener listener) {
			this.listener = listener;
			return this;
		}

		/**
		 * Define a {@link TaskExecutor} to be used. Default executor will be
		 * a {@link ThreadPoolTaskExecutor} set with a thread pool size of
		 * a top-level task count.
		 *
		 * @param taskExecutor the task executor
		 * @return the builder for chaining
		 */
		public Builder taskExecutor(TaskExecutor taskExecutor) {
			this.taskExecutor = taskExecutor;
			return this;
		}

		/**
		 * Builds the {@link TasksHandler}.
		 *
		 * @return the tasks handler
		 */
		public TasksHandler build() {
			return new TasksHandler(tasks, listener, taskExecutor, persist);
		}

	}

	/**
	 * Gets a tasks entry action.
	 *
	 * @return the tasks entry action
	 */
	private TasksEntryAction tasksEntryAction() {
		return new TasksEntryAction();
	}

	/**
	 * Gets a local runnable action.
	 *
	 * @param runnable the runnable
	 * @param id the task id
	 * @return the local runnable action
	 */
	private LocalRunnableAction runnableAction(Runnable runnable, String id) {
		return new LocalRunnableAction(runnable, id);
	}

	/**
	 * Tasks choice guard. This {@link Guard} will check if related
	 * extended state variables contains negative values for related
	 * tasks id's and returns true if so, else false.
	 *
	 * @return the guard
	 */
	private Guard<String, String> tasksChoiceGuard() {
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
								listener.onTasksError();
								return true;
							}
						}
					}
				}
				listener.onTasksSuccess();
				return false;
			}
		};
	}

	/**
	 * {@link Action} which simply sends an event of continue
	 * tasks into a state machine.
	 *
	 * @return the action
	 */
	private Action<String, String> continueAction() {
		return new Action<String, String>() {

			@Override
			public void execute(StateContext<String, String> context) {
				listener.onTasksContinue();
			}
		};
	}

	/**
	 * {@link Action} calls {@link TasksListener#onTasksAutomaticFix(StateContext)}
	 * before checking status of extended state variables related to tasks. If all
	 * variables are ok, event {@code EVENT_CONTINUE} is sent, otherwise event
	 * {@code EVENT_FALLBACK} is send which takes state machine into a manual handling.
	 *
	 * @return the action
	 */
	private Action<String, String> automaticAction() {
		return new Action<String, String>() {

			@Override
			public void execute(StateContext<String, String> context) {

				listener.onTasksAutomaticFix(TasksHandler.this, context);

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
					context.getStateMachine().sendEvent(EVENT_FALLBACK);
				} else {
					context.getStateMachine().sendEvent(EVENT_CONTINUE);
				}
			}
		};
	}

	/**
	 * {@link Action} which resets related extended state variables
	 * to zero for tasks order to indicate a fixed tasks.
	 *
	 * @return the action
	 */
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
								variables.put(entry.getKey(), 0);
							}
						}
					}
				}
			}
		};
	}

	/**
	 * Adapter class for {@link TasksListener}.
	 */
	public static class TasksListenerAdapter implements TasksListener {

		@Override
		public void onTasksStarted() {
		}

		@Override
		public void onTasksContinue() {
		}

		@Override
		public void onTaskPreExecute(Object id) {
		}

		@Override
		public void onTaskPostExecute(Object id) {
		}

		@Override
		public void onTaskFailed(Object id, Exception exception) {
		}

		@Override
		public void onTaskSuccess(Object id) {
		}

		@Override
		public void onTasksSuccess() {
		}

		@Override
		public void onTasksError() {
		}

		@Override
		public void onTasksAutomaticFix(TasksHandler handler, StateContext<String, String> context) {
		}

	}

	/**
	 * {@code TasksListener} is a generic interface listening tasks
	 * execution events. Methods in this interface will be called in a
	 * tasks execution position where user most likely will want to get
	 * notified.
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
		 * Called when task execution result an error of any kind.
		 *
		 * @param id the task id
		 * @param exception the exception
		 */
		void onTaskFailed(Object id, Exception exception);

		/**
		 * Called when task execution result without errors.
		 *
		 * @param id the task id
		 */
		void onTaskSuccess(Object id);

		/**
		 * Called when all tasks has been executed successfully.
		 */
		void onTasksSuccess();

		/**
		 * Called when after an execution of full DAGs if some of the
		 * tasks executed with an error.
		 */
		void onTasksError();

		/**
		 * Called when tasks execution resulted an error and AUTOMATIC state
		 * is entered. This is a moment where extended state variables can be
		 * modified to allow continue into a READY state.
		 *
		 * @param handler the tasks handler
		 * @param context the state context
		 */
		void onTasksAutomaticFix(TasksHandler handler, StateContext<String, String> context);
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
		public void onTaskSuccess(Object id) {
			for (Iterator<TasksListener> iterator = getListeners().reverse(); iterator.hasNext();) {
				iterator.next().onTaskSuccess(id);
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

		@Override
		public void onTasksAutomaticFix(TasksHandler handler, StateContext<String, String> context) {
			for (Iterator<TasksListener> iterator = getListeners().reverse(); iterator.hasNext();) {
				iterator.next().onTasksAutomaticFix(handler, context);
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
	 * {@link Action} which is executed with every registered {@link Runnable}.
	 */
	private class LocalRunnableAction extends RunnableAction {

		public LocalRunnableAction(Runnable runnable, String id) {
			super(runnable, id);
		}

		@Override
		protected boolean shouldExecute(String id, StateContext<String, String> context) {
			return super.shouldExecute(id, context);
		}

		@Override
		protected void onPreExecute(String id, StateContext<String, String> context) {
			listener.onTaskPreExecute(id);
		}

		@Override
		protected void onPostExecute(String id, StateContext<String, String> context) {
			listener.onTaskPostExecute(id);
		}

		@Override
		protected void onSuccess(String id, StateContext<String, String> context) {
			listener.onTaskSuccess(id);
			changeCount(1, context);
		}

		@Override
		protected void onError(String id, StateContext<String, String> context, Exception e) {
			listener.onTaskFailed(id, e);
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
	 * Local {@link StateMachineInterceptor} persisting state machine states.
	 */
	private class LocalStateMachineInterceptor extends StateMachineInterceptorAdapter<String, String> {

		// TODO: should try to find a common way to build context and
		//       not do tweaks here.
		private final StateMachinePersist<String, String, Void> persist;
		private DefaultStateMachineContext<String, String> currentContext;
		private State<String, String> currentContextState;
		private final List<StateMachineContext<String, String>> childs = new ArrayList<StateMachineContext<String, String>>();

		public LocalStateMachineInterceptor(StateMachinePersist<String, String, Void> persist) {
			this.persist = persist;
		}

		@Override
		public void preStateChange(State<String, String> state, Message<String> message,
				Transition<String, String> transition, StateMachine<String, String> stateMachine) {

			// skip all other pseudostates than initial
			if (state == null || (state.getPseudoState() != null && state.getPseudoState().getKind() != PseudoStateKind.INITIAL)) {
				return;
			}

			// track root state here and update childs
			if (currentContext != null && StateMachineUtils.isSubstate(currentContextState, state)) {
				DefaultStateMachineContext<String, String> context = new DefaultStateMachineContext<String, String>(
						transition != null ? transition.getTarget().getId() : null, message != null ? message.getPayload()
								: null, message != null ? message.getHeaders() : null, stateMachine.getExtendedState());
				currentContext.getChilds().add(context);
			} else {
				childs.clear();
				DefaultStateMachineContext<String, String> context = new DefaultStateMachineContext<String, String>(
						new ArrayList<StateMachineContext<String, String>>(childs), state.getId(), message != null ? message.getPayload()
								: null, message != null ? message.getHeaders() : null, stateMachine.getExtendedState());
				currentContext = context;
				currentContextState = state;
			}

			try {
				persist.write(currentContext, null);
			} catch (Exception e) {
				throw new StateMachineException("Error persisting", e);
			}
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
