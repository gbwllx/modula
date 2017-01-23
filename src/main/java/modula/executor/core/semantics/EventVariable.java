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
package modula.executor.core.semantics;

import java.io.Serializable;

/**
 * 事件相关数据信息
 */
public class EventVariable implements Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    public static final String TYPE_PLATFORM = "platform";
    public static final String TYPE_INTERNAL = "internal";
    public static final String TYPE_EXTERNAL = "external";

    /**
     * name
     */
    private final String name;

    /**
     * type
     */
    private final String type;

    /**
     * sendid
     */
    private final String sendid;

    /**
     * 事件源的URI
     */
    private final String origin;

    /**
     * 事件源类型
     */
    private final String origintype;

    /**
     * invokeId
     */
    private final String invokeid;

    /**
     * 相关数据
     */
    private final Object data;

    public EventVariable(final String name, final String type, final String sendid, final String origin, final String origintype, final String invokeid, final Object data) {
        this.name = name;
        this.type = type;
        this.sendid = sendid;
        this.origin = origin;
        this.origintype = origintype;
        this.invokeid = invokeid;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getSendid() {
        return sendid;
    }

    public String getOrigin() {
        return origin;
    }

    public String getOrigintype() {
        return origintype;
    }

    public String getInvokeid() {
        return invokeid;
    }

    public Object getData() {
        return data;
    }
}

