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
package org.springframework.statemachine.processor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.TypeConverter;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.annotation.EventHeaders;
import org.springframework.statemachine.support.AbstractExpressionEvaluator;
import org.springframework.statemachine.support.AnnotatedMethodFilter;
import org.springframework.statemachine.support.FixedMethodFilter;
import org.springframework.statemachine.support.UniqueMethodFilter;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;

/**
 * A helper class using spel to execute target methods.
 *
 * @author Janne Valkealahti
 *
 * @param <T> the return type
 */
public class StateMachineMethodInvokerHelper<T, S, E> extends AbstractExpressionEvaluator {

	private static final String CANDIDATE_METHODS = "CANDIDATE_METHODS";

	private static final String CANDIDATE_MESSAGE_METHODS = "CANDIDATE_MESSAGE_METHODS";

	private final Log logger = LogFactory.getLog(this.getClass());

	private final Object targetObject;

	private volatile String displayString;

	private volatile boolean requiresReply;

	private final Map<Class<?>, HandlerMethod> handlerMethods;

	private final Map<Class<?>, HandlerMethod> handlerMessageMethods;

	private final LinkedList<Map<Class<?>, HandlerMethod>> handlerMethodsList;

	private final HandlerMethod handlerMethod;

	private final Class<?> expectedType;

	public StateMachineMethodInvokerHelper(Object targetObject, Method method) {
		this(targetObject, method, null);
	}

	public StateMachineMethodInvokerHelper(Object targetObject, Method method, Class<?> expectedType) {
		this(targetObject, null, method, expectedType);
	}

	public StateMachineMethodInvokerHelper(Object targetObject, String methodName) {
		this(targetObject, methodName, null);
	}

	public StateMachineMethodInvokerHelper(Object targetObject, String methodName, Class<?> expectedType) {
		this(targetObject, null, methodName, expectedType);
	}

	public StateMachineMethodInvokerHelper(Object targetObject, Class<? extends Annotation> annotationType) {
		this(targetObject, annotationType, null);
	}

	public StateMachineMethodInvokerHelper(Object targetObject, Class<? extends Annotation> annotationType,
			Class<?> expectedType) {
		this(targetObject, annotationType, (String) null, expectedType);
	}

	public T process(StateMachineRuntime<S, E> stateMachineRuntime) throws Exception {
		ParametersWrapper<S, E> wrapper = new ParametersWrapper<S, E>(stateMachineRuntime.getStateContext());
		return processInternal(wrapper);
	}

	@Override
	public String toString() {
		return this.displayString;
	}

	private StateMachineMethodInvokerHelper(Object targetObject, Class<? extends Annotation> annotationType,
			Method method, Class<?> expectedType) {
		Assert.notNull(method, "method must not be null");
		this.expectedType = expectedType;
		this.requiresReply = expectedType != null;
		if (expectedType != null) {
			Assert.isTrue(method.getReturnType() != Void.class && method.getReturnType() != Void.TYPE,
					"method must have a return type");
		}
		Assert.notNull(targetObject, "targetObject must not be null");
		this.targetObject = targetObject;
		this.handlerMethod = new HandlerMethod(method);
		this.handlerMethods = null;
		this.handlerMessageMethods = null;
		this.handlerMethodsList = null;
		this.prepareEvaluationContext(this.getEvaluationContext(false), method, annotationType);
		this.setDisplayString(targetObject, method);
	}

