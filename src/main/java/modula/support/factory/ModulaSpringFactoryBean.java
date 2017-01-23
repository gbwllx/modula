package modula.support.factory;

import modula.executor.context.StateMachineBuildContext;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.ClassUtils;

/**
 * @description:
 * @author: gubing.gb
 * @date: 2017/1/1.
 */
public class ModulaSpringFactoryBean extends AbstractFactoryBean {
    private ClassLoader               classLoader   = ClassUtils.getDefaultClassLoader();
    private StateMachineBuildContext createContext = new StateMachineBuildContext();

    @Override
    protected StateMachineBuildContext createInstance() throws Exception {
        return createContext;
    }

    @Override
    public Class<?> getObjectType() {
        return StateMachineBuildContext.class;
    }

    public void setPath(String modulaPath) {
        if (modulaPath.startsWith("/")) {
            modulaPath = modulaPath.substring(1);
        }
        this.createContext.setModulaURL(classLoader.getResource(modulaPath));
    }
}
