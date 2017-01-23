package modula.core.semantics;


import modula.core.*;
import modula.core.invoke.Invoker;
import modula.core.invoke.InvokerException;
import modula.core.model.*;
import modula.core.system.EventVariable;

import java.util.*;

/**
 * 执行语义实现
 */
public class ModulaSemanticsImpl implements ModulaSemantics {

    /**
     * 错误后缀
     */
    public static final String ERR_ILLEGAL_ALLOC = ".error.illegalalloc";

    /**
     * 暂时不用
     */
    public Modula normalizeStateMachine(final Modula input, final ErrorReporter errRep) {
        return input;
    }

    public void firstStep(final ModulaExecutionContext exctx) throws ModelException {
        // 初始化上下文与状态机
        exctx.initialize();
        // 初始state
        HashSet<TransitionalState> statesToInvoke = new HashSet<TransitionalState>();
        Step step = new Step(null);
        step.getTransitList().add(exctx.getStateMachine().getInitialTransition());
        microStep(exctx, step, statesToInvoke);
        // AssignCurrentStatus
        setSystemAllStatesVariable(exctx.getScInstance());
        // Execute Immediate Transitions

        if (exctx.isRunning()) {
            macroStep(exctx, statesToInvoke);
        }

        if (!exctx.isRunning()) {
            finalStep(exctx);
        }
    }

    public void nextStep(final ModulaExecutionContext exctx, final TriggerEvent event) throws ModelException {
        if (!exctx.isRunning()) {
            return;
        }
        if (isCancelEvent(event)) {
            exctx.stopRunning();
        } else {
            setSystemEventVariable(exctx.getScInstance(), event, false);
            processInvokes(exctx, event);
            Step step = new Step(event);
            selectTransitions(exctx, step);
            if (!step.getTransitList().isEmpty()) {
                HashSet<TransitionalState> statesToInvoke = new HashSet<TransitionalState>();
                microStep(exctx, step, statesToInvoke);
                setSystemAllStatesVariable(exctx.getScInstance());
                if (exctx.isRunning()) {
                    macroStep(exctx, statesToInvoke);
                }
            }
        }
        if (!exctx.isRunning()) {
            finalStep(exctx);
        }
    }

    public void finalStep(ModulaExecutionContext exctx) throws ModelException {
        if (exctx.isRunning()) {
            return;
        }
        ArrayList<EnterableState> configuration = new ArrayList<EnterableState>(exctx.getScInstance().getCurrentStatus().getAllStates());
        Collections.sort(configuration, DocumentOrder.reverseDocumentOrderComparator);
        for (EnterableState es : configuration) {
            for (OnExit onexit : es.getOnExits()) {
                executeContent(exctx, onexit);
            }
            if (es instanceof TransitionalState) {
                // check if invokers are active in this state
                for (Invoke inv : ((TransitionalState) es).getInvokes()) {
                    exctx.cancelInvoker(inv);
                }
            }
            exctx.getNotificationRegistry().fireOnExit(es, es);
            exctx.getNotificationRegistry().fireOnExit(exctx.getStateMachine(), es);
            if (!(es instanceof Final && es.getParent() == null)) {
                exctx.getScInstance().getCurrentStatus().getStates().remove(es);
            }
            // else: keep final Final
            // TODO: returnDoneEvent(s.donedata)?
        }
    }

    /**
     * microStep
     */
    public void microStep(final ModulaExecutionContext exctx, final Step step,
                          final Set<TransitionalState> statesToInvoke)
            throws ModelException {
        buildStep(exctx, step);
        exitStates(exctx, step, statesToInvoke);
        executeTransitionContent(exctx, step);
        enterStates(exctx, step, statesToInvoke);
    }

    /**
     * buildStep：exitSet，entrySet，transitionList
     */
    public void buildStep(final ModulaExecutionContext exctx, final Step step) throws ModelException {
        step.getExitSet().clear();
        step.getEntrySet().clear();
        step.getDefaultEntrySet().clear();
        step.getDefaultHistoryTransitionEntryMap().clear();

        // 计算出口集合
        if (!exctx.getScInstance().getCurrentStatus().getStates().isEmpty()) {
            computeExitSet(step, exctx.getScInstance().getCurrentStatus().getAllStates());
        }
        // 计算入口集合
        computeEntrySet(exctx, step);

        // default result states to entrySet
        Set<EnterableState> states = step.getEntrySet();
        if (!step.getExitSet().isEmpty()) {
            // calculate result states by taking current states, subtracting exitSet and adding entrySet
            states = new HashSet<EnterableState>(exctx.getScInstance().getCurrentStatus().getStates());
            states.removeAll(step.getExitSet());
            states.addAll(step.getEntrySet());
        }
    }

