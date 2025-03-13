package org.springframework.statemachine.plantuml.transition;

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

    private final Direction direction;
    private final int length;

    public Arrow(Direction direction, int length) {
        this.direction = direction;
        this.length = length;
    }

    public static Arrow of(Direction direction) {
        return new Arrow(direction, 1);
    }

    public static Arrow of(Direction direction, int length) {
        return new Arrow(direction, length);
    }

    public Direction getDirection() {
        return direction;
    }

    public String getLengthAsString() {
        return "-".repeat(length);
    }

}
