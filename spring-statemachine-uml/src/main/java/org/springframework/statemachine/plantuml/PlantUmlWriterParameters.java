package org.springframework.statemachine.plantuml;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class allows to tweak / fine-tune PlantUml StateDiagram visualisation
 *
 * @param <S>
 */
public class PlantUmlWriterParameters<S> {

    private static final Log log = LogFactory.getLog(PlantUmlWriterParameters.class);

    // TODO add hidden arrows  'S1 -[hidden]-> S2'

    /**
     * Direction of an arrow connecting 2 States
     */
    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }


    @Value
    @EqualsAndHashCode
    private static class Connection<S> {
        S source;
        S target;
    }

    /**
     * Map of ( (sourceSate, targetState) -> Direction )
     */
    private final Map<Connection<S>, Direction> arrows = new HashMap<>();

    public PlantUmlWriterParameters<S> arrowDirection(S source, Direction direction, S target) {
        arrows.put(new Connection<>(source, target), direction);
        return this;
    }

    /**
     * At least one of source and target must be non-null<BR/>
     * See implementation for 'arrow direction rule priority'
     *
     * @param source source State
     * @param target target State
     * @return Direction.name()
     */
    String getDirection(S source, S target) {
        if (source == null && target == null) {
            throw new IllegalArgumentException("source and target state cannot both be null!");
        }
        Connection<S> sourceAndTarget = new Connection<>(source, target);
        if (arrows.containsKey(sourceAndTarget)) {
            return arrows.get(sourceAndTarget).name().toLowerCase();
        }
        Connection<S> sourceOnly = new Connection<>(source, null);
        Connection<S> targetOnly = new Connection<>(null, target);
        if (arrows.containsKey(sourceOnly)
            && arrows.containsKey(targetOnly)
            && !arrows.get(sourceOnly).equals(arrows.get(targetOnly))
        ) {
            log.warn(
                    String.format("Two 'unary' 'arrowDirection' rules found for (%s, %s) with DIFFERENT values! Using 'target' rule!", source, target));
            return arrows.get(targetOnly).name().toLowerCase();
        } else if (arrows.containsKey(sourceOnly)) {
            return arrows.get(sourceOnly).name().toLowerCase();
        } else if (arrows.containsKey(targetOnly)) {
            return arrows.get(targetOnly).name().toLowerCase();
        } else {
            return Direction.DOWN.name().toLowerCase();
        }
    }

    /**
     * Map of ( Connection(sourceSate, targetState) -> Direction )
     * Used to add EXTRA HIDDEN arrows, which are just helping with diagram layout.<BR/>
     * This is typically useful to 'force' the position of a state comparing to another, EVEN IF THESE TWO STATES AR NOT CONNECTED in the statemachine :-)<BR/>
     * <p>
     * exemple:
     * S1 -left[hidden]-> S2
     */
    private final Map<Connection<S>, Direction> hiddenTransitions = new HashMap<>();

    public PlantUmlWriterParameters<S> hiddenTransition(S source, Direction direction, S target) {
        hiddenTransitions.put(new Connection<>(source, target), direction);
        return this;
    }

    public String getHiddenTransitions() {
        return hiddenTransitions.entrySet().stream()
                .map(hiddenTransition -> "%s -%s[hidden]-> %s"
                        .formatted(
                                hiddenTransition.getKey().getSource(),
                                hiddenTransition.getValue().name(),
                                hiddenTransition.getKey().getTarget()
                        ))
                .collect(Collectors.joining("\n"));
    }

    @Builder
    @Getter
    @EqualsAndHashCode
    static class LabelDecorator {
        String prefix = "";
        String suffix = "";

        String decorate(String label) {
            return prefix + label + suffix;
        }
    }

    /**
     * Map of ( Connection(sourceSate, targetState) -> LabelDecorator(labelPrefix, labelSuffix) )
     */
    private final Map<Connection<S>, LabelDecorator> arrowLabelDecorator = new HashMap<>();

    public PlantUmlWriterParameters<S> arrowLabelDecorator(S source, S target, String prefix, String suffix) {
        arrowLabelDecorator.put(
                new Connection<>(source, target),
                LabelDecorator.builder().prefix(prefix).suffix(suffix).build()
        );
        return this;
    }

    public String decorateLabel(
            @Nullable S source,
            @Nullable S target,
            @Nullable String transitionLabel
    ) {
        if (transitionLabel == null) {
            return null;
        }

        if (source == null && target == null) {
            throw new IllegalArgumentException("source and target state cannot both be null!");
        }
        Connection<S> sourceAndTarget = new Connection<>(source, target);
        if (arrowLabelDecorator.containsKey(sourceAndTarget)) {
            return arrowLabelDecorator.get(sourceAndTarget).decorate(transitionLabel);
        }
        Connection<S> sourceOnly = new Connection<>(source, null);
        Connection<S> targetOnly = new Connection<>(null, target);
        if (arrowLabelDecorator.containsKey(sourceOnly)
            && arrowLabelDecorator.containsKey(targetOnly)
            && !arrowLabelDecorator.get(sourceOnly).equals(arrowLabelDecorator.get(targetOnly))
        ) {
            log.warn(
                    String.format("Two 'unary' 'arrowLabelDecorator' rules found for (%s, %s) with DIFFERENT values! Using 'target' rule!", source, target));
            return arrowLabelDecorator.get(targetOnly).decorate(transitionLabel);
        } else if (arrowLabelDecorator.containsKey(sourceOnly)) {
            return arrowLabelDecorator.get(sourceOnly).decorate(transitionLabel);
        } else if (arrowLabelDecorator.containsKey(targetOnly)) {
            return arrowLabelDecorator.get(targetOnly).decorate(transitionLabel);
        } else {
            return transitionLabel;
        }
    }

    // Colors

    private String defaultStateColor = "";
    private String currentStateColor = "#FFFF77";

    @Getter
    private String transitionLabelSeparator = "\\n";

    public PlantUmlWriterParameters<S> setTransitionLabelSeparator(String transitionLabelSeparator) {
        this.transitionLabelSeparator = transitionLabelSeparator;
        return this;
    }

    public PlantUmlWriterParameters<S> defaultStateColor(String defaultStateColor) {
        this.defaultStateColor = defaultStateColor;
        return this;
    }

    public PlantUmlWriterParameters<S> currentStateColor(String currentStateColor) {
        this.currentStateColor = currentStateColor;
        return this;
    }

    public String getStateColor(S state, @Nullable S currentState) {
        if (state == null) {
            log.warn("null state!");
            return ""; // TODO check why this is happening
        }
        return state.equals(currentState) ? currentStateColor : defaultStateColor;
    }

    public String getArrowColor(boolean isCurrentTransaction) {
        return isCurrentTransaction ? "#FF0000" : "#000000";
    }

}
