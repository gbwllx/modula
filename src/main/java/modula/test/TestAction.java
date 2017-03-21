package modula.test;

import modula.executor.core.action.AbstractAction;
import modula.executor.core.context.ActionExecutionContext;
import modula.parser.model.ModelException;

/**
 * description:
 * author: gubing.gb
 * date: 2017/3/21.
 */
public class TestAction extends AbstractAction {
//    @Resource
//    private AService aService;

    @Override
    public void execute(ActionExecutionContext exctx) throws ModelException {
        System.out.println("testaction");
    }
}
