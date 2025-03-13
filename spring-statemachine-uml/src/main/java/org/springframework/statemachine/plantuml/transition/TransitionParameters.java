package org.springframework.statemachine.plantuml.transition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.Nullable;
import org.springframework.statemachine.plantuml.StringDecorator;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class TransitionParameters<S> {

    private static final Log log = LogFactory.getLog(TransitionParameters.class);

    /**
     * Map of ((sourceSate, targetState) -> Direction)
     */
    private final Map<Connection<S>, Arrow> arrows = new HashMap<>();

    /**
     * Map of <code>Connection(sourceSate, targetState) -> Direction</code>
     * Used to add EXTRA HIDDEN arrows, which are just helping with diagram layout.<BR/>
     * This is typically useful to position a State relative to another, EVEN IF THESE TWO STATES AR NOT CONNECTED in the statemachine :-)<BR/>
     * <p>
     * exemple:
     * S1 -left[hidden]-> S2
     */
    private final Map<Connection<S>, Arrow.Direction> additionalHiddenTransitions = new TreeMap<>();

    /**
     * Array of Connection(sourceSate, targetState) used to IGNORE EXISTING transitions.<BR/>
     * The goal is to give the ability to IGNORE some transitions on big state machine diagram to clarify it.
     * (So, this is DIFFERENT from 'additionalHiddenTransitions', which is used to add extra 'hidden' / 'fake' transitions )<BR/>
     */
    private final TreeSet<Connection<S>> ignoredTransitions = new TreeSet<>();

    /**
     * Map of (Connection(sourceSate, targetState) -> LabelDecorator(labelPrefix, labelSuffix))
     */
    private final Map<Connection<S>, StringDecorator> arrowLabelDecorator = new HashMap<>();

    public void arrow(S source, Arrow.Direction direction, S target) {
        arrows.put(new Connection<>(source, target), Arrow.of(direction));
    }

    public void arrow(S source, Arrow.Direction direction, S target, int length) {
        arrows.put(new Connection<>(source, target), Arrow.of(direction, length));
    }

    /**
     * At least one of source and target must be non-null<BR/>
     * See implementation for 'arrow direction rule priority'
     *
     * @param source source State
     * @param target target State
     * @return Direction.name()
     */
    private Arrow getArrow(S source, S target) {
        if (source == null && target == null) {
            throw new IllegalArgumentException("source and target state cannot both be null!");
        }
        if (source != null && target != null) {
            Connection<S> sourceAndTarget = new Connection<>(source, target);
            if (arrows.containsKey(sourceAndTarget)) {
                return arrows.get(sourceAndTarget);
            }
        }
        Connection<S> sourceOnly = new Connection<>(source, null);
        Connection<S> targetOnly = new Connection<>(null, target);
        if (arrows.containsKey(sourceOnly)
                && arrows.containsKey(targetOnly)
                && !arrows.get(sourceOnly).equals(arrows.get(targetOnly))
        ) {
            log.warn("Two 'unary' 'arrowDirection' rules found for (" + source + ", " + target + ") with DIFFERENT values! Using 'target' rule!");
            return arrows.get(targetOnly);
        } else if (arrows.containsKey(sourceOnly)) {
            return arrows.get(sourceOnly);
        } else if (arrows.containsKey(targetOnly)) {
            return arrows.get(targetOnly);
        } else {
            return Arrow.DEFAULT;
        }
    }

    public String getDirection(S source, S target) {
        return getArrow(source, target).getDirection().name().toLowerCase();
    }

    public String getArrowLength(S source, S target) {
        return getArrow(source, target).getLengthAsString();
    }

    /**
     * Add EXTRA HIDDEN transitions to align states WITHOUT connecting them.<BR/>
     * These transitions are NOT part of the state machine!
     */
    public void addAdditionalHiddenTransition(S source, Arrow.Direction direction, S target) {
        additionalHiddenTransitions.put(new Connection<>(source, target), direction);
    }

    public String getAdditionalHiddenTransitions() {
        String hiddenTransitionsText = additionalHiddenTransitions.entrySet().stream()
                .map(hiddenTransition -> "%s -%s[hidden]-> %s"
                        .formatted(
                                hiddenTransition.getKey().source(),
                                hiddenTransition.getValue().name().toLowerCase(),
                                hiddenTransition.getKey().target()
                        ))
                .collect(Collectors.joining("\n"));

        return hiddenTransitionsText.isEmpty()
                ? ""
                : "\n" + hiddenTransitionsText + "\n";
    }

    /**
     * IGNORE a transition<BR/>
     * Transition (source -> destination) will NOT be present in PlantUML diagram
     */
    public void ignoreTransition(S source, S target) {
        ignoredTransitions.add(new Connection<>(source, target));
    }

    public boolean isTransitionIgnored(S source, S target) {
        // if 'source' is null, we always show the transition (initial state ?)
        return source != null && ignoredTransitions.contains(new Connection<>(source, target));
    }

    public void arrowLabelDecorator(S source, S target, String prefix, String suffix) {
        arrowLabelDecorator.put(
                new Connection<>(source, target),
                new StringDecorator(prefix, suffix)
        );
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
            log.warn("Two 'unary' 'arrowLabelDecorator' rules found for (" + source + ", " + target + ") with DIFFERENT values! Using 'target' rule!");
            return arrowLabelDecorator.get(targetOnly).decorate(transitionLabel);
        } else if (arrowLabelDecorator.containsKey(sourceOnly)) {
            return arrowLabelDecorator.get(sourceOnly).decorate(transitionLabel);
        } else if (arrowLabelDecorator.containsKey(targetOnly)) {
            return arrowLabelDecorator.get(targetOnly).decorate(transitionLabel);
        } else {
            return transitionLabel;
        }
    }
}
