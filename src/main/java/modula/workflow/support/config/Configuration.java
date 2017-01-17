package modula.workflow.support.config;

import modula.core.model.CustomAction;
import org.apache.commons.beanutils.ConvertUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * 有限状态机配置
 */
public abstract class Configuration implements Serializable {

    private static final long serialVersionUID = -3356758446289248061L;

    /**
     * 默认action 命名空间
     */
    public static final String             DEFAULT_NAMESAPCE            = "http://www.alibaba-inc.com/2015/xfsm";
    /**
     * 全局变量前缀
     */
    public static final String             DEFAULT_GLOBAL_VAR_PREFIX    = "$";
    /**
     * 外部context的key
     */
    public static final String             EXTERNAL_CONTEXT             = "$ctx";
    /**
     * default concurrency level
     */
    public static final int                DEFAULT_CONCURRENCY_LEVEL    = 16;
    /**
     * 自定义 action 的类型
     */
    public static final String             CUSTOM_ACTION_CLASS          = "targetClass";
    /**
     * 默认的自定义 action 的执行方法
     */
    public static final String             DEFAULT_CUSTOM_ACTION_METHOD = "execute";
    /**
     * 状态机执行结果 key
     */
    public static final String             RESULT_KEY                   = "$_result";
    /**
     * 状态机 SCInstance
     */
    public static final String             SCInstance                   = "$_SCInstance";
    /**
     * 是否开启按类型注入action的执行方法入参
     */
    public static final boolean            ENABLE_INJECT_BY_TYPE        = true;
    /**
     * 状态机的日志logger
     */
    public static final String             XFSM_LOGGER                  = "xfsmLogger";
    /**
     * 内置的一些 action
     */
    public static final List<CustomAction> BUILTIN_ACTIONS              = new ArrayList<CustomAction>();

    static {
    }

    public static final ConcurrentMap<String, String> config = new ConcurrentHashMap<String, String>();
    static {
        String[] searchName = {"xfsm", "xfsm-user"};
        for(String name : searchName) {
            ResourceBundle bundle = findBundle(name);
            if(bundle != null) {
                for(String key : bundle.keySet()) {
                    config.put(key, bundle.getString(key));
                }
            }
        }
        //System.out.println("workflow.config = " + config);
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
