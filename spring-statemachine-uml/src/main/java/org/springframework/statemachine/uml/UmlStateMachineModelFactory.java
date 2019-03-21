/*
 * Copyright 2016-2018 the original author or authors.
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.UMLPackage;
import org.springframework.core.io.Resource;
import org.springframework.statemachine.config.model.AbstractStateMachineModelFactory;
import org.springframework.statemachine.config.model.DefaultStateMachineModel;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.uml.support.UmlModelParser;
import org.springframework.statemachine.uml.support.UmlModelParser.DataHolder;
import org.springframework.statemachine.uml.support.UmlUtils;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

/**
 * {@link StateMachineModelFactory} which builds {@link StateMachineModel} from
 * uml representation.
 *
 * @author Janne Valkealahti
 */
public class UmlStateMachineModelFactory extends AbstractStateMachineModelFactory<String, String>
		implements StateMachineModelFactory<String, String> {

	private Resource resource;
	private String location;

	/**
	 * Instantiates a new uml state machine model factory.
	 *
	 * @param resource the resource
	 */
	public UmlStateMachineModelFactory(Resource resource) {
		Assert.notNull(resource, "Resource must be set");
		this.resource = resource;
	}

	/**
	 * Instantiates a new uml state machine model factory.
	 *
	 * @param location the resource location
	 */
	public UmlStateMachineModelFactory(String location) {
		Assert.notNull(location, "Location must be set");
		this.location = location;
	}

	@Override
	public StateMachineModel<String, String> build() {
		Model model = null;
		Holder holder = null;
		org.eclipse.emf.ecore.resource.Resource resource = null;
		try {
			holder = getResourceUri(resolveResource());
			resource = UmlUtils.getResource(holder.uri.getPath());
			model = (Model) EcoreUtil.getObjectByType(resource.getContents(), UMLPackage.Literals.MODEL);
		} catch (IOException e) {
			throw new IllegalArgumentException("Cannot build build model from resource " + resource + " or location " + location, e);
		} finally {
			// if we have a path, tmp file were created, clean it
			if (holder != null && holder.path != null) {
				try {
					Files.deleteIfExists(holder.path);
				} catch (Exception e2) {
				}
			}
		}
		UmlModelParser parser = new UmlModelParser(model, this);
		DataHolder dataHolder = parser.parseModel();

		// clean up
		if (resource != null) {
			try {
				resource.unload();
			} catch (Exception e) {
			}
		}
		if (holder != null && holder.path != null) {
			try {
				Files.deleteIfExists(holder.path);
			} catch (Exception e2) {
			}
		}

		// we don't set configurationData here, so assume null
		return new DefaultStateMachineModel<String, String>(null, dataHolder.getStatesData(), dataHolder.getTransitionsData());
	}

	private Resource resolveResource() {
		if (resource != null) {
			return resource;
		} else {
			return getResourceLoader().getResource(location);
		}
	}

	private Holder getResourceUri(Resource resource) throws IOException {
		// try to see if resource is an actual File and eclipse
		// libs cannot use input stream. thus creating a tmp file with
		// needed .uml prefix and getting URI from there.
		try {
			return new Holder(resource.getFile().toURI());
		} catch (Exception e) {
		}
		Path tempFile = Files.createTempFile(null, ".uml");
		FileCopyUtils.copy(resource.getInputStream(), new FileOutputStream(tempFile.toFile()));
		return new Holder(tempFile.toUri(), tempFile);
	}

	private static class Holder {
		URI uri;
		Path path;

		public Holder(URI uri) {
			this(uri, null);
		}

		public Holder(URI uri, Path path) {
			this.uri = uri;
			this.path = path;
		}
	}
}
