package modula.core.model;

/**
 * state元素
 */
public class State extends TransitionalState {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 2L;

    /**
     * 初始子节点，对应initial属性
     */
    private String first;

    /**
     * 状态机的初始state
     */
    private Initial initial;


    public State() {
    }

    /**
     * Get the initial state.
     *
     * @return Initial Returns the initial state.
     */
    public final Initial getInitial() {
        return initial;
    }

    /**
     * Set the initial state.
     *
     * @param target The target to set.
     */
    public final void setInitial(final Initial target) {
        this.first = null;
        this.initial = target;
        target.setParent(this);
    }

    /**
     * Get the initial state's ID.
     *
     * @return The initial state's string ID.
     */
    public final String getFirst() {
        return first;
    }

    /**
     * Set the initial state by its ID string.
     *
     * @param target The initial target's ID to set.
     */
    public final void setFirst(final String target) {
        this.first = target;
        SimpleTransition t = new SimpleTransition();
        t.setNext(target);
        Initial ini = new Initial();
        ini.setGenerated();
        ini.setTransition(t);
        ini.setParent(this);
        this.initial = ini;
    }

    /**
     * {@inheritDoc}
     *
     * @return Returns true 如果state没有孩子
     */
    public final boolean isAtomicState() {
        return getChildren().isEmpty();
    }

    /**
     * Check whether this is a simple (leaf) state (UML terminology).
     *
     * @return true if this is a simple state, otherwise false
     */
    public final boolean isSimple() {
        return isAtomicState();
    }

    /**
     * Check whether this is a composite state (UML terminology).
     *
     * @return true if this is a composite state, otherwise false
     */
    public final boolean isComposite() {
        return !isSimple();
    }

    /**
     * Checks whether it is a region state (directly nested to parallel - UML
     * terminology).
     *
     * @return true if this is a region state, otherwise false
     * @see 、、Parallel TODO
     */
    //public final boolean isRegion() {
    //    return getParent() instanceof Parallel;
    //}

    /**
     * Adds an EnterableState (State, Final or Parallel) child
     *
     * @param es the child to add
     */
    @Override
    public final void addChild(final EnterableState es) {
        super.addChild(es);
    }
}

