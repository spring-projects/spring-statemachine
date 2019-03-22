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

import java.util.List;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.statemachine.config.common.annotation.AnnotationConfigurerBuilder;

/**
 * Interface for {@link DefaultResourceConfigurer} which act
 * as intermediate gatekeeper between a user and
 * an {@link org.springframework.statemachine.config.common.annotation.AnnotationConfigurer}.
 *
 * @author Janne Valkealahti
 *
 * @param <I> The parent return type of the configurer.
 */
public interface ResourceConfigurer<I> extends AnnotationConfigurerBuilder<I> {

	ResourceConfigurer<I> resources(Set<Resource> resources);

	ResourceConfigurer<I> resources(List<String> resources);

	ResourceConfigurer<I> resource(Resource resource);

	ResourceConfigurer<I> resource(String resource);

}
