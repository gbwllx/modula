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
package modula.core;

import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;

/**
 * 发送事件消息或其他消息
 * 1. 通过EventIOProcessor发送给外部系统
 * 2. 直接发给当前session
 */
public interface EventDispatcher {

    /**
     * 取消消息
     */
    void cancel(String sendId);

    /**
     * Send this message to the target.
     *
     * @param sendId        消息ID
     * @param target        An expression returning the target location of the event
     * @param type          接收消息的EventIOProcessor类型
     * @param event         event类型
     * @param params        0以上个变量
     * @param hints         信息
     * @param delay         延迟时间
     * @param externalNodes 外部节点
     */
    void send(String sendId, String target, String type,
              String event, Map<String, Object> params, Object hints,
              long delay, List<Node> externalNodes);

}

