/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package modula.parser.io;

import modula.parser.model.*;
import org.apache.commons.logging.LogFactory;

import java.text.MessageFormat;
import java.util.*;

/**
 * ModelUpdater用于检查状态机的定义是否正确，处理modula model以方便executor执行
 */
final class ModelUpdater {

    //// Error messages
    /**
     * Error message when Modula document specifies an illegal initial state.
     */
    private static final String ERR_Modula_NO_INIT = "No Modula child state "
            + "with ID \"{0}\" found; illegal initial state for Modula document";

    /**
     * Error message when Modula document specifies an illegal initial state.
     */
    private static final String ERR_UNSUPPORTED_INIT = "Initial attribute or element not supported for "
            + "atomic {0}";

    /**
     * Error message when a state element specifies an initial state which
     * is not a direct descendent.
     */
    private static final String ERR_STATE_BAD_INIT = "Initial state "
            + "null or not a descendant of {0}";

    /**
     * Error message when a referenced history state cannot be found.
     */
    private static final String ERR_STATE_NO_HIST = "Referenced history state"
            + " null for {0}";

    /**
     * Error message when a shallow history state is not a child state.
     */
    private static final String ERR_STATE_BAD_SHALLOW_HIST = "History state"
            + " for shallow history is not child for {0}";

    /**
     * Error message when a deep history state is not a descendent state.
     */
    private static final String ERR_STATE_BAD_DEEP_HIST = "History state"
            + " for deep history is not descendant for {0}";

    /**
     * Transition target is not a legal IDREF (not found).
     */
    private static final String ERR_TARGET_NOT_FOUND =
            "Transition target with ID \"{0}\" not found";

    /**
     * Transition targets do not form a legal configuration.
     */
    private static final String ERR_ILLEGAL_TARGETS =
            "Transition targets \"{0}\" do not satisfy the requirements for"
                    + " target regions belonging to a <parallel>";

    /**
     * Simple states should not contain a history.
     */
    private static final String ERR_HISTORY_SIMPLE_STATE =
            "Simple {0} contains history elements";

    /**
     * History does not specify a default transition target.
     */
    private static final String ERR_HISTORY_NO_DEFAULT =
            "No default target specified for history with ID \"{0}\""
                    + " belonging to {1}";

    /**
     * Error message when an &lt;invoke&gt; does not specify a "type"
     * attribute.
     */
    private static final String ERR_INVOKE_NO_TYPE = "{0} contains "
            + "<invoke> with no \"type\" attribute specified.";

    /**
     * Error message when an &lt;invoke&gt; does not specify a "src"
     * or a "srcexpr" attribute.
     */
    private static final String ERR_INVOKE_NO_SRC = "{0} contains "
            + "<invoke> without a \"src\" or \"srcexpr\" attribute specified.";

    /**
     * Error message when an &lt;invoke&gt; specifies both "src" and "srcexpr"
     * attributes.
     */
    private static final String ERR_INVOKE_AMBIGUOUS_SRC = "{0} contains "
            + "<invoke> with both \"src\" and \"srcexpr\" attributes specified,"
            + " must specify either one, but not both.";

    /**
     * Discourage instantiation since this is a utility class.
     */
    private ModelUpdater() {
        super();
    }

    /*
     * Post-processing methods to make the Modula object ModulaExecutor ready.
     */

    /**
     * 更新Modula对象
     */
    static void updateModula(final Modula modula) throws ModelException {
        initDocumentOrder(modula.getChildren(), 1);

        String initial = modula.getInitial();
        SimpleTransition initialTransition = new SimpleTransition();

        if (initial != null) {

            initialTransition.setNext(modula.getInitial());
            updateTransition(initialTransition, modula.getTargets());

            if (initialTransition.getTargets().size() == 0) {
                logAndThrowModelError(ERR_Modula_NO_INIT, new Object[]{
                        initial});
            }
        } else {
            // If 'initial' is not specified, the default initial state is
            // the first child state in document order.
            initialTransition.getTargets().add(modula.getFirstChild());
        }

        modula.setInitialTransition(initialTransition);
        Map<String, TransitionTarget> targets = modula.getTargets();
        for (EnterableState es : modula.getChildren()) {
            if (es instanceof State) {
                updateState((State) es, targets);
            }
        }

        modula.getInitialTransition().setObservableId(1);
        initObservables(modula.getChildren(), 2);
    }

