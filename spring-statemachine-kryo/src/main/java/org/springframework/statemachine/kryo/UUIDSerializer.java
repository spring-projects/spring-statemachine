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

import java.util.UUID;

/**
 * Kryo {@link Serializer} for {@link UUID}.
 *
 * @author Janne Valkealahti
 *
 */
public class UUIDSerializer extends Serializer<UUID> {

    /**
     * Instantiates a new UUID serializer.
     */
    public UUIDSerializer() {
        setImmutable(true);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final UUID uuid) {
        output.writeLong(uuid.getMostSignificantBits());
        output.writeLong(uuid.getLeastSignificantBits());
    }

    @Override
    public UUID read(final Kryo kryo,final Input input,final Class<? extends UUID> aClass) {
        return new UUID(input.readLong(), input.readLong());
    }

}

