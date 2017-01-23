package modula.executor.factory;


import modula.parser.model.ModelException;
import modula.parser.model.Modula;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Map;


/**
 * @description:
 * @author: gubing.gb
 * @date: 2016/12/30.
 */
public interface ModulaFactory {
    /**
     * 创建 Modula
     *
     * @param actionMap 状态机绑定的action，可以为null
     */
    Modula createModula(Map<String, Class> actionMap) throws ModelException, XMLStreamException, IOException;
}

