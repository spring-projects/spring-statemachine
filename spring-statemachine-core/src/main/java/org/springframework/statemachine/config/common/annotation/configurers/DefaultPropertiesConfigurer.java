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

import java.util.Map;
import java.util.Properties;

import org.springframework.statemachine.config.common.annotation.AnnotationBuilder;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurerAdapter;

/**
 * {@link org.springframework.statemachine.config.common.annotation.AnnotationConfigurer AnnotationConfigurer}
 * which knows how to handle configuring a {@link Properties}.
 *
 * @author Janne Valkealahti
 *
 * @param <O> The Object being built by B
 * @param <I> The type of interface or builder itself returned by the configurer
 * @param <B> The Builder that is building O and is configured by {@link AnnotationConfigurerAdapter}
 */
public class DefaultPropertiesConfigurer<O,I,B extends AnnotationBuilder<O>>
		extends AnnotationConfigurerAdapter<O,I,B> implements PropertiesConfigurer<I> {

	private Properties properties = new Properties();

	/**
	 * Adds a {@link Properties} to this builder.
	 *
	 * @param properties the properties
	 * @return the {@link PropertiesConfigurer} for chaining
	 */
	@Override
	public PropertiesConfigurer<I> properties(Properties properties) {
		if (properties != null) {
			this.properties.putAll(properties);
		}
		return this;
	}

	@Override
	public PropertiesConfigurer<I> properties(Map<String, String> properties) {
		Properties props = new Properties();
		if (properties != null) {
			props.putAll(properties);
		}
		return properties(props);
	}

	/**
	 * Adds a property to this builder.
	 *
	 * @param key the key
	 * @param value the value
	 * @return the {@link PropertiesConfigurer} for chaining
	 */
	@Override
	public PropertiesConfigurer<I> property(String key, String value) {
		properties.put(key, value);
		return this;
	}

	/**
	 * Gets the {@link Properties} configured for this builder.
	 *
	 * @return the properties
	 */
	public Properties getProperties() {
		return properties;
	}

	@Override
	public void configure(B builder) throws Exception {
		if (!configureProperties(builder, properties)) {
			if (builder instanceof PropertiesConfigurerAware) {
				((PropertiesConfigurerAware)builder).configureProperties(properties);
			}
		}
	}

	/**
	 * Configure properties. If this implementation is extended,
	 * custom configure handling can be handled here.
	 *
	 * @param builder the builder
	 * @param properties the properties
	 * @return true, if properties configure is handled
	 */
	protected boolean configureProperties(B builder, Properties properties){
		return false;
	};

}
