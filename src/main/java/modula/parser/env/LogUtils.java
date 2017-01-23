package modula.parser.env;


import modula.parser.model.Transition;
import modula.parser.model.TransitionTarget;

/**
 * 日志工具类
 */
public final class LogUtils {

    /**
     * transition格式化
     */
    public static String transToString(final TransitionTarget from,
                                       final TransitionTarget to, final Transition transition, String event) {
        StringBuffer buf = new StringBuffer("(");
        buf.append("event = ").append(event);
        buf.append(", cond = ").append(transition.getCond());
        buf.append(", from = ").append(getTTPath(from));
        buf.append(", to = ").append(getTTPath(to));
        buf.append(')');
        return buf.toString();
    }

    /**
     * 转换成xpath格式，/xxx/xxx/xx
     */
    public static String getTTPath(final TransitionTarget tt) {
        StringBuilder sb = new StringBuilder("/");
        for (int i = 0; i < tt.getNumberOfAncestors(); i++) {
            sb.append(tt.getAncestor(i).getId());
            sb.append("/");
        }
        sb.append(tt.getId());
        return sb.toString();
    }

    /**
     * 工具类，不支持实例化
     */
    private LogUtils() {
        super();
    }

}
