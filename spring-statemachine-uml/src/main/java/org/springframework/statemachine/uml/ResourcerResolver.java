/*
 * Copyright 2020 the original author or authors.
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
package org.springframework.statemachine.uml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Stream;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.statemachine.StateMachineException;
import org.springframework.util.FileCopyUtils;

/**
 * Support class to resolve uml resources and handling a cases where things
 * needs to be copied away from a classpath trying to handle common relative
 * paths with a locations or resource paths in case of a cross relative linking.
 *
 * @author Janne Valkealahti
 */
class ResourcerResolver {

	private ResourceLoader resourceLoader;
	private Resource mainResource;
	private String mainLocation;
	private Resource[] additionalResources;
	private String[] additionalLocations;

	public ResourcerResolver(Resource mainResource, Resource[] additionalResources) {
		this.mainResource = mainResource;
		this.additionalResources = additionalResources != null ? additionalResources : new Resource[0];
	}

	public ResourcerResolver(ResourceLoader resourceLoader, String mainLocation, String[] additionalLocations) {
		this.resourceLoader = resourceLoader;
		this.mainLocation = mainLocation;
		this.additionalLocations = additionalLocations != null ? additionalLocations : new String[0];
	}

	public Holder[] resolve() {
		ArrayList<Holder> holders = new ArrayList<>();
		if (mainLocation != null) {
			Resource[] resources = Stream.concat(Stream.of(mainLocation), Stream.of(additionalLocations))
				.map(location -> resourceLoader.getResource(location))
				.toArray(Resource[]::new);
			return getResourceUris(resources);
		} else if (mainResource != null) {
			Resource[] resources = Stream.concat(Stream.of(mainResource), Stream.of(additionalResources))
				.toArray(Resource[]::new);
			return getResourceUris(resources);
		}
		return holders.toArray(new Holder[0]);
	}

	private Holder[] getResourceUris(Resource... resources) {
		ArrayList<Holder> holders = new ArrayList<>();
		try {
			if (allPhysical(resources)) {
				for (Resource resource : resources) {
					holders.add(new Holder(resource.getFile().toURI()));
				}
			} else {
				Path tempDir = Files.createTempDirectory(null);
				for (Resource resource : resources) {
					if (resource instanceof ClassPathResource) {
						ClassPathResource cpr = (ClassPathResource)resource;
						File f = new File(tempDir.toFile(), cpr.getPath());
						f.getParentFile().mkdirs();
						FileCopyUtils.copy(resource.getInputStream(), new FileOutputStream(f));
						holders.add(new Holder(f.toURI(), f.toPath()));
					}
				}
			}
		} catch (IOException e) {
			throw new StateMachineException(e);
		}
		return holders.toArray(new Holder[0]);
	}

	private boolean isPhysical(Resource resource) {
		try {
			resource.getFile();
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	private boolean allPhysical(Resource... resources) {
		for (Resource resource : resources) {
			if (!isPhysical(resource)) {
				return false;
			}
		}
		return true;
	}

	static class Holder {
		URI uri;
		Path path;

		public Holder(URI uri) {
			this(uri, null);
		}

		public Holder(URI uri, Path path) {
			this.uri = uri;
			this.path = path;
		}

		public URI getUri() {
			return uri;
		}

		public Path getPath() {
			return path;
		}
	}
}
