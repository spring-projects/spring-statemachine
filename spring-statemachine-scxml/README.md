# Spring Statemachine SCXML Module

This module provides support for SCXML (State Chart XML) protocol specification, allowing Spring Statemachine to load state machine definitions from SCXML files.

## Overview

SCXML (State Chart XML) is a W3C standard for describing state machines. This module parses SCXML files and converts them into Spring Statemachine models, enabling you to define state machines using the standard SCXML format.

## Features

- Parse SCXML files and convert to Spring Statemachine models
- Support for states, transitions, events, guards, and actions
- Support for entry/exit actions
- Support for parallel states
- Support for final states
- Support for history states (shallow and deep)
- Bean reference resolution for actions and guards
- SpEL expression support for guards and actions

## Usage

### Basic Configuration

```java
@Configuration
@EnableStateMachine
public class ScxmlConfig extends StateMachineConfigurerAdapter<String, String> {

    @Override
    public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
        model
            .withModel()
                .factory(modelFactory());
    }

    @Bean
    public StateMachineModelFactory<String, String> modelFactory() {
        return new ScxmlStateMachineModelFactory(
            "classpath:statemachine.scxml");
    }
}
```

### With Component Resolver

```java
@Configuration
@EnableStateMachine
public class ScxmlConfig extends StateMachineConfigurerAdapter<String, String> {

    @Override
    public void configure(StateMachineModelConfigurer<String, String> model) throws Exception {
        model
            .withModel()
                .factory(modelFactory());
    }

    @Bean
    public StateMachineModelFactory<String, String> modelFactory() {
        ScxmlStateMachineModelFactory factory = new ScxmlStateMachineModelFactory(
            "classpath:statemachine.scxml");
        factory.setStateMachineComponentResolver(componentResolver());
        return factory;
    }

    @Bean
    public StateMachineComponentResolver<String, String> componentResolver() {
        DefaultStateMachineComponentResolver<String, String> resolver = 
            new DefaultStateMachineComponentResolver<>();
        resolver.registerAction("myAction", myAction());
        resolver.registerGuard("myGuard", myGuard());
        return resolver;
    }
}
```

## SCXML File Format

### Simple State Machine

```xml
<?xml version="1.0" encoding="UTF-8"?>
<scxml xmlns="http://www.w3.org/2005/07/scxml" version="1.0" initial="S1">
  <state id="S1">
    <transition event="E1" target="S2"/>
  </state>
  <state id="S2">
    <onentry>
      <log expr="'Entered S2'"/>
    </onentry>
  </state>
</scxml>
```

### With Guards

```xml
<?xml version="1.0" encoding="UTF-8"?>
<scxml xmlns="http://www.w3.org/2005/07/scxml" version="1.0" initial="S1">
  <state id="S1">
    <transition event="E1" target="S2" cond="bean:myGuard"/>
    <transition event="E1" target="S3" cond="true"/>
  </state>
  <state id="S2"/>
  <state id="S3"/>
</scxml>
```

### With Actions

```xml
<?xml version="1.0" encoding="UTF-8"?>
<scxml xmlns="http://www.w3.org/2005/07/scxml" version="1.0" initial="S1">
  <state id="S1">
    <onentry>
      <action bean="myAction"/>
    </onentry>
    <transition event="E1" target="S2"/>
  </state>
  <state id="S2">
    <onexit>
      <log expr="'Exiting S2'"/>
    </onexit>
  </state>
</scxml>
```

## Supported SCXML Elements

- `<scxml>` - Root element
- `<state>` - Regular state
- `<initial>` - Initial state
- `<final>` - Final state
- `<parallel>` - Parallel state (creates orthogonal regions)
- `<history>` - History state (shallow or deep)
- `<transition>` - State transition
- `<onentry>` - Entry actions
- `<onexit>` - Exit actions
- `<invoke>` - State actions (via bean references)
- `<log>` - Logging action with SpEL expressions

## Guard Conditions

Guards can be specified using the `cond` attribute on transitions:

- Bean reference: `cond="bean:guardBeanId"`
- SpEL expression: `cond="context.getExtendedState().getVariables().get('key') == 'value'"`

## Actions

Actions can be specified in several ways:

- Bean reference: `<action bean="actionBeanId"/>`
- SpEL expression: `<log expr="'message'"/>`
- Element name matching: Custom elements matching registered bean names

## Dependencies

This module depends on:
- `spring-statemachine-core`
- Standard Java XML parsing libraries

## References

- [W3C SCXML Specification](https://www.w3.org/TR/scxml/)
- [Spring Statemachine Documentation](https://projects.spring.io/spring-statemachine/)