    /**
     * macroStep
     */
    public void macroStep(final ModulaExecutionContext exctx, final Set<TransitionalState> statesToInvoke)
            throws ModelException {
        do {
            boolean macroStepDone = false;
            do {
                Step step = new Step(null);
                selectTransitions(exctx, step);
                if (step.getTransitList().isEmpty()) {
                    TriggerEvent event = exctx.nextInternalEvent();
                    if (event != null) {
                        if (isCancelEvent(event)) {
                            exctx.stopRunning();
                        } else {
                            setSystemEventVariable(exctx.getScInstance(), event, true);
                            step = new Step(event);
                            selectTransitions(exctx, step);
                        }
                    }
                }
                if (step.getTransitList().isEmpty()) {
                    macroStepDone = true;
                } else {
                    microStep(exctx, step, statesToInvoke);
                    setSystemAllStatesVariable(exctx.getScInstance());
                }

            } while (exctx.isRunning() && !macroStepDone);

            if (exctx.isRunning() && !statesToInvoke.isEmpty()) {
                initiateInvokes(exctx, statesToInvoke);
                statesToInvoke.clear();
            }
        } while (exctx.isRunning() && exctx.hasPendingInternalEvent());
    }

    public void computeExitSet(final Step step, final Set<EnterableState> configuration) {
        for (SimpleTransition st : step.getTransitList()) {
            computeExitSet(st, step.getExitSet(), configuration);
        }
    }

    public void computeExitSet(SimpleTransition transition, Set<EnterableState> exitSet, Set<EnterableState> configuration) {
        if (!transition.getTargets().isEmpty()) {
            TransitionalState transitionDomain = transition.getTransitionDomain();
            if (transitionDomain == null) {
                // root transition: every active state will be exited
                exitSet.addAll(configuration);
            } else {
                for (EnterableState state : configuration) {
                    if (state.isDescendantOf(transitionDomain)) {
                        exitSet.add(state);
                    }
                }
            }
        }
    }

    public void computeEntrySet(final ModulaExecutionContext exctx, final Step step) {
        Set<History> historyTargets = new HashSet<History>();
        Set<EnterableState> entrySet = new HashSet<EnterableState>();
        for (SimpleTransition st : step.getTransitList()) {
            for (TransitionTarget tt : st.getTargets()) {
                if (tt instanceof EnterableState) {
                    entrySet.add((EnterableState) tt);
                } else {
                    // History
                    historyTargets.add((History) tt);
                }
            }
        }
        for (EnterableState es : entrySet) {
            addDescendantStatesToEnter(exctx, step, es);
        }
        for (History h : historyTargets) {
            addDescendantStatesToEnter(exctx, step, h);
        }
        for (SimpleTransition st : step.getTransitList()) {
            TransitionalState ancestor = st.getTransitionDomain();
            for (TransitionTarget tt : st.getTargets()) {
                addAncestorStatesToEnter(exctx, step, tt, ancestor);
            }
        }
    }

    public void addDescendantStatesToEnter(final ModulaExecutionContext exctx, final Step step,
                                           final TransitionTarget tt) {
        if (tt instanceof History) {
            History h = (History) tt;
            if (exctx.getScInstance().isEmpty(h)) {
                step.getDefaultHistoryTransitionEntryMap().put(h.getParent(), h.getTransition());
                for (TransitionTarget dtt : h.getTransition().getTargets()) {
                    addDescendantStatesToEnter(exctx, step, dtt);
                    addAncestorStatesToEnter(exctx, step, dtt, tt.getParent());
                }
            } else {
                for (TransitionTarget dtt : exctx.getScInstance().getLastConfiguration(h)) {
                    addDescendantStatesToEnter(exctx, step, dtt);
                    addAncestorStatesToEnter(exctx, step, dtt, tt.getParent());
                }
            }
        } else { // tt instanceof EnterableState
            EnterableState es = (EnterableState) tt;
            step.getEntrySet().add(es);
//            if (es instanceof Parallel) {
//                for (EnterableState child : ((Parallel) es).getChildren()) {
//                    if (!containsDescendant(step.getEntrySet(), child)) {
//                        addDescendantStatesToEnter(exctx, step, child);
//                    }
//                }
//            } else if (es instanceof State && ((State) es).isComposite()) {
//                step.getDefaultEntrySet().add(es);
//                for (TransitionTarget dtt : ((State) es).getInitial().getTransition().getTargets()) {
//                    addDescendantStatesToEnter(exctx, step, dtt);
//                    addAncestorStatesToEnter(exctx, step, dtt, tt);
//                }
//            }
        }
    }

