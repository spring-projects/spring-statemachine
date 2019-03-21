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

import java.util.Properties;

/**
 * Main pojo used to collect together configs.
 *
 * @author Janne Valkealahti
 *
 */
public class SimpleTestConfig {

	public String simpleData;
	public Properties simpleProperties;
	public SimpleTestConfigBeanA simpleBeanA;
	public SimpleTestConfigBeanB simpleBeanB;

	public SimpleTestConfig(String config, Properties properties) {
		simpleData = config;
		simpleProperties = properties;
	}

	public void setSimpleBeanB(SimpleTestConfigBeanB simpleBeanB) {
		this.simpleBeanB = simpleBeanB;
	}

	public void setSimpleBeanA(SimpleTestConfigBeanA simpleBeanA) {
		this.simpleBeanA = simpleBeanA;
	}

}