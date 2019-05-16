/*
 * Copyright 2016-2017 the original author or authors.
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
package org.springframework.statemachine.buildtests;

import org.junit.After;
import org.junit.Before;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public abstract class AbstractBuildTests {

	protected AnnotationConfigApplicationContext context;

	@Before
	public void setup() {
		context = buildContext();
	}

	@After
	public void clean() {
		if (context != null) {
			context.close();
		}
		context = null;
	}

	protected enum TestStates {
		SI,S1,S2,S3,S4,SF,SH,
		S10,S11,S101,S111,S112,S12,S121,S122,S13,
		S20,S21,S201,S211,S212,
		S1011,S1012,S2011,S2012,
		S30,S31,S32,S33
	}

	protected enum TestEvents {
		E1,E2,E3,E4,EF,EH
	}

	/**
	 * Builds the context.
	 *
	 * @return the annotation config application context
	 */
	protected AnnotationConfigApplicationContext buildContext() {
		return null;
	}
}
