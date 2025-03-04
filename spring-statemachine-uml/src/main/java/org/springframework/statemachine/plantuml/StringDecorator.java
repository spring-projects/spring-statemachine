package org.springframework.statemachine.plantuml;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Builder
@Getter
@EqualsAndHashCode
public class StringDecorator {
    @Builder.Default
    private String prefix = "";
    @Builder.Default
    private String suffix = "";

    public String decorate(String label) {
        return prefix + label + suffix;
    }
}
