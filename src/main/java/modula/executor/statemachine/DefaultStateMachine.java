package modula.executor.statemachine;


import modula.engine.event.WorkflowEvent;
import modula.engine.context.WorkflowContext;
import modula.executor.factory.config.Configuration;
import modula.listener.DefaultWorkflowListener;
import modula.listener.ModulaListener;
import modula.executor.core.event.TriggerEvent;
import modula.parser.model.*;

import java.util.List;

/**
 * @description:
 * @author: gubing.gb
 * @date: 2016/12/29.
 */
public class DefaultStateMachine extends AbstractStateMachine implements StateMachine {
    private boolean initial = false;

    public DefaultStateMachine(Modula modula) {
        super(modula);
    }

    @Override
    public StateMachine refresh(WorkflowContext context) throws Exception {
        //executor.getRootContext().reset();
        executor.initialize();
        return null;
    }

    @Override
    public StateMachine start() throws Exception {
        if (!initialized) {
            executor.initialize();
        }
        executor.go();
        return this;
    }


    public DefaultStateMachine start(WorkflowContext ctx) throws Exception {
        List<WorkflowEvent> events = ctx.getEvents();
        if (!initialized) {// fixme false问题
            refresh(ctx);
            start(); // 先启动状态机
        }
//        if (ctx.getCurrentState() == null) { // 没有设置初始状态
//            start(); // 先启动状态机
//        }
        if (events != null && !events.isEmpty()) {  // 如果有事件需要触发
            triggerEvent(events.toArray(new WorkflowEvent[events.size()]));
        }
        return this;
    }

    public DefaultStateMachine triggerEvent(WorkflowEvent... events) throws Exception {
        TriggerEvent[] evts = new TriggerEvent[events.length];
        for (int i = 0; i < events.length; ++i) {
            if (events[i] == null) {
                continue;
            }
            evts[i] = new TriggerEvent(events[i].getEvent(), TriggerEvent.SIGNAL_EVENT, events[i].getPayload());
        }
        executor.triggerEvents(evts);
        return this;
    }

    public DefaultStateMachine addListener(DefaultWorkflowListener... listeners) {
        if (listeners != null && listeners.length > 0) {
            for (DefaultWorkflowListener listener : listeners) {
                Observable observable = null;
                if (listener.getId() == null || "modula".equals(listener.getId())) {
                    observable = this.modula;
                } else {
                    observable = executor.getStateMachine().getTargets().get(listener.getId());
                }
                if (observable != null) {
                    executor.addListener(observable, listener);
                }
            }
        }
        return this;
    }

    public DefaultStateMachine set(String name, Object value) {
        executor.getRootContext().set(name, value);
        return this;
    }

    public <T> T get(String name) {
        return (T) executor.getRootContext().get(name);
    }
}
