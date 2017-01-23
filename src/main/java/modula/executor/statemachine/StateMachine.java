package modula.executor.statemachine;

import modula.executor.context.WorkflowContext;

/**
 * @description:
 * @author: gubing.gb
 * @date: 2016/12/29.
 */
public interface StateMachine {
    /**
     * 使用 WorkflowContext 来刷新状态机
     *
     * @param context
     * @return
     */
    StateMachine refresh(WorkflowContext context) throws Exception;

    /**
     * 开始启动状态机
     *
     * @return
     * @throws Exception
     */
    StateMachine start() throws Exception;

    /**
     * 开始启动状态机
     *
     * @return
     * @throws Exception
     */
    StateMachine start(WorkflowContext context) throws Exception;
}
