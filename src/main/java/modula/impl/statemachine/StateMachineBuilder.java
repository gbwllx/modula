package modula.impl.statemachine;


import modula.core.model.Modula;
import modula.impl.factory.ModulaFactory;

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
     * 创建Modula工厂
     */
    private ModulaFactory modulaFactory = null;

    /**
     * modula缓存
     */
    private final ModulaHolder modulaCache = new ModulaHolder();

    public StateMachine build() {
        verify();
        //TODO:学到1，缓存的频繁使用
        Modula modula = modulaCache.get();
        if (modula == null) {
            int version = modulaCache.version.get();
            modula = modulaFactory.createModula(actions);
            modulaCache.set(version, modula);
        }

        DefaultStateMachine stateMachine = new DefaultStateMachine(modula);

        return null;
    }

    private void verify() {
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
