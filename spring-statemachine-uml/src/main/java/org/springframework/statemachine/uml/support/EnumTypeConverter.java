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
package org.springframework.statemachine.uml.support;

import org.springframework.util.ObjectUtils;

/**
 * Reference converter for Enumerations. Used in conjunction with:
 * {@link org.springframework.statemachine.uml.EnumUmlStateMachineModelFactory} to facilitate the enumerated
 * states and/or events, as opposed to Strings when using {@link org.springframework.statemachine.uml.UmlStateMachineModelFactory}.
 * @param <T> The type to convert to/from String, e.g. MyStateEnum, MyEventEnum
 */
public class EnumTypeConverter<T extends Enum<T>> implements GenericTypeConverter<T> {
    private final Class<T> enumClass;

    public EnumTypeConverter(Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public T convert(String from) {
        if (ObjectUtils.isEmpty(from)) {
            return null;
        } else {
            try {
                return T.valueOf(enumClass, from);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("'" + from + "' is not a valid enum in type: " + enumClass.getSimpleName(), e);
            }
        }
    }

    @Override
    public String revert(T from) {
        if (ObjectUtils.isEmpty(from)) {
            return "";
        } else {
            return from.name();
        }
    }
}
