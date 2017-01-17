package modula.core;

import java.io.Serializable;
import java.util.*;

/**
 * ModulaSystemContext:只读
 * 提供Modula系统变量
 */
public class ModulaSystemContext implements Context, Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    public static final String EVENT_KEY = "_event";
    public static final String SESSIONID_KEY = "_sessionid";
    public static final String SCXML_NAME_KEY = "_name";
    public static final String IOPROCESSORS_KEY = "_ioprocessors";
    public static final String X_KEY = "_x";

    /**
     * Commons SCXML internal system variable holding the current SCXML configuration of all (including ancestors)
     * active states.
     */
    public static final String ALL_STATES_KEY = "_ALL_STATES";

    /**
     * The set of protected system variables names
     */
    private static final Set<String> PROTECTED_NAMES = new HashSet<String>(Arrays.asList(
            new String[]{EVENT_KEY, SESSIONID_KEY, SCXML_NAME_KEY, IOPROCESSORS_KEY, X_KEY, ALL_STATES_KEY}
    ));

    /**
     * The wrapped system context
     */

    private Context systemContext;

    /**
     * Initialize or replace systemContext
     *
     * @param systemContext the system context to set
     */
    void setSystemContext(Context systemContext) {
        if (this.systemContext != null) {
            // replace systemContext
            systemContext.getVars().putAll(this.systemContext.getVars());
        }
        this.systemContext = systemContext;
        this.protectedVars = Collections.unmodifiableMap(systemContext.getVars());
    }

    /**
     * The unmodifiable wrapped variables map from the wrapped system context
     */
    private Map<String, Object> protectedVars;

    public ModulaSystemContext(Context systemContext) {
        setSystemContext(systemContext);
    }

    @Override
    public void set(final String name, final Object value) {
        if (PROTECTED_NAMES.contains(name)) {
            throw new UnsupportedOperationException();
        }
        // non-protected variables are set on the parent of the system context (e.g. root context)
        systemContext.getParent().set(name, value);
    }

    @Override
    public void setLocal(final String name, final Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object get(final String name) {
        return systemContext.get(name);
    }

    @Override
    public boolean has(final String name) {
        return systemContext.has(name);
    }

    @Override
    public boolean hasLocal(final String name) {
        return systemContext.hasLocal(name);
    }

    @Override
    public Map<String, Object> getVars() {
        return protectedVars;
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Context getParent() {
        return systemContext.getParent();
    }

    /**
     * @return Returns the wrapped (modifiable) system context
     */
    Context getContext() {
        return systemContext;
    }
}
