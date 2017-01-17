package modula.workflow.executor.context;

import modula.workflow.executor.statemachine.StateMachine;
import modula.workflow.executor.statemachine.StateMachineBuilder;

import java.net.URL;

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
    /**
     * 线程加载器
     */
    private ClassLoader         classLoader = Thread.currentThread().getContextClassLoader();
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

    public void setModulaURL(URL modulaURL) {
        builder.url(modulaURL);
    }
}
