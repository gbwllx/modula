package modula.workflow.executor.context;

/**
 * @description:
 * @author: gubing.gb
 * @date: 2016/12/30.
 */
public class WorkflowContext {
    private String key;
    private String currentState;

    public WorkflowContext(String key) {
        this(key, null);
    }

    public WorkflowContext(String key, String currentState) {
        this.key = key;
        this.currentState = currentState;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }
}
