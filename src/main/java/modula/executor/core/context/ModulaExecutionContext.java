package modula.executor.core.context;

import modula.executor.core.SCInstance;
import modula.executor.core.reporter.ErrorReporter;
import modula.executor.core.dispatcher.EventDispatcher;
import modula.executor.core.event.TriggerEvent;
import modula.executor.core.invoke.Invoker;
import modula.listener.NotificationRegistry;
import modula.parser.model.Invoke;
import modula.parser.model.ModelException;
import modula.executor.core.dispatcher.SimpleDispatcher;
import modula.executor.core.reporter.SimpleErrorReporter;
import modula.executor.core.invoke.InvokerException;
import modula.parser.model.Modula;
import modula.parser.ModulaIOProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import java.util.*;

/**
 * ModulaExecutionContext提供Modula状态机执行期间需要的服务和数据
 */
public class ModulaExecutionContext implements ModulaIOProcessor {
    private Log appLog = LogFactory.getLog(ModulaExecutionContext.class);

    /**
     * Action操作Context
     */
    private final ActionExecutionContext actionExecutionContext;

    /**
     * The SCInstance.
     */
    private SCInstance scInstance;

    /**
     * 外部IOProcessor，用于invoker回调
     */
    private ModulaIOProcessor externalIOProcessor;

    /**
     * eventDispatcher
     */
    private EventDispatcher eventdispatcher;

    /**
     * errorReporter
     */
    private ErrorReporter errorReporter = null;

    /**
     * notification registry.
     */
    private NotificationRegistry notificationRegistry;

    /**
     * 内部事件队列
     */
    private final Queue<TriggerEvent> internalEventQueue = new LinkedList<TriggerEvent>();

    /**
     * key：调用目标类型 (specified using "type" attribute).
     */
    private final Map<String, Class<? extends Invoker>> invokerClasses = new HashMap<String, Class<? extends Invoker>>();

    /**
     * invokeId列表
     */
    private final Map<Invoke, String> invokeIds = new HashMap<Invoke, String>();

    /**
     * key: invokeId
     */
    private final Map<String, Invoker> invokers = new HashMap<String, Invoker>();

    /**
     * 运行状态
     */
    private boolean running;

    /**
     * Constructor
     *
     * @param externalIOProcessor The external IO Processor
     * @param //evaluator         The evaluator
     * @param eventDispatcher     The event dispatcher, if null a SimpleDispatcher instance will be used
     * @param errorReporter       The error reporter, if null a SimpleErrorReporter instance will be used
     */
    public ModulaExecutionContext(ModulaIOProcessor externalIOProcessor, /*Evaluator evaluator,*/
                                  EventDispatcher eventDispatcher, ErrorReporter errorReporter) {
        this.externalIOProcessor = externalIOProcessor;
        //this.evaluator = evaluator;
        this.eventdispatcher = eventDispatcher != null ? eventDispatcher : new SimpleDispatcher();
        this.errorReporter = errorReporter != null ? errorReporter : new SimpleErrorReporter();
        this.notificationRegistry = new NotificationRegistry();

        this.scInstance = new SCInstance(this, /*this.evaluator,*/ this.errorReporter);
        this.actionExecutionContext = new ActionExecutionContext(this);
    }

    public ModulaIOProcessor getExternalIOProcessor() {
        return externalIOProcessor;
    }

    public ModulaIOProcessor getInternalIOProcessor() {
        return this;
    }

    /**
     * @return Returns the restricted execution context for actions
     */
    public ActionExecutionContext getActionExecutionContext() {
        return actionExecutionContext;
    }

    /**
     * @return Returns true if this state machine is running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Stop a running state machine
     */
    public void stopRunning() {
        this.running = false;
    }

