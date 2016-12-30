package modula.core.model;


import modula.core.lifecycle.LifeCycle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: gubing.gb
 * @date: 2016/12/30.
 */
public class Modula implements Serializable, LifeCycle {
    /**
     * Modula 版本号
     */
    private String version;

    /**
     * Modula初始Transition
     */
    private SimpleTransition initialTransition;

    /**
     * 根节点的子节点
     */
    private List<EnterableState> children;

    /**
     * 所有State的集合，key是id
     */
    private Map<String, TransitionTarget> targets;

    /**
     * 状态机名字
     */
    private String name;

    public Modula(){
        this.children = new ArrayList<EnterableState>();
        this.targets = new HashMap<String, TransitionTarget>();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public SimpleTransition getInitialTransition() {
        return initialTransition;
    }

    public void setInitialTransition(SimpleTransition initialTransition) {
        this.initialTransition = initialTransition;
    }

    public List<EnterableState> getChildren() {
        return children;
    }

    public void setChildren(List<EnterableState> children) {
        this.children = children;
    }

    public Map<String, TransitionTarget> getTargets() {
        return targets;
    }

    public void setTargets(Map<String, TransitionTarget> targets) {
        this.targets = targets;
    }
}
