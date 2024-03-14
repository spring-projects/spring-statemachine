/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyConstraintHandler;
import org.gradle.api.plugins.JavaPlatformPlugin;
import org.gradle.api.plugins.PluginManager;

/**
 * @author Janne Valkealahti
 */
class BomPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		PluginManager pluginManager = project.getPluginManager();
		pluginManager.apply(SpringMavenPlugin.class);
		pluginManager.apply(JavaPlatformPlugin.class);
		new ArtifactoryConventions().apply(project);

		// bom should have main modules user can consume
		DependencyConstraintHandler constraints = project.getDependencies().getConstraints();
		project.getRootProject().getAllprojects().forEach(p -> {
			p.getPlugins().withType(ModulePlugin.class, m -> {
				constraints.add("api", p);
			});
			p.getPlugins().withType(StarterPlugin.class, m -> {
				constraints.add("api", p);
			});
		});
	}
}
