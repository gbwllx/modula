package modula.core.semantics;

/**
 * 错误定义
 */
public class ErrorConstants {

    /**
     * initial state缺失
     *
     * @see modula.core.model.Modula#getInitialTransition()
     * @see modula.core.model.State#getInitial()
     */
    public static final String NO_INITIAL = "NO_INITIAL";

    /**
     * 混合状态的初始状态非法，没有指向混合状态的后代
     */
    public static final String ILLEGAL_INITIAL = "ILLEGAL_INITIAL";

    /**
     * 不支持的action
     * 支持的action列表：assign, cancel, elseif, else, if, log, send, var
     */
    public static final String UNKNOWN_ACTION = "UNKNOWN_ACTION";

    /**
     * 非法的状态机配置
     */
    public static final String ILLEGAL_CONFIG = "ILLEGAL_CONFIG";

    /**
     * 没有定义
     */
    public static final String UNDEFINED_VARIABLE = "UNDEFINED_VARIABLE";

    /**
     * An expression language error.
     */
    public static final String EXPRESSION_ERROR = "EXPRESSION_ERROR";

    //---------------------------------------------- STATIC CONSTANTS ONLY

    /**
     * Discourage instantiation.
     */
    private ErrorConstants() {
        super(); // humor checkstyle
    }

}