    public void addAncestorStatesToEnter(final ModulaExecutionContext exctx, final Step step,
                                         final TransitionTarget tt, TransitionTarget ancestor) {
        // for for anc in getProperAncestors(tt,ancestor)
        for (int i = tt.getNumberOfAncestors() - 1; i > -1; i--) {
            EnterableState anc = tt.getAncestor(i);
            if (anc == ancestor) {
                break;
            }
            step.getEntrySet().add(anc);
//            if (anc instanceof Parallel) {
//                for (EnterableState child : ((Parallel) anc).getChildren()) {
//                    if (!containsDescendant(step.getEntrySet(), child)) {
//                        addDescendantStatesToEnter(exctx, step, child);
//                    }
//                }
//
//            }
        }
    }

    public boolean containsDescendant(Set<EnterableState> states, EnterableState state) {
        for (EnterableState es : states) {
            if (es.isDescendantOf(state)) {
                return true;
            }
        }
        return false;
    }

    public void selectTransitions(final ModulaExecutionContext exctx, final Step step) throws ModelException {
        step.getTransitList().clear();
        ArrayList<Transition> enabledTransitions = new ArrayList<Transition>();

        ArrayList<EnterableState> configuration = new ArrayList<EnterableState>(exctx.getScInstance().getCurrentStatus().getAllStates());
        Collections.sort(configuration, DocumentOrder.documentOrderComparator);

        HashSet<EnterableState> visited = new HashSet<EnterableState>();

        String eventName = step.getEvent() != null ? step.getEvent().getName() : null;
        for (EnterableState es : configuration) {
            if (es.isAtomicState()) {
                if (es instanceof Final) {
                    // Final states don't have transitions, skip to parent
                    if (es.getParent() == null) {
                        // should not happen: a top level active Final state should have stopped the state machine
                        throw new ModelException("Illegal state machine configuration: encountered top level <final> "
                                + "state while processing an event");
                    } else {
                        es = es.getParent();
                    }
                }
                TransitionalState state = (TransitionalState) es;
                TransitionalState current = state;
                int ancestorIndex = state.getNumberOfAncestors() - 1;
                boolean transitionMatched = false;
                do {
                    for (Transition transition : current.getTransitionsList()) {
                        if (transitionMatched = matchTransition(exctx, transition, eventName)) {
                            enabledTransitions.add(transition);
                            break;
                        }
                    }
                    current = (!transitionMatched && ancestorIndex > -1) ? state.getAncestor(ancestorIndex--) : null;
                } while (!transitionMatched && current != null && visited.add(current));
            }
        }
        removeConflictingTransitions(exctx, step, enabledTransitions);
    }

    public void removeConflictingTransitions(final ModulaExecutionContext exctx, final Step step,
                                             final List<Transition> enabledTransitions) {
        LinkedHashSet<Transition> filteredTransitions = new LinkedHashSet<Transition>();
        LinkedHashSet<Transition> preemptedTransitions = new LinkedHashSet<Transition>();
        Map<Transition, Set<EnterableState>> exitSets = new HashMap<Transition, Set<EnterableState>>();

        Set<EnterableState> configuration = exctx.getScInstance().getCurrentStatus().getAllStates();
        Collections.sort(enabledTransitions, DocumentOrder.documentOrderComparator);

        for (Transition t1 : enabledTransitions) {
            boolean t1Preempted = false;
            Set<EnterableState> t1ExitSet = exitSets.get(t1);
            for (Transition t2 : filteredTransitions) {
                if (t1ExitSet == null) {
                    t1ExitSet = new HashSet<EnterableState>();
                    computeExitSet(t1, t1ExitSet, configuration);
                    exitSets.put(t1, t1ExitSet);
                }
                Set<EnterableState> t2ExitSet = exitSets.get(t2);
                if (t2ExitSet == null) {
                    t2ExitSet = new HashSet<EnterableState>();
                    computeExitSet(t2, t2ExitSet, configuration);
                    exitSets.put(t2, t2ExitSet);
                }
                Set<EnterableState> smaller = t1ExitSet.size() < t2ExitSet.size() ? t1ExitSet : t2ExitSet;
                Set<EnterableState> larger = smaller == t1ExitSet ? t2ExitSet : t1ExitSet;
                boolean hasIntersection = false;
                for (EnterableState s1 : smaller) {
                    hasIntersection = larger.contains(s1);
                    if (hasIntersection) {
                        break;
                    }
                }
                if (hasIntersection) {
                    if (t1.getParent().isDescendantOf(t2.getParent())) {
                        preemptedTransitions.add(t2);
                    } else {
                        t1Preempted = true;
                        break;
                    }
                }
            }
            if (t1Preempted) {
                exitSets.remove(t1);
            } else {
                for (Transition preempted : preemptedTransitions) {
                    filteredTransitions.remove(preempted);
                    exitSets.remove(preempted);
                }
                filteredTransitions.add(t1);
            }
        }
        step.getTransitList().addAll(filteredTransitions);
    }

