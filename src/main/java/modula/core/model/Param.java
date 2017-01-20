package modula.core.model;

import java.io.Serializable;
import java.util.Map;

/**
 * param元素
 */
public class Param implements NamespacePrefixesHolder, Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * param名
     */
    private String name;

    /**
     * TODO
     * The param expression, may be null.
     */
    private String expr;

    /**
     * Modula文档定义的action的命名空间
     */
    private Map<String, String> namespaces;

    public Param() {
        name = null;
        expr = null;
    }

    /**
     * Get the name for this param.
     *
     * @return String The param name.
     */
    public final String getName() {
        return name;
    }

    /**
     * Set the name for this param.
     *
     * @param name The param name.
     */
    public final void setName(final String name) {
        this.name = name;
    }

    /**
     * Get the expression for this param value.
     *
     * @return String The expression for this param value.
     */
    public final String getExpr() {
        return expr;
    }

    /**
     * Set the expression for this param value.
     *
     * @param expr The expression for this param value.
     */
    public final void setExpr(final String expr) {
        this.expr = expr;
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

}

