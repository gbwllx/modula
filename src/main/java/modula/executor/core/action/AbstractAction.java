package modula.executor.core.action;

import modula.executor.core.context.ActionExecutionContext;
import modula.parser.model.ModelException;
import modula.parser.model.Action;

/**
 * @description:
 * @author: gubing.gb
 * @date: 2016/12/29.
 */
public class AbstractAction extends Action {
    @Override
    public void execute(ActionExecutionContext exctx) throws ModelException {
        System.out.println("abstract");
    }
}
