package demo.tasks;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.statemachine.EnumStateMachine;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

import demo.CommonConfiguration;
import demo.tasks.Application.Events;
import demo.tasks.Application.States;

public class TasksTests {

	private AnnotationConfigApplicationContext context;

	private StateMachine<States,Events> machine;

	private Tasks tasks;

	private TestListener listener;

	@Test
	public void testInitialState() throws InterruptedException {
		assertThat(listener.stateChangedLatch.await(1, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(1));
		assertThat(machine.getState().getIds(), contains(States.READY));
	}

	@Test
	public void testRunOnce() throws InterruptedException {
		listener.reset(3, 0, 0);
		tasks.run();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(machine.getState().getIds(), contains(States.READY));
	}

	@Test
	public void testRunTwice() throws InterruptedException {
		listener.reset(3, 0, 0);
		tasks.run();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(machine.getState().getIds(), contains(States.READY));

		listener.reset(3, 0, 0);
		tasks.run();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(machine.getState().getIds(), contains(States.READY));
	}

	@Test
	public void testFailAutomaticFix() throws InterruptedException {
		listener.reset(11, 0, 0);
		tasks.fail("T1");
		tasks.run();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(listener.stateChangedCount, is(11));
		assertThat(machine.getState().getIds(), contains(States.READY));
	}

	@Test
	public void testFailManualFix() throws InterruptedException {
		listener.reset(3, 0, 0);
		tasks.fail("T2");
		tasks.run();
		tasks.fix("T2");
		tasks.cont();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS), is(true));
		assertThat(machine.getState().getIds(), contains(States.READY));
	}

	@SuppressWarnings("unchecked")
	@Before
	public void setup() throws Exception {
		context = new AnnotationConfigApplicationContext();
		context.register(CommonConfiguration.class, Application.class, TestConfig.class);
		context.refresh();
		machine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, EnumStateMachine.class);
		tasks = context.getBean(Tasks.class);
		listener = context.getBean(TestListener.class);
		machine.start();
		// lets do a little sleep to wait sm to start
		Thread.sleep(1000);
	}

	@After
	public void clean() {
		machine.stop();
		context.close();
		context = null;
		machine = null;
		tasks = null;
		listener = null;
	}

	static class TestConfig {

		@Autowired
		private StateMachine<States,Events> machine;

		@Bean
		public StateMachineListener<States, Events> stateMachineListener() {
			TestListener listener = new TestListener();
			machine.addStateListener(listener);
			return listener;
		}

	}

	static class TestListener extends StateMachineListenerAdapter<States, Events> {

		volatile CountDownLatch stateChangedLatch = new CountDownLatch(1);
		volatile CountDownLatch stateEnteredLatch = new CountDownLatch(2);
		volatile CountDownLatch stateExitedLatch = new CountDownLatch(0);
		volatile CountDownLatch transitionLatch = new CountDownLatch(0);
		volatile int stateChangedCount = 0;
		volatile int transitionCount = 0;
		List<State<States, Events>> statesEntered = new ArrayList<State<States,Events>>();
		List<State<States, Events>> statesExited = new ArrayList<State<States,Events>>();

		@Override
		public void stateChanged(State<States, Events> from, State<States, Events> to) {
			stateChangedLatch.countDown();
			stateChangedCount++;
		}

		@Override
		public void stateEntered(State<States, Events> state) {
			statesEntered.add(state);
			stateEnteredLatch.countDown();
		}

		@Override
		public void stateExited(State<States, Events> state) {
			statesExited.add(state);
			stateExitedLatch.countDown();
		}

		@Override
		public void transitionEnded(Transition<States, Events> transition) {
			transitionLatch.countDown();
			transitionCount++;
		}

		public void reset(int c1, int c2, int c3) {
			reset(c1, c2, c3, 0);
		}

		public void reset(int c1, int c2, int c3, int c4) {
			stateChangedLatch = new CountDownLatch(c1);
			stateEnteredLatch = new CountDownLatch(c2);
			stateExitedLatch = new CountDownLatch(c3);
			transitionLatch = new CountDownLatch(c4);
			stateChangedCount = 0;
			transitionCount = 0;
			statesEntered.clear();
			statesExited.clear();
		}

	}

}
