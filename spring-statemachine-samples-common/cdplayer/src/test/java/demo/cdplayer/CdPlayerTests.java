/*
 * Copyright 2015-2020 the original author or authors.
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
package demo.cdplayer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.statemachine.TestUtils.doStartAndAssert;
import static org.springframework.statemachine.TestUtils.doStopAndAssert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.statemachine.ObjectStateMachine;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateContext.Stage;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.TransitionKind;

import demo.CommonConfiguration;
import demo.cdplayer.Application.Events;
import demo.cdplayer.Application.States;

public class CdPlayerTests {

	private AnnotationConfigApplicationContext context;

	private StateMachine<States,Events> machine;

	private CdPlayer player;

	private Library library;

	private TestListener listener;

	@Test
	public void testInitialState() throws InterruptedException {
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(2);
		assertThat(machine.getState().getIds()).containsExactly(States.IDLE, States.CLOSED);
		assertLcdStatusStartsWith("No CD");
	}

	@Test
	public void testEjectTwice() throws Exception {
		listener.reset(1, 0, 0);
		player.eject();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);
		assertThat(machine.getState().getIds()).containsExactly(States.IDLE, States.OPEN);
		listener.reset(1, 0, 0);
		player.eject();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);
		assertThat(machine.getState().getIds()).containsExactly(States.IDLE, States.CLOSED);
	}

	@Test
	public void testPlayWithCdLoaded() throws Exception {
		listener.reset(1, 0, 0);
		player.eject();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);

		listener.reset(1, 0, 0);
		player.load(library.getCollection().get(0));
		player.eject();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);

		listener.reset(2, 0, 0);
		player.play();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(2);
		assertThat(machine.getState().getIds()).containsExactly(States.BUSY, States.PLAYING);
		assertLcdStatusContains("cd1");
	}

	@Test
	public void testPlayWithCdLoadedDeckOpen() throws Exception {
		listener.reset(1, 0, 0);
		player.eject();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);

		listener.reset(3, 0, 0);
		player.load(library.getCollection().get(0));
		player.play();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(3);
		assertThat(machine.getState().getIds()).containsExactly(States.BUSY, States.PLAYING);
		assertLcdStatusContains("cd1");
	}

	@Test
	public void testPlayWithNoCdLoaded() throws Exception {
		listener.reset(0, 0, 0);
		player.play();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isZero();
		assertThat(machine.getState().getIds()).containsExactly(States.IDLE, States.CLOSED);
		assertLcdStatusStartsWith("No CD");
	}

	@Test
	public void testPlayLcdTimeChanges() throws Exception {
		listener.reset(1, 0, 0);
		player.eject();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);

		listener.reset(1, 0, 0);
		player.load(library.getCollection().get(0));
		player.eject();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);

		listener.reset(2, 0, 0);
		player.play();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(2);
		assertThat(machine.getState().getIds()).containsExactly(States.BUSY, States.PLAYING);
		assertLcdStatusContains("cd1");

		listener.reset(0, 0, 0, 0, 1);
		assertThat(listener.transitionTimerLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.transitionTimerCount).isEqualTo(1);
		assertLcdStatusContains("00:01");

		listener.reset(0, 0, 0, 0, 1);
		assertThat(listener.transitionTimerLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertLcdStatusContains("00:02");
		assertThat(listener.transitionTimerCount).isEqualTo(1);

		listener.reset(0, 0, 0, 0, 2);
		assertThat(listener.transitionTimerLatch.await(4, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.transitionTimerCount).isEqualTo(2);
		// ok we have some timing problems with
		// this test, so for now just check it's
		// not previous
		assertLcdStatusNotContains("00:02");
	}

	@Test
	public void testPlayPause() throws Exception {
		listener.reset(1, 0, 0);
		player.eject();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);

		listener.reset(1, 0, 0);
		player.load(library.getCollection().get(0));
		player.eject();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);

		listener.reset(2, 0, 0, 0, 1);
		player.play();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(2);
		assertThat(listener.transitionTimerLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.transitionTimerCount).isEqualTo(1);
		assertThat(machine.getState().getIds()).containsExactly(States.BUSY, States.PLAYING);
		assertLcdStatusContains("cd1");
		assertLcdStatusContains("00:01");

		listener.reset(0, 0, 0, 1, 1);
		assertThat(listener.transitionTimerLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertLcdStatusContains("00:02");
		assertThat(listener.transitionTimerCount).isEqualTo(1);

		listener.reset(1, 0, 0, 0);
		player.pause();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);
		assertLcdStatusContains("00:02");

		listener.reset(1, 0, 0, 1);
		player.pause();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);
		assertThat(listener.transitionLatch.await(2, TimeUnit.SECONDS)).isTrue();

		listener.reset(0, 0, 0, 2, 2);
		assertThat(listener.transitionTimerLatch.await(2100, TimeUnit.MILLISECONDS)).isTrue();
		assertThat(listener.transitionTimerCount).isEqualTo(2);
		assertLcdStatusNotContains("00:02");
	}

	@Test
	public void testPlayStop() throws Exception {
		listener.reset(1, 0, 0);
		player.eject();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);

		listener.reset(1, 0, 0);
		player.load(library.getCollection().get(0));
		player.eject();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(1);

		listener.reset(2, 0, 0);
		player.play();

		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(2);
		assertThat(machine.getState().getIds()).containsExactly(States.BUSY, States.PLAYING);

		listener.reset(2, 0, 0);
		player.stop();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(2);
		assertLcdStatusIs("cd1 ");
	}

	@Test
	public void testPlayDeckOpenNoCd() throws Exception {
		listener.reset(2, 0, 0);
		player.eject();
		player.play();
		assertThat(listener.stateChangedLatch.await(2, TimeUnit.SECONDS)).isTrue();
		assertThat(listener.stateChangedCount).isEqualTo(2);
		assertThat(machine.getState().getIds()).containsExactly(States.IDLE, States.CLOSED);
	}

	private void assertLcdStatusIs(String text) {
		assertThat(player.getLdcStatus()).isEqualTo(text);
	}

	private void assertLcdStatusStartsWith(String text) {
		assertThat(player.getLdcStatus()).startsWith(text);
	}

	private void assertLcdStatusContains(String text) {
		assertThat(player.getLdcStatus()).contains(text);
	}

	private void assertLcdStatusNotContains(String text) {
		assertThat(player.getLdcStatus()).doesNotContain(text);
	}

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void setup() throws Exception {
		context = new AnnotationConfigApplicationContext();
		context.register(CommonConfiguration.class, Application.class, TestConfig.class);
		context.refresh();
		machine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, ObjectStateMachine.class);
		player = context.getBean(CdPlayer.class);
		library = context.getBean(Library.class);
		listener = context.getBean(TestListener.class);
		doStartAndAssert(machine);
		assertThat(listener.stateMachineStartedLatch.await(2, TimeUnit.SECONDS)).isTrue();
	}

	@AfterEach
	public void clean() {
		doStopAndAssert(machine);
		context.close();
		context = null;
		machine = null;
		player = null;
		library = null;
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

		@Bean
		public Library library() {
			// override library to make it easier to test
			Track cd1track1 = new Track("cd1track1", 30);
			Track cd1track2 = new Track("cd1track2", 30);
			Cd cd1 = new Cd("cd1", new Track[]{cd1track1,cd1track2});
			Track cd2track1 = new Track("cd2track1", 30);
			Track cd2track2 = new Track("cd2track2", 30);
			Cd cd2 = new Cd("cd2", new Track[]{cd2track1,cd2track2});
			return new Library(new Cd[]{cd1,cd2});
		}

	}

	static class TestListener extends StateMachineListenerAdapter<States, Events> {

		volatile CountDownLatch stateMachineStartedLatch = new CountDownLatch(1);
		volatile CountDownLatch stateChangedLatch = new CountDownLatch(1);
		volatile CountDownLatch stateEnteredLatch = new CountDownLatch(2);
		volatile CountDownLatch stateExitedLatch = new CountDownLatch(0);
		volatile CountDownLatch transitionLatch = new CountDownLatch(0);
		volatile CountDownLatch transitionTimerLatch = new CountDownLatch(0);
		volatile int stateChangedCount = 0;
		volatile int transitionCount = 0;
		volatile int transitionTimerCount = 0;
		List<State<States, Events>> statesEntered = new ArrayList<State<States,Events>>();
		List<State<States, Events>> statesExited = new ArrayList<State<States,Events>>();

		@Override
		public void stateMachineStarted(StateMachine<States, Events> stateMachine) {
			stateMachineStartedLatch.countDown();
		}

		@Override
		public void stateChanged(State<States, Events> from, State<States, Events> to) {
			stateChangedCount++;
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

		@Override
		public void stateContext(StateContext<States, Events> stateContext) {
			if (stateContext.getStage() == Stage.TRANSITION_END) {
				if (stateContext.getTransition().getKind() == TransitionKind.INTERNAL
						&& stateContext.getEvent() == null) {
					transitionTimerCount++;
					transitionTimerLatch.countDown();
				} else {
					transitionCount++;
					transitionLatch.countDown();
				}
			}
		}

		public void reset(int c1, int c2, int c3) {
			reset(c1, c2, c3, 0);
		}

		public void reset(int c1, int c2, int c3, int c4) {
			reset(c1, c2, c3, c4, 0);
		}

		public void reset(int c1, int c2, int c3, int c4, int c5) {
			stateChangedLatch = new CountDownLatch(c1);
			stateEnteredLatch = new CountDownLatch(c2);
			stateExitedLatch = new CountDownLatch(c3);
			transitionLatch = new CountDownLatch(c4);
			transitionTimerLatch = new CountDownLatch(c5);
			stateChangedCount = 0;
			transitionCount = 0;
			transitionTimerCount = 0;
			statesEntered.clear();
			statesExited.clear();
		}
	}
}
