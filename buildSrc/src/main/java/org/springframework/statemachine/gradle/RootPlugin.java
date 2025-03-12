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

import java.io.File;

import org.asciidoctor.gradle.jvm.AsciidoctorJPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.CoreJavadocOptions;

/**
 * Manages tasks creating zip file for docs and publishing it.
 *
 * @author Janne Valkealahti
 */
class RootPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		PluginManager pluginManager = project.getPluginManager();
		pluginManager.apply(MavenPublishPlugin.class);
		pluginManager.apply(PublishLocalPlugin.class);
		pluginManager.apply(SpringMavenPlugin.class);
		new ArtifactoryConventions().apply(project);
		Javadoc apiTask = createApiTask(project);
		Zip zipTask = createZipTask(project);
		zipTask.dependsOn(apiTask);
	}

	private Zip createZipTask(Project project) {
		Zip zipTask = project.getTasks().create("distZip", Zip.class, zip -> {
			zip.setGroup("Distribution");
			zip.from("docs/build/docs/asciidoc/", copy -> {
				copy.into("docs");
			});
			zip.from("build/api", copy -> {
				copy.into("api");
			});
		});

		project.getRootProject().getAllprojects().forEach(p -> {
			p.getPlugins().withType(AsciidoctorJPlugin.class, a -> {
				p.getTasksByName("asciidoctor", false).forEach(t -> {
					zipTask.dependsOn(t);
				});;
			});
		});

		project.getPlugins().withType(MavenPublishPlugin.class).all(mavenPublish -> {
			PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
			publishing.getPublications().withType(MavenPublication.class)
				.all(mavenPublication -> {
					mavenPublication.artifact(zipTask);
				});
		});

		return zipTask;
	}

	private Javadoc createApiTask(Project project) {
		Javadoc api = project.getTasks().create("api", Javadoc.class, a -> {
			a.setGroup("Documentation");
			a.setDescription("Generates aggregated Javadoc API documentation.");
			a.setDestinationDir(new File(project.getBuildDir(), "api"));
			CoreJavadocOptions options = (CoreJavadocOptions) a.getOptions();
			options.source("17");
			options.encoding("UTF-8");
			options.addStringOption("Xdoclint:none", "-quiet");
		});

		project.getRootProject().getSubprojects().forEach(p -> {
			p.getPlugins().withType(ModulePlugin.class, m -> {
				JavaPluginConvention java = p.getConvention().getPlugin(JavaPluginConvention.class);
				SourceSet mainSourceSet = java.getSourceSets().getByName("main");

				api.setSource(api.getSource().plus(mainSourceSet.getAllJava()));

				p.getTasks().withType(Javadoc.class, j -> {
					api.setClasspath(api.getClasspath().plus(j.getClasspath()));
				});
			});
		});
		return api;
	}
}
