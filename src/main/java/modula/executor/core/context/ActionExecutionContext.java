package modula.executor.core.context;

import modula.executor.core.dispatcher.EventDispatcher;
import modula.executor.core.reporter.ErrorReporter;
import modula.parser.ModulaIOProcessor;
import modula.parser.model.Action;
import modula.parser.model.EnterableState;
import modula.parser.model.Modula;
import org.apache.commons.logging.Log;

/**
 * ActionExecutionContext  提供受限操作Modula模型,实例和服务的类
 * {@link Action}执行期间需要
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