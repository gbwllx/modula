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

import modula.executor.core.semantics.ErrorConstants;
import modula.parser.env.LogUtils;
import modula.parser.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * errorReporter一般实现
 */
public class SimpleErrorReporter implements ErrorReporter, Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    private Log log = LogFactory.getLog(getClass());

    public SimpleErrorReporter() {
        super();
    }

    /**
     * @see ErrorReporter#onError(String, String, Object)
     */
    @SuppressWarnings("unchecked")
    public void onError(final String errorCode, final String errDetail,
                        final Object errCtx) {
        //Note: the if-then-else below is based on the actual usage
        // (codebase search), it has to be kept up-to-date as the code changes
        String errCode = errorCode.intern();
        StringBuffer msg = new StringBuffer();
        msg.append(errCode).append(" (");
        msg.append(errDetail).append("): ");
        if (errCode == ErrorConstants.NO_INITIAL) {
            if (errCtx instanceof Modula) {
                //determineInitialStates
                msg.append("<Modula>");
            } else if (errCtx instanceof State) {
                //determineInitialStates
                //determineTargetStates
                msg.append("State " + LogUtils.getTTPath((State) errCtx));
            }
        } else if (errCode == ErrorConstants.UNKNOWN_ACTION) {
            //executeActionList
            msg.append("Action: " + errCtx.getClass().getName());
        } else if (errCode == ErrorConstants.ILLEGAL_CONFIG) {
            //isLegalConfig
            if (errCtx instanceof Map.Entry) { //unchecked cast below
                Map.Entry<EnterableState, Set<EnterableState>> badConfigMap =
                        (Map.Entry<EnterableState, Set<EnterableState>>) errCtx;
                EnterableState es = badConfigMap.getKey();
                Set<EnterableState> vals = badConfigMap.getValue();
                msg.append(LogUtils.getTTPath(es) + " : [");
                for (Iterator<EnterableState> i = vals.iterator(); i.hasNext(); ) {
                    EnterableState ex = i.next();
                    msg.append(LogUtils.getTTPath(ex));
                    if (i.hasNext()) { // reason for iterator usage
                        msg.append(", ");
                    }
                }
                msg.append(']');
            } else if (errCtx instanceof Set) { //unchecked cast below
                Set<EnterableState> vals = (Set<EnterableState>) errCtx;
                msg.append("<Modula> : [");
                for (Iterator<EnterableState> i = vals.iterator(); i.hasNext(); ) {
                    EnterableState ex = i.next();
                    msg.append(LogUtils.getTTPath(ex));
                    if (i.hasNext()) {
                        msg.append(", ");
                    }
                }
                msg.append(']');
            }
        } else if (errCode == ErrorConstants.EXPRESSION_ERROR) {
            if (errCtx instanceof Executable) {
                TransitionTarget parent = ((Executable) errCtx).getParent();
                msg.append("Expression error inside " + LogUtils.getTTPath(parent));
            } else if (errCtx instanceof Modula) {
                // Global Script
                msg.append("Expression error inside the global script");
            }
        }
        handleErrorMessage(errorCode, errDetail, errCtx, msg);
    }

    /**
     * 错误处理函数{@link #onError(String, String, Object)} onError}.
     * 默认只记录warning日志
     */
    protected void handleErrorMessage(final String errorCode, final String errDetail,
                                      final Object errCtx, final CharSequence errorMessage) {

        if (log.isWarnEnabled()) {
            log.warn(errorMessage.toString());
        }
    }
}

