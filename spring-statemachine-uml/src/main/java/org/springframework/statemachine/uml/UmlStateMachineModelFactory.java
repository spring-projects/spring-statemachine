/*
 * Copyright 2016-2020 the original author or authors.
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

import org.springframework.core.io.Resource;
import org.springframework.statemachine.config.model.StateMachineModel;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.uml.support.*;

/**
 * {@link StateMachineModelFactory} which builds {@link StateMachineModel} from
 * uml representation.
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
public class UmlStateMachineModelFactory extends GenericUmlStateMachineModelFactory<String, String> {

    public UmlStateMachineModelFactory(Resource resource) {
        super(resource, new NoOpTypeConverter(), new NoOpTypeConverter());
    }

    public UmlStateMachineModelFactory(String location) {
        super(location, new NoOpTypeConverter(), new NoOpTypeConverter());
    }

    public UmlStateMachineModelFactory(Resource resource, Resource[] additionalResources) {
        super(resource, additionalResources, new NoOpTypeConverter(), new NoOpTypeConverter());
    }

    public UmlStateMachineModelFactory(String location, String[] additionalLocations) {
        super(location, additionalLocations, new NoOpTypeConverter(), new NoOpTypeConverter());
    }
}
