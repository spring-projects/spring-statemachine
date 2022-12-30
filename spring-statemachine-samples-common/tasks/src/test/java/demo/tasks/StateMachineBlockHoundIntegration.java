/*
 * Copyright 2019 the original author or authors.
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
package demo.tasks;

import reactor.blockhound.BlockHound.Builder;
import reactor.blockhound.integration.BlockHoundIntegration;

public class StateMachineBlockHoundIntegration implements BlockHoundIntegration {

	@Override
	public void applyTo(Builder builder) {
		// whitelisting some blocking calls in tests
		builder
			.allowBlockingCallsInside("demo.tasks.Tasks", "sleep")
			.allowBlockingCallsInside("java.util.concurrent.locks.LockSupport", "park");
	}
}
