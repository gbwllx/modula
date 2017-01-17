package modula.core;

import modula.core.model.EnterableState;
import modula.core.model.Observable;
import modula.core.model.Transition;
import modula.core.model.TransitionTarget;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * listener注册感兴趣的节点例如：Modula root, states, transitions
 * 监控三种事件：onentry,onexit,ontransition
 */
public final class NotificationRegistry {

    /**
     * key:{@link modula.core.model.Observable#getObservableId()}.
     */
    private final Map<Integer, Set<ModulaListener>> regs;

    /**
     * Constructor.
     */
    public NotificationRegistry() {
        this.regs = new HashMap<Integer, Set<ModulaListener>>();
    }

    /**
     * Register this ModulaListener for this Observable.
     *
     * @param source The observable this listener wants to listen to
     * @param lst    The listener
     */
    synchronized void addListener(final Observable source, final ModulaListener lst) {
        if (source != null && source.getObservableId() != null) {
            Set<ModulaListener> entries = regs.get(source.getObservableId());
            if (entries == null) {
                entries = new LinkedHashSet<ModulaListener>();
                regs.put(source.getObservableId(), entries);
            }
            entries.add(lst);
        }
    }

    /**
     * Deregister this ModulaListener for this Observable.
     *
     * @param source The observable this listener wants to stop listening to
     * @param lst    The listener
     */
    synchronized void removeListener(final Observable source, final ModulaListener lst) {
        if (source != null && source.getObservableId() != null) {
            Set<ModulaListener> entries = regs.get(source.getObservableId());
            if (entries != null) {
                entries.remove(lst);
                if (entries.size() == 0) {
                    regs.remove(source.getObservableId());
                }
            }
        }
    }

    /**
     * Inform all relevant listeners that a EnterableState has been
     * entered.
     *
     * @param source The Observable
     * @param state  The EnterableState that was entered
     */
    public synchronized void fireOnEntry(final Observable source,
                                         final EnterableState state) {
        if (source != null && source.getObservableId() != null) {
            Set<ModulaListener> entries = regs.get(source.getObservableId());
            if (entries != null) {
                for (ModulaListener lst : entries) {
                    lst.onEntry(state);
                }
            }
        }
    }

    /**
     * Inform all relevant listeners that a EnterableState has been
     * exited.
     *
     * @param source The Observable
     * @param state  The EnterableState that was exited
     */
    public synchronized void fireOnExit(final Observable source,
                                        final EnterableState state) {
        if (source != null && source.getObservableId() != null) {
            Set<ModulaListener> entries = regs.get(source.getObservableId());
            if (entries != null) {
                for (ModulaListener lst : entries) {
                    lst.onExit(state);
                }
            }
        }
    }

    /**
     * Inform all relevant listeners of a transition that has occured.
     *
     * @param source     The Observable
     * @param from       The source EnterableState
     * @param to         The destination EnterableState
     * @param transition The Transition that was taken
     * @param event      The event name triggering the transition
     */
    public synchronized void fireOnTransition(final Observable source,
                                              final TransitionTarget from, final TransitionTarget to,
                                              final Transition transition, final String event) {
        if (source != null && source.getObservableId() != null) {
            Set<ModulaListener> entries = regs.get(source.getObservableId());
            if (entries != null) {
                for (ModulaListener lst : entries) {
                    lst.onTransition(from, to, transition, event);
                }
            }
        }
    }
}

