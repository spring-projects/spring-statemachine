package demo.showcase;

import org.junit.After;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.statemachine.EnumStateMachine;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineSystemConstants;

import demo.CommonConfiguration;
import demo.showcase.Application.Events;
import demo.showcase.Application.States;

public class ShowcaseTests {

	private AnnotationConfigApplicationContext context;

	@SuppressWarnings("resource")
	@Test
	public void testApplication() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(CommonConfiguration.class, Application.class);
		context.refresh();
		@SuppressWarnings("unchecked")
		StateMachine<States,Events> machine = context.getBean(StateMachineSystemConstants.DEFAULT_ID_STATEMACHINE, EnumStateMachine.class);

		machine.start();

		machine.sendEvent(Events.A);

	}

	@After
	public void clean() {
		if (context != null) {
			context.close();
			context = null;
		}
	}

}
