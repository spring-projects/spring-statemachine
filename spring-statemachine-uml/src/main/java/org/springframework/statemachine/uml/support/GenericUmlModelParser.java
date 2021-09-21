/*
 * Copyright 2016-2021 the original author or authors.
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
package org.springframework.statemachine.uml.support;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.*;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.action.Actions;
import org.springframework.statemachine.action.SpelExpressionAction;
import org.springframework.statemachine.config.model.*;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.guard.Guards;
import org.springframework.statemachine.guard.SpelExpressionGuard;
import org.springframework.statemachine.state.PseudoStateKind;
import org.springframework.statemachine.transition.TransitionKind;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Model parser which constructs states and transitions data out from an uml
 * model. This implementation is not thread safe and model parsing can only be
 * used once per instance.
 *
 * @author Janne Valkealahti
 */
public class GenericUmlModelParser<S, E> {

    public final static String LANGUAGE_BEAN = "bean";
    public final static String LANGUAGE_SPEL = "spel";
    private final Model model;
    private final StateMachineComponentResolver<S, E> resolver;
    private final Collection<StateData<S, E>> stateDatas = new ArrayList<>();
    private final Collection<TransitionData<S, E>> transitionDatas = new ArrayList<>();
    private final Collection<EntryData<S, E>> entrys = new ArrayList<>();
    private final Collection<ExitData<S, E>> exits = new ArrayList<>();
    private final Collection<HistoryData<S, E>> historys = new ArrayList<>();
    private final Map<S, LinkedList<ChoiceData<S, E>>> choices = new HashMap<>();
    private final Map<S, LinkedList<JunctionData<S, E>>> junctions = new HashMap<>();
    private final Map<S, List<S>> forks = new HashMap<>();
    private final Map<S, List<S>> joins = new HashMap<>();

    private final AtomicInteger pseudostateNamingCounter = new AtomicInteger(1);
    private final Map<NamedElement, S> pseudostateNaming = new HashMap<>();
    private final List<S> seenStateData = new ArrayList<>();
    private final List<String> seenEntryData = new ArrayList<>();
    private final List<String> seenExitData = new ArrayList<>();
    private final List<String> seenTransitionData = new ArrayList<>();
    private final GenericTypeConverter<S> stateConverter;
    private final GenericTypeConverter<E> eventConverter;

    /**
     * Instantiates a new uml model parser.
     * @param model the model
     * @param resolver the resolver
     * @param stateConverter converts between Strings and concrete state type 'S'.
     * @param eventConverter converts between Strings and concrete event type 'E'
     */
    public GenericUmlModelParser(Model model, StateMachineComponentResolver<S, E> resolver, GenericTypeConverter<S> stateConverter, GenericTypeConverter<E> eventConverter) {
        this.stateConverter = stateConverter;
        this.eventConverter = eventConverter;
        Assert.notNull(model, "Model must be set");
        Assert.notNull(resolver, "Resolver must be set");
        this.model = model;
        this.resolver = resolver;
    }

    /**
     * Parses the model.
     *
     * @return the data holder for states and transitions
     */
    public DataHolder parseModel() {
        EList<PackageableElement> packagedElements = model.getPackagedElements();

        // expect root machine to be a one having no machines in a submachineState field.
        StateMachine stateMachine = null;
        Collection<StateMachine> stateMachines = EcoreUtil.getObjectsByType(packagedElements, UMLPackage.Literals.STATE_MACHINE);
        for (StateMachine machine : stateMachines) {
            // multiple substates can point to same machine, thus it's a back reference list
            EList<State> submachineRefs = machine.getSubmachineStates();
            if (submachineRefs.size() == 0) {
                stateMachine = machine;
            }
            handleStateMachine(machine);
        }

        // all machines are iterated so we only do sanity check here for a root machine
        if (stateMachine == null) {
            throw new IllegalArgumentException("Can't find root statemachine from model");
        }

        // LinkedList can be passed due to generics, need to copy
        HashMap<S, List<ChoiceData<S, E>>> choicesCopy = new HashMap<>(choices);
        HashMap<S, List<JunctionData<S, E>>> junctionsCopy = new HashMap<>(junctions);
        return new DataHolder(new StatesData<>(stateDatas),
                new TransitionsData<>(transitionDatas, choicesCopy, junctionsCopy, forks, joins, entrys, exits, historys));
    }

