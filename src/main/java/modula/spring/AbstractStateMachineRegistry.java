package modula.spring;

import modula.executor.statemachine.StateMachine;
import modula.engine.context.StateMachineBuildContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: gubing.gb
 * @date: 2017/1/18.
 */
public class AbstractStateMachineRegistry {
    protected final Map<String, StateMachineBuildContext> configs = new HashMap<String, StateMachineBuildContext>();
    private final Map<String, StateMachine> stateMachineCache = new HashMap<>();

    public StateMachine get(String id) {
        StateMachineBuildContext createContext = configs.get(id);
        if (createContext == null) {
            return null;
        }
        try {
            StateMachine stateMachine = stateMachineCache.get(id);
            if (stateMachine == null) {
                stateMachine = createContext.createStateMachine();
            }
            stateMachineCache.put(id, stateMachine);
            return stateMachine;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    public synchronized void register(StateMachineBuildContext createContext) {
        configs.put(createContext.getId(), createContext);
    }
}
