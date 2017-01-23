package modula.parser.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: state，final等可进入状态的基类
 * @author: gubing.gb
 * @date: 2016/12/30.
 */
public abstract class EnterableState extends TransitionTarget implements DocumentOrder {
    /**
     * 状态的文档排序
     */
    private int order;

    /**
     * 可选的OnEntry集合
     */
    private List<OnEntry> onEntries;

    /**
     * 可选的OnExit集合
     */
    private List<OnExit> onExits;

    public EnterableState() {
        super();
        onEntries = new ArrayList<OnEntry>();
        onExits = new ArrayList<OnExit>();
    }

    /**
     * @return the document order of this state
     * @see DocumentOrder
     */
    @Override
    public final int getOrder() {
        return order;
    }

    /**
     * Sets the document order of this state
     *
     * @param order the document order
     * @see DocumentOrder
     */
    public final void setOrder(int order) {
        this.order = order;
    }

    /**
     * Get the OnEntry elements.
     *
     * @return Returns the onEntry elements
     */
    public final List<OnEntry> getOnEntries() {
        return onEntries;
    }

    /**
     * Adds an OnEntry element
     *
     * @param onEntry The onEntry to add.
     */
    public final void addOnEntry(final OnEntry onEntry) {
        onEntry.setParent(this);
        onEntries.add(onEntry);
    }

    /**
     * Get the OnExit elements
     *
     * @return Returns the onExit elements
     */
    public final List<OnExit> getOnExits() {
        return onExits;
    }

    /**
     * Add an OnExit element
     *
     * @param onExit The onExit to add.
     */
    public final void addOnExit(final OnExit onExit) {
        onExit.setParent(this);
        onExits.add(onExit);
    }

    /**
     * Check whether this is an atomic state.
     * <p>
     * An atomic state is a state of type Final or of type State without children,
     * </p>
     *
     * @return Returns true if this is an atomic state.
     */
    public abstract boolean isAtomicState();
}
