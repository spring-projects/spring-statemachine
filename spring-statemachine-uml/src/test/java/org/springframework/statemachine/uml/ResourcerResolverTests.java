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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.statemachine.uml.ResourcerResolver.Holder;

public class ResourcerResolverTests {

	private ResourceLoader resourceLoader = new DefaultResourceLoader();
	// private String[] EMPTY_LOCATIONS = new String[0];

	// in a below tests,
	// we expect resources to resolve as a physical files

	@Test
	public void testOneMainLocation() throws Exception {
		String mainLocation = "classpath:org/springframework/statemachine/uml/import-main/import-main.uml";
		ResourcerResolver resolver = new ResourcerResolver(resourceLoader, mainLocation, null);
		Holder[] resolved = resolver.resolve();
		assertThat(resolved).isNotNull();
		assertThat(resolved.length).isEqualTo(1);
		assertThat(resolved[0].getUri().getPath()).isNotNull();
	}

	@Test
	public void testOneMainResource() throws Exception {
		Resource mainResource = new ClassPathResource("org/springframework/statemachine/uml/import-main/import-main.uml");
		ResourcerResolver resolver = new ResourcerResolver(mainResource, null);
		Holder[] resolved = resolver.resolve();
		assertThat(resolved).isNotNull();
		assertThat(resolved.length).isEqualTo(1);
		assertThat(resolved[0].getUri().getPath()).isNotNull();
	}

	@Test
	public void testMultipleLocations() throws Exception {
		String mainLocation = "classpath:org/springframework/statemachine/uml/import-main/import-main.uml";
		String subLocation = "classpath:org/springframework/statemachine/uml/import-sub/import-sub.uml";
		ResourcerResolver resolver = new ResourcerResolver(resourceLoader, mainLocation, new String[]{subLocation});
		Holder[] resolved = resolver.resolve();
		assertThat(resolved).isNotNull();
		assertThat(resolved.length).isEqualTo(2);
		assertThat(resolved[0].getUri().getPath()).isNotNull();
		assertThat(resolved[1].getUri().getPath()).isNotNull();
	}

	@Test
	public void testMultipleResources() throws Exception {
		Resource mainResource = new ClassPathResource("org/springframework/statemachine/uml/import-main/import-main.uml");
		Resource subResource = new ClassPathResource("org/springframework/statemachine/uml/import-sub/import-sub.uml");
		ResourcerResolver resolver = new ResourcerResolver(mainResource, new Resource[]{subResource});
		Holder[] resolved = resolver.resolve();
		assertThat(resolved).isNotNull();
		assertThat(resolved.length).isEqualTo(2);
		assertThat(resolved[0].getUri().getPath()).isNotNull();
		assertThat(resolved[1].getUri().getPath()).isNotNull();
	}

	// in a below tests,
	// do monkey thing and setup resource which will not resolve to
	// an actual file as would happen with file inside boot fat-jar classpath

	@Test
	public void testOneMainResourceNotPhysicalFile() throws Exception {
		Resource mainResource = new TestClassPathResource("org/springframework/statemachine/uml/import-main/import-main.uml");
		ResourcerResolver resolver = new ResourcerResolver(mainResource, null);
		Holder[] resolved = resolver.resolve();
		assertThat(resolved).isNotNull();
		assertThat(resolved.length).isEqualTo(1);
		assertThat(resolved[0].getUri().getPath()).isNotNull();
		assertThat(resolved[0].getPath().toFile().exists()).isTrue();
	}

	@Test
	public void testMultipleResourcesNotPhysicalFile() throws Exception {
		Resource mainResource = new TestClassPathResource("org/springframework/statemachine/uml/import-main/import-main.uml");
		Resource subResource = new TestClassPathResource("org/springframework/statemachine/uml/import-sub/import-sub.uml");
		ResourcerResolver resolver = new ResourcerResolver(mainResource, new Resource[]{subResource});
		Holder[] resolved = resolver.resolve();
		assertThat(resolved).isNotNull();
		assertThat(resolved.length).isEqualTo(2);
		assertThat(resolved[0].getUri().getPath()).isNotNull();
		assertThat(resolved[0].getPath().toFile().exists()).isTrue();
		assertThat(resolved[1].getUri().getPath()).isNotNull();
		assertThat(resolved[1].getPath().toFile().exists()).isTrue();
	}

	private static class TestClassPathResource extends ClassPathResource {

		public TestClassPathResource(String path) {
			super(path);
		}

		@Override
		public File getFile() throws IOException {
			throw new IOException();
		}
	}
}
