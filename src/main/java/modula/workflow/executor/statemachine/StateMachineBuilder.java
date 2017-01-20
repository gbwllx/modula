package modula.workflow.executor.statemachine;


import modula.core.model.ModelException;
import modula.core.model.Modula;
import modula.workflow.executor.factory.ModulaFactory;
import modula.workflow.executor.factory.URLModulaFactory;
import modula.workflow.listener.DefaultWorkflowListener;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @description:
 * @author: gubing.gb
 * @date: 2016/12/30.
 */
public class StateMachineBuilder {
    /**
     * 状态机绑定的组件action
     */
    private Map<String, Class> actions = null;

    /**
     * 状态机监听器
     */
    private List<DefaultWorkflowListener> listeners = null;

    /**
     * 创建Modula工厂
     */
    private ModulaFactory modulaFactory = null;

    /**
     * modula缓存
     */
    private final ModulaHolder modulaCache = new ModulaHolder();

    public StateMachine build() throws ModelException, XMLStreamException, IOException {
        verify();
        //TODO:学到1，缓存的频繁使用
        Modula modula = modulaCache.get();
        if (modula == null) {
            int version = modulaCache.version.get();
            modula = modulaFactory.createModula(actions);
            modulaCache.set(version, modula);
        }

        DefaultStateMachine stateMachine = new DefaultStateMachine(modula);

        return stateMachine;
    }

    private void verify() {
    }

    public StateMachineBuilder url(URL modulaURL) {
        this.modulaFactory = new URLModulaFactory(modulaURL);
        return this;
    }

    public StateMachineBuilder setActions(Map<String, Class> actions) {
        this.actions = actions;
        return this;
    }

    public StateMachineBuilder addAction(String key, Class clazz) {
        if (actions == null) {
            actions = new HashMap<String, Class>();
        }
        actions.put(key, clazz);
        return this;
    }

    public StateMachineBuilder setListeners(List<DefaultWorkflowListener> listeners) {
        this.listeners = listeners;
        return this;
    }

    class ModulaHolder {
        /**
         * Modula 缓存版本
         */
        final AtomicInteger version = new AtomicInteger();
        /**
         * Modula 缓存
         */
        volatile Modula modula = null;

        public Modula get() {
            return modula;
        }

        public int set(int current, Modula modula) {
            if (version.compareAndSet(current, current + 1)) {
                this.modula = modula;
            }
            return version.get();
        }

        public synchronized int set(Modula modula) {
            this.modula = modula;
            return version.incrementAndGet();
        }
    }
}
