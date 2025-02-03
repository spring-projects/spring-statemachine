/*
 * Copyright 2002-2024 the original author or authors.
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

package org.springframework.statemachine.plantuml.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.expression.Expression;
import org.springframework.lang.Nullable;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.action.SpelExpressionAction;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.guard.SpelExpressionGuard;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NameGetter {

	private static final Log log = LogFactory.getLog(NameGetter.class);

	/**
	 * Implement a 'name strategy' based on this sequence:
	 * <UL>
	 * <LI>{@link Expression}</LI>
	 * <LI>{@link BeanNameAware}</LI>
	 * <LI>Lambda's unique "arg$1" parameter</LI>
	 * </UL>
	 * If all these strategy are failing, fallback to "class name" of the object
	 *
	 * @param object object to get "name" from
	 * @return name of the object
	 */
	public static String getName(Object object) {
		String name = getSpellExpression(object);
		if (name != null) {
			return name;
		}

		// try to 'unwrap' CGLib proxy object:
		Object proxyTarget = AopProxyUtils.getSingletonTarget(object);
		if(proxyTarget != null) {
			name = getName(proxyTarget);
			if(name != null) {
				return name;
			}
		}

		name = getBeanName(object);
		if(name != null) {
			return name;
		}

		Object action = extractArg$1(object, Action.class);
		if (action != object) {
			name = getName(action);
			if(name != null) {
				return name;
			}
		}

		Object guard = extractArg$1(object, Guard.class);
		if (guard != object) {
			name = getName(guard);
			if(name != null) {
				return name;
			}
		}

		// fallback
		return getNameUsingFallBackStrategy(object);
	}

	private static String getNameUsingFallBackStrategy(Object object) {

		String name = object.getClass().toString();
		name = name.replace("class ", "");
		// remove trailing "/0x..." in class name
		// example: "Actions$$Lambda$504/0x0000000800ec3e00"
		name = RegExUtils.removeAll(name, "/0x.*");

		// remove trailing "$(0-9)" in action name
		// example: "Actions$$Lambda$504"
		// name = RegExUtils.removeAll(name, "\\$\\d+");

		// only keep class name
		// a.b.c.D$32/0x25764366 -> D$32
		String nameWithoutDollar = name;
		int indexOfDollarSymbol = name.indexOf("$");
		if (indexOfDollarSymbol != -1) {
			nameWithoutDollar = name.substring(0, indexOfDollarSymbol);
		}
		int lastIndexOfDot = nameWithoutDollar.lastIndexOf(".");
		if (lastIndexOfDot != -1) {
			name = name.substring(lastIndexOfDot + 1);
		}
		return name;
	}

	@Nullable
	private static String getSpellExpression(Object object) {
		if (object instanceof SpelExpressionAction<?, ?> || object instanceof SpelExpressionGuard<?, ?>) {
			try {
				return ((Expression) FieldUtils.readDeclaredField(object, "expression", true)).getExpressionString();
			} catch (IllegalAccessException ex) {
				log.error("error while getting SpelExpression", ex);
			}
		}
		return null;
	}


	// Disable "Rename this ... variable to match the regular expression '^[a-z][a-zA-Z0-9]*$'"
	// we want to the name of this method to clearly state what is does, i.e., "extract Arg $1"
	@SuppressWarnings({"squid:S100", "squid:S117"})
	private static Object extractArg$1(Object actionWrappedInFunction, Class<?> typeOfArg) {
		Field arg$1Field = FieldUtils.getDeclaredField(actionWrappedInFunction.getClass(), "arg$1", true);
		if (arg$1Field != null && arg$1Field.getType() == typeOfArg) {
			try {
				return FieldUtils.readDeclaredField(actionWrappedInFunction, "arg$1", true);
			} catch (IllegalAccessException ex) {
				log.error("Error while extracting action from function!", ex);
			}
		}
		return actionWrappedInFunction;
	}

	/**
	 * Try to get bean name using:
	 * <UL>
	 *     <LI>BeanNameAware interface</LI>
	 *     <LI>getBeanName method</LI>
	 * </UL>
	 * @param object the bean candidate
	 * @return beanName or null
	 */
	@Nullable
	private static String getBeanName(Object object) {
		Class<?> clazz = object.getClass();
		if (ClassUtils.getAllInterfaces(clazz).contains(BeanNameAware.class)) {
/*
            log.error("Class " + clazz + " doesn't implement " + BeanNameAware.class + "! " +
                      "Make sure " + clazz + " implements BeanNameAware AND contains a 'String beanName' field."
            );
*/
			try {
				// 'clazz' implements BeanNameAware .. let's try to get beanName
				Field beanNameFielOfClazz = FieldUtils.getField(clazz, "beanName", true);
				if (beanNameFielOfClazz == null) {
					log.error("Class " + clazz + " does NOT contains a 'beanNameAware' field! " +
							"Make sure " + clazz + " contains a 'String beanName' field."
					);
				} else if (beanNameFielOfClazz.getType() == String.class) {
					return (String) FieldUtils.readField(object, "beanName", true);
				} else {
					log.error("Class " + clazz + " contains a '" + beanNameFielOfClazz.getType() + " beanNameAware' field, but type should be 'String'! " +
							"Make sure " + clazz + " contains a 'String beanName' field."
					);
				}
			} catch (IllegalAccessException ex) {
				log.error("Error while accessing field!", ex);
			}
		}

		// second try using getBeanName method
		try {
			return (String) MethodUtils.invokeMethod(object, true, "getBeanName");
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
/*
            log.error("Class " + clazz + " doesn't provide a getBeanName method! " +
                      "Make sure " + clazz + " implements BeanNameAware AND contains a 'String beanName' field."
            );
*/
			return null;
		}
	}
}
