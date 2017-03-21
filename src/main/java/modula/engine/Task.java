package modula.engine;

import modula.engine.context.WorkflowContext;
import modula.executor.statemachine.StateMachine;

/**
 * description:
 * author: gubing.gb
 * date: 2017/3/21.
 */
public class Task implements Runnable {
    private StateMachine stateMachine;
    private WorkflowContext context;

    public Task(StateMachine stateMachine, WorkflowContext context) {
        this.stateMachine = stateMachine;
        this.context = context;
    }

    public static Task of(StateMachine stateMachine, WorkflowContext context) {
        return new Task(stateMachine, context);
    }

    @Override
    public void run() {
        if (stateMachine == null) {
            throw new RuntimeException("create stateMachine error, stateMachine=" + stateMachine);
        }
        try {
            stateMachine.start(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName());
    }
}
