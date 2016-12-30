package modula.workflow.engine;

import modula.workflow.executor.context.WorkflowContext;
import modula.workflow.executor.statemachine.StateMachine;
import modula.workflow.register.StateMachineRegistry;

/**
 * @description:
 * @author: gubing.gb
 * @date: 2016/12/30.
 */
public class DefaultWorkflowEngine implements WorkflowEngine {
    private StateMachineRegistry registry;

    public <T> T execute(WorkflowContext context) {
        StateMachine stateMachine = registry.get(context.getKey());
        return null;
    }
}
