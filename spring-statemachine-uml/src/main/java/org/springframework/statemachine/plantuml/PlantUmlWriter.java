package org.springframework.statemachine.plantuml;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.plantuml.SourceStringReader;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.lang.Nullable;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.plantuml.helper.*;
import org.springframework.statemachine.region.Region;
import org.springframework.statemachine.state.*;
import org.springframework.statemachine.support.StateMachineUtils;
import org.springframework.statemachine.transition.Transition;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * This class convert a Spring StateMachine to a PlantUml StateDiagram<BR/>
 * <BR/>
 * To display *.puml diagram, install PlantUml plugin<BR/>
 * <a href="https://plugins.jetbrains.com/plugin/7017-plantuml-integration">https://plugins.jetbrains.com/plugin/7017-plantuml-integration</a><BR/>
 * <BR/>
 * For PNG support, install GraphViz<BR/>
 * <a href="https://graphviz.org/download/#windows">https://graphviz.org/download/#windows</a><BR/>
 * <BR/>
 * Install then define GRAPHVIZ_DOT environment variable: <BR/>
 * <BR/>
 * GRAPHVIZ_DOT=C:\Program Files\Graphviz\bin\dot.exe <BR/>
 * <BR/>
 * To obtain better results, {@link Action} s and {@link Guard} s should:
 * <UL>
 * <LI>be {@link org.springframework.context.annotation.Bean} s</LI>
 * <LI>implement {@link BeanNameAware}</LI>
 * <LI>have a "String beanName" field (can be private)</LI>
 * </UL>
 * <BR/>
 * Links: <BR/>
 * <UL>
 * <LI><a href="https://plantuml.com/fr/state-diagram">https://plantuml.com/fr/state-diagram</a></LI>
 * <LI><a href="https://github.com/plantuml/plantuml">https://github.com/plantuml/plantuml</a></LI>
 * <LI><a href="https://www.webdevtutor.net/blog/plantuml-state-machine-diagrams">https://www.webdevtutor.net/blog/plantuml-state-machine-diagrams</a></LI>
 * <LI><a href="https://pdf.plantuml.net/1.2023.11/PlantUML_Language_Reference_Guide_fr.pdf">https://pdf.plantuml.net/1.2023.11/PlantUML_Language_Reference_Guide_fr.pdf</a></LI>
 * <LI><a href="https://www.planttext.com/">https://www.planttext.com/</a></LI>
 * <LI><a href="https://sparxsystems.com/resources/tutorials/uml2/state-diagram.html">https://sparxsystems.com/resources/tutorials/uml2/state-diagram.html</a></LI>
 * </UL>
 */
// Disabling "Method has * parameters, which is greater than 7 authorized." warning
@SuppressWarnings("squid:S107")
public class PlantUmlWriter<S, E> {

    private static final Log log = LogFactory.getLog(PlantUmlWriter.class);

    private static final String INDENT_INCREMENT = "  ";

    // History states are handled in a special way:
    // 1 - During the 1st pass, History states 'IDs' are 'computed' and collected in 'historyStatesToHistoryId'
    // 2 - During the 2nd pass (the main one), 'history' transitions are collected in 'historyTransitions'
    // 3 - Then, the collected 'historyTransitions' are added at the end of the PlantUml diagram using 'historyStatesToHistoryId'

    private Map<State<S, E>, String> historyStatesToHistoryId;
    private List<Transition<S, E>> historyTransitions;

    // Comparators. Used to keep order of region, states and transitions stable in generated puml

    private final StateComparator<S, E> stateComparator = new StateComparator<>();

    private final Comparator<Transition<S, E>> transitionComparator = new TransitionComparator<>();

    private final Comparator<Region<S, E>> regionComparator = new RegionComparator<>(stateComparator);

    //

    public void save(
            @NotNull StateMachine<S, E> stateMachine,
            @NotNull File file
    ) throws IOException {
        save(stateMachine, null, null, file);
    }

