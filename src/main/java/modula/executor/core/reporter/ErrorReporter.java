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
package modula.executor.core.reporter;

/**
 * 上报解析modula文档时发生的错误给宿主环境
 */
public interface ErrorReporter {

    /**
     * 上报错误
     *
     * @param errCode 错误码
     * @param errDetail 错误信息
     * @param errCtx 引起错误的文档位置
     */
    void onError(String errCode, String errDetail, Object errCtx);
}
