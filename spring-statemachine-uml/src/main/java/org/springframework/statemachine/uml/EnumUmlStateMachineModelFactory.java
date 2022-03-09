package org.springframework.statemachine.uml;

import org.springframework.core.io.Resource;
import org.springframework.statemachine.config.model.StateMachineModelFactory;
import org.springframework.statemachine.uml.support.EnumTypeConverter;

/**
 * A {@link StateMachineModelFactory} implementation that facilitates using Enumerations to represent the states and
 * events parsed from a UML model.
 * It is recommended to name all states and events in your diagrams when using enumerations. The parser will generate
 * names when missing, but the state/event numerations would have to declare those generated names, which would
 * create a fragile dependency on the internal name generating algorithm.
 * @param <S> the concrete state type.
 * @param <E> the concrete event type.
 */
public class EnumUmlStateMachineModelFactory<S extends Enum<S>, E extends Enum<E>> extends GenericUmlStateMachineModelFactory<S, E> {
    public EnumUmlStateMachineModelFactory(Resource resource, Class<S> stateClass, Class<E> eventClass) {
        super(resource, new EnumTypeConverter<>(stateClass), new EnumTypeConverter<>(eventClass));
    }

    public EnumUmlStateMachineModelFactory(String location, Class<S> stateClass, Class<E> eventClass) {
        super(location, new EnumTypeConverter<>(stateClass), new EnumTypeConverter<>(eventClass));
    }

    public EnumUmlStateMachineModelFactory(Resource resource, Resource[] additionalResources, Class<S> stateClass, Class<E> eventClass) {
        super(resource, additionalResources, new EnumTypeConverter<>(stateClass), new EnumTypeConverter<>(eventClass));
    }

    public EnumUmlStateMachineModelFactory(String location, String[] additionalLocations, Class<S> stateClass, Class<E> eventClass) {
        super(location, additionalLocations, new EnumTypeConverter<>(stateClass), new EnumTypeConverter<>(eventClass));
    }
}