    /**
     * @param stateMachine             stateMachine
     * @param stateContext             stateContext
     * @param plantUmlWriterParameters plantUmlWriterParameters
     * @param file                     filename must be "*.puml" or "*.png"
     * @throws IOException
     */
    public void save(
            @NotNull StateMachine<S, E> stateMachine,
            @Nullable StateContext<S, E> stateContext,
            @Nullable PlantUmlWriterParameters<S> plantUmlWriterParameters,
            @NotNull File file
    ) throws IOException {
        if (plantUmlWriterParameters == null) {
            plantUmlWriterParameters = new PlantUmlWriterParameters<>();
        }
        String plantUmlDiagram = toPlantUml(
                stateMachine,
                stateContext,
                plantUmlWriterParameters
        );

        if (file.getName().endsWith(".puml")) {
            Files.write(file.toPath(), plantUmlDiagram.getBytes());
        } else if (file.getName().endsWith(".png")) {
            SourceStringReader reader = new SourceStringReader(plantUmlDiagram);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            reader.outputImage(os);
            Files.write(file.toPath(), os.toByteArray());
        } else {
            throw new IllegalArgumentException("file name must be *.puml or *.png");
        }
    }

    public String toPlantUml(StateMachine<S, E> stateMachine) {
        return toPlantUml(stateMachine, null, null);
    }

    /**
     * Convert a State machine in PlantUML notation<BR/>
     * limited support!
     *
     * @param stateMachine             stateMachine
     * @param stateContext             stateContext
     * @param plantUmlWriterParameters plantUmlWriterParameters
     * @return plantUml representation of the stateMachine
     */
    public String toPlantUml(
            StateMachine<S, E> stateMachine,
            @Nullable StateContext<S, E> stateContext,
            @Nullable PlantUmlWriterParameters<S> plantUmlWriterParameters
    ) {
        if (plantUmlWriterParameters == null) {
            plantUmlWriterParameters = new PlantUmlWriterParameters<>();
        }

        // 1st pass: Collecting history states
        historyStatesToHistoryId = StateMachineHelper.collectHistoryStates(stateMachine);
        historyTransitions = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        sb.append("""
                @startuml
                'https://plantuml.com/state-diagram
                                
                'hide description area for state without description
                hide empty description
                                
                """);

        // 2nd pass: processing statemachine AND collecting history transitions in 'historyTransitions
        processRegion(stateMachine, stateContext, plantUmlWriterParameters, sb, "", null);

        // finally, adding the collected history transitions
        for (Transition<S, E> transition : historyTransitions) {
            processPseudoStatesTransition(
                    ContextTransition.of(stateContext),
                    transition,
                    transition.getSource(),
                    transition.getTarget(),
                    this::getHistoryStateId,
                    plantUmlWriterParameters,
                    sb,
                    ""
            );
        }

        sb.append("\n@enduml");

        log.debug("toPlantUml:" + sb);

        return sb.toString();
    }

    // TODO check this... is this a good idea?
    private interface HistoryIdGetter<S, E> {
        String getId(State<S, E> state);
    }

    String getHistoryStateId(State<S, E> state) {
        if (historyStatesToHistoryId.containsKey(state)) {
            return historyStatesToHistoryId.get(state);
        } else {
            return state.getId().toString();
        }
    }

    private void processRegion(
            @NotNull Region<S, E> region,
            @Nullable StateContext<S, E> stateContext,
            @Nullable PlantUmlWriterParameters<S> plantUmlWriterParameters,
            StringBuilder sb,
            String indent,
            @Nullable Predicate<Transition<S, E>> transitionAllowed // not working for the moment
    ) {
        if (plantUmlWriterParameters == null) {
            plantUmlWriterParameters = new PlantUmlWriterParameters<>();
        }

        // check 'entry' and 'exit' pseudo states:
        // these states MUST NOT be added in this region, BUT in the subregion / submachine they are related to!
        Map<Boolean, List<State<S, E>>> allStates = region.getStates()
                .stream()
                .collect(Collectors.groupingBy(this::isEntryOrExit));

        List<State<S, E>> entryAndExitStates = allStates.get(Boolean.TRUE);
        List<State<S, E>> otherStates = allStates.get(Boolean.FALSE);

        // states
        processStates(
                region,
                stateContext,
                entryAndExitStates,
                otherStates,
                plantUmlWriterParameters,
                sb,
                indent
        );

        // transitions
        ContextTransition<S, E> currentContextTransition = ContextTransition.of(stateContext);
        processPseudoStatesTransitions(region, currentContextTransition, plantUmlWriterParameters, sb, indent);
        processTransitions(region, plantUmlWriterParameters, currentContextTransition, sb, indent, transitionAllowed);
    }

