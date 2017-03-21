package modula.executor.statemachine;


import modula.executor.core.ModulaExecutor;
import modula.parser.model.ModelException;
import modula.parser.model.Modula;

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
