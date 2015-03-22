package demo.showcase;

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

import demo.CommonConfiguration;
import demo.showcase.Application.Events;
import demo.showcase.Application.States;

public class ShowcaseTests {

	private AnnotationConfigApplicationContext context;

	private StateMachine<States,Events> machine;

	private TestListener listener;

	@Test
	public void testInitialState() throws Exception {
		listener.stateChangedLatch.await(1, TimeUnit.SECONDS);
		listener.stateEnteredLatch.await(1, TimeUnit.SECONDS);
		assertThat(machine.getState().getIds(), contains(States.S0, States.S1, States.S11));
		assertThat(listener.statesEntered.size(), is(3));
		assertThat(listener.statesEntered.get(0).getId(), is(States.S0));
		assertThat(listener.statesEntered.get(1).getId(), is(States.S1));
		assertThat(listener.statesEntered.get(2).getId(), is(States.S11));
		assertThat(listener.statesExited.size(), is(0));
	}

	@Test
	public void testA() throws Exception {
		listener.reset(1, 2, 2);
		machine.sendEvent(Events.A);
		listener.stateChangedLatch.await(1, TimeUnit.SECONDS);
		listener.stateEnteredLatch.await(1, TimeUnit.SECONDS);
		listener.stateExitedLatch.await(1, TimeUnit.SECONDS);
		assertThat(machine.getState().getIds(), contains(States.S0, States.S1, States.S11));
		assertThat(listener.statesEntered.size(), is(2));
		assertThat(listener.statesEntered.get(0).getId(), is(States.S1));
		assertThat(listener.statesEntered.get(1).getId(), is(States.S11));
		assertThat(listener.statesExited.size(), is(2));
		assertThat(listener.statesExited.get(0).getId(), is(States.S11));
		assertThat(listener.statesExited.get(1).getId(), is(States.S1));
	}

	@Test
	public void testC() throws Exception {
		listener.reset(1, 3, 0);
		machine.sendEvent(Events.C);
		listener.stateChangedLatch.await(1, TimeUnit.SECONDS);
		listener.stateEnteredLatch.await(1, TimeUnit.SECONDS);
		assertThat(machine.getState().getIds(), contains(States.S0, States.S2, States.S21, States.S211));
		assertThat(listener.statesEntered.size(), is(3));
		assertThat(listener.statesEntered.get(0).getId(), is(States.S2));
		assertThat(listener.statesEntered.get(1).getId(), is(States.S21));
		assertThat(listener.statesEntered.get(2).getId(), is(States.S211));
	}

	@Test
	public void testCC() throws Exception {
		listener.reset(1, 3, 0);
		machine.sendEvent(Events.C);
		listener.stateChangedLatch.await(1, TimeUnit.SECONDS);
		assertThat(machine.getState().getIds(), contains(States.S0, States.S2, States.S21, States.S211));
		listener.reset(1, 2, 0);
		machine.sendEvent(Events.C);
		listener.stateChangedLatch.await(1, TimeUnit.SECONDS);
		listener.stateEnteredLatch.await(1, TimeUnit.SECONDS);
		assertThat(machine.getState().getIds(), contains(States.S0, States.S1, States.S11));
		assertThat(listener.statesEntered.size(), is(2));
		assertThat(listener.statesEntered.get(0).getId(), is(States.S1));
		assertThat(listener.statesEntered.get(1).getId(), is(States.S11));
	}

	@Test
	public void testCD() throws Exception {
		listener.reset(1, 3, 0);
		machine.sendEvent(Events.C);
		listener.stateChangedLatch.await(1, TimeUnit.SECONDS);
		listener.reset(1, 2, 0);
		machine.sendEvent(Events.D);
		listener.stateChangedLatch.await(1, TimeUnit.SECONDS);
		listener.stateEnteredLatch.await(1, TimeUnit.SECONDS);
		assertThat(machine.getState().getIds(), contains(States.S0, States.S2, States.S21, States.S211));
		assertThat(listener.statesEntered.size(), is(2));
		assertThat(listener.statesEntered.get(0).getId(), is(States.S21));
		assertThat(listener.statesEntered.get(1).getId(), is(States.S211));
	}

	@Test
	public void testI() throws Exception {
		listener.reset(1, 1, 1);
		machine.sendEvent(Events.I);
		listener.stateChangedLatch.await(1, TimeUnit.SECONDS);
		listener.stateEnteredLatch.await(1, TimeUnit.SECONDS);
		listener.stateExitedLatch.await(1, TimeUnit.SECONDS);
		assertThat(machine.getState().getIds(), contains(States.S0, States.S1, States.S12));
		assertThat(listener.statesEntered.size(), is(1));
		assertThat(listener.statesEntered.get(0).getId(), is(States.S12));
		assertThat(listener.statesExited.size(), is(1));
		assertThat(listener.statesExited.get(0).getId(), is(States.S11));
	}

	static class Config {

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
		volatile CountDownLatch stateEnteredLatch = new CountDownLatch(3);
		volatile CountDownLatch stateExitedLatch = new CountDownLatch(0);
		List<State<States, Events>> statesEntered = new ArrayList<State<States,Events>>();
		List<State<States, Events>> statesExited = new ArrayList<State<States,Events>>();

		@Override
		public void stateChanged(State<States, Events> from, State<States, Events> to) {
			stateChangedLatch.countDown();
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

		public void reset(int c1, int c2, int c3) {
			stateChangedLatch = new CountDownLatch(c1);
			stateEnteredLatch = new CountDownLatch(c2);
			stateExitedLatch = new CountDownLatch(c3);
			statesEntered.clear();
			statesExited.clear();
		}

	}

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		context = new AnnotationConfigApplicationContext();
		context.register(CommonConfiguration.class, Application.class, Config.class);
		context.refresh();
		machine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, EnumStateMachine.class);
		listener = context.getBean(TestListener.class);
		machine.start();
	}

	@After
	public void clean() {
		machine.stop();
		context.close();
		context = null;
		machine = null;
	}

}
