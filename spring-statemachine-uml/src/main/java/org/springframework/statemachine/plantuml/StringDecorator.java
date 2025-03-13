package org.springframework.statemachine.plantuml;

public class StringDecorator {

    private final String prefix;
    private final String suffix;

    public StringDecorator(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public String decorate(String label) {
        return prefix + label + suffix;
    }
}
