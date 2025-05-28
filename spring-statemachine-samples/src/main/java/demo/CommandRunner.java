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

import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.io.Console;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Command line runner that executes commands.
 * <p>
 * This class implements the {@link ApplicationRunner} interface and is responsible for
 * executing commands in the command line interface. It retrieves all beans of type {@link Command}
 * from the application context and allows the user to execute them interactively.
 *
 * @author Mahmoud Ben Hassine
 */
@Component
public class CommandRunner implements ApplicationRunner, ApplicationContextAware, Ordered {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Map<String, Command> commands = this.applicationContext.getBeansOfType(Command.class);
        Console console = System.console();
        if (console == null) {
            System.err.println("No console available.");
            return;
        }

        final String lineSeparator = System.lineSeparator();
        console.printf("Available commands:" + lineSeparator);
        for (Command command : commands.values()) {
            console.printf("  " + command.getName() + ": " + command.getDescription() + lineSeparator);
        }
        console.printf("  quit: exit" + lineSeparator);

        String commandString = "";
        while (!commandString.equalsIgnoreCase("quit")) {
            console.printf("sm>");
            commandString = console.readLine();
            String[] tokens = commandString.split(" ");
            String commandName = tokens[0];
            String[] commandArgs = Stream.of(tokens).skip(1).limit(tokens.length).toArray(String[]::new);
            if (commandName.equalsIgnoreCase("quit")) {
                continue;
            }
            try {
                Command command = commands.get(commandName);
                if (command == null) {
                    console.printf("Command not found: " + commandString + lineSeparator);
                    continue;
                }
                console.printf(command.execute(commandArgs));
                console.printf(lineSeparator);
            } catch (Exception exception) {
                console.printf("Error while executing command: " + commandString + lineSeparator);
                exception.printStackTrace();
            }
        }
        console.printf("bye!" + lineSeparator);
    }

}
