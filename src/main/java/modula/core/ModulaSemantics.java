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

import modula.core.model.ModelException;
import modula.core.model.Modula;

/**
 * 算法语义
 */
public interface ModulaSemantics {

    /**
     * 暂时不用
     */
    Modula normalizeStateMachine(final Modula input, final ErrorReporter errRep);

    /**
     * 状态机第一步
     */
    void firstStep(final ModulaExecutionContext exctx) throws ModelException;

    /**
     * 状态机执行下一步
     */
    void nextStep(final ModulaExecutionContext exctx, final TriggerEvent event) throws ModelException;

    /**
     * 状态机执行最后一步
     */
    void finalStep(final ModulaExecutionContext exctx) throws ModelException;
}
