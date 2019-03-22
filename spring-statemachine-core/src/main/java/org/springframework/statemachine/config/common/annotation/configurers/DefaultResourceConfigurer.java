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
package org.springframework.statemachine.config.common.annotation.configurers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.statemachine.config.common.annotation.AnnotationBuilder;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurer;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurerAdapter;

/**
 * {@link AnnotationConfigurer} which knows how to handle
 * configuring a {@link Resource}s.
 *
 * @author Janne Valkealahti
 *
 * @param <O> The Object being built by B
 * @param <B> The Builder that is building O and is configured by {@link AnnotationConfigurerAdapter}
 * @param <I> The type of an interface of B
 */
public class DefaultResourceConfigurer<O,I,B extends AnnotationBuilder<O>>
		extends AnnotationConfigurerAdapter<O,I,B> implements ResourceConfigurer<I> {

	private Set<Resource> resources = new HashSet<Resource>();
	private final DefaultResourceLoader resourceLoader = new DefaultResourceLoader();

	@Override
	public void configure(B builder) throws Exception {
		if (!configureResources(builder, resources)) {
			if (builder instanceof ResourceConfigurerAware) {
				((ResourceConfigurerAware)builder).configureResources(resources);
			}
		}
	}

	/**
	 * Adds a {@link Set} of {@link Resource}s to this builder.
	 *
	 * @param resources the resources
	 * @return the {@link ResourceConfigurer} for chaining
	 */
	@Override
	public ResourceConfigurer<I> resources(Set<Resource> resources) {
		this.resources.addAll(resources);
		return this;
	}

	/**
	 * Adds a {@link Resource} to this builder.
	 *
	 * @param resource the resource
	 * @return the {@link ResourceConfigurer} for chaining
	 */
	@Override
	public ResourceConfigurer<I> resource(Resource resource) {
		resources.add(resource);
		return this;
	}

	/**
	 * Adds a {@link Resource} to this builder.
	 *
	 * @param resource the resource
	 * @return the {@link ResourceConfigurer} for chaining
	 */
	@Override
	public ResourceConfigurer<I> resource(String resource) {
		resources.add(resourceLoader.getResource(resource));
		return this;
	}

	/**
	 * Adds a {@link Resource}s to this builder.
	 *
	 * @param resources the resources
	 * @return the {@link ResourceConfigurer} for chaining
	 */
	@Override
	public ResourceConfigurer<I> resources(List<String> resources) {
		if (resources != null) {
			for (String resource : resources) {
				resource(resource);
			}
		}
		return this;
	}

	/**
	 * Gets the {@link Resource}s configured for this builder.
	 *
	 * @return the resources
	 */
	public Set<Resource> getResources() {
		return resources;
	}

	/**
	 * Configure resources. If this implementation is extended,
	 * custom configure handling can be handled here.
	 *
	 * @param builder the builder
	 * @param resources the resources
	 * @return true, if resources configure is handled
	 */
	protected boolean configureResources(B builder, Set<Resource> resources){
		return false;
	};

}
