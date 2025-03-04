package org.springframework.statemachine.plantuml.transition;

import lombok.Value;

@Value
public class Arrow {

    /**
     * Direction of an arrow connecting 2 States
     */
    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    /**
     * The 'default' arrow:
     * <UL>
     * <LI>Direction is {@link Direction#DOWN}</LI>
     * <LI>Lenght is 1</LI>
     * </UL>
     */
    static final Arrow DEFAULT = Arrow.of(Direction.DOWN);

    Direction direction;
    int length;

    public static Arrow of(Direction direction) {
        return new Arrow(direction, 1);
    }

    public static Arrow of(Direction direction, int length) {
        return new Arrow(direction, length);
    }

    public String getLengthAsString() {
        return "-".repeat(length);
    }

}
