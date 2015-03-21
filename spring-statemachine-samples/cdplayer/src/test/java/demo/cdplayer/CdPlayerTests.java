package demo.cdplayer;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.statemachine.EnumStateMachine;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;

import demo.CommonConfiguration;
import demo.cdplayer.Application.Events;
import demo.cdplayer.Application.States;

public class CdPlayerTests {

	private AnnotationConfigApplicationContext context;

	private StateMachine<States,Events> machine;

	private CdPlayer player;

	private Library library;

	@Test
	public void testInitialState() throws InterruptedException {
		assertThat(machine.getState().getIds(), contains(States.IDLE, States.CLOSED));
		assertLcdStatusStartsWith("No CD");
	}

	@Test
	public void testEjectTwice() throws Exception {
		player.eject();
		Thread.sleep(100);
		assertThat(machine.getState().getIds(), contains(States.IDLE, States.OPEN));
		player.eject();
		Thread.sleep(100);
		assertThat(machine.getState().getIds(), contains(States.IDLE, States.CLOSED));
	}

	@Test
	public void testPlayWithCdLoaded() throws Exception {
		player.eject();
		player.load(library.getCollection().get(0));
		player.eject();
		player.play();
		Thread.sleep(100);
		assertThat(machine.getState().getIds(), contains(States.BUSY, States.PLAYING));
		assertLcdStatusContains("cd1");
	}

	@Test
	public void testPlayWithNoCdLoaded() {
		player.play();
		assertThat(machine.getState().getIds(), contains(States.IDLE, States.CLOSED));
		assertLcdStatusStartsWith("No CD");
	}

	@Test
	public void testPlayLcdTimeChanges() throws Exception {
		player.eject();
		player.load(library.getCollection().get(0));
		player.eject();
		player.play();
		assertThat(machine.getState().getIds(), contains(States.BUSY, States.PLAYING));
		assertLcdStatusContains("cd1");
		Thread.sleep(1000);
		assertLcdStatusContains("00:01");
		Thread.sleep(1000);
		assertLcdStatusContains("00:02");
		Thread.sleep(1000);
		assertLcdStatusContains("00:03");
	}

	@Test
	public void testPlayPause() throws Exception {
		player.eject();
		player.load(library.getCollection().get(0));
		player.eject();
		player.play();
		Thread.sleep(100);
		assertThat(machine.getState().getIds(), contains(States.BUSY, States.PLAYING));
		assertLcdStatusContains("cd1");
		Thread.sleep(1000);
		assertLcdStatusIs("cd1 00:01");
		Thread.sleep(1000);
		assertLcdStatusContains("00:02");
		player.pause();
		Thread.sleep(2000);
		assertLcdStatusContains("00:02");
		player.pause();
		assertLcdStatusContains("00:03");
		Thread.sleep(1000);
		assertLcdStatusContains("00:04");
	}

	@Test
	public void testPlayStop() throws Exception {
		player.eject();
		player.load(library.getCollection().get(0));
		player.eject();
		player.play();
		Thread.sleep(100);
		assertThat(machine.getState().getIds(), contains(States.BUSY, States.PLAYING));
		player.stop();
		Thread.sleep(100);
		assertLcdStatusIs("cd1 ");
	}

	@Test
	public void testPlayDeckOpenNoCd() throws Exception {
		player.eject();
		player.play();
		assertThat(machine.getState().getIds(), contains(States.IDLE, States.CLOSED));
	}

	private void assertLcdStatusIs(String text) {
		assertThat(player.getLdcStatus(), is(text));
	}

	private void assertLcdStatusStartsWith(String text) {
		assertThat(player.getLdcStatus(), startsWith(text));
	}

	private void assertLcdStatusContains(String text) {
		assertThat(player.getLdcStatus(), containsString(text));
	}

	@SuppressWarnings("unchecked")
	@Before
	public void setup() {
		context = new AnnotationConfigApplicationContext();
		context.register(CommonConfiguration.class, Application.class, TestConfig.class);
		context.refresh();
		machine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, EnumStateMachine.class);
		player = context.getBean(CdPlayer.class);
		library = context.getBean(Library.class);
		machine.start();
	}

	@After
	public void clean() {
		machine.stop();
		context.close();
		context = null;
		machine = null;
		player = null;
		library = null;
	}

	static class TestConfig {

		@Bean
		public Library library() {
			// override library to make it easier to test
			Track cd1track1 = new Track("cd1track1", 3);
			Track cd1track2 = new Track("cd1track2", 3);
			Cd cd1 = new Cd("cd1", new Track[]{cd1track1,cd1track2});
			Track cd2track1 = new Track("cd2track1", 3);
			Track cd2track2 = new Track("cd2track2", 3);
			Cd cd2 = new Cd("cd2", new Track[]{cd2track1,cd2track2});
			return new Library(new Cd[]{cd1,cd2});
		}

	}

}
