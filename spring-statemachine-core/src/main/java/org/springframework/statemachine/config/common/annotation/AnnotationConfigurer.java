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
package org.springframework.statemachine.config.common.annotation;

/**
 * Allows for configuring an {@link AnnotationBuilder}. All
 * {@link AnnotationConfigurer}s first have their {@link #init(AnnotationBuilder)}
 * method invoked. After all {@link #init(AnnotationBuilder)} methods have been
 * invoked, each {@link #configure(AnnotationBuilder)} method is invoked.
 *
 * @see AbstractConfiguredAnnotationBuilder
 *
 * @author Rob Winch
 * @author Janne Valkealahti
 *
 * @param <O> The object being built by the {@link AnnotationBuilder} B
 * @param <B> The {@link AnnotationBuilder} that builds objects of type O. This is
 *            also the {@link AnnotationBuilder} that is being configured.
 */
public interface AnnotationConfigurer<O, B extends AnnotationBuilder<O>> {

	/**
	 * Initialise the {@link AnnotationBuilder}. Here only shared state should be
	 * created and modified, but not properties on the {@link AnnotationBuilder}
	 * used for building the object. This ensures that the
	 * {@link #configure(AnnotationBuilder)} method uses the correct shared
	 * objects when building.
	 *
	 * @param builder the builder
	 * @throws Exception if error occurred
	 */
	void init(B builder) throws Exception;

	/**
	 * Configure the {@link AnnotationBuilder} by setting the necessary properties
	 * on the {@link AnnotationBuilder}.
	 *
	 * @param builder the builder
	 * @throws Exception if error occurred
	 */
	void configure(B builder) throws Exception;

	boolean isAssignable(AnnotationBuilder<O> builder);

}
