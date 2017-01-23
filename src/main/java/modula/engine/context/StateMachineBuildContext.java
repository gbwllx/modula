package modula.engine.context;

import modula.executor.statemachine.StateMachine;
import modula.parser.model.ModelException;
import modula.executor.statemachine.StateMachineBuilder;
import modula.listener.DefaultWorkflowListener;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

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

    public StateMachine createStateMachine() throws ModelException, XMLStreamException, IOException {
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

    public void setActions(Map<String,Class> actions) {
        builder.setActions(actions);
    }

    public void setListeners(List<DefaultWorkflowListener> listeners) {
        this.builder.setListeners(listeners);
    }
}
