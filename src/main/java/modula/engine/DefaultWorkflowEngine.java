package modula.engine;

import modula.engine.context.StateMachineBuildContext;
import modula.engine.context.WorkflowContext;
import modula.executor.statemachine.StateMachine;
import modula.parser.model.ModelException;
import modula.spring.DefaultStateMachineRegistry;
import modula.spring.SpringHolder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: 流程驱动器默认实现
 * @author: gubing.gb
 * @date: 2016/12/30.
 */
public class DefaultWorkflowEngine implements WorkflowEngine {
    private DefaultStateMachineRegistry registry;
    private Map<String, Object> globals;
    private ThreadPoolTaskExecutor executor;
    private ConcurrentHashMap<Long, StateMachine> smCache = new ConcurrentHashMap<>();

    public <T> T execute(WorkflowContext context) throws ModelException, XMLStreamException, IOException {
        //StateMachine stateMachine = registry.get(context.getKey());

        Long threadId = Thread.currentThread().getId();
        StateMachine stateMachine = smCache.get(threadId);
        if (stateMachine == null) {
            stateMachine = SpringHolder.getService(StateMachineBuildContext.class).createStateMachine();
            smCache.put(threadId, stateMachine);
        }

        executor.execute(Task.of(stateMachine, context));

        return null;
    }

    public void setRegistry(DefaultStateMachineRegistry registry) {
        this.registry = registry;
    }

    public void setGlobals(Map<String, Object> globals) {
        this.globals = globals;
    }

    public void setExecutor(ThreadPoolTaskExecutor executor) {
        this.executor = executor;
    }
}
