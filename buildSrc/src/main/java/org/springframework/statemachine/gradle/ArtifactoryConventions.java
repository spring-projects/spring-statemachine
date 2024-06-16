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

import java.util.HashMap;
import java.util.Map;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.publish.tasks.GenerateModuleMetadata;
import org.jfrog.build.extractor.clientConfiguration.ArtifactSpec;
import org.jfrog.build.extractor.clientConfiguration.ArtifactSpecs;
import org.jfrog.gradle.plugin.artifactory.ArtifactoryPlugin;
import org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask;

/**
 * @author Janne Valkealahti
 */
public class ArtifactoryConventions {

	void apply(Project project) {
		PluginManager pluginManager = project.getPluginManager();
		pluginManager.apply(ArtifactoryPlugin.class);

		project.getTasks().withType(GenerateModuleMetadata.class, metadata -> {
			metadata.setEnabled(false);
		});

		project.getPlugins().withType(ArtifactoryPlugin.class, artifactory -> {
			Task task = project.getTasks().findByName(ArtifactoryTask.ARTIFACTORY_PUBLISH_TASK_NAME);
			if (task != null) {
				ArtifactoryTask aTask = (ArtifactoryTask) task;
				aTask.setCiServerBuild();
				// bom is not a java project so plugin doesn't
				// add defaults for publications.
				aTask.publications("mavenJava");

				// plugin is difficult to work with, use this hack
				// to set props before task does its real work
				task.doFirst(t -> {
					// this needs mods if we ever have zips other than
					// docs zip having asciidoc/javadoc.
					ArtifactoryTask at = (ArtifactoryTask) t;
					ArtifactSpecs artifactSpecs = at.getArtifactSpecs();
					Map<String, String> propsMap = new HashMap<>();
					propsMap.put("zip.deployed", "false");
					propsMap.put("zip.type", "docs");
					ArtifactSpec spec = ArtifactSpec.builder()
						.artifactNotation("*:*:*:*@zip")
						// archives is manually set for zip in root plugin
						.configuration("mavenJava")
						.properties(propsMap)
						.build();
					artifactSpecs.add(spec);
				});
			}
		});
	}
}
