package modula.core.model;

import modula.core.ActionExecutionContext;
import modula.core.Context;
import modula.workflow.listener.DefaultWorkflowListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description:
 * @author: gubing.gb
 * @date: 2017/1/19.
 */
public class Log extends Action {
    private static final Logger logger = LoggerFactory.getLogger(Log.class);

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * An expression evaluating to a string to be logged.
     */
    private String expr;

    /**
     * An expression which returns string which may be used, for example,
     * to indicate the purpose of the log.
     */
    private String label;

    /**
     * Constructor.
     */
    public Log() {
        super();
    }

    /**
     * Get the log expression.
     *
     * @return Returns the expression.
     */
    public final String getExpr() {
        return expr;
    }

    /**
     * Set the log expression.
     *
     * @param expr The expr to set.
     */
    public final void setExpr(final String expr) {
        this.expr = expr;
    }

    /**
     * Get the log label.
     *
     * @return Returns the label.
     */
    public final String getLabel() {
        return label;
    }

    /**
     * Set the log label.
     *
     * @param label The label to set.
     */
    public final void setLabel(final String label) {
        this.label = label;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(ActionExecutionContext exctx) throws ModelException {
        //TODO
        Context ctx = exctx.getContext(getParentEnterableState());
        //ctx.setLocal(getNamespacesKey(), getNamespaces());
        exctx.getAppLog().info(label + ": " + expr);
        //ctx.setLocal(getNamespacesKey(), null);
    }
}
