package org.springframework.statemachine.kryo;
/*
 * Copyright 2015-2019 the original author or authors.
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

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.support.DefaultExtendedState;
import org.springframework.statemachine.support.DefaultStateMachineContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Kryo {@link Serializer} for {@link StateMachineContext}.
 *
 * @author Janne Valkealahti
 *
 * @param <S> the type of state
 * @param <E> the type of event
 */
public class StateMachineContextSerializer<S, E> extends Serializer<StateMachineContext<S, E>> {

    // NOTE: when structure of this serialisation is changed, see how things are tested
    //       in StateMachineContextSerializerTests.

    @Override
    public void write(Kryo kryo, Output output, StateMachineContext<S, E> context) {
        kryo.writeClassAndObject(output, context.getEvent());
        kryo.writeClassAndObject(output, context.getState());
        kryo.writeClassAndObject(output, context.getEventHeaders());
        kryo.writeClassAndObject(output, context.getExtendedState() != null ? context.getExtendedState().getVariables() : null);
        kryo.writeClassAndObject(output, context.getChilds());
        kryo.writeClassAndObject(output, context.getHistoryStates());
        kryo.writeClassAndObject(output, context.getId());
        // child refs were added after initial implementation, leaving this here
        // in case it's starting to cause issues with any existing serialised contexts
        // which doesn't have this field
        // NOTE: PR #722 added fixes with new tests
        kryo.writeClassAndObject(output, context.getChildReferences());
    }

    @SuppressWarnings("unchecked")
    @Override
    public StateMachineContext<S, E> read(Kryo kryo, Input input, Class<? extends StateMachineContext<S, E>> clazz) {
        E event = (E) kryo.readClassAndObject(input);
        S state = (S) kryo.readClassAndObject(input);
        Map<String, Object> eventHeaders = (Map<String, Object>) kryo.readClassAndObject(input);
        Map<Object, Object> variables = (Map<Object, Object>) kryo.readClassAndObject(input);
        List<StateMachineContext<S, E>> childs = (List<StateMachineContext<S, E>>) kryo.readClassAndObject(input);
        Map<S, S> historyStates = (Map<S, S>) kryo.readClassAndObject(input);
        String id = (String) kryo.readClassAndObject(input);
        List<String> childRefs = new ArrayList<>();
        if(input.canReadInt()) {
            childRefs = (List<String>) kryo.readClassAndObject(input);
        }

        return new DefaultStateMachineContext<S, E>(childRefs, childs, state, event, eventHeaders,
                new DefaultExtendedState(variables), historyStates, id);
    }
}
