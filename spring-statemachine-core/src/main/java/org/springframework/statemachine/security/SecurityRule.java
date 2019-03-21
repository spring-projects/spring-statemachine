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

import java.util.Collection;
import java.util.HashSet;

import org.springframework.util.StringUtils;

/**
 * Encapsulates the rules for comparing security attributes and expression.
 *
 * @author Janne Valkealahti
 */
public class SecurityRule {

	private Collection<String> attributes;
	private ComparisonType comparisonType = ComparisonType.ANY;
	private String expression;

	/**
	 * Convert attributes to comma separated String
	 *
	 * @param attributes the attributes to convert
	 * @return comma separated String
	 */
	public static String securityAttributesToCommaDelimitedList(Collection<?> attributes) {
		return StringUtils.collectionToDelimitedString(attributes, ", ");
	}

	/**
	 * Convert attributes from comma separated String to Collection
	 *
	 * @param attributes the attributes to convert
	 * @return comma parsed Collection
	 */
	public static Collection<String> commaDelimitedListToSecurityAttributes(String attributes) {
		Collection<String> attrs = new HashSet<String>();
		for (String attribute : attributes.split(",")) {
			attribute = attribute.trim();
			if (!"".equals(attribute)) {
				attrs.add(attribute);
			}
		}
		return attrs;
	}

	/**
	 * Gets the security attributes.
	 *
	 * @return the security attributes
	 */
	public Collection<String> getAttributes() {
		return attributes;
	}

	/**
	 * Sets the security attributes.
	 *
	 * @param attributes the new security attributes
	 */
	public void setAttributes(Collection<String> attributes) {
		this.attributes = attributes;
	}

	/**
	 * Gets the comparison type.
	 *
	 * @return the comparison type
	 */
	public ComparisonType getComparisonType() {
		return comparisonType;
	}

	/**
	 * Sets the comparison type.
	 *
	 * @param comparisonType the new comparison type
	 */
	public void setComparisonType(ComparisonType comparisonType) {
		this.comparisonType = comparisonType;
	}

	/**
	 * Gets the security expression.
	 *
	 * @return the security expression
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * Sets the security expression.
	 *
	 * @param expression the new security expression
	 */
	public void setExpression(String expression) {
		this.expression = expression;
	}

	/**
	 * Security comparison types.
	 */
	public static enum ComparisonType {

		/**
		 * Compare method where any attribute authorization allows access
		 */
		ANY,

		/**
		 * Compare method where all attribute authorization allows access
		 */
		ALL,

		/**
		 * Compare method where majority attribute authorization allows access
		 */
		MAJORITY;
	}

	@Override
	public String toString() {
		return "SecurityRule [attributes=" + attributes + ", comparisonType=" + comparisonType + ", expression=" + expression + "]";
	}

}