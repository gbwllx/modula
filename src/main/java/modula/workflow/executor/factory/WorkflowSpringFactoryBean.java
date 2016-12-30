package modula.workflow.executor.factory;

import modula.workflow.executor.statemachine.StateMachine;
import modula.workflow.executor.context.StateMachineBuildContext;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * @description: 用于创建Modula的spring bean
 * @author: gubing.gb
 * @date: 2016/12/30.
 */
public class WorkflowSpringFactoryBean extends AbstractFactoryBean{
    private StateMachineBuildContext createContext = new StateMachineBuildContext();

    @Override
    public Class<?> getObjectType() {
        return StateMachine.class;
    }

    @Override
    protected StateMachine createInstance() throws Exception {
        return createContext.createStateMachine();
    }
}
