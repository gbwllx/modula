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
package modula.core;

import modula.core.model.*;
import org.w3c.dom.Node;

import java.io.Serializable;
import java.util.*;

/**
 * SCInstance 记账功能
 */
public class SCInstance implements Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 2L;

    /**
     * 没有设置状态机错误
     */
    private static final String ERR_NO_STATE_MACHINE = "SCInstance: State machine not set";

    /**
     * 没有设置Evaluator错误
     */
    private static final String ERR_NO_EVALUATOR = "SCInstance: Evaluator not set";

    /**
     * 没有设置errorReporter错误
     */
    private static final String ERR_NO_ERROR_REPORTER = "SCInstance: ErrorReporter not set";

    /**
     * 状态机是否已经初始化
     */
    private boolean initialized;

    /**
     * 状态机
     */
    private Modula stateMachine;

    /**
     * 当前状态
     */
    private final Status currentStatus;

    /**
     * The SCXML I/O Processor for the internal event queue
     */
    private transient ModulaIOProcessor internalIOProcessor;

    /**
     * The Evaluator used for this state machine instance.
     */
    //private transient Evaluator evaluator;

    /**
     * The error reporter.
     */
    private transient ErrorReporter errorReporter = null;

    /**
     * EnterableState context map
     */
    private final Map<EnterableState, Context> contexts = new HashMap<EnterableState, Context>();

    /**
     * The map of last known configurations per History.
     */
    private final Map<History, Set<EnterableState>> histories = new HashMap<History, Set<EnterableState>>();

    /**
     * 根Context
     */
    private Context rootContext;

    /**
     * 系统context
     */
    private ModulaSystemContext systemContext;

    /**
     * 全局context
     */
    private Context globalContext;

    protected SCInstance(final ModulaIOProcessor internalIOProcessor, /*final Evaluator evaluator,*/
                         final ErrorReporter errorReporter) {
        this.internalIOProcessor = internalIOProcessor;
        //this.evaluator = evaluator;
        this.errorReporter = errorReporter;
        this.currentStatus = new Status();
    }

    /**
     * 重新初始化状态机
     */
    protected void initialize() throws ModelException {
        if (stateMachine == null) {
            throw new ModelException(ERR_NO_STATE_MACHINE);
        }
        //if (evaluator == null) {
        //    throw new ModelException(ERR_NO_EVALUATOR);
        //}
        if (errorReporter == null) {
            throw new ModelException(ERR_NO_ERROR_REPORTER);
        }
        systemContext = null;
        globalContext = null;
        contexts.clear();
        histories.clear();
        currentStatus.clear();

        // Clone root datamodel
        Datamodel rootdm = stateMachine.getDatamodel();
        cloneDatamodel(rootdm, getRootContext(), /*evaluator,*/ errorReporter);
        initialized = true;
    }

    /**
     * 分离状态机，使能够序列化，分离errorReporte，eveluator等
     */
    protected void detach() {
        //this.evaluator = null;
        this.errorReporter = null;
    }

    /**
     * Sets the I/O Processor for the internal event queue
     *
     * @param internalIOProcessor
     */
    protected void setInternalIOProcessor(ModulaIOProcessor internalIOProcessor) {
        this.internalIOProcessor = internalIOProcessor;
    }

    /**
     * Set or re-attach the evaluator
     * <p>
     * If this state machine instance has been initialized before, it will be initialized again, destroying all existing
     * state!
     * </p>
     *
     * @param evaluator The evaluator for this state machine instance.
     * @throws ModelException if an attempt is made to set a null value for the evaluator
     */
