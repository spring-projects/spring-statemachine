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

import org.springframework.statemachine.config.common.annotation.AbstractConfiguredAnnotationBuilder;
import org.springframework.statemachine.config.common.annotation.AnnotationBuilder;
import org.springframework.statemachine.config.common.annotation.configurers.DefaultResourceConfigurer;
import org.springframework.statemachine.config.common.annotation.configurers.ResourceConfigurer;

/**
 * {@link AnnotationBuilder} for {@link ComplexTestConfigBeanB}.
 *
 * @author Janne Valkealahti
 *
 */
public class ComplexTestConfigBeanBBuilder
		extends AbstractConfiguredAnnotationBuilder<ComplexTestConfigBeanB, ComplexTestConfigBeanBConfigurer,ComplexTestConfigBeanBBuilder>
		implements ComplexTestConfigBeanBConfigurer {

	private String dataB;
	private String dataBB;

	@Override
	protected ComplexTestConfigBeanB performBuild() throws Exception {
		ComplexTestConfigBeanB bean = new ComplexTestConfigBeanB();
		bean.dataB = dataB;
		bean.dataBB = dataBB;
		return bean;
	}

	@Override
	public ComplexTestConfigBeanBConfigurer setData(String data) {
		this.dataB = data;
		return this;
	}

	@Override
	public ComplexTestConfigBeanBConfigurer setDataBB(String data) {
		this.dataBB = data;
		return this;
	}

	@Override
	public ResourceConfigurer<ComplexTestConfigBeanBConfigurer> withResources() throws Exception {
		return getOrApply(new DefaultResourceConfigurer<ComplexTestConfigBeanB,ComplexTestConfigBeanBConfigurer,ComplexTestConfigBeanBBuilder>());
	}

}
