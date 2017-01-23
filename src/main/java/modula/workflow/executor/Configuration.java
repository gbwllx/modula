package modula.workflow.executor;

import org.apache.commons.beanutils.ConvertUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @description:
 * @author: gubing.gb
 * @date: 2017/1/23.
 */
public class Configuration implements Serializable {
    private static final long serialVersionUID = -3356758446289248061L;

    /**
     * 默认action 命名空间
     */
    public static final String DEFAULT_NAMESAPCE = "http://www.alibaba-inc.com/2017/modula";
    /**
     * 全局变量前缀
     */
    public static final String DEFAULT_GLOBAL_VAR_PREFIX = "$";
    /**
     * 外部context的key
     */
    public static final String EXTERNAL_CONTEXT = "$ctx";
    /**
     * default concurrency level
     */
    public static final int DEFAULT_CONCURRENCY_LEVEL = 16;
    /**
     * 自定义 action 的类型
     */
    public static final String CUSTOM_ACTION_CLASS = "targetClass";
    /**
     * 默认的自定义 action 的执行方法
     */
    public static final String DEFAULT_CUSTOM_ACTION_METHOD = "execute";
    /**
     * 状态机执行结果 key
     */
    public static final String RESULT_KEY = "$_result";
    /**
     * 状态机 SCInstance
     */
    public static final String SCInstance = "$_SCInstance";
    /**
     * 是否开启按类型注入action的执行方法入参
     */
    public static final boolean ENABLE_INJECT_BY_TYPE = true;
    /**
     * 状态机的日志logger
     */
    public static final String WORKFLOW_LOGGER = "WorkflowLogger";


    public static final ConcurrentMap<String, String> config = new ConcurrentHashMap<String, String>();

    static {
        String[] searchName = {"workflow", "workflow-user"};
        for (String name : searchName) {
            ResourceBundle bundle = findBundle(name);
            if (bundle != null) {
                for (String key : bundle.keySet()) {
                    config.put(key, bundle.getString(key));
                }
            }
        }
        System.out.println("xfsm.config = " + config);
    }

    public static String getConfig(String key) {
        return config.get(key);
    }

    public static <T> T getConfig(String key, Class<T> type) {
        String value = config.get(key);
        return (T) (type != null && value != null ? ConvertUtils.convert(value, type) : value);
    }

    public static ResourceBundle findBundle(String baseName) {
        try {
            return ResourceBundle.getBundle(baseName, Locale.ROOT);
        } catch (MissingResourceException e) {
            //ignore
        }
        return null;
    }

    public static String getVersion() {
        return getConfig("version");
    }

}
