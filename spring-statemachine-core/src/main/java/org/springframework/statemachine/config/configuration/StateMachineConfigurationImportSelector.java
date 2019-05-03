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
package org.springframework.statemachine.config.configuration;

import java.util.Map;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;

/**
 * Spring {@link ImportSelector} choosing appropriate {@link Configuration}
 * based on {@link EnableStateMachine} or {@link EnableStateMachineFactory} annotations.
 *
 * @author Janne Valkealahti
 *
 */
public class StateMachineConfigurationImportSelector implements ImportSelector {

	@Override
	public String[] selectImports(AnnotationMetadata importingClassMetadata) {
		Map<String, Object> attrMap = importingClassMetadata.getAnnotationAttributes(EnableStateMachine.class.getName());
		if (attrMap == null) {
			attrMap = importingClassMetadata.getAnnotationAttributes(EnableStateMachineFactory.class.getName());
		}

		if (attrMap != null && AnnotationAttributes.fromMap(attrMap).getBoolean("contextEvents")) {
			return new String[] { "org.springframework.statemachine.event.StateMachineEventPublisherConfiguration",
					"org.springframework.statemachine.config.configuration.StateMachineCommonConfiguration" };
		} else {
			return new String[0];
		}
	}

}
