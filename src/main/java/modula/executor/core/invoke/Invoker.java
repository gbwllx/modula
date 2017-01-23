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
package modula.executor.core.invoke;

import modula.parser.ModulaIOProcessor;
import modula.executor.core.event.TriggerEvent;


import java.util.Map;

/**
 * <p> Invoker接口用于定义状态机(executor)和调用活动之间的交互</p>
 * <p> 调用活动需要注册一个Invoker实现类 作为合适的ModulaParentIOProcessor的target</p>
 * <p> 被调用活动需要发送一个“done”事件T
 * <p>
 * <p>Invoker的生命周期如下：
 * <ol>
 * <li>Instantiation via {@link Class#newInstance()}
 * (Invoker implementation requires accessible constructor).</li>
 * <li>Configuration (setters for invoke ID and
 * {@link ModulaIOProcessor}).</li>
 * <li>Initiation of invoked activity via invoke() method, passing
 * the source URI and the map of params.</li>
 * <li>Zero or more bi-directional event triggering.</li>
 * <li>Either completion or cancellation.</li>
 * </ol>
 * </p>
 * <p>
 * <p>invoke()只支持异步，需要同步换用其他实现，例如event</p>
 */
public interface Invoker {

    /**
     * Set the invoke ID provided by the parent state machine executor
     * Implementations must use this ID for constructing the event name for
     * the special "done" event (and optionally, for other event names
     * as well).
     *
     * @param invokeId The invoke ID provided by the parent state machine executor.
     */
    void setInvokeId(String invokeId);

    /**
     * Set I/O Processor of the parent state machine, which provides the
     * channel.
     *
     * @param parentIOProcessor The I/O Processor of the parent state machine.
     */
    void setParentIOProcessor(ModulaIOProcessor parentIOProcessor);


    /**
     * Begin this invocation.
     *
     * @param source The source URI of the activity being invoked.
     * @param params The &lt;param&gt; values
     * @throws InvokerException In case there is a fatal problem with
     *                          invoking the source.
     */
    void invoke(String source, Map<String, Object> params)
            throws InvokerException;

    /**
     * Forwards the event triggered on the parent state machine
     * on to the invoked activity.
     *
     * @param event an external event which triggered during the last
     *              time quantum
     * @throws InvokerException In case there is a fatal problem with
     *                          processing the events forwarded by the
     *                          parent state machine.
     */
    void parentEvent(TriggerEvent event)
            throws InvokerException;

    /**
     * Cancel this invocation.
     *
     * @throws InvokerException In case there is a fatal problem with
     *                          canceling this invoke.
     */
    void cancel()
            throws InvokerException;

}

