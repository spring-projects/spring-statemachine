/*
 * Copyright 2015-2025 the original author or authors.
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
package demo;

/**
 * Basic implementation of the {@link Command} interface.
 * This class provides a simple way to create commands with a name and description.
 * It can be extended to implement specific command logic.
 *
 * @author Mahmoud Ben Hassine
 */
public class BasicCommand implements Command {

    private final String name;
    private final String description;

    /**
     * Create a new {@link BasicCommand} with a name and description.
     *
     * @param name the command name
     * @param description the command description
     */
    public BasicCommand(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String execute(String[] args) throws Exception {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
