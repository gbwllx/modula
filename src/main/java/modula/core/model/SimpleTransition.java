package modula.core.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 没有Transition规则的events或guard-conditions
 * {@link History}使用
 */
public class SimpleTransition extends Executable
        implements NamespacePrefixesHolder, Observable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 2L;

    /**
     * observableId Modula状态机中唯一
     */
    private Integer observableId;

    /**
     * 转移类型: internal or external (default)
     *
     * @see #isTypeInternal()
     */
    private TransitionType type;

    /**
     * 转移域
     *
     * @see #getTransitionDomain()
     */
    private TransitionalState transitionDomain;

    /**
     * 内部标记
     */
    private boolean modulaTransitionDomain;

    /**
     * 转移类型
     *
     * @see #isTypeInternal()
     */
    private Boolean typeInternal;

    /**
     * 转移目标。可选属性
     */
    private Set<TransitionTarget> targets;

    /**
     * transition目标ID
     */
    private String next;

    /**
     * 命名空间
     */
    private Map<String, String> namespaces;

    public SimpleTransition() {
        super();
        this.targets = new HashSet<TransitionTarget>();
    }

    //是否混合状态 TODO
    private boolean isCompoundStateParent(TransitionalState ts) {
        return ts != null && ts instanceof State && ((State) ts).isComposite();
    }

    /**
     * {@inheritDoc}
     */
    public final Integer getObservableId() {
        return observableId;
    }

    /**
     * Sets the observableId for this Observable, which must be unique within the SCXML state machine
     *
     * @param observableId the observableId
     */
    public final void setObservableId(Integer observableId) {
        this.observableId = observableId;
    }

    /**
     * Get the TransitionalState (State or Parallel) parent.
     *
     * @return Returns the parent.
     */
    @Override
    public TransitionalState getParent() {
        return (TransitionalState) super.getParent();
    }

    /**
     * Set the TransitionalState (State or Parallel) parent
     * <p>
     * For transitions of Initial or History elements their TransitionalState parent must be set.
     * </p>
     *
     * @param parent The parent to set.
     */
    public final void setParent(final TransitionalState parent) {
        super.setParent(parent);
    }

    /**
     * @return true if Transition type == internal or false if type == external (default)
     */
    public final TransitionType getType() {
        return type;
    }

    /**
     * Sets the Transition type
     *
     * @param type the Transition type
     */
    public final void setType(final TransitionType type) {
        this.type = type;
    }

    /**
     * 返回有效的Transition类型
     * 一、transitionType是internal：
     * 1. 它的{@link #getType()} == {@link TransitionType#internal}
     * 2. 它的源state {@link #getParent()} {@link State#isComposite()}
     * 3. 它的{@link #getTargets()} 是{@link #getParent()}后继
     * <p>
     * 二、否则是{@link TransitionType#external}
     *
     * @return true 如果类型是 {@link TransitionType#internal}
     */
    public final boolean isTypeInternal() {
        if (typeInternal == null) {

            // derive typeInternal
            typeInternal = TransitionType.internal == type && isCompoundStateParent(getParent());

            if (typeInternal && targets.size() > 0) {
                for (TransitionTarget tt : targets) {
                    if (!tt.isDescendantOf(getParent())) {
                        typeInternal = false;
                        break;
                    }
                }
            }
        }
        return typeInternal;
    }

    /**
     * 1. 如果没有目标，或者转移域是Modula文档本身，返回null
     * 2. 如果有目标，以下情况转移域是父混合状态
     *  执行transition可达的所有的state都是该父混合状态的后继
     *  它的后继都没有转移域属性
     * 3. 如果没有这样的父混合状态，那么转移域就是Modula文档本身，返回null
     *
     * @return 转移域
     */
    public TransitionalState getTransitionDomain() {
        TransitionalState ts = transitionDomain;
        if (ts == null && targets.size() > 0 && !modulaTransitionDomain) {

            if (getParent() != null) {
                if (isTypeInternal()) {
                    transitionDomain = getParent();
                } else {
                    // findLCCA
                    for (int i = getParent().getNumberOfAncestors() - 1; i > -1; i--) {
                        if (isCompoundStateParent(getParent().getAncestor(i))) {
                            boolean allDescendants = true;
                            for (TransitionTarget tt : targets) {
                                if (i >= tt.getNumberOfAncestors()) {
                                    i = tt.getNumberOfAncestors();
                                    allDescendants = false;
                                    break;
                                }
                                if (tt.getAncestor(i) != getParent().getAncestor(i)) {
                                    allDescendants = false;
                                    break;
                                }
                            }
                            if (allDescendants) {
                                transitionDomain = getParent().getAncestor(i);
                                break;
                            }
                        }
                    }
                }
            }
            ts = transitionDomain;
            if (ts == null) {
                modulaTransitionDomain = true;
            }
        }
        return ts;
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
     * Get the set of transition targets (may be an empty list).
     *
     * @return Returns the target(s) as specified in SCXML markup.
     * <p>Remarks: Is <code>empty</code> for &quot;stay&quot; transitions.
     * @since 0.7
     */
    public final Set<TransitionTarget> getTargets() {
        return targets;
    }

    /**
     * Get the ID of the transition target (may be null, if, for example,
     * the target is specified inline).
     *
     * @return String Returns the transition target ID
     * @see #getTargets()
     */
    public final String getNext() {
        return next;
    }

    /**
     * Set the transition target by specifying its ID.
     *
     * @param next The the transition target ID
     */
    public final void setNext(final String next) {
        this.next = next;
    }
}