	private StateMachineMethodInvokerHelper(Object targetObject, Class<? extends Annotation> annotationType,
			String methodName, Class<?> expectedType) {
		Assert.notNull(targetObject, "targetObject must not be null");
		this.expectedType = expectedType;
		this.targetObject = targetObject;
		this.requiresReply = expectedType != null;
		Map<String, Map<Class<?>, HandlerMethod>> handlerMethodsForTarget = this.findHandlerMethodsForTarget(
				targetObject, annotationType, methodName, requiresReply);
		Map<Class<?>, HandlerMethod> handlerMethods = handlerMethodsForTarget.get(CANDIDATE_METHODS);
		Map<Class<?>, HandlerMethod> handlerMessageMethods = handlerMethodsForTarget.get(CANDIDATE_MESSAGE_METHODS);
		if ((handlerMethods.size() == 1 && handlerMessageMethods.isEmpty())
				|| (handlerMessageMethods.size() == 1 && handlerMethods.isEmpty())) {
			if (handlerMethods.size() == 1) {
				this.handlerMethod = handlerMethods.values().iterator().next();
			} else {
				this.handlerMethod = handlerMessageMethods.values().iterator().next();
			}
			this.handlerMethods = null;
			this.handlerMessageMethods = null;
			this.handlerMethodsList = null;
		} else {
			this.handlerMethod = null;
			this.handlerMethods = handlerMethods;
			this.handlerMessageMethods = handlerMessageMethods;
			this.handlerMethodsList = new LinkedList<Map<Class<?>, HandlerMethod>>();

			// TODO Consider to use global option to determine a precedence of
			// methods
			this.handlerMethodsList.add(this.handlerMethods);
			this.handlerMethodsList.add(this.handlerMessageMethods);
		}
		this.prepareEvaluationContext(this.getEvaluationContext(false), methodName, annotationType);
		this.setDisplayString(targetObject, methodName);
	}

	private void setDisplayString(Object targetObject, Object targetMethod) {
		StringBuilder sb = new StringBuilder(targetObject.getClass().getName());
		if (targetMethod instanceof Method) {
			sb.append("." + ((Method) targetMethod).getName());
		} else if (targetMethod instanceof String) {
			sb.append("." + targetMethod);
		}
		this.displayString = sb.toString() + "]";
	}

	private void prepareEvaluationContext(StandardEvaluationContext context, Object method,
			Class<? extends Annotation> annotationType) {
		Class<?> targetType = AopUtils.getTargetClass(this.targetObject);
		if (method instanceof Method) {
			context.registerMethodFilter(targetType, new FixedMethodFilter((Method) method));
			if (expectedType != null) {
				Assert.state(
						context.getTypeConverter().canConvert(
								TypeDescriptor.valueOf(((Method) method).getReturnType()),
								TypeDescriptor.valueOf(expectedType)), "Cannot convert to expected type ("
								+ expectedType + ") from " + method);
			}
		} else if (method == null || method instanceof String) {
			AnnotatedMethodFilter filter = new AnnotatedMethodFilter(annotationType, (String) method,
					this.requiresReply);
			Assert.state(canReturnExpectedType(filter, targetType, context.getTypeConverter()),
					"Cannot convert to expected type (" + expectedType + ") from " + method);
			context.registerMethodFilter(targetType, filter);
		}
		context.setVariable("target", targetObject);
	}

	private boolean canReturnExpectedType(AnnotatedMethodFilter filter, Class<?> targetType, TypeConverter typeConverter) {
		if (expectedType == null) {
			return true;
		}
		List<Method> methods = filter.filter(Arrays.asList(ReflectionUtils.getAllDeclaredMethods(targetType)));
		for (Method method : methods) {
			if (typeConverter.canConvert(TypeDescriptor.valueOf(method.getReturnType()), TypeDescriptor.valueOf(expectedType))) {
				return true;
			}
		}
		return false;
	}

	private T processInternal(ParametersWrapper<S, E> parameters) throws Exception {
		HandlerMethod candidate = this.findHandlerMethodForParameters(parameters);
		Assert.notNull(candidate, "No candidate methods found for messages.");
		Expression expression = candidate.getExpression();
		Class<?> expectedType = this.expectedType != null ? this.expectedType : candidate.method.getReturnType();
		try {
			@SuppressWarnings("unchecked")
			T result = (T) this.evaluateExpression(expression, parameters, expectedType);
			if (this.requiresReply) {
				Assert.notNull(result, "Expression evaluation result was null, but this processor requires a reply.");
			}
			return result;
		} catch (Exception e) {
			Throwable evaluationException = e;
			if (e instanceof EvaluationException && e.getCause() != null) {
				evaluationException = e.getCause();
			}
			if (evaluationException instanceof Exception) {
				throw (Exception) evaluationException;
			} else {
				throw new IllegalStateException("Cannot process message", evaluationException);
			}
		}
	}

