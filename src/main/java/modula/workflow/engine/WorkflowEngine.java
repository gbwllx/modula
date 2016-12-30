package modula.workflow.engine;

import modula.workflow.executor.context.WorkflowContext;

/**
 * @description: modula引擎
 * @author: gubing.gb
 * @date: 2016/12/30.
 */
public interface WorkflowEngine {
    /**
     * 执行流程入口
     *
     * @param context 请求上下文
     * @param <T> 有返回结果， 则返回结果，否则返回null
     * @return
     */
    <T> T execute(WorkflowContext context);


}
