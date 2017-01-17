package modula.core;

import modula.core.model.EnterableState;
import modula.core.model.Modula;
import org.apache.commons.logging.Log;

/**
 * ActionExecutionContext  提供受限操作Modula模型,实例和服务的类
 * {@link modula.core.model.Action}执行期间需要
 */
public class ActionExecutionContext {

    /**
     * action所属的ModulaExecutionContext
     */
    private final ModulaExecutionContext exctx;

    public ActionExecutionContext(ModulaExecutionContext exctx) {
        this.exctx = exctx;
    }

    /**
     * @return Returns the state machine
     */
    public Modula getStateMachine() {
        return exctx.getStateMachine();
    }

    /**
     * @return Returns the global context
     */
    public Context getGlobalContext() {
        return exctx.getScInstance().getGlobalContext();
    }

    /**
     * @return Returns the context for an EnterableState
     */
    public Context getContext(EnterableState state) {
        return exctx.getScInstance().getContext(state);
    }

    /** TODO
     * @return Returns The evaluator.
     */
    //public Evaluator getEvaluator() {
    //    return exctx.getEvaluator();
    //}

    /**
     * @return Returns the error reporter
     */
    public ErrorReporter getErrorReporter() {
        return exctx.getErrorReporter();
    }

    /**
     * @return Returns the event dispatcher
     */
    public EventDispatcher getEventDispatcher() {
        return exctx.getEventDispatcher();
    }

    /**
     * @return Returns the I/O Processor for the internal event queue
     */
    public ModulaIOProcessor getInternalIOProcessor() {
        return exctx;
    }

    /**
     * @return Returns the SCXML Execution Logger for the application
     */
    public Log getAppLog() {
        return exctx.getAppLog();
    }
}