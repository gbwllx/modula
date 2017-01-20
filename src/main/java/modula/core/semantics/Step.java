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
package modula.core.semantics;


import modula.core.TriggerEvent;
import modula.core.model.EnterableState;
import modula.core.model.SimpleTransition;
import modula.core.model.TransitionalState;

import java.util.*;

/**
 * 执行单元
 */
public class Step {

    /**
     * 事件
     */
    private TriggerEvent event;

    /**
     * 出口State
     */
    private Set<EnterableState> exitSet;

    /**
     * 入口State
     */
    private Set<EnterableState> entrySet;

    /**
     * 默认入口State
     */
    private Set<EnterableState> defaultEntrySet;

    private Map<TransitionalState, SimpleTransition> defaultHistoryTransitionEntryMap;
    /**
     * 转移集合
     */
    private List<SimpleTransition> transitList;

    /**
     * @param event 收到事件
     */
    public Step(TriggerEvent event) {
        this.event = event;
        this.exitSet = new HashSet<EnterableState>();
        this.entrySet = new HashSet<EnterableState>();
        this.defaultEntrySet = new HashSet<EnterableState>();
        this.defaultHistoryTransitionEntryMap = new HashMap<TransitionalState, SimpleTransition>();
        this.transitList = new ArrayList<SimpleTransition>();
    }


    public Set<EnterableState> getEntrySet() {
        return entrySet;
    }

    public Set<EnterableState> getDefaultEntrySet() {
        return defaultEntrySet;
    }

    public Map<TransitionalState, SimpleTransition> getDefaultHistoryTransitionEntryMap() {
        return defaultHistoryTransitionEntryMap;
    }

    public Set<EnterableState> getExitSet() {
        return exitSet;
    }

    public TriggerEvent getEvent() {
        return event;
    }

    public List<SimpleTransition> getTransitList() {
        return transitList;
    }
}