    private void handleStateMachine(StateMachine stateMachine) {
        for (Region region : stateMachine.getRegions()) {
            handleRegion(region);
        }
    }

    private S resolveName(NamedElement pseudostate) {
        return pseudostateNaming.computeIfAbsent(pseudostate, key -> {
            S name = stateConverter.convert(pseudostate.getName());
            if (ObjectUtils.isEmpty(name)) {
                if (key instanceof Pseudostate) {
                    name = stateConverter.convert(((Pseudostate)key).getKind().getName() + pseudostateNamingCounter.getAndIncrement());
                }
            }
            return name;
        });
    }

    private void addStateData(StateData<S, E> stateData) {
        S key = stateData.getState();
        if (!seenStateData.contains(key)) {
            stateDatas.add(stateData);
            seenStateData.add(key);
        }
    }

    private void addEntryData(EntryData<S, E> entryData) {
        S skey = entryData.getSource();
        S tkey = entryData.getTarget();
        String key = skey + "_" + tkey;
        if (!seenEntryData.contains(key)) {
            entrys.add(entryData);
            seenEntryData.add(key);
        }
    }

    private void addExitData(ExitData<S, E> exitData) {
        S skey = exitData.getSource();
        S tkey = exitData.getTarget();
        String key = skey + "_" + tkey;
        if (!seenExitData.contains(key)) {
            exits.add(exitData);
            seenExitData.add(key);
        }
    }

    private void addTransitionData(TransitionData<S, E> transitionData) {
        S skey = transitionData.getSource();
        S tkey = transitionData.getTarget();
        E ekey = transitionData.getEvent();
        TransitionKind kkey = transitionData.getKind();
        String key = skey + "_" + tkey + "_" + ekey + "_" + kkey;
        if (!seenTransitionData.contains(key)) {
            transitionDatas.add(transitionData);
            seenTransitionData.add(key);
        }
    }

