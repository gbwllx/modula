package modula.workflow.engine;

import modula.workflow.executor.context.WorkflowContext;
import modula.workflow.executor.statemachine.StateMachine;
import modula.workflow.register.DefaultStateMachineRegistry;

import java.util.Map;

/**
 * @description:
 * @author: gubing.gb
 * @date: 2016/12/30.
 */
public class DefaultWorkflowEngine implements WorkflowEngine {
    private DefaultStateMachineRegistry registry;
    private Map<String, Object> globals;

    public <T> T execute(WorkflowContext context) {
        StateMachine stateMachine = registry.get(context.getKey());
        if (stateMachine == null) {
            throw new RuntimeException("create stateMachine error, stateMachine=" + stateMachine);
        }
        try {
            stateMachine.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setRegistry(DefaultStateMachineRegistry registry) {
        this.registry = registry;
    }

    public void setGlobals(Map<String, Object> globals) {
        this.globals = globals;
    }

}
