/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.util.StringUtils;

/**
 * Simple {@link ApplicationListener} which logs all events
 * based on {@link StateMachineEvent} using a log level
 * set during the construction.
 *
 * @author Janne Valkealahti
 *
 */
public class LoggingListener implements ApplicationListener<StateMachineEvent> {

	private static final Log log = LogFactory.getLog(LoggingListener.class);

	/** Internal enums to match the log level */
	private static enum Level {
		FATAL, ERROR, WARN, INFO, DEBUG, TRACE
	}

	/** Level to use */
	private final Level level;

	/**
	 * Constructs Logger listener with debug level.
	 */
	public LoggingListener() {
		level = Level.DEBUG;
	}

	/**
	 * Constructs Logger listener with given level.
	 *
	 * @param level the level string
	 */
	public LoggingListener(String level) {
		try {
			this.level = Level.valueOf(level.toUpperCase());
		}
		catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid log level '" + level
					+ "'. The (case-insensitive) supported values are: "
					+ StringUtils.arrayToCommaDelimitedString(Level.values()));
		}
	}

	@Override
	public void onApplicationEvent(StateMachineEvent event) {
		switch (this.level) {
		case FATAL:
			if (log.isFatalEnabled()) {
				log.fatal(event);
			}
			break;
		case ERROR:
			if (log.isErrorEnabled()) {
				log.error(event);
			}
			break;
		case WARN:
			if (log.isWarnEnabled()) {
				log.warn(event);
			}
			break;
		case INFO:
			if (log.isInfoEnabled()) {
				log.info(event);
			}
			break;
		case DEBUG:
			if (log.isDebugEnabled()) {
				log.debug(event);
			}
			break;
		case TRACE:
			if (log.isTraceEnabled()) {
				log.trace(event);
			}
			break;
		}
	}

}