    private void handleRegion(Region region) {
        // build states
        for (Vertex vertex : region.getSubvertices()) {
            // normal states
            if (vertex instanceof State) {
                State state = (State)vertex;

                // find parent state if submachine state, root states have null parent
                S parent = null;
                String regionId = null;
                if (state.getContainer().getOwner() instanceof State) {
                    parent = stateConverter.convert(((State)state.getContainer().getOwner()).getName());
                }
                // if parent is unknown, check if it's a ref where parent is then that
                if (parent == null && region.getOwner() instanceof StateMachine) {
                    EList<State> submachineStates = ((StateMachine)region.getOwner()).getSubmachineStates();
                    if (submachineStates.size() == 1) {
                        parent = stateConverter.convert(submachineStates.get(0).getName());
                    }
                }
                if (state.getOwner() instanceof Region) {
                    regionId = ((Region)state.getOwner()).getName();
                }
                boolean isInitialState = UmlUtils.isInitialState(state);
                StateData<S, E> stateData = handleActions(
                        new StateData<>(parent, regionId, stateConverter.convert(state.getName()), isInitialState), state);
                if (isInitialState) {
                    // set possible initial transition
                    stateData.setInitialAction(resolveInitialTransitionAction(state));
                }
                stateData.setDeferred(UmlUtils.resolveDeferredEvents(state, eventConverter));
                if (UmlUtils.isFinalState(state)) {
                    stateData.setEnd(true);
                }
                addStateData(stateData);

                // add states via entry/exit reference points
                for (ConnectionPointReference cpr : state.getConnections()) {
                    if (cpr.getEntries() != null) {
                        for (Pseudostate cp : cpr.getEntries()) {
                            StateData<S, E> cpStateData = new StateData<>(parent, regionId, stateConverter.convert(cp.getName()), false);
                            cpStateData.setPseudoStateKind(PseudoStateKind.ENTRY);
                            addStateData(cpStateData);
                        }
                    }
                    if (cpr.getExits() != null) {
                        for (Pseudostate cp : cpr.getExits()) {
                            StateData<S, E> cpStateData = new StateData<>(parent, regionId, stateConverter.convert(cp.getName()), false);
                            cpStateData.setPseudoStateKind(PseudoStateKind.EXIT);
                            addStateData(cpStateData);
                        }
                    }
                }

                // add states via entry/exit points
                for (Pseudostate cp : state.getConnectionPoints()) {
                    PseudoStateKind kind = null;
                    if (cp.getKind() == PseudostateKind.ENTRY_POINT_LITERAL) {
                        kind = PseudoStateKind.ENTRY;
                    } else if (cp.getKind() == PseudostateKind.EXIT_POINT_LITERAL) {
                        kind = PseudoStateKind.EXIT;
                    }
                    if (kind != null) {
                        StateData<S, E> cpStateData = new StateData<>(parent, regionId, stateConverter.convert(cp.getName()), false);
                        cpStateData.setPseudoStateKind(kind);
                        addStateData(cpStateData);
                    }
                }

                if (!state.getRegions().isEmpty()) {
                    // do recursive handling of regions
                    for (Region sub : state.getRegions()) {
                        handleRegion(sub);
                    }
                } else if (state.getSubmachine() != null) {
                    // submachine would be there i.e. with import
                    handleStateMachine(state.getSubmachine());
                }
            }
            // pseudostates like choice, etc
            if (vertex instanceof Pseudostate) {
                Pseudostate state = (Pseudostate)vertex;
                S parent = null;
                String regionId = null;
                if (state.getContainer().getOwner() instanceof State) {
                    parent = stateConverter.convert(((State)state.getContainer().getOwner()).getName());
                } else if (state.getContainer().getOwner() instanceof StateMachine) {
                    // in case of a submachine ref, owner should be StateMachine instead
                    // of State so try to find from owning submachines states a matching one with a
                    // same name.
                    StateMachine owningMachine = (StateMachine)state.getContainer().getOwner();
                    parent = stateConverter.convert(owningMachine.getSubmachineStates().stream()
                            .filter(m -> owningMachine == m.getSubmachine())
                            .map(NamedElement::getName)
                            .findFirst()
                            .orElse(null));
                }
                if (state.getOwner() instanceof Region) {
                    regionId = ((Region)state.getOwner()).getName();
                }
                if (state.getKind() == PseudostateKind.CHOICE_LITERAL) {
                    StateData<S, E> cpStateData = new StateData<>(parent, regionId, resolveName(state), false);
                    cpStateData.setPseudoStateKind(PseudoStateKind.CHOICE);
                    addStateData(cpStateData);
                } else if (state.getKind() == PseudostateKind.JUNCTION_LITERAL) {
                    StateData<S, E> cpStateData = new StateData<>(parent, regionId, stateConverter.convert(state.getName()), false);
                    cpStateData.setPseudoStateKind(PseudoStateKind.JUNCTION);
                    addStateData(cpStateData);
                } else if (state.getKind() == PseudostateKind.FORK_LITERAL) {
                    StateData<S, E> cpStateData = new StateData<>(parent, regionId, stateConverter.convert(state.getName()), false);
                    cpStateData.setPseudoStateKind(PseudoStateKind.FORK);
                    addStateData(cpStateData);
                } else if (state.getKind() == PseudostateKind.JOIN_LITERAL) {
                    StateData<S, E> cpStateData = new StateData<>(parent, regionId, stateConverter.convert(state.getName()), false);
                    cpStateData.setPseudoStateKind(PseudoStateKind.JOIN);
                    addStateData(cpStateData);
                } else if (state.getKind() == PseudostateKind.SHALLOW_HISTORY_LITERAL) {
                    StateData<S, E> cpStateData = new StateData<>(parent, regionId, stateConverter.convert(state.getName()), false);
                    cpStateData.setPseudoStateKind(PseudoStateKind.HISTORY_SHALLOW);
                    addStateData(cpStateData);
                } else if (state.getKind() == PseudostateKind.DEEP_HISTORY_LITERAL) {
                    StateData<S, E> cpStateData = new StateData<>(parent, regionId, stateConverter.convert(state.getName()), false);
                    cpStateData.setPseudoStateKind(PseudoStateKind.HISTORY_DEEP);
                    addStateData(cpStateData);
                }
            }
        }

        // build transitions
        for (Transition transition : region.getTransitions()) {
            // for entry/exit points we need to create these outside
            // of triggers as link from point to a state is most likely
            // just a link and don't have any triggers.
            // little unclear for now if link from points to a state should
            // have trigger?
            // anyway, we need to add entrys and exits to a model

            if (transition.getSource() instanceof ConnectionPointReference) {
                // support ref points if only one is defined as for some
                // reason uml can define multiple ones which is not
                // realistic with state machines
                EList<Pseudostate> cprentries = ((ConnectionPointReference)transition.getSource()).getEntries();
                if (cprentries != null && cprentries.size() == 1 && cprentries.get(0).getKind() == PseudostateKind.ENTRY_POINT_LITERAL) {
                    addEntryData(new EntryData<>(stateConverter.convert(cprentries.get(0).getName()), resolveName(transition.getTarget())));
                }
                EList<Pseudostate> cprexits = ((ConnectionPointReference)transition.getSource()).getExits();
                if (cprexits != null && cprexits.size() == 1 && cprexits.get(0).getKind() == PseudostateKind.EXIT_POINT_LITERAL) {
                    addExitData(new ExitData<>(stateConverter.convert(cprexits.get(0).getName()), resolveName(transition.getTarget())));
                }
            }

            if (transition.getSource() instanceof Pseudostate) {
                if (((Pseudostate)transition.getSource()).getKind() == PseudostateKind.ENTRY_POINT_LITERAL) {
                    addEntryData(new EntryData<>(resolveName(transition.getSource()), resolveName(transition.getTarget())));
                } else if (((Pseudostate)transition.getSource()).getKind() == PseudostateKind.EXIT_POINT_LITERAL) {
                    addExitData(new ExitData<>(resolveName(transition.getSource()), resolveName(transition.getTarget())));
                } else if (((Pseudostate)transition.getSource()).getKind() == PseudostateKind.CHOICE_LITERAL) {
                    LinkedList<ChoiceData<S, E>> list = choices.computeIfAbsent(resolveName(transition.getSource()), k -> new LinkedList<>());
                    Guard<S, E> guard = resolveGuard(transition);
                    Collection<org.springframework.statemachine.action.Action<S, E>> actions = UmlUtils.resolveTransitionActions(transition, resolver);
                    // we want null guards to be at the end
                    if (guard == null) {
                        list.addLast(new ChoiceData<>(resolveName(transition.getSource()), resolveName(transition.getTarget()), guard, actions));
                    } else {
                        list.addFirst(new ChoiceData<>(resolveName(transition.getSource()), resolveName(transition.getTarget()), guard, actions));
                    }
                } else if (((Pseudostate)transition.getSource()).getKind() == PseudostateKind.JUNCTION_LITERAL) {
                    LinkedList<JunctionData<S, E>> list = junctions.computeIfAbsent(resolveName(transition.getSource()), k -> new LinkedList<>());
                    Guard<S, E> guard = resolveGuard(transition);
                    Collection<org.springframework.statemachine.action.Action<S, E>> actions = UmlUtils.resolveTransitionActions(transition, resolver);
                    // we want null guards to be at the end
                    if (guard == null) {
                        list.addLast(new JunctionData<>(resolveName(transition.getSource()), resolveName(transition.getTarget()), guard, actions));
                    } else {
                        list.addFirst(new JunctionData<>(resolveName(transition.getSource()), resolveName(transition.getTarget()), guard, actions));
                    }
                } else if (((Pseudostate)transition.getSource()).getKind() == PseudostateKind.FORK_LITERAL) {
                    List<S> list = forks.computeIfAbsent(resolveName(transition.getSource()), k -> new ArrayList<>());
                    list.add(resolveName(transition.getTarget()));
                } else if (((Pseudostate)transition.getSource()).getKind() == PseudostateKind.SHALLOW_HISTORY_LITERAL) {
                    historys.add(new HistoryData<>(resolveName(transition.getSource()), resolveName(transition.getTarget())));
                } else if (((Pseudostate)transition.getSource()).getKind() == PseudostateKind.DEEP_HISTORY_LITERAL) {
                    historys.add(new HistoryData<>(resolveName(transition.getSource()), resolveName(transition.getTarget())));
                }
            }
            if (transition.getTarget() instanceof Pseudostate) {
                if (((Pseudostate)transition.getTarget()).getKind() == PseudostateKind.JOIN_LITERAL) {
                    List<S> list = joins.computeIfAbsent(resolveName(transition.getTarget()), k -> new ArrayList<>());
                    list.add(resolveName(transition.getSource()));
                }
            }

            // go through all triggers and create transition
            // from signals, or transitions from timers
            for (Trigger trigger : transition.getTriggers()) {
                Guard<S, E> guard = resolveGuard(transition);
                Event event = trigger.getEvent();
                if (event instanceof SignalEvent) {
                    Signal signal = ((SignalEvent)event).getSignal();
                    if (signal != null) {
                        // special case for ref point
                        if (transition.getTarget() instanceof ConnectionPointReference) {
                            EList<Pseudostate> cprentries = ((ConnectionPointReference)transition.getTarget()).getEntries();
                            if (cprentries != null && cprentries.size() == 1) {
                                addTransitionData(new TransitionData<>(resolveName(transition.getSource()),
                                        stateConverter.convert(cprentries.get(0).getName()), eventConverter.convert(signal.getName()),
                                        UmlUtils.resolveTransitionActionFunctions(transition, resolver), Guards.from(guard),
                                        UmlUtils.mapUmlTransitionType(transition), transition.getName()));
                            }
                        } else {
                            addTransitionData(new TransitionData<>(resolveName(transition.getSource()),
                                    resolveName(transition.getTarget()), eventConverter.convert(signal.getName()),
                                    UmlUtils.resolveTransitionActionFunctions(transition, resolver), Guards.from(guard),
                                    UmlUtils.mapUmlTransitionType(transition), transition.getName()));
                        }
                    }
                } else if (event instanceof TimeEvent) {
                    TimeEvent timeEvent = (TimeEvent)event;
                    Long period = getTimePeriod(timeEvent);
                    if (period != null) {
                        Integer count = null;
                        if (timeEvent.isRelative()) {
                            count = 1;
                        }
                        addTransitionData(new TransitionData<>(resolveName(transition.getSource()),
                                resolveName(transition.getTarget()), period, count, UmlUtils.resolveTransitionActionFunctions(transition, resolver),
                                Guards.from(guard), UmlUtils.mapUmlTransitionType(transition), transition.getName()));
                    }
                }
            }

            // create anonymous transition if needed
            if (shouldCreateAnonymousTransition(transition)) {
                addTransitionData(new TransitionData<>(resolveName(transition.getSource()),
                        resolveName(transition.getTarget()), null,
                        UmlUtils.resolveTransitionActionFunctions(transition, resolver),
                        Guards.from(resolveGuard(transition)), UmlUtils.mapUmlTransitionType(transition), transition.getName()));
            }
        }
    }

