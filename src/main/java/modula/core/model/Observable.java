package modula.core.model;

/**
 * MODULA观察接口，通过{@link modula.core.NotificationRegistry}观察元素进程.
 * 包括 {@link TransitionTarget}s, {@link Transition}s 或者状态机{@link Modula}.
 * 需保证元素个数不超过Integer.MAX_VALUE
 */
public interface Observable {

    /**
     * @return Returns the id for this Observable which is unique within the Modula state machine
     */
    Integer getObservableId();
}

