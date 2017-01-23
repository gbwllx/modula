package modula.executor.context;

import modula.executor.event.WorkflowEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @description:
 * @author: gubing.gb
 * @date: 2016/12/30.
 */
public class WorkflowContext {
    private String key;
    private String currentState;
    private List<WorkflowEvent> events;

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

    public List<WorkflowEvent> getEvents() {
        return events;
    }

    public WorkflowContext setEvents(WorkflowEvent... events) {
        if (events != null && events.length > 0){
            this.events = new ArrayList<WorkflowEvent>();
            this.events.addAll(Arrays.asList(events));
        } else {
            this.events = null;
        }
        return this;
    }
}
