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
package modula.parser.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * namespaceURI,customActionName and {@link Action}的简单组合
 */
public class CustomAction {

    /**
     * 空的命名空间错误
     */
    private static final String ERR_NO_NAMESPACE =
            "Cannot define a custom Modula action with a null or empty namespace";

    /**
     * Modula命名空间
     */
    private static final String NAMESPACE_MODULA =
            "http://localhost/2017/01/modula";

    /**
     * 不能使用保留命名空间NAMESPACE_MODULA错误
     */
    private static final String ERR_RESERVED_NAMESPACE =
            "Cannot define a custom MODULA action within the MODULA namespace '"
                    + NAMESPACE_MODULA + "'";

    /**
     * 空local name错误
     */
    private static final String ERR_NO_LOCAL_NAME =
            "Cannot define a custom MODULA action with a null or empty local name";

    /**
     * 没有继承{@link Action}错误
     */
    private static final String ERR_NOT_AN_ACTION =
            "Custom MODULA action does not extend Action superclass";

    /**
     * 命名空间
     */
    private String namespaceURI;

    /**
     * localName
     */
    private String localName;

    /**
     * custom action实现
     */
    private Class<? extends Action> actionClass;

    /**
     * @param namespaceURI
     * @param localName
     * @param actionClass  customAction实现
     */
    public CustomAction(final String namespaceURI, final String localName,
                        final Class<? extends Action> actionClass) {
        Log log = LogFactory.getLog(CustomAction.class);
        if (namespaceURI == null || namespaceURI.trim().length() == 0) {
            log.error(ERR_NO_NAMESPACE);
            throw new IllegalArgumentException(ERR_NO_NAMESPACE);
        }
        if (namespaceURI.trim().equalsIgnoreCase(NAMESPACE_MODULA)) {
            log.error(ERR_RESERVED_NAMESPACE);
            throw new IllegalArgumentException(ERR_RESERVED_NAMESPACE);
        }
        if (localName == null || localName.trim().length() == 0) {
            log.error(ERR_NO_LOCAL_NAME);
            throw new IllegalArgumentException(ERR_NO_LOCAL_NAME);
        }
        if (actionClass == null || !Action.class.isAssignableFrom(actionClass)) {
            log.error(ERR_NOT_AN_ACTION);
            throw new IllegalArgumentException(ERR_NOT_AN_ACTION);
        }
        this.namespaceURI = namespaceURI;
        this.localName = localName;
        this.actionClass = actionClass;
    }

    /**
     * Get this custom action's implementation.
     *
     * @return Returns the action class.
     */
    public Class<? extends Action> getActionClass() {
        return actionClass;
    }

    /**
     * Get the local name for this custom action.
     *
     * @return Returns the local name.
     */
    public String getLocalName() {
        return localName;
    }

    /**
     * Get the namespace URI for this custom action.
     *
     * @return Returns the namespace URI.
     */
    public String getNamespaceURI() {
        return namespaceURI;
    }
}

