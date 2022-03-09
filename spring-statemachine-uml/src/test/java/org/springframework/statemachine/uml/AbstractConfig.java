package org.springframework.statemachine.uml;

import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineModelConfigurer;
import org.springframework.statemachine.config.model.StateMachineModelFactory;

public abstract class AbstractConfig<S, E> extends StateMachineConfigurerAdapter<S, E> {
    @Override
    public void configure(StateMachineModelConfigurer<S, E> model) throws Exception {
        model
            .withModel()
                .factory(modelFactory());
    }

    protected abstract StateMachineModelFactory<S, E> modelFactory();
}