	private Map<String, Map<Class<?>, HandlerMethod>> findHandlerMethodsForTarget(final Object targetObject,
			final Class<? extends Annotation> annotationType, final String methodName, final boolean requiresReply) {

		Map<String, Map<Class<?>, HandlerMethod>> handlerMethods = new HashMap<String, Map<Class<?>, HandlerMethod>>();

		final Map<Class<?>, HandlerMethod> candidateMethods = new HashMap<Class<?>, HandlerMethod>();
		final Map<Class<?>, HandlerMethod> candidateMessageMethods = new HashMap<Class<?>, HandlerMethod>();
		final Class<?> targetClass = this.getTargetClass(targetObject);
		MethodFilter methodFilter = new UniqueMethodFilter(targetClass);
		ReflectionUtils.doWithMethods(targetClass, new MethodCallback() {
			@Override
			public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
				boolean matchesAnnotation = false;
				if (method.isBridge()) {
					return;
				}
				if (isMethodDefinedOnObjectClass(method)) {
					return;
				}
				if (method.getDeclaringClass().equals(Proxy.class)) {
					return;
				}
				if (!Modifier.isPublic(method.getModifiers())) {
					return;
				}
				if (requiresReply && void.class.equals(method.getReturnType())) {
					return;
				}
				if (methodName != null && !methodName.equals(method.getName())) {
					return;
				}
				if (annotationType != null && AnnotationUtils.findAnnotation(method, annotationType) != null) {
					matchesAnnotation = true;
				}
				HandlerMethod handlerMethod = null;
				try {
					handlerMethod = new HandlerMethod(method);
				}
				catch (Exception e) {
					if (logger.isDebugEnabled()) {
						logger.debug("Method [" + method + "] is not eligible for container handling.", e);
					}
					return;
				}
				Class<?> targetParameterType = handlerMethod.getTargetParameterType();
				if (matchesAnnotation || annotationType == null) {
					if (handlerMethod.isMessageMethod()) {
						if (candidateMessageMethods.containsKey(targetParameterType)) {
							throw new IllegalArgumentException("Found more than one method match for type " +
									"[Message<" + targetParameterType + ">]");
						}
						candidateMessageMethods.put(targetParameterType, handlerMethod);
					} else {
						if (candidateMethods.containsKey(targetParameterType)) {
							String exceptionMessage = "Found more than one method match for ";
							if (Void.class.equals(targetParameterType)) {
								exceptionMessage += "empty parameter for 'payload'";
							} else {
								exceptionMessage += "type [" + targetParameterType + "]";
							}
							throw new IllegalArgumentException(exceptionMessage);
						}
						candidateMethods.put(targetParameterType, handlerMethod);
					}
				}
			}
		}, methodFilter);

		if (!candidateMethods.isEmpty() || !candidateMessageMethods.isEmpty()) {
			handlerMethods.put(CANDIDATE_METHODS, candidateMethods);
			handlerMethods.put(CANDIDATE_MESSAGE_METHODS, candidateMessageMethods);
			return handlerMethods;
		}

		Assert.state(!handlerMethods.isEmpty(), "Target object of type [" + this.targetObject.getClass()
				+ "] has no eligible methods for handling Container.");

