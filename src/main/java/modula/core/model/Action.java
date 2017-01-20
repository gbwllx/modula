package modula.core.model;

/**
 * @description:
 * @author: gubing.gb
 * @date: 2016/12/29.
 */

import modula.core.ActionExecutionContext;
import modula.core.Context;

import java.io.Serializable;
import java.util.Map;

/**
 * Modula 可执行元素的基类
 * 例如assign, log etc.
 */
public abstract class Action implements NamespacePrefixesHolder,
        Serializable {

    /**
     * parent或者container
     */
    private Executable parent;

    /**
     * XML命名空间
     * preserved for deferred XPath evaluation.
     */
    private Map<String, String> namespaces;

    public Action() {
        super();
        this.parent = null;
        this.namespaces = null;
    }

    /**
     * Get the Executable parent.
     *
     * @return Returns the parent.
     */
    public final Executable getParent() {
        return parent;
    }

    /**
     * Set the Executable parent.
     *
     * @param parent The parent to set.
     */
    public final void setParent(final Executable parent) {
        this.parent = parent;
    }

    /**
     * Get the XML namespaces at this action node in the SCXML document.
     *
     * @return Returns the map of namespaces.
     */
    public final Map<String, String> getNamespaces() {
        return namespaces;
    }

    /**
     * Set the XML namespaces at this action node in the SCXML document.
     *
     * @param namespaces The document namespaces.
     */
    public final void setNamespaces(final Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    public final EnterableState getParentEnterableState()
            throws ModelException {
        if (parent == null) {
            // global script doesn't have a EnterableState
            return null;
        }
        TransitionTarget tt = parent.getParent();
        if (tt instanceof EnterableState) {
            return (EnterableState) tt;
        } else if (tt instanceof History) {
            return ((History) tt).getParent();
        } else {
            throw new ModelException("Unknown TransitionTarget subclass:"
                    + tt.getClass().getName());
        }
    }

    public abstract void execute(ActionExecutionContext exctx) throws ModelException;

    protected static String getNamespacesKey() {
        return Context.NAMESPACES_KEY;
    }
}
