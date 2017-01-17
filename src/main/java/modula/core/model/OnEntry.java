package modula.core.model;

/**
 * @description: 进入state时的执行内容，可选属性
 * @author: gubing.gb
 * @date: 2016/12/30.
 */
public class OnEntry extends Executable {
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * 执行完OnEntry是否抛出"entry.state.id"事件
     */
    private Boolean raiseEvent;

    /**
     * Constructor.
     */
    public OnEntry() {
        super();
    }

    /**
     * Set the EnterableState parent.
     *
     * @param parent The parent to set.
     */
    @Override
    public final void setParent(final EnterableState parent) {
        super.setParent(parent);
    }

    /**
     * @return true if the non-standard internal "entry.state.id" event will be raised after executing this OnEntry
     */
    public final boolean isRaiseEvent() {
        return raiseEvent != null && raiseEvent;
    }

    /**
     * @return The indicator whether to raise the non-standard "entry.state.id" internal event after executing
     * this OnEntry. When null no event will be raised
     */
    public final Boolean getRaiseEvent() {
        return raiseEvent;
    }

    /**
     * Set the indicator whether to raise the non-standard "entry.state.id" internal event after executing this OnEntry.
     *
     * @param raiseEvent The indicator, when null no event will be raised
     */
    public final void setRaiseEvent(final Boolean raiseEvent) {
        this.raiseEvent = raiseEvent;
    }

}
