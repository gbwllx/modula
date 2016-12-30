package modula.impl.factory;

import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * @description: 用于创建Modula的spring bean
 * @author: gubing.gb
 * @date: 2016/12/30.
 */
public class ModulaSpringFactoryBean extends AbstractFactoryBean{
    @Override
    public Class<?> getObjectType() {
        return null;
    }

    @Override
    protected Object createInstance() throws Exception {
        return null;
    }
}