    /**
     * 清空Invoker, 清空内部实践队列，重启状态机实例
     */
    public void initialize() throws ModelException {
        running = false;
        if (!invokeIds.isEmpty()) {
            for (Invoke invoke : new ArrayList<Invoke>(invokeIds.keySet())) {
                cancelInvoker(invoke);
            }
        }
        internalEventQueue.clear();
        scInstance.initialize();
        running = true;
    }

    /**
     * @return Returns the Modula Execution Logger for the application
     */
    public Log getAppLog() {
        return appLog;
    }

    /**
     * @return Returns the state machine
     */
    public Modula getStateMachine() {
        return scInstance.getStateMachine();
    }

    /**
     * 设置或替换状态机
     * 如果状态机初始化过，会再次初始化，并删除所有已存在状态
     *
     * @param stateMachine The state machine to set
     * @throws ModelException if attempting to set a null value or the state machine instance failed to re-initialize
     */
    public void setStateMachine(Modula stateMachine) throws ModelException {
        scInstance.setStateMachine(stateMachine);
    }

    /**
     * @return Returns the SCInstance
     */
    public SCInstance getScInstance() {
        return scInstance;
    }

    /**
     * @return Returns The evaluator.
     */
    //public Evaluator getEvaluator() {
    //    return evaluator;
    //}

    /**
     * Set or replace the evaluator
     * <p>
     * If the state machine instance has been initialized before, it will be initialized again, destroying all existing
     * state!
     * </p>
     *
     * @param evaluator The evaluator to set
     * @throws ModelException if attempting to set a null value or the state machine instance failed to re-initialize
     */
    //public void setEvaluator(Evaluator evaluator) throws ModelException {
    //    scInstance.setEvaluator(evaluator);
    //    this.evaluator = evaluator;
    //}

    /**
     * @return Returns the error reporter
     */
    public ErrorReporter getErrorReporter() {
        return errorReporter;
    }

    /**
     * Set or replace the error reporter
     *
     * @param errorReporter The error reporter to set, if null a SimpleErrorReporter instance will be used instead
     */
    public void setErrorReporter(ErrorReporter errorReporter) {
        this.errorReporter = errorReporter != null ? errorReporter : new SimpleErrorReporter();
        try {
            scInstance.setErrorReporter(errorReporter);
        } catch (ModelException me) {
            // won't happen
            return;
        }
    }

    /**
     * @return Returns the event dispatcher
     */
    public EventDispatcher getEventDispatcher() {
        return eventdispatcher;
    }

    /**
     * Set or replace the event dispatch
     *
     * @param eventdispatcher The event dispatcher to set, if null a SimpleDispatcher instance will be used instead
     */
    public void setEventdispatcher(EventDispatcher eventdispatcher) {
        this.eventdispatcher = eventdispatcher != null ? eventdispatcher : new SimpleDispatcher();
    }

    /**
     * @return Returns the notification registry
     */
    public NotificationRegistry getNotificationRegistry() {
        return notificationRegistry;
    }

    /**
     * Detach the current SCInstance to allow external serialization.
     * <p>
     * {@link #attachInstance(SCInstance)} can be used to re-attach a previously detached instance
     * </p>
     *
     * @return the detached instance
     */
    public SCInstance detachInstance() {
        SCInstance instance = scInstance;
        scInstance.detach();
        scInstance = null;
        return instance;
    }

    /**
     * Re-attach a previously detached SCInstance.
     * <p>
     * Note: an already attached instance will get overwritten (and thus lost).
     * </p>
     *
     * @param instance An previously detached SCInstance
     */
    public void attachInstance(SCInstance instance) {
        if (scInstance != null) {
            scInstance.detach();
        }
        scInstance = instance;
        if (scInstance != null) {
            scInstance.detach();
            try {
                //scInstance.setEvaluator(evaluator);
                scInstance.setErrorReporter(errorReporter);
            } catch (ModelException me) {
                // should not happen
                return;
            }
        }
    }

