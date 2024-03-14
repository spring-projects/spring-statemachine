/*
 * Copyright 2024 the original author or authors.
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

import org.gradle.api.Project;
import org.gradle.plugins.ide.api.XmlFileContentMerger;
import org.gradle.plugins.ide.eclipse.EclipsePlugin;
import org.gradle.plugins.ide.eclipse.model.AbstractClasspathEntry;
import org.gradle.plugins.ide.eclipse.model.Classpath;
import org.gradle.plugins.ide.eclipse.model.ClasspathEntry;
import org.gradle.plugins.ide.eclipse.model.EclipseClasspath;
import org.gradle.plugins.ide.eclipse.model.EclipseModel;

class EclipseConventions {

	void apply(Project project) {
		project.getPlugins().withType(EclipsePlugin.class, (eclipse) -> {
			EclipseModel eclipseModel = project.getExtensions().getByType(EclipseModel.class);
			eclipseModel.classpath(this::configureClasspath);
		});
	}

	private void configureClasspath(EclipseClasspath classpath) {
		classpath.file(this::configureClasspathFile);
	}

	private void configureClasspathFile(XmlFileContentMerger merger) {
		merger.whenMerged((content) -> {
			if (content instanceof Classpath) {
				Classpath classpath = (Classpath) content;
				for (ClasspathEntry entry : classpath.getEntries()) {
					if (entry instanceof AbstractClasspathEntry) {
						AbstractClasspathEntry ace = (AbstractClasspathEntry) entry;
						Object value = ace.getEntryAttributes().get("test");
						if (value instanceof String) {
							if (Boolean.parseBoolean((String) value)) {
								ace.getEntryAttributes().put("test", "false");
							}
						}
					}
				}
			}
		});
	}

}
