package modula.core.model;

/**
 * <finalize>元素
 */
public class Finalize extends Executable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    public Finalize() {
        super();
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

