package modula.spring;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.Map;

/**
 * @description:
 * @author: gubing.gb
 * @date: 2017/1/18.
 */
public class SpringHolder {
    private static ConfigurableListableBeanFactory beanFactory;

    public static <T> T getService(String name) {
        return beanFactory == null && name != null ? null : (T) beanFactory.getBean(name);
    }

    public static <T> T getService(Class<T> clazz) {
        Map<String, T> result = beanFactory.getBeansOfType(clazz);
        return result.values().iterator().next();
    }

    public static ConfigurableListableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    public static void setBeanFactory(BeanFactory beanFactory) {
        SpringHolder.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }
}
