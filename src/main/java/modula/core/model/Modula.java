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
public class Modula implements Serializable, LifeCycle, Observable, NamespacePrefixesHolder {
    /**
     * Modula 版本号
     */
    private String version;

    /**
     * 初始态
     */
    private String initial;

    /**
     * datamodel 可选属性
     */
    private Datamodel datamodel;
    /**
     * 自动生成TransitionTarget id 的前缀
     */
    public static final String GENERATED_TT_ID_PREFIX = "_generated_tt_id_";
    /**
     * 默认observerId
     */
    private static final Integer MODULA_OBSERVABLE_ID = 0;
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
     * 命名空间
     */
    private Map<String, String> namespaces;

    /**
     * 状态机名字
     */
    private String name;

    /**
     * transitionTarget下一个ID
     *
     * @see #generateTransitionTargetId()
     */
    private long ttNextId;

    public Modula() {
        this.children = new ArrayList<EnterableState>();
        this.targets = new HashMap<String, TransitionTarget>();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Get the data model placed at document root.
     *
     * @return Returns the data model.
     */
    public final Datamodel getDatamodel() {
        return datamodel;
    }

    /**
     * Set the data model at document root.
     *
     * @param datamodel The Datamodel to set.
     */
    public final void setDatamodel(final Datamodel datamodel) {
        this.datamodel = datamodel;
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

    public final EnterableState getFirstChild() {
        if (!children.isEmpty()) {
            return children.get(0);
        }
        return null;
    }

    public final void addChild(final EnterableState es) {
        children.add(es);
    }

    public Map<String, TransitionTarget> getTargets() {
        return targets;
    }

    public void setTargets(Map<String, TransitionTarget> targets) {
        this.targets = targets;
    }

    public final void addTarget(final TransitionTarget target) {
        targets.put(target.getId(), target);
    }


    public String getInitial() {
        return initial;
    }

    public void setInitial(String initial) {
        this.initial = initial;
    }

    @Override
    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    @Override
    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    @Override
    public Integer getObservableId() {
        return MODULA_OBSERVABLE_ID;
    }

    /**
     * Simple unique TransitionTarget id value generation
     *
     * @return a unique TransitionTarget id for this SCXML instance
     */
    public final String generateTransitionTargetId() {
        return GENERATED_TT_ID_PREFIX + ttNextId++;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
