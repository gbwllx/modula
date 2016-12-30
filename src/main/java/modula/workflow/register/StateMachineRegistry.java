package modula.workflow.register;

import modula.workflow.executor.context.StateMachineBuildContext;
import modula.workflow.executor.statemachine.StateMachine;

/**
 * @description:
 * @author: gubing.gb
 * @date: 2016/12/30.
 */
public interface StateMachineRegistry {

    StateMachine get(String id);

    void register(StateMachineBuildContext createContext);

}