    private Boolean isEntryOrExit(State<S, E> state) {
        if (state.getPseudoState() == null) {
            return Boolean.FALSE;
        }
        return PseudoStateKind.ENTRY.equals(state.getPseudoState().getKind())
               || PseudoStateKind.EXIT.equals(state.getPseudoState().getKind());
    }

    // TODO create processState() with optional '{' and '}' to allow text on stereotype states?
    //  warning -> this change the visual representation of the state, using the 'default' representation :/
    private void processStates(
            Region<S, E> region,
            StateContext<S, E> stateContext,
            @Nullable Collection<State<S, E>> entryAndExitStates,
            List<State<S, E>> otherStates,
            PlantUmlWriterParameters<S> plantUmlWriterParameters,
            StringBuilder sb,
            String indent
    ) {
        S currentState = region.getState() != null
                ? region.getState().getId()
                : null;

        Collection<Transition<S, E>> regionTransitions = region.getTransitions();

        // associating each entry to its targets, and each exit to its sources (as per transitions)
        Map<Boolean, List<State<S, E>>> allStates = entryAndExitStates == null ? Map.of()
                : entryAndExitStates
                .stream()
                .collect(Collectors.groupingBy(state -> PseudoStateKind.ENTRY.equals(state.getPseudoState().getKind())));

        List<State<S, E>> entryStates = allStates.get(Boolean.TRUE);
        Map<State<S, E>, List<S>> entryToTargetStates = entryStates == null ? Map.of() :
                entryStates.stream().collect(
                        toMap(
                                entry -> entry,
                                entry -> getEntryTargets(entry, regionTransitions),
                                (s, s2) -> {
                                    throw new IllegalStateException("This should not happen!");
                                }));

        List<State<S, E>> exitStates = allStates.get(Boolean.FALSE);
        Map<State<S, E>, List<S>> exitToSourceStates = exitStates == null ? Map.of() :
                exitStates.stream().collect(
                        toMap(
                                exit -> exit,
                                exit -> getExitSources(exit, regionTransitions),
                                (s, s2) -> {
                                    throw new IllegalStateException("This should not happen!");
                                }));

        // processing states
        otherStates.stream()
                .sorted(stateComparator)
                .toList()
                .forEach(state -> processState(state, currentState, entryToTargetStates, exitToSourceStates, stateContext, plantUmlWriterParameters, sb, indent));

        sb.append("\n");
    }

    private List<S> getEntryTargets(State<S, E> entry, Collection<Transition<S, E>> regionTransitions) {
        return regionTransitions.stream()
                .filter(aTransition -> entry.getId().equals(aTransition.getSource().getId()))
                .map(aTransition -> aTransition.getTarget().getId())
                .toList();
    }

    private List<S> getExitSources(State<S, E> exit, Collection<Transition<S, E>> regionTransitions) {
        return regionTransitions.stream()
                .filter(aTransition -> exit.getId().equals(aTransition.getTarget().getId()))
                .map(aTransition -> aTransition.getSource().getId())
                .toList();
    }