		return handlerMethods;
	}

	private Class<?> getTargetClass(Object targetObject) {
		Class<?> targetClass = targetObject.getClass();
		if (AopUtils.isAopProxy(targetObject)) {
			targetClass = AopUtils.getTargetClass(targetObject);
			if (targetClass == targetObject.getClass()) {
				try {
					// Maybe a proxy with no target - e.g. gateway
					Class<?>[] interfaces = ((Advised) targetObject).getProxiedInterfaces();
					if (interfaces != null && interfaces.length == 1) {
						targetClass = interfaces[0];
					}
				}
				catch (Exception e) {
					if (logger.isDebugEnabled()) {
						logger.debug("Exception trying to extract interface", e);
					}
				}
			}
		}
		else if (org.springframework.util.ClassUtils.isCglibProxyClass(targetClass)) {
			Class<?> superClass = targetObject.getClass().getSuperclass();
			if (!Object.class.equals(superClass)) {
				targetClass = superClass;
			}
		}
		return targetClass;
	}

	private HandlerMethod findHandlerMethodForParameters(ParametersWrapper<S, E> parameters) {
		if (this.handlerMethod != null) {
			return this.handlerMethod;
		} else {
			return this.handlerMethods.get(Void.class);
		}
	}

	private static boolean isMethodDefinedOnObjectClass(Method method) {
		if (method == null) {
			return false;
		}
		if (method.getDeclaringClass().equals(Object.class)) {
			return true;
		}
		if (ReflectionUtils.isEqualsMethod(method) || ReflectionUtils.isHashCodeMethod(method)
				|| ReflectionUtils.isToStringMethod(method) || AopUtils.isFinalizeMethod(method)) {
			return true;
		}
		return (method.getName().equals("clone") && method.getParameterTypes().length == 0);
	}


	/**
	 * Helper class for generating and exposing metadata for a candidate handler method. The metadata includes the SpEL
	 * expression and the expected payload type.
	 */
	private static class HandlerMethod {

		private static final SpelExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

//		private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new LocalVariableTableParameterNameDiscoverer();

		private final Method method;

		private final Expression expression;

		private volatile TypeDescriptor targetParameterTypeDescriptor;

		private volatile Class<?> targetParameterType = Void.class;

		private volatile boolean messageMethod;

		HandlerMethod(Method method) {
			this.method = method;
			this.expression = this.generateExpression(method);
		}


		Expression getExpression() {
			return this.expression;
		}

		Class<?> getTargetParameterType() {
			return this.targetParameterType;
		}

		private boolean isMessageMethod() {
			return messageMethod;
		}

		@Override
		public String toString() {
			return this.method.toString();
		}

		private Expression generateExpression(Method method) {
			StringBuilder sb = new StringBuilder("#target." + method.getName() + "(");
			Class<?>[] parameterTypes = method.getParameterTypes();
			Annotation[][] parameterAnnotations = method.getParameterAnnotations();
			boolean hasUnqualifiedMapParameter = false;
			for (int i = 0; i < parameterTypes.length; i++) {
				if (i != 0) {
					sb.append(", ");
				}
				MethodParameter methodParameter = new MethodParameter(method, i);
				TypeDescriptor parameterTypeDescriptor = new TypeDescriptor(methodParameter);
				Class<?> parameterType = parameterTypeDescriptor.getObjectType();
				Annotation mappingAnnotation = findMappingAnnotation(parameterAnnotations[i]);
				if (mappingAnnotation != null) {
					Class<? extends Annotation> annotationType = mappingAnnotation.annotationType();

					if (annotationType.equals(EventHeaders.class)) {
						sb.append("headers");
					}

				} else if (ExtendedState.class.isAssignableFrom(parameterType)) {
					sb.append("extendedState");
				}
			}
			if (hasUnqualifiedMapParameter) {
				if (targetParameterType != null && Map.class.isAssignableFrom(this.targetParameterType)) {
					throw new IllegalArgumentException(
							"Unable to determine payload matching parameter due to ambiguous Map typed parameters. "
									+ "Consider adding the @Payload and or @Headers annotations as appropriate.");
				}
			}
			sb.append(")");
			if (this.targetParameterTypeDescriptor == null) {
				this.targetParameterTypeDescriptor = TypeDescriptor.valueOf(Void.class);
			}
			return EXPRESSION_PARSER.parseExpression(sb.toString());
		}

		private Annotation findMappingAnnotation(Annotation[] annotations) {
			if (annotations == null || annotations.length == 0) {
				return null;
			}
			Annotation match = null;
			for (Annotation annotation : annotations) {
				Class<? extends Annotation> type = annotation.annotationType();
				if (type.equals(EventHeaders.class)) {
					if (match != null) {
						throw new IllegalArgumentException(
								"At most one parameter annotation can be provided for message mapping, "
										+ "but found two: [" + match.annotationType().getName() + "] and ["
										+ annotation.annotationType().getName() + "]");
					}
					match = annotation;
				}
			}
			return match;
		}

	}

	/**
	 * Wrapping everything we need to work with spel.
	 */
	public class ParametersWrapper<SS, EE> {

		private final StateContext<SS, EE> stateContext;

		public ParametersWrapper(StateContext<SS, EE> stateContext) {
			this.stateContext = stateContext;
		}

		public StateContext<SS, EE> getStateContext() {
			return stateContext;
		}

		public Map<String, ?> getHeaders() {
			return stateContext.getMessageHeaders();
		}

		public ExtendedState getExtendedState() {
			return stateContext.getExtendedState();
		}

	}

}
