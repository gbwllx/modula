package modula.engine;

import modula.engine.context.WorkflowContext;
import modula.parser.model.ModelException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * @description: 流程驱动接口
 * @author: gubing.gb
 * @date: 2016/12/30.
 */
public interface WorkflowEngine {
    /**
     * 执行流程入口
     *
     * @param context 请求上下文
     * @param <T>     有返回结果， 则返回结果，否则返回null
     * @return
     */
    <T> T execute(WorkflowContext context) throws ModelException, XMLStreamException, IOException;


}