    private void processState(
            State<S, E> state,
            S currentState,
            Map<State<S, E>, List<S>> entryToTargetStates,
            Map<State<S, E>, List<S>> exitToSourceStates,
            StateContext<S, E> stateContext,
            PlantUmlWriterParameters<S> plantUmlWriterParameters,
            StringBuilder sb,
            String indent
    ) {
        if (state.isSimple()) {
            processSimpleState(state, currentState, plantUmlWriterParameters, sb, indent);
        } else if (state.isSubmachineState()) {
            if (state instanceof AbstractState<S, E> abstractState) {
                processSubmachine(abstractState, currentState, plantUmlWriterParameters, sb, indent, stateContext, entryToTargetStates, exitToSourceStates);
            }
        } else if (state.isComposite() || state.isOrthogonal()) {
            if (state instanceof RegionState<S, E> regionState) {
                processCompositeOrOrthogonalState(regionState, currentState, plantUmlWriterParameters, sb, indent, stateContext, entryToTargetStates, exitToSourceStates);
            }
        } else {
            throw new NotImplementedException("Unexpected state type " + state.getId());
        }
        processStateDescription(state, sb, indent);
    }

    private void processCompositeOrOrthogonalState(
            RegionState<S, E> regionState,
            S currentState,
            PlantUmlWriterParameters<S> plantUmlWriterParameters,
            StringBuilder sb,
            String indent,
            StateContext<S, E> stateContext,
            Map<State<S, E>, List<S>> entryToTargetStates,
            Map<State<S, E>, List<S>> exitToSourceStates
    ) {
        sb.append("""
                %s {
                """
                .formatted(stateToString(indent, regionState.getId(), currentState, plantUmlWriterParameters))
                .stripIndent());

        final String regionIndent = indent + INDENT_INCREMENT;
        List<Region<S, E>> regions = regionState.getRegions().stream()
                .sorted(regionComparator)
                .toList();

        for (int i = 0, nRegions = regions.size(); i < nRegions; i++) {
            Region<S, E> subRegion = regions.get(i);
            processRegion(subRegion, stateContext, plantUmlWriterParameters, sb, regionIndent, new Predicate<Transition<S, E>>() {
                @Override
                public boolean test(Transition<S, E> transition) {
                    // TODO implement this by checking if source or target state belong to another region ???
//                    log.warn("Transition {} from / to another region is not allowed");
                    return true;
                }
            });
            processEntries(currentState, subRegion.getStates(), entryToTargetStates, plantUmlWriterParameters, sb, regionIndent);
            processExits(currentState, subRegion.getStates(), exitToSourceStates, plantUmlWriterParameters, sb, regionIndent);
            // using "--" caused problem ... how to solve this..?
/*
            if (i != nRegions - 1) {
                // Separating regions. see "États concurrents [--, ||]" https://plantuml.com/fr/state-diagram#73b918d90b24a6c6
                sb.append(regionIndent).append("--\n");
            }
*/
        }
        sb.append("""
                %s}
                """.formatted(indent));
    }

    private void processSubmachine(
            AbstractState<S, E> abstractState,
            S currentState,
            PlantUmlWriterParameters<S> plantUmlWriterParameters,
            StringBuilder sb,
            String indent,
            StateContext<S, E> stateContext,
            Map<State<S, E>, List<S>> entryToTargetStates,
            Map<State<S, E>, List<S>> exitToSourceStates
    ) {
        sb.append("""
                %s {
                """
                .formatted(stateToString(indent, abstractState.getId(), currentState, plantUmlWriterParameters))
                .stripIndent());
        final String regionIndent = indent + INDENT_INCREMENT;
        processRegion(abstractState.getSubmachine(), stateContext, plantUmlWriterParameters, sb, regionIndent, null);
        processEntries(currentState, abstractState.getSubmachine().getStates(), entryToTargetStates, plantUmlWriterParameters, sb, regionIndent);
        processExits(currentState, abstractState.getSubmachine().getStates(), exitToSourceStates, plantUmlWriterParameters, sb, regionIndent);
        sb.append("""
                %s}
                """.formatted(indent));
    }

