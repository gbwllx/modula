package modula.executor.factory;

import modula.executor.context.StateMachineBuildContext;
import modula.listener.DefaultWorkflowListener;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.ClassUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @description: 用于创建Modula的spring bean
 * @author: gubing.gb
 * @date: 2016/12/30.
 */
public class WorkflowSpringFactoryBean extends AbstractFactoryBean {
    private ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
    private StateMachineBuildContext buildContext = new StateMachineBuildContext();

    @Override
    public Class<?> getObjectType() {
        return StateMachineBuildContext.class;
    }

    @Override
    protected StateMachineBuildContext createInstance() throws Exception {
        return buildContext;
    }

    public void setPath(String modulaPath) {
        if (modulaPath.startsWith("/")) {
            modulaPath = modulaPath.substring(1);
        }

        this.buildContext.setModulaURL(classLoader.getResource(modulaPath));
    }

    public void setAction(Map<String, Class> actionMap) {
        this.buildContext.setActions(actionMap);
    }

    public void setListeners(List<DefaultWorkflowListener> listeners) {
        this.buildContext.setListeners(listeners);
    }

    public void setListener(DefaultWorkflowListener listener) {
        if (listener != null) {
            this.buildContext.setListeners(Arrays.asList(listener));
        }
    }
}
