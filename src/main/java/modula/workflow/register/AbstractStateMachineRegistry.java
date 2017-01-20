package modula.workflow.register;

import modula.workflow.executor.context.StateMachineBuildContext;
import modula.workflow.executor.statemachine.StateMachine;

import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: gubing.gb
 * @date: 2017/1/18.
 */
public class AbstractStateMachineRegistry{
    protected final Map<String, StateMachineBuildContext> configs = new HashMap<String, StateMachineBuildContext>();

    public StateMachine get(String id) {
        StateMachineBuildContext createContext = configs.get(id);
        if (createContext == null) {
            return null;
        }
        try {
            return createContext.createStateMachine();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    public synchronized void register(StateMachineBuildContext createContext) {
        configs.put(createContext.getId(), createContext);
    }
}