    private void processSimpleState(
            State<S, E> state,
            S currentState,
            PlantUmlWriterParameters<S> plantUmlWriterParameters,
            StringBuilder sb,
            String indent
    ) {
        if (state.getPseudoState() != null) {
            processPseudoState(state, currentState, plantUmlWriterParameters, sb, indent);
        } else {
//            TODO allow plantUmlWriterParameters to add links in description ??
//             eg: state "CarWithWheel [[http://plantuml.com/state-diagram]]" as CarWithWheel
            sb.append("""
                    %s
                    """
                    .formatted(stateToString(indent, state.getId(), currentState, plantUmlWriterParameters))
                    .stripIndent());
        }
    }

    private void processEntries(
            S currentState,
            Collection<State<S, E>> regionStates,
            Map<State<S, E>, List<S>> entryToTargetStates,
            PlantUmlWriterParameters<S> plantUmlWriterParameters,
            StringBuilder sb,
            String indent
    ) {
        entryToTargetStates.keySet()
                .forEach(entryState -> entryToTargetStates.get(entryState)
                        .stream()
                        .filter(targetStateId -> regionStates.stream().anyMatch(regionState -> targetStateId.equals(regionState.getId())))
                        .forEach(s -> processPseudoState(entryState, currentState, plantUmlWriterParameters, sb, indent)));
    }

    private void processExits(
            S currentState,
            Collection<State<S, E>> regionStates,
            Map<State<S, E>, List<S>> exitToSourceStates,
            PlantUmlWriterParameters<S> plantUmlWriterParameters,
            StringBuilder sb,
            String indent
    ) {
        exitToSourceStates.keySet()
                .forEach(exitState -> exitToSourceStates.get(exitState)
                        .stream()
                        .filter(sourceStateId -> regionStates.stream().anyMatch(regionState -> sourceStateId.equals(regionState.getId())))
                        .forEach(s -> processPseudoState(exitState, currentState, plantUmlWriterParameters, sb, indent)));
    }

    private void processPseudoState(
            State<S, E> state,
            S currentState,
            PlantUmlWriterParameters<S> plantUmlWriterParameters,
            StringBuilder sb,
            String indent
    ) {
        PseudoStateKind pseudoStateKind = state.getPseudoState().getKind();
        switch (pseudoStateKind) {
            case INITIAL -> {
                if (StateMachineUtils.isPseudoState(state, PseudoStateKind.INITIAL)) {
                    sb.append("""
                            %s
                            """
                            .formatted(stateToString(indent, state.getId(), currentState, plantUmlWriterParameters))
                            .stripIndent());
                }
            }
            case HISTORY_SHALLOW, HISTORY_DEEP -> {
                // no-op
                // History states are NOT added in the diagram as per themselves
                // They are added 'just' by creating a transition involving an history state, i.e., [H] or [H*]
                // see historyStatesToHistoryId and historyTransitions
            }
            case ENTRY, EXIT -> sb.append("""
                    %sstate %s <<%s>>
                    """
                    .formatted(
                            indent,
                            state.getId(),
                            getPseudoStatePlantUmlStereotype(pseudoStateKind)
                    ).stripIndent());
            case END, CHOICE, FORK, JOIN, JUNCTION -> sb.append("""
                    %s'%s <<%s>>
                    %sstate %s <<%s>>
                    %snote left of %s %s: %s
                    """
                    .formatted(
                            indent,
                            state.getId(),
                            pseudoStateKind.name(),
                            indent,
                            state.getId(),
                            getPseudoStatePlantUmlStereotype(pseudoStateKind),
                            indent,
                            state.getId(),
                            plantUmlWriterParameters.getStateColor(state.getId(), currentState),
                            state.getId()
                    ).stripIndent());
        }
    }

    /**
     * Return a PlantUML stereotype for a given PseudoStateKind<BR/>
     * we use <<start>> stereotype for JUNCTION<BR/>
     * see <a href="https://sparxsystems.com/resources/tutorials/uml2/state-diagram.html">UML 2 - State Machine Diagram</a>
     *
     * @param pseudoStateKind pseudoStateKind
     * @return PlantUml stereotype corresponding to pseudoStateKind
     */
    private String getPseudoStatePlantUmlStereotype(PseudoStateKind pseudoStateKind) {
        return switch (pseudoStateKind) {
            case INITIAL, JUNCTION -> "start";
            case ENTRY, EXIT -> pseudoStateKind.name().toLowerCase() + "Point";
            default -> pseudoStateKind.name().toLowerCase();
        };
    }

