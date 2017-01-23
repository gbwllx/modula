package modula.listener;


import modula.parser.model.Modula;
import modula.parser.model.State;
import modula.parser.model.Transition;
import modula.parser.model.EnterableState;
import modula.parser.model.TransitionTarget;

/**
 * Listener接口
 * 可观察实体包括{@link Modula}实例，
 * {@link State} 实例，{@link Transition}实例
 */
public interface ModulaListener {

    /**
     * Handle the entry into a EnterableState.
     *
     * @param state The EnterableState entered
     */
    void onEntry(EnterableState state);

    /**
     * Handle the exit out of a EnterableState.
     *
     * @param state The EnterableState exited
     */
    void onExit(EnterableState state);

    /**
     * Handle the transition.
     *
     * @param from       The source TransitionTarget
     * @param to         The destination TransitionTarget
     * @param transition The Transition taken
     * @param event      The event name triggering the transition
     */
    void onTransition(TransitionTarget from, TransitionTarget to,
                      Transition transition, String event);

}

