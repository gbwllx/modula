package modula.core.model;

import java.io.Serializable;

/**
 * initial元素
 */
public class Initial implements Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * 父state
     */
    private State parent;

    /**
     * 目标必须是parent的后继
     */
    private SimpleTransition transition;

    /**
     * 是自动生成的还是xml文档定义的
     */
    private boolean generated;

    public Initial() {
        super();
    }

    /**
     * Get the parent State.
     *
     * @return Returns the parent state
     */
    public final State getParent() {
        return parent;
    }


    /**
     * Set the parent TransitionTarget.
     *
     * @param parent The parent state to set
     */
    public final void setParent(final State parent) {
        this.parent = parent;
        if (transition != null) {
            transition.setParent(parent);
        }
    }

    /**
     * Get the initial transition.
     *
     * @return Returns the transition.
     */
    public final SimpleTransition getTransition() {
        return transition;
    }

    /**
     * Set the initial transition.
     *
     * @param transition The transition to set.
     */
    public final void setTransition(final SimpleTransition transition) {
        this.transition = transition;
        this.transition.setParent(getParent());
    }

    /**
     * @return true if this Initial was automatically generated and not loaded from the Modula Document itself
     */
    public final boolean isGenerated() {
        return generated;
    }

    /**
     * Marks this Initial as automatically generated after loading the Modula Document
     */
    public final void setGenerated() {
        this.generated = true;
    }
}

