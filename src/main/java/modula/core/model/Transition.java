/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package modula.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

/**
 * transition元素
 * Transition rules 由events触发; 受guard-conditions限制
 */
public class Transition extends SimpleTransition implements DocumentOrder {

    /**
     * 文档顺序
     */
    private int order;

    /**
     * 触发条件
     */
    private String event;

    /**
     * 事件列表
     */
    private List<String> events = Collections.emptyList();

    /**
     * 不匹配任何事件
     */
    private boolean noEvents;

    /**
     * 匹配所有事件
     */
    private boolean allEvents;

    /**
     * guard condition. 可选
     */
    private String cond;


    public Transition() {
        super();
    }


    /**
     * @return the document order of this transition
     * @see DocumentOrder
     */
    @Override
    public final int getOrder() {
        return order;
    }

    /**
     * Sets the document order of this transition
     *
     * @param order the document order
     * @see DocumentOrder
     */
    public final void setOrder(int order) {
        this.order = order;
    }

    /**
     * Get the guard condition (may be null).
     *
     * @return Returns the cond.
     */
    public String getCond() {
        return cond;
    }

    /**
     * Set the guard condition.
     *
     * @param cond The cond to set.
     */
    public void setCond(final String cond) {
        this.cond = cond;
    }

    /**
     * Get the event that will trigger this transition (pending
     * evaluation of the guard condition in favor).
     *
     * @return Returns the event.
     */
    public String getEvent() {
        return event;
    }

    /** TODO
     * Set the event that will trigger this transition (pending
     * evaluation of the guard condition in favor).
     *
     * @param event The event to set.
     */
    public void setEvent(final String event) {
        this.event = event == null ? null : event.trim();
        if (this.event != null) {
            // 'event' is a space separated list of event descriptors
            events = new ArrayList<String>();
            StringTokenizer st = new StringTokenizer(this.event);
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                if (token.equals("*")) {
                    events.clear();
                    events.add(token);
                    break;
                } else {
                    if (token.endsWith("*")) {
                        token = token.substring(0, token.length() - 1);
                    }
                    if (token.endsWith(".")) {
                        token = token.substring(0, token.length() - 1);
                    }
                    if (token.length() > 0) {
                        events.add(token);
                    }
                }
            }
        } else {
            events = Collections.emptyList();
        }
        noEvents = events.isEmpty();
        allEvents = !noEvents && events.get(0).equals("*");
    }

    /**
     * @return The list of this transition event descriptors
     */
    public final List<String> getEvents() {
        return events;
    }

    /**
     * @return True if this transition is event-less
     */
    public final boolean isNoEventsTransition() {
        return noEvents;
    }

    /**
     * @return True if this transition matches any events (*)
     */
    public final boolean isAllEventsTransition() {
        return allEvents;
    }
}
