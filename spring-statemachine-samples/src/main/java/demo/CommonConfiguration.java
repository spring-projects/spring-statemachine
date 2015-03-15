package demo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.statemachine.event.OnStateChangedEvent;
import org.springframework.statemachine.event.OnTransitionEvent;
import org.springframework.statemachine.event.StateMachineEvent;
import org.springframework.statemachine.event.StateMachineEventPublisherConfiguration;

@Configuration
public class CommonConfiguration {

	private final static Log log = LogFactory.getLog(CommonConfiguration.class);

	@Configuration
	@Import(StateMachineEventPublisherConfiguration.class)
	static class ApplicationConfig {

		@Bean
		public TaskExecutor taskExecutor() {
			return new SyncTaskExecutor();
		}

		@Bean
		public TaskScheduler taskScheduler() {
			return new ConcurrentTaskScheduler();
		}

		@Bean
		public TestEventListener testEventListener() {
			return new TestEventListener();
		}

		@Bean
		public String stateChartModel() throws IOException {
			ClassPathResource model = new ClassPathResource("statechartmodel.txt");
			InputStream inputStream = model.getInputStream();
			Scanner scanner = new Scanner(inputStream);
			String content = scanner.useDelimiter("\\Z").next();
			scanner.close();
			return content;
		}

	}

	static class TestEventListener implements ApplicationListener<StateMachineEvent> {

		@Override
		public void onApplicationEvent(StateMachineEvent event) {
			if (event instanceof OnStateChangedEvent) {
				OnStateChangedEvent e = (OnStateChangedEvent)event;
				log.info("State changed to " + e.getTargetState().getId());
			} else if (event instanceof OnTransitionEvent) {
				OnTransitionEvent e = (OnTransitionEvent)event;
				log.info("Transition " + e.getTransition().toString());
			}
		}

	}

}
