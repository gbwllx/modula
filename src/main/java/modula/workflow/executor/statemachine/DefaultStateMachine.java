package modula.workflow.executor.statemachine;


import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory;
import modula.core.model.Modula;
import modula.workflow.executor.WorkflowEvent;
import modula.workflow.executor.context.WorkflowContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

/**
 * @description:
 * @author: gubing.gb
 * @date: 2016/12/29.
 */
public class DefaultStateMachine extends AbstractStateMachine implements StateMachine {
    public DefaultStateMachine(Modula modula) {
        super(modula);
    }

    @Override
    public StateMachine refresh(WorkflowContext context) throws Exception {
        executor.getRootContext().reset();
        //executor.initialize();
        return null;
    }

    @Override
    public StateMachine start() throws Exception {
//        if (!initialized) {
//            executor.initialize();
//        }
        executor.go();
        return this;
    }


    public DefaultStateMachine start(WorkflowContext ctx) throws Exception {
        List<WorkflowEvent> events = ctx.getEvents();
//        if (!initialized) {
//            refresh(ctx);
//        }
        if (ctx.getCurrentState() == null) { // 没有设置初始状态
            start(); // 先启动状态机
        }
//        if (events != null && !events.isEmpty()) {  // 如果有事件需要触发
//            triggerEvent(events.toArray(new WorkflowEvent[events.size()]));
//        }
        return this;
    }
}