    private String stateToString(
            String indent, S state,
            S currentState,
            PlantUmlWriterParameters<S> plantUmlWriterParameters
    ) {
        return "%sstate %s %s"
                .formatted(
                        indent,
                        state,
                        plantUmlWriterParameters.getStateColor(state, currentState)
                );
    }

    private void processStateDescription(
            State<S, E> state,
            StringBuilder sb,
            String indent
    ) {
        for (E deferredEvent : state.getDeferredEvents()) {
            sb.append("""
                    %s%s : %s /defer
                    """
                    .formatted(
                            indent,
                            state.getId(),
                            deferredEvent
                    )
                    .stripIndent()
            );
        }
        for (Function<StateContext<S, E>, Mono<Void>> entryAction : state.getEntryActions()) {
            sb.append("""
                    %s%s : /entry %s
                    """
                    .formatted(
                            indent,
                            state.getId(),
                            NameGetter.getName(entryAction)
                    )
                    .stripIndent()
            );
        }
        for (Function<StateContext<S, E>, Mono<Void>> stateAction : state.getStateActions()) {
            sb.append("""
                    %s%s : /do %s
                    """
                    .formatted(
                            indent,
                            state.getId(),
                            NameGetter.getName(stateAction)
                    )
                    .stripIndent()
            );
        }
        for (Function<StateContext<S, E>, Mono<Void>> exitAction : state.getExitActions()) {
            sb.append("""
                    %s%s : /exit %s
                    """
                    .formatted(
                            indent,
                            state.getId(),
                            NameGetter.getName(exitAction)
                    )
                    .stripIndent()
            );
        }
    }


    private void processPseudoStatesTransitions(
            Region<S, E> region,
            @Nullable ContextTransition<S, E> currentContextTransition,
            PlantUmlWriterParameters<S> plantUmlWriterParameters,
            StringBuilder sb,
            String indent
    ) {
        List<State<S, E>> states = region.getStates().stream()
                .sorted(stateComparator)
                .toList();
        for (State<S, E> state : states) {
            // collecting transitions for 'pseudoStateKinds'
            if (state.getPseudoState() != null) {
                PseudoStateKind pseudoStateKind = state.getPseudoState().getKind();
                switch (pseudoStateKind) {
                    // already taken care of ;-)
                    case INITIAL, END, CHOICE, JOIN, JUNCTION, ENTRY, EXIT, HISTORY_SHALLOW, HISTORY_DEEP -> {
                        // already taken care of ;-)
                    }
                    case FORK -> {
                        // TODO why do I have to do this here? try to do it elsewhere?
                        for (State<S, E> nextState : ((ForkPseudoState<S, E>) state.getPseudoState()).getForks()) {
                            processPseudoStatesTransition(
                                    currentContextTransition,
                                    null,
                                    state,
                                    nextState,
                                    null,
                                    plantUmlWriterParameters,
                                    sb,
                                    indent
                            );
                        }
                    }
                }
            }
        }
        sb.append("\n");
    }

    private void processPseudoStatesTransition(
            @Nullable ContextTransition<S, E> currentContextTransition,
            @Nullable Transition<S, E> transition,
            State<S, E> sourceState,
            State<S, E> targetState,
            @Nullable HistoryIdGetter<S, E> historyIdGetter,
            PlantUmlWriterParameters<S> plantUmlWriterParameters,
            StringBuilder sb,
            String indent
    ) {
        S source = sourceState.getId();
        S target = targetState.getId();
        sb.append("""
                %s%s
                %s%s -%s[%s]-> %s %s
                """
                .formatted(
                        indent,
                        historyIdGetter == null ? "" : "'" + source + " -> " + target, // if history transition, add a comment with 'real' state names
                        indent,
                        historyIdGetter == null ? sourceState.getId() : historyIdGetter.getId(sourceState),
                        plantUmlWriterParameters.getDirection(source, target),
                        plantUmlWriterParameters.getArrowColor(
                                currentContextTransition != null
                                && (
                                        currentContextTransition.getSource() == source
                                        // && currentContextTransition.event == transition.getTrigger().getEvent()
                                        && currentContextTransition.getTarget() == target
                                )
                        ),
                        historyIdGetter == null ? targetState.getId() : historyIdGetter.getId(targetState),
                        TransactionHelper.getTransitionDescription(transition, plantUmlWriterParameters)
                )
                .stripIndent()
        );
    }