    /**
     * 初始化文档排序
     */
    private static int initDocumentOrder(final List<EnterableState> states, int nextOrder) {
        for (EnterableState state : states) {
            state.setOrder(nextOrder++);
            if (state instanceof TransitionalState) {
                TransitionalState ts = (TransitionalState) state;
                for (Transition t : ts.getTransitionsList()) {
                    t.setOrder(nextOrder++);
                }
                nextOrder = initDocumentOrder(ts.getChildren(), nextOrder);
            }
        }
        return nextOrder;
    }

    /**
     * 初始化观察者
     */
    private static int initObservables(final List<EnterableState> states, int nextObservableId) {
        for (EnterableState es : states) {
            es.setObservableId(nextObservableId++);
            if (es instanceof TransitionalState) {
                TransitionalState ts = (TransitionalState) es;
                if (ts instanceof State) {
                    State s = (State) ts;
                    if (s.getInitial() != null && s.getInitial().getTransition() != null) {
                        s.getInitial().getTransition().setObservableId(nextObservableId++);
                    }
                }
                for (Transition t : ts.getTransitionsList()) {
                    t.setObservableId(nextObservableId++);
                }
                for (History h : ts.getHistory()) {
                    h.setObservableId(nextObservableId++);
                    if (h.getTransition() != null) {
                        h.getTransition().setObservableId(nextObservableId++);
                    }
                }
                nextObservableId = initObservables(ts.getChildren(), nextObservableId);
            }
        }
        return nextObservableId;
    }

    /**
     * 更新state
     */
    private static void updateState(final State state, final Map<String, TransitionTarget> targets)
            throws ModelException {
        List<EnterableState> children = state.getChildren();
        if (state.isComposite()) {
            //initialize next / initial
            Initial ini = state.getInitial();
            if (ini == null) {
                state.setFirst(children.get(0).getId());
                ini = state.getInitial();
            }
            SimpleTransition initialTransition = ini.getTransition();
            updateTransition(initialTransition, targets);
            Set<TransitionTarget> initialStates = initialTransition.getTargets();
            // we have to allow for an indirect descendant initial (targets)
            //check that initialState is a descendant of s
            if (initialStates.size() == 0) {
                logAndThrowModelError(ERR_STATE_BAD_INIT,
                        new Object[]{getName(state)});
            } else {
                for (TransitionTarget initialState : initialStates) {
                    if (!initialState.isDescendantOf(state)) {
                        logAndThrowModelError(ERR_STATE_BAD_INIT,
                                new Object[]{getName(state)});
                    }
                }
            }
        } else if (state.getInitial() != null) {
            logAndThrowModelError(ERR_UNSUPPORTED_INIT, new Object[]{getName(state)});
        }

        List<History> histories = state.getHistory();
        if (histories.size() > 0 && state.isSimple()) {
            logAndThrowModelError(ERR_HISTORY_SIMPLE_STATE,
                    new Object[]{getName(state)});
        }
        for (History history : histories) {
            updateHistory(history, targets, state);
        }
        for (Transition transition : state.getTransitionsList()) {
            updateTransition(transition, targets);
        }

        for (Invoke inv : state.getInvokes()) {
            if (inv.getType() == null) {
                logAndThrowModelError(ERR_INVOKE_NO_TYPE, new Object[]{getName(state)});
            }
            if (inv.getSrc() == null && inv.getSrcexpr() == null) {
                logAndThrowModelError(ERR_INVOKE_NO_SRC, new Object[]{getName(state)});
            }
            if (inv.getSrc() != null && inv.getSrcexpr() != null) {
                logAndThrowModelError(ERR_INVOKE_AMBIGUOUS_SRC, new Object[]{getName(state)});
            }
        }

        for (EnterableState es : children) {
            if (es instanceof State) {
                updateState((State) es, targets);
            }
        }
    }

