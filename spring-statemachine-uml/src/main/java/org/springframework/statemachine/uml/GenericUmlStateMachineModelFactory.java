/*
 * Copyright 2016-2021 the original author or authors.
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

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.UMLPackage;
import org.springframework.core.io.Resource;
import org.springframework.statemachine.config.model.AbstractStateMachineModelFactory;
import org.springframework.statemachine.config.model.DefaultStateMachineModel;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.uml.support.GenericTypeConverter;
import org.springframework.statemachine.uml.support.GenericUmlModelParser;
import org.springframework.statemachine.uml.support.UmlUtils;
import org.springframework.util.Assert;

import java.nio.file.Files;

/**
 * {@link StateMachineModelFactory} which builds {@link StateMachineModel} from
 * uml representation using the generic types specified for state and event.
 *
 * {@code resource} or {@code location} is a main uml file used as a source
 * passed to parser classes. {@code additionalResources} and {@code additionalLocations}
 * are needed if uml model have references or links to additional uml files as an
 * import. In case of a these files being located in a classpath which is inside of
 * a jar, files are copied out into filesystem as eclipse uml libs can only parse
 * physical files. In a case of this a common "path" from all resources are resolved
 * and copied into filesystem with a structure so that at least relative links in uml
 * files will work.
 *
 * @author Janne Valkealahti
 */
public class GenericUmlStateMachineModelFactory<S, E> extends AbstractStateMachineModelFactory<S, E> {

    private Resource resource;
    private String location;
    private Resource[] additionalResources;
    private String[] additionalLocations;
    private final GenericTypeConverter<S> stateAdapter;
    private final GenericTypeConverter<E> eventAdapter;

    /**
     * Instantiates a new uml state machine model factory.
     *  @param resource the resource
     * @param stateAdapter
     * @param eventAdapter
     */
    public GenericUmlStateMachineModelFactory(Resource resource, GenericTypeConverter<S> stateAdapter, GenericTypeConverter<E> eventAdapter) {
        this(resource, null, stateAdapter, eventAdapter);
    }

    /**
     * Instantiates a new uml state machine model factory.
     *  @param location the resource location
     * @param stateAdapter
     * @param eventAdapter
     */
    public GenericUmlStateMachineModelFactory(String location, GenericTypeConverter<S> stateAdapter, GenericTypeConverter<E> eventAdapter) {
        this(location, null, stateAdapter, eventAdapter);
    }

    /**
     * Instantiates a new uml state machine model factory.
     * @param resource the resource
     * @param additionalResources the additional resources
     * @param stateAdapter
     * @param eventAdapter
     */
    public GenericUmlStateMachineModelFactory(Resource resource, Resource[] additionalResources, GenericTypeConverter<S> stateAdapter, GenericTypeConverter<E> eventAdapter) {
        this.stateAdapter = stateAdapter;
        this.eventAdapter = eventAdapter;
        Assert.notNull(resource, "Resource must be set");
        this.resource = resource;
        this.additionalResources = additionalResources;
    }

    /**
     * Instantiates a new uml state machine model factory.
     * @param location the resource location
     * @param additionalLocations the additional locations
     * @param stateAdapter
     * @param eventAdapter
     */
    public GenericUmlStateMachineModelFactory(String location, String[] additionalLocations, GenericTypeConverter<S> stateAdapter, GenericTypeConverter<E> eventAdapter) {
        this.stateAdapter = stateAdapter;
        this.eventAdapter = eventAdapter;
        Assert.notNull(location, "Location must be set");
        this.location = location;
        this.additionalLocations = additionalLocations;
    }

    @Override
    public StateMachineModel<S, E> build() {
        ResourceResolver resourceResolver = null;
        if (this.location != null) {
            resourceResolver = new ResourceResolver(getResourceLoader(), location, additionalLocations);
        } else if (this.resource != null) {
            resourceResolver = new ResourceResolver(resource, additionalResources);
        }

        ResourceResolver.Holder holder = null;
        Model model = null;
        org.eclipse.emf.ecore.resource.Resource resource = null;
        try {
            ResourceResolver.Holder[] resources = resourceResolver.resolve();
            holder = resources != null && resources.length > 0 ? resources[0] : null;
            resource = UmlUtils.getResource(holder.uri.getPath());
            model = (Model) EcoreUtil.getObjectByType(resource.getContents(), UMLPackage.Literals.MODEL);
        } catch (Exception e) {
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

        GenericUmlModelParser<S, E> parser = new GenericUmlModelParser<S, E>(model, this, stateAdapter, eventAdapter);
        GenericUmlModelParser.DataHolder dataHolder = parser.parseModel();

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
        return new DefaultStateMachineModel<S, E>(null, dataHolder.getStatesData(), dataHolder.getTransitionsData());
    }
}
