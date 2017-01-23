package modula.spring;

import modula.engine.context.StateMachineBuildContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Map;

/**
 * @description:
 * @author: gubing.gb
 * @date: 2017/1/18.
 */
public class DefaultStateMachineRegistry extends AbstractStateMachineRegistry implements ApplicationListener, BeanFactoryAware {
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        SpringHolder.setBeanFactory(beanFactory);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event != null && event instanceof ContextRefreshedEvent) {
            ConfigurableListableBeanFactory beanFactory = (ConfigurableListableBeanFactory) SpringHolder.getBeanFactory();
            this.configs.putAll(beanFactory.getBeansOfType(StateMachineBuildContext.class));
        }

        for (Map.Entry<String, StateMachineBuildContext> entry : configs.entrySet()) {
            entry.getValue().setId(entry.getKey());
        }
    }
}
