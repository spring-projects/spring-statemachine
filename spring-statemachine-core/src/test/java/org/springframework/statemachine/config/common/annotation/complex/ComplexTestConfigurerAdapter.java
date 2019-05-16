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

import org.springframework.statemachine.config.common.annotation.AnnotationBuilder;

/**
 * Generic adapter example which user would extend
 * in @{@link org.springframework.context.annotation.Configuration}
 *
 * @author Janne Valkealahti
 *
 */
public class ComplexTestConfigurerAdapter implements ComplexTestConfigurer {

	private ComplexTestConfigBeanABuilder beanABuilder;
	private ComplexTestConfigBeanBBuilder beanBBuilder;

	@Override
	public final void init(ComplexTestConfigBuilder config) throws Exception {
		config.setSharedObject(String.class, "complexSharedData");
		config.setSharedObject(ComplexTestConfigBeanABuilder.class, getSimpleTestConfigBeanABuilder());
		config.setSharedObject(ComplexTestConfigBeanBBuilder.class, getSimpleTestConfigBeanBBuilder());
	}

	@Override
	public void configure(ComplexTestConfigBuilder config) throws Exception {
	}

	@Override
	public void configure(ComplexTestConfigBeanABuilder a) throws Exception {
	}

	@Override
	public void configure(ComplexTestConfigBeanBConfigurer b) throws Exception {
	}

	protected final ComplexTestConfigBeanBBuilder getSimpleTestConfigBeanBBuilder() throws Exception {
		if (beanBBuilder != null) {
			return beanBBuilder;
		}
		beanBBuilder = new ComplexTestConfigBeanBBuilder();
		configure(beanBBuilder);
		return beanBBuilder;
	}

	protected final ComplexTestConfigBeanABuilder getSimpleTestConfigBeanABuilder() throws Exception {
		if (beanABuilder != null) {
			return beanABuilder;
		}
		beanABuilder = new ComplexTestConfigBeanABuilder();
		configure(beanABuilder);
		return beanABuilder;
	}

	@Override
	public boolean isAssignable(AnnotationBuilder<ComplexTestConfig> builder) {
		return builder instanceof ComplexTestConfigBuilder;
	}

}
