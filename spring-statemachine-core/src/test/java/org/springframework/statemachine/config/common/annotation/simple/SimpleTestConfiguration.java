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
package org.springframework.statemachine.config.common.annotation.simple;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.common.annotation.AbstractAnnotationConfiguration;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurer;

/**
 * @{@link Configuration} which is imported from @{@ EnableSimpleTest}.
 *
 * @author Janne Valkealahti
 *
 */
@Configuration
public class SimpleTestConfiguration extends AbstractAnnotationConfiguration<SimpleTestConfigBuilder, SimpleTestConfig> {

	private final SimpleTestConfigBuilder builder = new SimpleTestConfigBuilder();

	@Bean(name="simpleConfig")
	public SimpleTestConfig simpleTestConfig() {
		SimpleTestConfig config = builder.getOrBuild();
		return config;
	}

	@Bean(name="simpleConfigData")
	public String simpleTestConfigData() {
		SimpleTestConfig config = builder.getOrBuild();
		return config.simpleData;
	}

	@Bean(name="simpleConfigBeanB")
	public SimpleTestConfigBeanB simpleTestConfigBeanB() {
		SimpleTestConfig config = builder.getOrBuild();
		return config.simpleBeanB;
	}

	@Override
	protected void onConfigurers(List<AnnotationConfigurer<SimpleTestConfig, SimpleTestConfigBuilder>> configurers)	throws Exception {
		for (AnnotationConfigurer<SimpleTestConfig, SimpleTestConfigBuilder> configurer : configurers) {
			if (configurer.isAssignable(builder)) {
				builder.apply(configurer);
			}
		}
	}

}