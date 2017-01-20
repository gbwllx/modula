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

import modula.core.invoke.Invoker;
import modula.core.model.EnterableState;
import modula.core.model.ModelException;
import modula.core.model.Modula;
import modula.core.model.Observable;
import modula.core.semantics.ModulaSemanticsImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 执行器
 */
public class ModulaExecutor implements ModulaIOProcessor {
    private static final String ERR_NO_STATE_MACHINE = "SCXMLExecutor: State machine not set";

    private Log log = LogFactory.getLog(ModulaExecutor.class);

    /**
     * Modula语义
     */
    private ModulaSemantics semantics;

    /**
     * 状态机上下文
     */
    private ModulaExecutionContext exctx;

    /**
     * 外部事件队列
     */
    private final Queue<TriggerEvent> externalEventQueue = new ConcurrentLinkedQueue<TriggerEvent>();


    public ModulaExecutor() {
        this(null, null, null);
    }

    public ModulaExecutor(
            final EventDispatcher evtDisp, final ErrorReporter errRep) {
        this(evtDisp, errRep, null);
    }

    public ModulaExecutor(final EventDispatcher evtDisp, final ErrorReporter errRep,
                          final ModulaSemantics semantics) {
        this.semantics = semantics != null ? semantics : new ModulaSemanticsImpl();
        this.exctx = new ModulaExecutionContext(this, evtDisp, errRep);
    }

    /**
     * 获取当前状态机状态
     */
    public synchronized Status getCurrentStatus() {
        return exctx.getScInstance().getCurrentStatus();
    }


    /**
     * Get the root context for the state machine execution.
     *
     * @return Context The root context.
     */
    public Context getRootContext() {
        return exctx.getScInstance().getRootContext();
    }

    /**
     * Set the root context for the state machine execution.
     * <b>NOTE:</b> Should only be used before the executor is set in motion.
     */
    public void setRootContext(final Context rootContext) {
        exctx.getScInstance().setRootContext(rootContext);
    }

    /**
     * 获取当前状态机
     *
     * @return Returns the stateMachine.
     */
    public Modula getStateMachine() {
        return exctx.getStateMachine();
    }

    /**
     * 重置状态机，会删除之前运行的状态机
     */
    public void setStateMachine(final Modula stateMachine) throws ModelException {
        exctx.setStateMachine(semantics.normalizeStateMachine(stateMachine, exctx.getErrorReporter()));
        externalEventQueue.clear();
    }

    public ErrorReporter getErrorReporter() {
        return exctx.getErrorReporter();
    }

    public void setErrorReporter(final ErrorReporter errorReporter) {
        exctx.setErrorReporter(errorReporter);
    }

    public EventDispatcher getEventdispatcher() {
        return exctx.getEventDispatcher();
    }

    public void setEventdispatcher(final EventDispatcher eventdispatcher) {
        exctx.setEventdispatcher(eventdispatcher);
    }

    public NotificationRegistry getNotificationRegistry() {
        return exctx.getNotificationRegistry();
    }

    public void addListener(final Observable observable, final ModulaListener listener) {
        exctx.getNotificationRegistry().addListener(observable, listener);
    }

    public void removeListener(final Observable observable,
                               final ModulaListener listener) {
        exctx.getNotificationRegistry().removeListener(observable, listener);
    }

    /**
     * Invoker type属性，现在不用
     */
    public void registerInvokerClass(final String type, final Class<? extends Invoker> invokerClass) {
        exctx.registerInvokerClass(type, invokerClass);
    }

    public void unregisterInvokerClass(final String type) {
        exctx.unregisterInvokerClass(type);
    }

    /**
     * 为了序列化，分离当前实例，分离期间不响应任何事件，可通过attach恢复
     */
    public SCInstance detachInstance() {
        return exctx.detachInstance();
    }

    /**
     * 恢复实例，如果当前有运行实例，会丢失
     */
    public void attachInstance(SCInstance instance) {
        exctx.attachInstance(instance);
    }

    public boolean isRunning() {
        return exctx.isRunning();
    }

    /**
     * 运行函数
     */
    public void go() throws ModelException {
        // same as reset
        this.reset();
    }

    /**
     * 清除所有状态，运行状态机，firstStep
     */
    public void reset() throws ModelException {
        // clear any pending external events
        externalEventQueue.clear();

        // go
        semantics.firstStep(exctx);

        if (!exctx.isRunning()) {
            semantics.finalStep(exctx);
        }

        logState();
    }

    /**
     * 添加事件
     */
    public void addEvent(final TriggerEvent evt) {
        if (evt != null) {
            externalEventQueue.add(evt);
        }
    }

    /**
     * 外部事件队列是否为空
     */
    public boolean hasPendingEvents() {
        return !externalEventQueue.isEmpty();
    }

    /**
     * 外部事件队列大小
     */
    public int getPendingEvents() {
        return externalEventQueue.size();
    }

    /**
     * 触发事件
     */
    public void triggerEvent(final TriggerEvent evt)
            throws ModelException {
        if (evt != null) {
            externalEventQueue.add(evt);
        }
        triggerEvents();
    }

    /**
     * 触发事件
     */
    public void triggerEvents(final TriggerEvent[] evts)
            throws ModelException {
        if (evts != null) {
            for (TriggerEvent evt : evts) {
                if (evt != null) {
                    externalEventQueue.add(evt);
                }
            }
        }
        triggerEvents();
    }

    /**
     * 触发事件，执行nextStep
     */
    public void triggerEvents() throws ModelException {
        TriggerEvent evt;
        while (exctx.isRunning() && (evt = externalEventQueue.poll()) != null) {
            eventStep(evt);
        }
    }

    protected void eventStep(TriggerEvent event) throws ModelException {
        semantics.nextStep(exctx, event);

        if (!exctx.isRunning()) {
            semantics.finalStep(exctx);
        }

        logState();
    }

    /**
     * 获取SCInstance
     */
    protected SCInstance getSCInstance() {
        return exctx.getScInstance();
    }

    /**
     * 打印当前State
     */
    protected void logState() {
        if (log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("Current States: [ ");
            for (EnterableState es : getCurrentStatus().getStates()) {
                sb.append(es.getId()).append(", ");
            }
            int length = sb.length();
            sb.delete(length - 2, length).append(" ]");
            log.debug(sb.toString());
        }
    }
}

