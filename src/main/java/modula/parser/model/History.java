package modula.parser.model;

/**
 * 状态转移历史
 */
public class History extends TransitionTarget {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * 潜历史还是深历史
     */
    private boolean isDeep;

    /**
     * 默认的history state
     */
    private SimpleTransition transition;

    public History() {
        super();
    }

    /**
     * Get the transition.
     *
     * @return Returns the transition.
     */
    public final SimpleTransition getTransition() {
        return transition;
    }

    /**
     * Set the transition.
     *
     * @param transition The transition to set.
     */
    public final void setTransition(final SimpleTransition transition) {
        if (getParent() == null) {
            throw new IllegalStateException("History transition cannot be set before setting its parent");
        }
        this.transition = transition;
        this.transition.setParent(getParent());
    }

    /**
     * Is this history &quot;deep&quot; (as against &quot;shallow&quot;).
     *
     * @return Returns whether this is a &quot;deep&quot; history
     */
    public final boolean isDeep() {
        return isDeep;
    }

    /**
     * @param type The history type, which can be &quot;shallow&quot; or
     *             &quot;deep&quot;
     */
    public final void setType(final String type) {
        if ("deep".equals(type)) {
            isDeep = true;
        }
        //shallow is by default
    }

    /**
     * @return Returns the TransitionalState parent
     */
    @Override
    public TransitionalState getParent() {
        return (TransitionalState) super.getParent();
    }

    /**
     * Set the TransitionalState parent.
     *
     * @param parent The parent to set.
     */
    public final void setParent(final TransitionalState parent) {
        super.setParent(parent);
    }
}

