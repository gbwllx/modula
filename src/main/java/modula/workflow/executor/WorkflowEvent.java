package modula.workflow.executor;

/**
 * @description:
 * @author: gubing.gb
 * @date: 2017/1/19.
 */
public class WorkflowEvent {
    /**
     * 事件id
     */
    private String event;
    /**
     * 事件payload
     */
    private Object payload;

    public WorkflowEvent(String event) {
        this(event, null);
    }

    public WorkflowEvent(String event, Object payload) {
        if (event == null) {
            throw new IllegalArgumentException("null source");
        }
        this.event = event;
        this.payload = payload;
    }

    public String getEvent() {
        return event;
    }

    public Object getPayload() {
        return payload;
    }
}
