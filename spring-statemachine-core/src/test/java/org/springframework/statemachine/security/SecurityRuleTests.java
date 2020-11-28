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
package org.springframework.statemachine.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.jupiter.api.Test;


public class SecurityRuleTests {

	@Test
	public void testConvertAttributesToCommaSeparatedString() {
		Collection<String> attributes = new ArrayList<String>();
		attributes.add("ROLE_1");
		attributes.add("ROLE_2");
		assertThat("ROLE_1, ROLE_2").isEqualTo(SecurityRule.securityAttributesToCommaDelimitedList(attributes));
	}

	@Test
	public void testConvertAttributesFromCommaSeparatedString() {
		Collection<String> attributes = SecurityRule.commaDelimitedListToSecurityAttributes(" ,,ROLE_1, ROLE_2");
		assertThat(attributes.size()).isEqualTo(2);
		assertThat(attributes.contains("ROLE_1")).isTrue();
		assertThat(attributes.contains("ROLE_2")).isTrue();
	}

	@Test
	public void testDefaultComparisonType() {
		SecurityRule rule = new SecurityRule();
		assertThat(rule.getComparisonType() == SecurityRule.ComparisonType.ANY).isTrue();
	}
}
