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
package org.springframework.statemachine.config.common.annotation.configuration;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.common.annotation.EnableAnnotationConfiguration;
import org.springframework.statemachine.config.common.annotation.ObjectPostProcessor;

/**
 * Spring {@link Configuration} that exports the default
 * {@link ObjectPostProcessor}. This class is not intended to be imported
 * manually rather it is imported automatically when using {@link EnableAnnotationConfiguration}
 *
 * @author Rob Winch
 * @author Janne Valkealahti
 *
 * @see EnableAnnotationConfiguration
 */
@Configuration
public class ObjectPostProcessorConfiguration {

	private static final String POST_PROCESSOR_BEAN_ID = "org.springframework.statemachine.config.common.annotation.configuration.objectPostProcessor";

	@Bean(name = POST_PROCESSOR_BEAN_ID)
	public ObjectPostProcessor<Object> objectPostProcessor(AutowireCapableBeanFactory beanFactory) {
		return new AutowireBeanFactoryObjectPostProcessor(beanFactory);
	}

}
