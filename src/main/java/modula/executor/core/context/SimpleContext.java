package modula.executor.core.context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: {@link Context}的简单实现
 * @author: gubing.gb
 * @date: 2017/1/23.
 */
public class SimpleContext implements Context, Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Implementation independent log category.
     */
    private Log log = LogFactory.getLog(Context.class);
    /**
     * The parent Context to this Context.
     */
    private Context parent;
    /**
     * The Map of variables and their values in this Context.
     */
    private Map<String, Object> vars;

    /**
     * Constructor.
     */
    public SimpleContext() {
        this(null, null);
    }

    /**
     * Constructor.
     *
     * @param parent A parent Context, can be null
     */
    public SimpleContext(final Context parent) {
        this(parent, null);
    }

    /**
     * Constructor.
     *
     * @param initialVars A pre-populated initial variables map
     */
    public SimpleContext(final Map<String, Object> initialVars) {
        this(null, initialVars);
    }

    /**
     * Constructor.
     *
     * @param parent      A parent Context, can be null
     * @param initialVars A pre-populated initial variables map
     */
    public SimpleContext(final Context parent, final Map<String, Object> initialVars) {
        this.parent = parent;
        if (initialVars == null) {
            setVars(new HashMap<String, Object>());
        } else {
            setVars(this.vars = initialVars);
        }
    }

    /**
     * Assigns a new value to an existing variable or creates a new one.
     * The method searches the chain of parent Contexts for variable
     * existence.
     *
     * @param name  The variable name
     * @param value The variable value
     */
    public void set(final String name, final Object value) {
        if (getVars().containsKey(name)) { //first try to override local
            setLocal(name, value);
        } else if (parent != null && parent.has(name)) { //then check for global
            parent.set(name, value);
        } else { //otherwise create a new local variable
            setLocal(name, value);
        }
    }

    /**
     * Get the value of this variable; delegating to parent.
     *
     * @param name The variable name
     * @return Object The variable value
     */
    public Object get(final String name) {
        if (getVars().containsKey(name)) {
            return getVars().get(name);
        } else if (parent != null) {
            return parent.get(name);
        } else {
            return null;
        }
    }

    /**
     * Check if this variable exists, delegating to parent.
     *
     * @param name The variable name
     * @return boolean true if this variable exists
     */
    public boolean has(final String name) {
        return (hasLocal(name) || (parent != null && parent.has(name)));
    }

    /**
     * Check if this variable exists, only checking this Context
     *
     * @param name The variable name
     * @return boolean true if this variable exists
     */
    public boolean hasLocal(final String name) {
        return (getVars().containsKey(name));
    }

    /**
     * Clear this Context.
     */
    public void reset() {
        getVars().clear();
    }

    /**
     * Get the parent Context, may be null.
     *
     * @return Context The parent Context
     */
    public Context getParent() {
        return parent;
    }

    /**
     * Assigns a new value to an existing variable or creates a new one.
     * The method allows to shaddow a variable of the same name up the
     * Context chain.
     *
     * @param name  The variable name
     * @param value The variable value
     */
    public void setLocal(final String name, final Object value) {
        getVars().put(name, value);
        if (log.isDebugEnabled() && !name.equals(ModulaSystemContext.ALL_STATES_KEY)) {
            log.debug(name + " = " + String.valueOf(value));
        }
    }

    /**
     * Set the variables map.
     *
     * @param vars The new Map of variables.
     */
    protected void setVars(final Map<String, Object> vars) {
        this.vars = vars;
    }

    /**
     * Get the Map of all local variables in this Context.
     *
     * @return Returns the vars.
     */
    public Map<String, Object> getVars() {
        return vars;
    }

    /**
     * Set the log used by this <code>Context</code> instance.
     *
     * @param log The new log.
     */
    protected void setLog(final Log log) {
        this.log = log;
    }

    /**
     * Get the log used by this <code>Context</code> instance.
     *
     * @return Log The log being used.
     */
    protected Log getLog() {
        return log;
    }

}


