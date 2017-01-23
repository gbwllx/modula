package modula.workflow.executor.statemachine;


import modula.core.ModulaExecutor;
import modula.core.model.ModelException;
import modula.core.model.Modula;
import modula.workflow.executor.context.WorkflowContext;

/**
 * @description:
 * @author: gubing.gb
 * @date: 2016/12/29.
 */
public class AbstractStateMachine {
    protected final Modula modula;
    protected final ExtendedModulaExecutor executor;
    protected volatile boolean initialized;

    public AbstractStateMachine(Modula modula) {
        this.modula = modula;
        this.executor = new ExtendedModulaExecutor();
        try {
            this.executor.setStateMachine(this.modula);
        } catch (ModelException e) {
            e.printStackTrace();
        }
    }

    protected class ExtendedModulaExecutor extends ModulaExecutor {
        public void initialize() {
            initialized = true;
        }
    }
}
