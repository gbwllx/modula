package modula.core.model;


import modula.core.PathResolver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * invoke元素，用于调用外部（external）服务
 */
public class Invoke implements NamespacePrefixesHolder, PathResolverHolder,
        Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private String id;

    /**
     * 目标类型
     */
    private String type;

    /**
     * 外部服务的调用源URL
     */
    private String src;

    /**
     * TODO
     * The expression that evaluates to the source URL for the
     * external service.
     */
    private String srcexpr;

    /**
     * 是否推送events到执行过程中，可空
     */
    private Boolean autoForward;

    /**
     * 推送到执行过程中的参数列表
     */
    private final List<Param> paramsList;

    /**
     * finalize子节点，可空
     */
    private Finalize finalize;

    /**
     * {@link PathResolver} for resolving the "src" or "srcexpr" result.
     */
    private PathResolver pathResolver;

    /**
     * The current XML namespaces in the SCXML document for this action node,
     * preserved for deferred XPath evaluation.
     */
    private Map<String, String> namespaces;

    public Invoke() {
        paramsList = Collections.synchronizedList(new ArrayList<Param>());
    }

    /**
     * Get the identifier for this invoke (may be null).
     *
     * @return Returns the id.
     */
    public final String getId() {
        return id;
    }

    /**
     * Set the identifier for this invoke.
     *
     * @param id The id to set.
     */
    public final void setId(final String id) {
        this.id = id;
    }

    /**
     * Get the type for this &lt;invoke&gt; element.
     *
     * @return String Returns the type.
     */
    public final String getType() {
        return type;
    }

    /**
     * Set the type for this &lt;invoke&gt; element.
     *
     * @param type The type to set.
     */
    public final void setType(final String type) {
        this.type = type;
    }

    /**
     * Get the URL for the external service.
     *
     * @return String The URL.
     */
    public final String getSrc() {
        return src;
    }

    /**
     * Set the URL for the external service.
     *
     * @param src The source URL.
     */
    public final void setSrc(final String src) {
        this.src = src;
    }

    /**
     * Get the expression that evaluates to the source URL for the
     * external service.
     *
     * @return String The source expression.
     */
    public final String getSrcexpr() {
        return srcexpr;
    }

    /**
     * Set the expression that evaluates to the source URL for the
     * external service.
     *
     * @param srcexpr The source expression.
     */
    public final void setSrcexpr(final String srcexpr) {
        this.srcexpr = srcexpr;
    }


    /**
     * @return Returns true if all external events should be forwarded to the invoked process.
     */
    public final boolean isAutoForward() {
        return autoForward != null && autoForward;
    }

    /**
     * @return Returns the flag indicating whether to forward events to the invoked process.
     */
    public final Boolean getAutoForward() {
        return autoForward;
    }

    /**
     * Set the flag indicating whether to forward events to the invoked process.
     *
     * @param autoForward the flag
     */
    public final void setAutoForward(final Boolean autoForward) {
        this.autoForward = autoForward;
    }

    /**
     * Get the list of {@link Param}s.
     *
     * @return List The params list.
     */
    public final List<Param> params() {
        return paramsList;
    }

    /**
     * Add this param to this invoke.
     *
     * @param param The invoke parameter.
     */
    public final void addParam(final Param param) {
        paramsList.add(param);
    }

    /**
     * Get the Finalize for this Invoke.
     *
     * @return Finalize The Finalize for this Invoke.
     */
    public final Finalize getFinalize() {
        return finalize;
    }

    /**
     * Set the Finalize for this Invoke.
     *
     * @param finalize The Finalize for this Invoke.
     */
    public final void setFinalize(final Finalize finalize) {
        this.finalize = finalize;
    }

    /**
     * Get the {@link PathResolver}.
     *
     * @return Returns the pathResolver.
     */
    public PathResolver getPathResolver() {
        return pathResolver;
    }

    /**
     * Set the {@link PathResolver}.
     *
     * @param pathResolver The pathResolver to set.
     */
    public void setPathResolver(final PathResolver pathResolver) {
        this.pathResolver = pathResolver;
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

    /**
     * Enforce identity equality only
     *
     * @param other other object to compare with
     * @return this == other
     */
    @Override
    public final boolean equals(final Object other) {
        return this == other;
    }

    /**
     * Enforce returning identity based hascode
     *
     * @return {@link System#identityHashCode(Object) System.identityHashCode(this)}
     */
    @Override
    public final int hashCode() {
        return System.identityHashCode(this);
    }
}

