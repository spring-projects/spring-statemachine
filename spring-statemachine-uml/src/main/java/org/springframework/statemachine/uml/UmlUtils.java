/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.statemachine.uml;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.PseudostateKind;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;

/**
 * Utilities for uml model processing.
 *
 * @author Janne Valkealahti
 */
public abstract class UmlUtils {

	/**
	 * Gets the model.
	 *
	 * @param modelPath the model path
	 * @return the model
	 */
	public static Model getModel(String modelPath) {
		URI modelUri = URI.createFileURI(modelPath);
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getPackageRegistry().put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
		resourceSet.createResource(modelUri);
		Resource resource = resourceSet.getResource(modelUri, true);
		Model m = (Model) EcoreUtil.getObjectByType(resource.getContents(), UMLPackage.Literals.MODEL);
		return m;
	}

	/**
	 * Checks if {@link State} is an initial state by checking if
	 * it has incoming transition from UML's initial literal.
	 *
	 * @param state the state
	 * @return true, if is initial state
	 */
	public static boolean isInitialState(State state) {
		for (Transition t : state.getIncomings()) {
			if (t.getSource() instanceof Pseudostate) {
				if (((Pseudostate)t.getSource()).getKind() == PseudostateKind.INITIAL_LITERAL) {
					return true;
				}
			}
		}
		return false;
	}
}