    /**
     * Register an Invoker for this target type.
     *
     * @param type         The target type (specified by "type" attribute of the invoke element).
     * @param invokerClass The Invoker class.
     */
    public void registerInvokerClass(final String type, final Class<? extends Invoker> invokerClass) {
        invokerClasses.put(type, invokerClass);
    }

    /**
     * Remove the Invoker registered for this target type (if there is one registered).
     *
     * @param type The target type (specified by "type" attribute of the invoke element).
     */
    public void unregisterInvokerClass(final String type) {
        invokerClasses.remove(type);
    }

    /**
     * Create a new {@link Invoker}
     *
     * @param type The type of the target being invoked.
     * @return An {@link Invoker} for the specified type, if an
     * invoker class is registered against that type,
     * <code>null</code> otherwise.
     * @throws InvokerException When a suitable {@link Invoker} cannot be instantiated.
     */
    public Invoker newInvoker(final String type) throws InvokerException {
        Class<? extends Invoker> invokerClass = invokerClasses.get(type);
        if (invokerClass == null) {
            throw new InvokerException("No Invoker registered for type \"" + type + "\"");
        }
        try {
            return invokerClass.newInstance();
        } catch (InstantiationException ie) {
            throw new InvokerException(ie.getMessage(), ie.getCause());
        } catch (IllegalAccessException iae) {
            throw new InvokerException(iae.getMessage(), iae.getCause());
        }
    }

    /**
     * Get the {@link Invoker} for this {@link Invoke}.
     * May return <code>null</code>. A non-null {@link Invoker} will be
     * returned if and only if the {@link Invoke} parent TransitionalState is
     * currently active and contains the &lt;invoke&gt; child.
     *
     * @param invoke The <code>Invoke</code>.
     * @return The Invoker.
     */
    public Invoker getInvoker(final Invoke invoke) {
        return invokers.get(invokeIds.get(invoke));
    }

    /**
     * Set the {@link Invoker} for a {@link Invoke} and returns the unique invokerId for the Invoker
     *
     * @param invoke  The Invoke.
     * @param invoker The Invoker.
     * @return The invokeId
     */
    public String setInvoker(final Invoke invoke, final Invoker invoker) {
        String invokeId = invoke.getId();
        if (invokeId == null) {
            invokeId = UUID.randomUUID().toString();
        }
        invokeIds.put(invoke, invokeId);
        invokers.put(invokeId, invoker);
        return invokeId;
    }

    /**
     * Remove a previously active Invoker, which must already have been canceled
     *
     * @param invoke The Invoke for the Invoker to remove
     */
    public void removeInvoker(final Invoke invoke) {
        invokers.remove(invokeIds.remove(invoke));
    }

    /**
     * @return Returns the map of current active Invokes and their invokeId
     */
    public Map<Invoke, String> getInvokeIds() {
        return invokeIds;
    }


    /**
     * Cancel and remove an active Invoker
     *
     * @param invoke The Invoke for the Invoker to cancel
     */
    public void cancelInvoker(Invoke invoke) {
        String invokeId = invokeIds.get(invoke);
        if (invokeId != null) {
            try {
                invokers.get(invokeId).cancel();
            } catch (InvokerException ie) {
                TriggerEvent te = new TriggerEvent("failed.invoke.cancel." + invokeId, TriggerEvent.ERROR_EVENT);
                addEvent(te);
            }
            removeInvoker(invoke);
        }
    }

    /**
     * Add an event to the internal event queue
     *
     * @param event The event
     */
    @Override
    public void addEvent(TriggerEvent event) {
        internalEventQueue.add(event);
    }

    /**
     * @return Returns the next event from the internal event queue, if available
     */
    public TriggerEvent nextInternalEvent() {
        return internalEventQueue.poll();
    }

    /**
     * @return Returns true if the internal event queue isn't empty
     */
    public boolean hasPendingInternalEvent() {
        return !internalEventQueue.isEmpty();
    }
}
