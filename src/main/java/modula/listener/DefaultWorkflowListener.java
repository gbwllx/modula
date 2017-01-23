package modula.listener;

import modula.parser.model.Transition;
import modula.parser.env.LogUtils;
import modula.parser.model.EnterableState;
import modula.parser.model.TransitionTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @description:
 * @author: gubing.gb
 * @date: 2017/1/18.
 */
public class DefaultWorkflowListener implements ModulaListener {
    private static final Logger logger = LoggerFactory.getLogger(DefaultWorkflowListener.class);

    /**
     * id = "modula" 或者 "state的id", value = 相应的事件监听器
     */
    private String id = "modula";

    public void onEntry(EnterableState state) {
        logger.info("enter {}", LogUtils.getTTPath(state));
    }

    public void onExit(EnterableState state) {
        logger.info("exit {}", LogUtils.getTTPath(state));
    }

    public void onTransition(TransitionTarget from, TransitionTarget to, Transition transition, String event) {
        logger.info("transition {}", LogUtils.transToString(from, to, transition, event));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
