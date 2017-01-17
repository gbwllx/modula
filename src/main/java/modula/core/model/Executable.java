package modula.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: onentry，onexit等可执行元素的基类
 * @author: gubing.gb
 * @date: 2016/12/30.
 */
public abstract class Executable implements Serializable {
    /**
     * action集合
     */
    private List<Action> actions;

    /**
     * 父容器
     */
    private EnterableState parent;

    /**
     * Constructor.
     */
    public Executable() {
        super();
        this.actions = new ArrayList<Action>();
    }

    /**
     * Get the executable actions contained in this Executable.
     *
     * @return Returns the actions.
     */
    public final List<Action> getActions() {
        return actions;
    }

    /**
     * Add an Action to the list of executable actions contained in
     * this Executable.
     *
     * @param action The action to add.
     */
    public final void addAction(final Action action) {
        if (action != null) {
            this.actions.add(action);
        }
    }

    /**
     * Get the EnterableState parent.
     *
     * @return Returns the parent.
     */
    public EnterableState getParent() {
        return parent;
    }

    /**
     * Set the EnterableState parent.
     *
     * @param parent The parent to set.
     */
    protected void setParent(final EnterableState parent) {
        this.parent = parent;
    }

}
