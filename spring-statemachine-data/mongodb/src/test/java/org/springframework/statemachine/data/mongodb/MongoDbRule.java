/*
 * Copyright 2016-2020 the original author or authors.
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
package org.springframework.statemachine.data.mongodb;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.util.SocketUtils;

/**
 * Rule skipping tests if MongoDb is not available from localhost simply by
 * checking if port can be bind.
 *
 * @author Janne Valkealahti
 *
 */
public class MongoDbRule extends TestWatcher {

	@Override
	public Statement apply(Statement base, Description description) {

		try {
			SocketUtils.findAvailableTcpPort(27017, 27017);
			return super.apply(new Statement() {
				@Override
				public void evaluate() throws Throwable {
				}
			}, Description.EMPTY);
		} catch (Exception e) {
			return super.apply(base, description);
		}
	}
}
