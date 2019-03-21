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
package org.springframework.statemachine.config.common.annotation.complex;

import java.util.HashSet;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.statemachine.config.common.annotation.AbstractConfiguredAnnotationBuilder;
import org.springframework.statemachine.config.common.annotation.AnnotationBuilder;
import org.springframework.statemachine.config.common.annotation.configurers.DefaultResourceConfigurer;
import org.springframework.statemachine.config.common.annotation.configurers.ResourceConfigurer;
import org.springframework.statemachine.config.common.annotation.configurers.ResourceConfigurerAware;

/**
 * {@link AnnotationBuilder} for {@link ComplexTestConfigBeanA}.
 *
 * @author Janne Valkealahti
 *
 */
public class ComplexTestConfigBeanABuilder
		extends AbstractConfiguredAnnotationBuilder<ComplexTestConfigBeanA, ComplexTestConfigBeanABuilder, ComplexTestConfigBeanABuilder>
		implements ResourceConfigurerAware {

	private String data;
	private Set<Resource> resources = new HashSet<Resource>();

	@Override
	protected ComplexTestConfigBeanA performBuild() throws Exception {
		ComplexTestConfigBeanA bean = new ComplexTestConfigBeanA();
		bean.dataA = data;
		bean.resources = resources;
		return bean;
	}

	@Override
	public void configureResources(Set<Resource> resources) {
		this.resources.addAll(resources);
	}

	public Set<Resource> getResources() {
		return resources;
	}

	public ComplexTestConfigBeanABuilder setData(String data) {
		this.data = data;
		return this;
	}

	public ResourceConfigurer<ComplexTestConfigBeanABuilder> withResources() throws Exception {
		return getOrApply(new DefaultResourceConfigurer<ComplexTestConfigBeanA, ComplexTestConfigBeanABuilder,ComplexTestConfigBeanABuilder>());
	}

}