    public boolean matchTransition(final ModulaExecutionContext exctx, final Transition transition, final String eventName) {
        if (eventName != null) {
            if (!(transition.isNoEventsTransition() || transition.isAllEventsTransition())) {
                boolean eventMatch = false;
                for (String event : transition.getEvents()) {
                    if (eventName.startsWith(event)) {
                        if (eventName.length() == event.length() || eventName.charAt(event.length()) == '.')
                            eventMatch = true;
                        break;
                    }
                }
                if (!eventMatch) {
                    return false;
                }
            }
        } else if (!transition.isNoEventsTransition()) {
            return false;
        }
        //暂时不支持transition的cond条件
        return true;
    }

    public boolean isInFinalState(final EnterableState es, final Set<EnterableState> configuration) {
        if (es instanceof State) {
            for (EnterableState child : ((State) es).getChildren()) {
                if (child instanceof Final && configuration.contains(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isInvokerEvent(final String invokerId, final TriggerEvent event) {
        return event.getName().equals("done.invoke." + invokerId) ||
                event.getName().startsWith("done.invoke." + invokerId + ".");
    }

    public boolean isCancelEvent(TriggerEvent event) {
        return (event.getType() == TriggerEvent.CANCEL_EVENT);
    }

    public void setSystemAllStatesVariable(final SCInstance scInstance) {
        Status currentStatus = scInstance.getCurrentStatus();
        //TODO
        //scInstance.getSystemContext().setLocal(ModulaSystemContext.ALL_STATES_KEY, currentStatus.getAllStates());
    }

    /**
     * 存储event到systemContext
     */
    public void setSystemEventVariable(final SCInstance scInstance, final TriggerEvent event, boolean internal) {
        Context systemContext = scInstance.getSystemContext();
        EventVariable eventVar = null;
        if (event != null) {
            String eventType = internal ? EventVariable.TYPE_INTERNAL : EventVariable.TYPE_EXTERNAL;

            final int triggerEventType = event.getType();
            if (triggerEventType == TriggerEvent.ERROR_EVENT || triggerEventType == TriggerEvent.CHANGE_EVENT) {
                eventType = EventVariable.TYPE_PLATFORM;
            }

            // TODO: determine sendid, origin, originType and invokeid based on context later.
            eventVar = new EventVariable(event.getName(), eventType, null, null, null, null, event.getPayload());
        }
        systemContext.setLocal(ModulaSystemContext.EVENT_KEY, eventVar);
    }

    public void exitStates(final ModulaExecutionContext exctx, final Step step,
                           final Set<TransitionalState> statesToInvoke)
            throws ModelException {
        if (step.getExitSet().isEmpty()) {
            return;
        }
        Set<EnterableState> configuration = null;
        ArrayList<EnterableState> exitList = new ArrayList<EnterableState>(step.getExitSet());
        Collections.sort(exitList, DocumentOrder.reverseDocumentOrderComparator);

        for (EnterableState es : exitList) {

            if (es instanceof TransitionalState && ((TransitionalState) es).hasHistory()) {
                TransitionalState ts = (TransitionalState) es;
                Set<EnterableState> shallow = null;
                Set<EnterableState> deep = null;
                for (History h : ts.getHistory()) {
                    if (h.isDeep()) {
                        if (deep == null) {
                            //calculate deep history for a given state once
                            deep = new HashSet<EnterableState>();
                            for (EnterableState ott : exctx.getScInstance().getCurrentStatus().getStates()) {
                                if (ott.isDescendantOf(es)) {
                                    deep.add(ott);
                                }
                            }
                        }
                        exctx.getScInstance().setLastConfiguration(h, deep);
                    } else {
                        if (shallow == null) {
                            //calculate shallow history for a given state once
                            if (configuration == null) {
                                configuration = exctx.getScInstance().getCurrentStatus().getAllStates();
                            }
                            shallow = new HashSet<EnterableState>(ts.getChildren());
                            shallow.retainAll(configuration);
                        }
                        exctx.getScInstance().setLastConfiguration(h, shallow);
                    }
                }
            }

            boolean onexitEventRaised = false;
            for (OnExit onexit : es.getOnExits()) {
                executeContent(exctx, onexit);
                if (!onexitEventRaised && onexit.isRaiseEvent()) {
                    onexitEventRaised = true;
                    exctx.getInternalIOProcessor().addEvent(new TriggerEvent("exit.state." + es.getId(), TriggerEvent.CHANGE_EVENT));
                }
            }
            exctx.getNotificationRegistry().fireOnExit(es, es);
            exctx.getNotificationRegistry().fireOnExit(exctx.getStateMachine(), es);

            if (es instanceof TransitionalState && !statesToInvoke.remove(es)) {
                // check if invokers are active in this state
                for (Invoke inv : ((TransitionalState) es).getInvokes()) {
                    exctx.cancelInvoker(inv);
                }
            }

        }
        exctx.getScInstance().getCurrentStatus().getStates().removeAll(exitList);
    }

    public void executeTransitionContent(final ModulaExecutionContext exctx, final Step step) throws ModelException {
        for (SimpleTransition transition : step.getTransitList()) {
            executeContent(exctx, transition);
        }
    }

    public void executeContent(ModulaExecutionContext exctx, Executable exec) throws ModelException {
        try {
            for (Action action : exec.getActions()) {
                action.execute(exctx.getActionExecutionContext());
            }
        } catch (ModelException e) {
            exctx.getInternalIOProcessor().addEvent(new TriggerEvent(TriggerEvent.ERROR_EXECUTION, TriggerEvent.ERROR_EVENT));
            exctx.getErrorReporter().onError(ErrorConstants.EXPRESSION_ERROR, e.getMessage(), exec);
        }
        if (exec instanceof Transition) {
            Transition t = (Transition) exec;
            if (t.getTargets().isEmpty()) {
                notifyOnTransition(exctx, t, t.getParent());
            } else {
                for (TransitionTarget tt : t.getTargets()) {
                    notifyOnTransition(exctx, t, tt);
                }
            }
        }
    }

    public void notifyOnTransition(final ModulaExecutionContext exctx, final Transition t,
                                   final TransitionTarget target) {
        EventVariable event = (EventVariable) exctx.getScInstance().getSystemContext().getVars().get(ModulaSystemContext.EVENT_KEY);
        String eventName = event != null ? event.getName() : null;
        exctx.getNotificationRegistry().fireOnTransition(t, t.getParent(), target, t, eventName);
        exctx.getNotificationRegistry().fireOnTransition(exctx.getStateMachine(), t.getParent(), target, t, eventName);
    }

    /**
     * This method corresponds to the Algorithm for SCXML processing enterStates() procedure, where the states to enter
     * already have been pre-computed in {@link #microStep(ModulaExecutionContext, Step, Set)}.
     *
     * @param exctx          The execution context for this micro step
     * @param step           the step
     * @param statesToInvoke the set of activated states which invokes need to be invoked at the end of the current
     *                       macro step
     * @throws ModelException if a SCXML model error occurred during the execution.
     */
    public void enterStates(final ModulaExecutionContext exctx, final Step step,
                            final Set<TransitionalState> statesToInvoke)
            throws ModelException {
        if (step.getEntrySet().isEmpty()) {
            return;
        }
        Set<EnterableState> configuration = null;
        ArrayList<EnterableState> entryList = new ArrayList<EnterableState>(step.getEntrySet());
        Collections.sort(entryList, DocumentOrder.documentOrderComparator);
        for (EnterableState es : entryList) {
            if (es.isAtomicState()) {
                // only track actomic active states in Status
                exctx.getScInstance().getCurrentStatus().getStates().add(es);
            }
            if (es instanceof TransitionalState && !((TransitionalState) es).getInvokes().isEmpty()) {
                statesToInvoke.add((TransitionalState) es);
            }

            boolean onentryEventRaised = false;
            for (OnEntry onentry : es.getOnEntries()) {
                executeContent(exctx, onentry);
                if (!onentryEventRaised && onentry.isRaiseEvent()) {
                    onentryEventRaised = true;
                    exctx.getInternalIOProcessor().addEvent(new TriggerEvent("entry.state." + es.getId(), TriggerEvent.CHANGE_EVENT));
                }
            }
            exctx.getNotificationRegistry().fireOnEntry(es, es);
            exctx.getNotificationRegistry().fireOnEntry(exctx.getStateMachine(), es);

            if (es instanceof State && step.getDefaultEntrySet().contains(es) && ((State) es).getInitial() != null) {
                executeContent(exctx, ((State) es).getInitial().getTransition());
            }
            if (es instanceof TransitionalState) {
                SimpleTransition hTransition = step.getDefaultHistoryTransitionEntryMap().get(es);
                if (hTransition != null) {
                    executeContent(exctx, hTransition);
                }
            }

            if (es instanceof Final) {
                State parent = (State) es.getParent();
                if (parent == null) {
                    exctx.stopRunning();
                }
            }
        }
    }

    /**
     * 暂时不支持
     */
    public void initiateInvokes(final ModulaExecutionContext exctx,
                                final Set<TransitionalState> statesToInvoke) {
        SCInstance scInstance = exctx.getScInstance();
        for (TransitionalState ts : statesToInvoke) {
            if (ts.getInvokes().isEmpty()) {
                continue;
            }
            Context context = scInstance.getContext(ts);
            for (Invoke i : ts.getInvokes()) {
                String src = i.getSrc();
                if (src == null) {
                    String srcexpr = i.getSrcexpr();
                    Object srcObj;

                    context.setLocal(Context.NAMESPACES_KEY, i.getNamespaces());
                    srcObj = srcexpr;
                    context.setLocal(Context.NAMESPACES_KEY, null);
                    src = String.valueOf(srcObj);
                }
                String source = src;
                PathResolver pr = i.getPathResolver();
                if (pr != null) {
                    source = i.getPathResolver().resolvePath(src);
                }
                Invoker inv;
                try {
                    inv = exctx.newInvoker(i.getType());
                } catch (InvokerException ie) {
                    exctx.getInternalIOProcessor().addEvent(new TriggerEvent("failed.invoke." + ts.getId(), TriggerEvent.ERROR_EVENT));
                    continue;
                }
                List<Param> params = i.params();
                Map<String, Object> args = new HashMap<String, Object>();
                for (Param p : params) {
                    String argExpr = p.getExpr();
                    Object argValue = null;
                    context.setLocal(Context.NAMESPACES_KEY, p.getNamespaces());
                    // Do we have an "expr" attribute?
                    if (argExpr != null && argExpr.trim().length() > 0) {
                        argValue = argExpr;
                    } else {
                        // No. Does value of "name" attribute refer to a valid
                        // location in the data model?

                        argValue = p.getName();
                        if (argValue == null) {
                            // Generate error, 4.3.1 in WD-scxml-20080516
                            exctx.getInternalIOProcessor().addEvent(new TriggerEvent(ts.getId() + ERR_ILLEGAL_ALLOC, TriggerEvent.ERROR_EVENT));
                        }
                    }
                    context.setLocal(Context.NAMESPACES_KEY, null);
                    args.put(p.getName(), argValue);
                }
                String invokeId = exctx.setInvoker(i, inv);
                inv.setInvokeId(invokeId);
                inv.setParentIOProcessor(exctx.getExternalIOProcessor());

                try {
                    inv.invoke(source, args);
                } catch (InvokerException ie) {
                    exctx.getInternalIOProcessor().addEvent(new TriggerEvent("failed.invoke." + ts.getId(), TriggerEvent.ERROR_EVENT));
                    exctx.removeInvoker(i);
                }
            }
        }
    }

    public void processInvokes(final ModulaExecutionContext exctx, final TriggerEvent event) throws ModelException {
        for (Map.Entry<Invoke, String> entry : exctx.getInvokeIds().entrySet()) {
            if (!isInvokerEvent(entry.getValue(), event)) {
                if (entry.getKey().isAutoForward()) {
                    Invoker inv = exctx.getInvoker(entry.getKey());
                    try {
                        inv.parentEvent(event);
                    } catch (InvokerException ie) {
                        exctx.getAppLog().error(ie.getMessage(), ie);
                        throw new ModelException(ie.getMessage(), ie.getCause());
                    }
                }
            }
            /*
            else {
                // TODO: applyFinalize
            }
            */
        }
    }
}