    private Guard<S, E> resolveGuard(Transition transition) {
        Guard<S, E> guard = null;
        for (Constraint c : transition.getOwnedRules()) {
            if (c.getSpecification() instanceof OpaqueExpression) {
                String beanId = UmlUtils.resolveBodyByLanguage(LANGUAGE_BEAN, (OpaqueExpression)c.getSpecification());
                if (StringUtils.hasText(beanId)) {
                    guard = resolver.resolveGuard(beanId);
                } else {
                    String expression = UmlUtils.resolveBodyByLanguage(LANGUAGE_SPEL, (OpaqueExpression)c.getSpecification());
                    if (StringUtils.hasText(expression)) {
                        SpelExpressionParser parser = new SpelExpressionParser(
                                new SpelParserConfiguration(SpelCompilerMode.MIXED, null));
                        guard = new SpelExpressionGuard<>(parser.parseExpression(expression));
                    }
                }
            }
        }
        return guard;
    }

    private Long getTimePeriod(TimeEvent event) {
        try {
            return (long) event.getWhen().getExpr().integerValue();
        } catch (Exception e) {
            return null;
        }
    }

    private org.springframework.statemachine.action.Action<S, E> resolveInitialTransitionAction(State state) {
        Transition transition = UmlUtils.resolveInitialTransition(state);
        if (transition != null) {
            return UmlUtils.resolveTransitionAction(transition, resolver);
        } else {
            return null;
        }
    }

