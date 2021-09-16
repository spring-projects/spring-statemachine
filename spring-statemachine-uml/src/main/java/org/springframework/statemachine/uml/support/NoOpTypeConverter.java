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

import org.springframework.statemachine.uml.UmlStateMachineModelFactory;

/**
 * Returns the original {@link String} state or event from the UML model.
 * Used exclusively by {@link UmlStateMachineModelFactory for backward compatibility}
 * where statemachine implementations do not require strictly defined types such as Enum.
 */
public class NoOpTypeConverter implements GenericTypeConverter<String> {
    @Override
    public String convert(String from) {
        return from;
    }

    @Override
    public String revert(String from) {
        return from;
    }
}