    /**
     * 更新history节点
     */
    private static void updateHistory(final History history,
                                      final Map<String, TransitionTarget> targets,
                                      final TransitionalState parent)
            throws ModelException {
        SimpleTransition transition = history.getTransition();
        if (transition == null || transition.getNext() == null) {
            logAndThrowModelError(ERR_HISTORY_NO_DEFAULT,
                    new Object[]{history.getId(), getName(parent)});
        } else {
            updateTransition(transition, targets);
            Set<TransitionTarget> historyStates = transition.getTargets();
            if (historyStates.size() == 0) {
                logAndThrowModelError(ERR_STATE_NO_HIST,
                        new Object[]{getName(parent)});
            }
            for (TransitionTarget historyState : historyStates) {
                if (!history.isDeep()) {
                    // Shallow history
                    if (!parent.getChildren().contains(historyState)) {
                        logAndThrowModelError(ERR_STATE_BAD_SHALLOW_HIST,
                                new Object[]{getName(parent)});
                    }
                } else {
                    // Deep history
                    if (!historyState.isDescendantOf(parent)) {
                        logAndThrowModelError(ERR_STATE_BAD_DEEP_HIST,
                                new Object[]{getName(parent)});
                    }
                }
            }
        }
    }

    /**
     * 更新transition目标
     */
    private static void updateTransition(final SimpleTransition transition,
                                         final Map<String, TransitionTarget> targets) throws ModelException {
        String next = transition.getNext();
        if (next == null) { // stay transition
            return;
        }
        Set<TransitionTarget> tts = transition.getTargets();
        if (tts.isEmpty()) {
            // 'next' is a space separated list of transition target IDs
            StringTokenizer ids = new StringTokenizer(next);
            while (ids.hasMoreTokens()) {
                String id = ids.nextToken();
                TransitionTarget tt = targets.get(id);
                if (tt == null) {
                    logAndThrowModelError(ERR_TARGET_NOT_FOUND, new Object[]{
                            id});
                }
                tts.add(tt);
            }
            if (tts.size() > 1) {
                boolean legal = verifyTransitionTargets(tts);
                if (!legal) {
                    logAndThrowModelError(ERR_ILLEGAL_TARGETS, new Object[]{
                            next});
                }
            }
        }
    }

    /**
     * 打日志
     */
    private static void logAndThrowModelError(final String errType,
                                              final Object[] msgArgs) throws ModelException {
        MessageFormat msgFormat = new MessageFormat(errType);
        String errMsg = msgFormat.format(msgArgs);
        org.apache.commons.logging.Log log = LogFactory.
                getLog(ModelUpdater.class);
        log.error(errMsg);
        throw new ModelException(errMsg);
    }

    private static String getName(final TransitionTarget tt) {
        String name = "anonymous transition target";
        if (tt instanceof State) {
            name = "anonymous state";
            if (tt.getId() != null) {
                name = "state with ID \"" + tt.getId() + "\"";
            }
        } else {
            if (tt.getId() != null) {
                name = "transition target with ID \"" + tt.getId() + "\"";
            }
        }
        return name;
    }

    /**
     * 一个transition有多个目标target，需要遵守以下原则：
     * 没有一个target是其他target的祖先
     */
    private static boolean verifyTransitionTargets(final Set<TransitionTarget> tts) {
        if (tts.size() <= 1) { // No contention
            return true;
        }

        Set<EnterableState> parents = new HashSet<EnterableState>();
        for (TransitionTarget tt : tts) {
            boolean hasParallelParent = false;
            for (int i = tt.getNumberOfAncestors() - 1; i > -1; i--) {
                EnterableState parent = tt.getAncestor(i);

                if (!parents.add(parent)) {
                    // this TransitionTarget is an descendant of another, or shares the same Parallel region
                    return false;

                }
            }
        }
        return true;
    }
}