    private void processTransitions(
            Region<S, E> region,
            PlantUmlWriterParameters<S> plantUmlWriterParameters,
            @Nullable ContextTransition<S, E> currentContextTransition,
            StringBuilder sb,
            String indent,
            @Nullable Predicate<Transition<S, E>> transitionAllowed
    ) {

        // initial transition
        Transition<S, E> initialTransition = TransactionHelper.getInitialTransition(region);
        if (initialTransition != null) {
            addTransition(
                    sb,
                    indent,
                    "[*]", // transition from initial state
                    null,
                    initialTransition.getTarget().getId(),
                    plantUmlWriterParameters,
                    plantUmlWriterParameters.getArrowColor(false),
                    initialTransition
            );
        }

        // region's transitions
        for (Transition<S, E> transition :
                region.getTransitions().stream().sorted(transitionComparator).toList()
        ) {
            // in orthogonalRegion, we do NOT allow transition to states from another region!
            if (transitionAllowed != null && !transitionAllowed.test(transition)) {
                return;
            }

            // collect history transitions: they will be added to the diagram later on
            if (isHistoryTransition(transition)) {
                historyTransitions.add(transition);
            } else {
                S source = transition.getSource().getId();
                S target = transition.getTarget().getId();

                switch (transition.getKind()) {
                    case EXTERNAL, INTERNAL, LOCAL -> {
                        String arrowColor = plantUmlWriterParameters.getArrowColor(
                                currentContextTransition != null
                                && (
                                        currentContextTransition.getSource() == source
                                        && currentContextTransition.getEvent() == transition.getTrigger().getEvent()
                                        && currentContextTransition.getTarget() == target
                                )
                        );
                        addTransition(sb, indent, source.toString(), source, target, plantUmlWriterParameters, arrowColor, transition);
                    }
                    case INITIAL -> throw new NotImplementedException(
                            "Unexpected INITIAL transition! They are handled in processInitialTransition(), not here! Check why we reached this exception!"
                    );
                }
            }
        }
    }

    private void addTransition(
            StringBuilder sb,
            String indent,
            String sourceLabel,
            S source,
            S target,
            PlantUmlWriterParameters<S> plantUmlWriterParameters,
            String arrowColor,
            Transition<S, E> transition
    ) {
        sb.append("""
                %s%s -%s[%s]-> %s %s
                """
                .formatted(
                        indent,
                        sourceLabel,
                        source == null ? "" : plantUmlWriterParameters.getDirection(source, target),
                        arrowColor,
                        target,
                        TransactionHelper.getTransitionDescription(transition, plantUmlWriterParameters)
                )
                .stripIndent()
        );
        if (StringUtils.isNotBlank(transition.getName())) {
            sb.append("""
                    %snote on link
                    %s    %s
                    %send note
                    """
                    .formatted(
                            indent,
                            indent,
                            transition.getName(),
                            indent
                    ));
        }
    }

    private boolean isHistoryTransition(@NotNull Transition<S, E> transition) {
        return isHistoryState(transition.getSource()) || isHistoryState(transition.getTarget());
    }

    private boolean isHistoryState(@NotNull State<S, E> state) {
        if (state.getPseudoState() == null) {
            return Boolean.FALSE;
        }
        return PseudoStateKind.HISTORY_SHALLOW.equals(state.getPseudoState().getKind())
               || PseudoStateKind.HISTORY_DEEP.equals(state.getPseudoState().getKind());
    }

}