//    protected void setEvaluator(Evaluator evaluator) throws ModelException {
//        if (evaluator == null) {
//            throw new ModelException(ERR_NO_EVALUATOR);
//        }
//        if (this.evaluator != null && initialized) {
//            this.evaluator = evaluator;
//            // change of evaluator after initialization: re-initialize
//            initialize();
//        } else {
//            this.evaluator = evaluator;
//        }
//    }

    /**
     * Set or re-attach the error reporter
     *
     * @param errorReporter The error reporter for this state machine instance.
     * @throws ModelException if an attempt is made to set a null value for the error reporter
     */
    protected void setErrorReporter(ErrorReporter errorReporter) throws ModelException {
        if (errorReporter == null) {
            throw new ModelException(ERR_NO_ERROR_REPORTER);
        }
        this.errorReporter = errorReporter;
    }

    /**
     * @return Return the state machine for this instance
     */
    public Modula getStateMachine() {
        return stateMachine;
    }

    /**
     * Sets the state machine for this instance.
     * <p>
     * If this state machine instance has been initialized before, it will be initialized again, destroying all existing
     * state!
     * </p>
     *
     * @param stateMachine The state machine for this instance
     * @throws ModelException if an attempt is made to set a null value for the state machine
     */
    protected void setStateMachine(Modula stateMachine) throws ModelException {
        if (stateMachine == null) {
            throw new ModelException(ERR_NO_STATE_MACHINE);
        }
        if (this.stateMachine != null && initialized) {
            this.stateMachine = stateMachine;
            // change of state machine after initialization: re-initialize
            initialize();
        } else {
            this.stateMachine = stateMachine;
        }
    }

    /**
     * Clone data model.
     *
     * @param ctx           The context to clone to.
     * @param datamodel     The datamodel to clone.
     * @param //evaluator   The expression evaluator.
     * @param errorReporter The error reporter
     */
    protected void cloneDatamodel(final Datamodel datamodel, final Context ctx, /*final Evaluator evaluator,*/
                                  final ErrorReporter errorReporter) {
        if (datamodel == null) {
            return;
        }
        List<Data> data = datamodel.getData();
        if (data == null) {
            return;
        }
        for (Data datum : data) {
            Node datumNode = datum.getNode();
            Node valueNode = null;
            if (datumNode != null) {
                valueNode = datumNode.cloneNode(true);
            }
            // prefer "src" over "expr" over "inline"
            if (datum.getSrc() != null) {
                ctx.setLocal(datum.getId(), valueNode);
            } else if (datum.getExpr() != null) {
                //TODO
//                Object value = null;
//                try {
//                    ctx.setLocal(Context.NAMESPACES_KEY, datum.getNamespaces());
//                    value = evaluator.eval(ctx, datum.getExpr());
//                    ctx.setLocal(Context.NAMESPACES_KEY, null);
//                } catch (SCXMLExpressionException see) {
//                    if (internalIOProcessor != null) {
//                        internalIOProcessor.addEvent(new TriggerEvent(TriggerEvent.ERROR_EXECUTION, TriggerEvent.ERROR_EVENT));
//                    }
//                    errorReporter.onError(ErrorConstants.EXPRESSION_ERROR, see.getMessage(), datum);
//                }
//                ctx.setLocal(datum.getId(), value);
            } else {
                ctx.setLocal(datum.getId(), valueNode);
            }
        }
    }

    /**
     * @return Returns the current status (active atomic states) for this instance
     */
    public Status getCurrentStatus() {
        return currentStatus;
    }

    /**
     * Get the root context.
     *
     * @return The root context.
     */
    public Context getRootContext() {
//        if (rootContext == null && evaluator != null) {
//            rootContext = evaluator.newContext(null);
//        }
        return rootContext;
    }

    /**
     * Set or replace the root context.
     *
     * @param context The new root context.
     */
    protected void setRootContext(final Context context) {
        this.rootContext = context;
        // force initialization of rootContext
        getRootContext();
//        if (systemContext != null) {
//            // re-parent the system context
//            systemContext.setSystemContext(evaluator.newContext(rootContext));
//        }
    }

    /**
     * Get the unwrapped (modifiable) system context.
     *
     * @return The unwrapped system context.
     */
    public Context getSystemContext() {
        if (systemContext == null) {
            // force initialization of rootContext
            getRootContext();
            if (rootContext != null) {
                // = new ModulaSystemContext(evaluator.newContext(rootContext));
                systemContext.getContext().set(ModulaSystemContext.SESSIONID_KEY, UUID.randomUUID().toString());
                //String _name = stateMachine != null && stateMachine.getName() != null ? stateMachine.getName() : "";
                //systemContext.getContext().set(ModulaSystemContext.SCXML_NAME_KEY, _name);
            }
        }
        return systemContext != null ? systemContext.getContext() : null;
    }

    /**
     * @return Returns the global context, which is the top context <em>within</em> the state machine.
     */
    public Context getGlobalContext() {
        if (globalContext == null) {
            // force initialization of systemContext
            getSystemContext();
            if (systemContext != null) {
                //globalContext = evaluator.newContext(systemContext);
            }
        }
        return globalContext;
    }

    /**
     * Get the context for an EnterableState or create one if not created before.
     *
     * @param state The EnterableState.
     * @return The context.
     */
    public Context getContext(final EnterableState state) {
        Context context = contexts.get(state);
        if (context == null) {
            EnterableState parent = state.getParent();
            if (parent == null) {
                // docroot
                //TODO
                //context = evaluator.newContext(getGlobalContext());
            } else {
                //context = evaluator.newContext(getContext(parent));
            }
            if (state instanceof TransitionalState) {
                Datamodel datamodel = ((TransitionalState) state).getDatamodel();
                //cloneDatamodel(datamodel, context, evaluator, errorReporter);
            }
            contexts.put(state, context);
        }
        return context;
    }

    /**
     * Get the context for an EnterableState if available.
     * <p>
     * <p>Note: used for testing purposes only</p>
     *
     * @param state The EnterableState
     * @return The context or null if not created yet.
     */
    Context lookupContext(final EnterableState state) {
        return contexts.get(state);
    }

    /**
     * Set the context for an EnterableState
     * <p>
     * <p>Note: used for testing purposes only</p>
     *
     * @param state   The EnterableState.
     * @param context The context.
     */
    void setContext(final EnterableState state,
                    final Context context) {
        contexts.put(state, context);
    }

    /**
     * Get the last configuration for this history.
     *
     * @param history The history.
     * @return Returns the lastConfiguration.
     */
    public Set<EnterableState> getLastConfiguration(final History history) {
        Set<EnterableState> lastConfiguration = histories.get(history);
        if (lastConfiguration == null) {
            lastConfiguration = new HashSet<EnterableState>();
            histories.put(history, lastConfiguration);
        }
        return lastConfiguration;
    }

    /**
     * Set the last configuration for this history.
     *
     * @param history The history.
     * @param lc      The lastConfiguration to set.
     */
    public void setLastConfiguration(final History history,
                                     final Set<EnterableState> lc) {
        Set<EnterableState> lastConfiguration = getLastConfiguration(history);
        lastConfiguration.clear();
        lastConfiguration.addAll(lc);
    }

    /**
     * Check whether we have prior history.
     *
     * @param history The history.
     * @return Whether we have a non-empty last configuration
     */
    public boolean isEmpty(final History history) {
        Set<EnterableState> lastConfiguration = histories.get(history);
        return lastConfiguration == null || lastConfiguration.isEmpty();
    }

    /**
     * Resets the history state.
     * <p>
     * <p>Note: used for testing purposes only</p>
     *
     * @param history The history.
     */
    public void reset(final History history) {
        Set<EnterableState> lastConfiguration = histories.get(history);
        if (lastConfiguration != null) {
            lastConfiguration.clear();
        }
    }
}

