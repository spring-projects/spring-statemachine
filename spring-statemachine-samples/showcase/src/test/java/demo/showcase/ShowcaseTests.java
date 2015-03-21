package demo.showcase;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

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
		listener.latch.await(1, TimeUnit.SECONDS);
		assertThat(machine.getState().getIds(), contains(States.S0, States.S1, States.S11));
	}

	@Test
	public void testA() throws Exception {
		listener.reset(1);
		machine.sendEvent(Events.A);
		listener.latch.await(1, TimeUnit.SECONDS);
		assertThat(machine.getState().getIds(), contains(States.S0, States.S1, States.S11));
	}

	@Test
	public void testC() throws Exception {
		listener.reset(1);
		machine.sendEvent(Events.C);
		listener.latch.await(1, TimeUnit.SECONDS);
		assertThat(machine.getState().getIds(), contains(States.S0, States.S2, States.S21, States.S211));
	}

	static class Config {

		@Autowired
		private StateMachine<States,Events> machine;

		@Bean
		public StateMachineListener<States, Events> stateMachineListener() {
			return new TestListener();
		}
	}

	static class TestListener extends StateMachineListenerAdapter<States, Events> {

		volatile CountDownLatch latch = new CountDownLatch(1);

		@Override
		public void stateChanged(State<States, Events> from, State<States, Events> to) {
			latch.countDown();
		}

		public void reset(int count) {
			latch = new CountDownLatch(count);
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
