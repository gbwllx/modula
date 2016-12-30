package modula.workflow.executor.context;

import modula.workflow.executor.statemachine.StateMachine;
import modula.workflow.executor.statemachine.StateMachineBuilder;

/**
 * @description:
 * @author: gubing.gb
 * @date: 2016/12/30.
 */
public class StateMachineBuildContext {
    /**
     * 状态机配置文件id
     */
    private String id = null;

    private StateMachineBuilder builder = new StateMachineBuilder();

    public StateMachine createStateMachine() {
        return builder.build();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
