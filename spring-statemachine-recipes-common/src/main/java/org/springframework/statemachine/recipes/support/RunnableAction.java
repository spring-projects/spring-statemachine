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
package org.springframework.statemachine.recipes.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.util.Assert;
import org.springframework.util.StopWatch;

/**
 * A {@link Action} which executes a {@link Runnable}.
 *
 * @author Janne Valkealahti
 *
 */
public class RunnableAction implements Action<String, String> {

	private static final Log log = LogFactory.getLog(RunnableAction.class);
	private final Runnable runnable;
	private final String id;

	/**
	 * Instantiates a new runnable action.
	 *
	 * @param runnable the runnable
	 */
	public RunnableAction(Runnable runnable) {
		this(runnable, null);
	}

	/**
	 * Instantiates a new runnable action.
	 *
	 * @param runnable the runnable
	 * @param id the optional id for logging
	 */
	public RunnableAction(Runnable runnable, String id) {
		Assert.notNull(runnable, "Runnable must be set");
		this.runnable = runnable;
		this.id = id;
	}

	@Override
	public final void execute(StateContext<String, String> context) {
		if (!shouldExecute(id, context)) {
			return;
		}
		StopWatch watch = new StopWatch();
		String logId = (id == null ? "" : (" id=" + id));
		log.info("Executing runnable" + logId);
		if (log.isDebugEnabled()) {
			watch.start();
		}
		try {
			onPreExecute(id, context);
			runnable.run();
			onSuccess(id, context);
		} catch (Exception e) {
			onError(id, context, e);
		} finally {
			onPostExecute(id, context);
		}
		if (log.isDebugEnabled()) {
			watch.stop();
			log.debug("Runnable execution took " + watch.getTotalTimeMillis() + " ms" + logId);
		}
	}

	public String getId() {
		return id;
	}

	protected boolean shouldExecute(String id, StateContext<String, String> context) {
		return true;
	}

	protected void onPreExecute(String id, StateContext<String, String> context) {
	}

	protected void onPostExecute(String id, StateContext<String, String> context) {
	}

	protected void onSuccess(String id, StateContext<String, String> context) {
	}

	protected void onError(String id, StateContext<String, String> context, Exception e) {
	}

}