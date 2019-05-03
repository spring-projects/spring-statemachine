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

/**
 * Strategy interface for post-processing annotated methods.
 *
 * @author Mark Fisher
 * @author Janne Valkealahti
 *
 * @param <T> the type of an annotation
 */
public interface MethodAnnotationPostProcessor<T extends Annotation> {

	/**
	 * Post process a bean. As a result of a given bean, its name, method and
	 * annotation in a method, this method can return a new bean or
	 * <code>null</code>. Caller of this method is then responsible to handle
	 * newly created object.
	 *
	 * @param beanClass the bean class
	 * @param bean the bean
	 * @param beanName the bean name
	 * @param method the method
	 * @param metaAnnotation the meta annotation
	 * @param annotation the annotation
	 * @return the post processed object
	 */
	Object postProcess(Class<?> beanClass, Object bean, String beanName, Method method, T metaAnnotation, Annotation annotation);

}
