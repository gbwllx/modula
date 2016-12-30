package modula.impl.statemachine;


import modula.core.model.Modula;

/**
 * @description:
 * @author: gubing.gb
 * @date: 2016/12/29.
 */
public class AbstractStateMachine implements StateMachine {
    protected final Modula modula;
    public AbstractStateMachine(Modula modula) {
        this.modula = modula;
    }
}
