package modula.executor.factory;

import modula.parser.io.ModulaReader;
import modula.parser.model.Action;
import modula.parser.model.CustomAction;
import modula.executor.factory.config.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: gubing.gb
 * @date: 2017/1/1.
 */
public abstract class AbstractModulaFactory implements ModulaFactory {
    protected ModulaReader.Configuration getConfiguration(Map<String, Class> actionMap) {
        if (actionMap == null || actionMap.isEmpty()) {
            return new ModulaReader.Configuration(null, null, Configuration.BUILTIN_ACTIONS);
        }
        List<CustomAction> customActions = new ArrayList<CustomAction>();
        customActions.addAll(Configuration.BUILTIN_ACTIONS);
        for (Map.Entry<String, Class> entry : actionMap.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                Class actionClass = entry.getValue();

                customActions.add(new CustomAction(Configuration.DEFAULT_NAMESAPCE, entry.getKey(), actionClass));
            }
        }
        return new ModulaReader.Configuration(null, null, customActions);
    }



}