    private boolean shouldCreateAnonymousTransition(Transition transition) {
        if (transition.getSource() == null || transition.getTarget() == null) {
            // nothing to do as would cause NPE later
            return false;
        }
        if (!transition.getTriggers().isEmpty()) {
            return false;
        }
        if (!StringUtils.hasText(stateConverter.revert(resolveName(transition.getSource())))) {
            return false;
        }
        if (!StringUtils.hasText(stateConverter.revert(resolveName(transition.getTarget())))) {
            return false;
        }
        if (transition.getSource() instanceof Pseudostate) {
            if (((Pseudostate)transition.getSource()).getKind() == PseudostateKind.FORK_LITERAL) {
                return false;
            }
        }
        if (transition.getTarget() instanceof Pseudostate) {
            return ((Pseudostate) transition.getTarget()).getKind() != PseudostateKind.JOIN_LITERAL;
        }
        return true;
    }

    private StateData<S, E> handleActions(StateData<S, E> stateData, State state) {
        if (state.getEntry() instanceof OpaqueBehavior) {
            String beanId = UmlUtils.resolveBodyByLanguage(LANGUAGE_BEAN, (OpaqueBehavior)state.getEntry());
            if (StringUtils.hasText(beanId)) {
                org.springframework.statemachine.action.Action<S, E> bean = resolver.resolveAction(beanId);
                if (bean != null) {
                    ArrayList<Function<StateContext<S, E>, Mono<Void>>> entrys = new ArrayList<>();
                    entrys.add(Actions.from(bean));
                    stateData.setEntryActions(entrys);
                }
            } else {
                String expression = UmlUtils.resolveBodyByLanguage(LANGUAGE_SPEL, (OpaqueBehavior)state.getEntry());
                if (StringUtils.hasText(expression)) {
                    SpelExpressionParser parser = new SpelExpressionParser(
                            new SpelParserConfiguration(SpelCompilerMode.MIXED, null));
                    ArrayList<Function<StateContext<S, E>, Mono<Void>>> entrys = new ArrayList<>();
                    entrys.add(Actions.from(new SpelExpressionAction<>(parser.parseExpression(expression))));
                    stateData.setEntryActions(entrys);
                }
            }
        }
        if (state.getExit() instanceof OpaqueBehavior) {
            String beanId = UmlUtils.resolveBodyByLanguage(LANGUAGE_BEAN, (OpaqueBehavior)state.getExit());
            if (StringUtils.hasText(beanId)) {
                org.springframework.statemachine.action.Action<S, E> bean = resolver.resolveAction(beanId);
                if (bean != null) {
                    ArrayList<Function<StateContext<S, E>, Mono<Void>>> exits = new ArrayList<>();
                    exits.add(Actions.from(bean));
                    stateData.setExitActions(exits);
                }
            } else {
                String expression = UmlUtils.resolveBodyByLanguage(LANGUAGE_SPEL, (OpaqueBehavior)state.getExit());
                if (StringUtils.hasText(expression)) {
                    SpelExpressionParser parser = new SpelExpressionParser(
                            new SpelParserConfiguration(SpelCompilerMode.MIXED, null));
                    ArrayList<Function<StateContext<S, E>, Mono<Void>>> exits = new ArrayList<>();
                    exits.add(Actions.from(new SpelExpressionAction<>(parser.parseExpression(expression))));
                    stateData.setExitActions(exits);
                }
            }
        }
        if (state.getDoActivity() instanceof OpaqueBehavior) {
            String beanId = UmlUtils.resolveBodyByLanguage(LANGUAGE_BEAN, (OpaqueBehavior)state.getDoActivity());
            if (StringUtils.hasText(beanId)) {
                org.springframework.statemachine.action.Action<S, E> bean = resolver.resolveAction(beanId);
                if (bean != null) {
                    ArrayList<Function<StateContext<S, E>, Mono<Void>>> stateActions = new ArrayList<>();
                    stateActions.add(Actions.from(bean));
                    stateData.setStateActions(stateActions);
                }
            } else {
                String expression = UmlUtils.resolveBodyByLanguage(LANGUAGE_SPEL, (OpaqueBehavior)state.getDoActivity());
                if (StringUtils.hasText(expression)) {
                    SpelExpressionParser parser = new SpelExpressionParser(
                            new SpelParserConfiguration(SpelCompilerMode.MIXED, null));
                    ArrayList<Function<StateContext<S, E>, Mono<Void>>> stateActions = new ArrayList<>();
                    stateActions.add(Actions.from(new SpelExpressionAction<>(parser.parseExpression(expression))));
                    stateData.setStateActions(stateActions);
                }
            }
        }
        if (state.getEntry() instanceof Activity) {
            String beanId = state.getEntry().getName();
            org.springframework.statemachine.action.Action<S, E> bean = resolver.resolveAction(beanId);
            if (bean != null) {
                ArrayList<Function<StateContext<S, E>, Mono<Void>>> entrys = new ArrayList<>();
                entrys.add(Actions.from(bean));
                stateData.setEntryActions(entrys);
            }
        }
        if (state.getExit() instanceof Activity) {
            String beanId = state.getExit().getName();
            org.springframework.statemachine.action.Action<S, E> bean = resolver.resolveAction(beanId);
            if (bean != null) {
                ArrayList<Function<StateContext<S, E>, Mono<Void>>> exits = new ArrayList<>();
                exits.add(Actions.from(bean));
                stateData.setExitActions(exits);
            }
        }
        if (state.getDoActivity() instanceof Activity) {
            String beanId = state.getDoActivity().getName();
            Action<S, E> bean = resolver.resolveAction(beanId);
            if (bean != null) {
                ArrayList<Function<StateContext<S, E>, Mono<Void>>> stateActions = new ArrayList<>();
                stateActions.add(Actions.from(bean));
                stateData.setStateActions(stateActions);
            }
        }
        return stateData;
    }

    /**
     * Holder object for results returned from uml parser.
     */
    public class DataHolder {
        private final StatesData<S, E> statesData;
        private final TransitionsData<S, E> transitionsData;

        /**
         * Instantiates a new data holder.
         *
         * @param statesData the states data
         * @param transitionsData the transitions data
         */
        public DataHolder(StatesData<S, E> statesData, TransitionsData<S, E> transitionsData) {
            this.statesData = statesData;
            this.transitionsData = transitionsData;
        }

        /**
         * Gets the states data.
         *
         * @return the states data
         */
        public StatesData<S, E> getStatesData() {
            return statesData;
        }

        /**
         * Gets the transitions data.
         *
         * @return the transitions data
         */
        public TransitionsData<S, E> getTransitionsData() {
            return transitionsData;
        }
    }
}
