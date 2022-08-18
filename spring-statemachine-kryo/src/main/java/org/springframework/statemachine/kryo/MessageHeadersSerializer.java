package org.springframework.statemachine.kryo;
/*
 * Copyright 2015 the original author or authors.
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
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Kryo {@link Serializer} for spring messaging message headers.
 *
 * @author Janne Valkealahti
 *
 */
public class MessageHeadersSerializer extends Serializer<MessageHeaders> {

    @Override
    public void write(Kryo kryo, Output output, MessageHeaders object) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        for (Entry<String, Object> entry : object.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        kryo.writeClassAndObject(output, map);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MessageHeaders read(Kryo kryo, Input input, Class<? extends MessageHeaders> aClass) {
        Map<String, Object> eventHeaders = (Map<String, Object>) kryo.readClassAndObject(input);
        return new MessageHeaders(eventHeaders);
    }

}
