/*
 * Licensed to the Indoqa Software Design und Beratung GmbH (Indoqa) under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Indoqa licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.indoqa.zookeeper.config.states;

import org.apache.zookeeper.KeeperException;

import com.indoqa.zookeeper.Execution;
import com.indoqa.zookeeper.config.model.AbstractServiceDescription;

public class ReadServiceDescriptionState<T extends AbstractServiceDescription> extends AbstractReadServiceDescriptonZooKeeperState<T> {

    private static final String SERVICE_DESCRIPTION_KEY = "service-description";

    private final String serviceId;
    private final Class<T> resultType;

    public ReadServiceDescriptionState(String serviceId, Class<T> type) {
        super("Read Service Description " + serviceId);

        this.serviceId = serviceId;
        this.resultType = type;
    }

    @SuppressWarnings("unchecked")
    public static <T extends AbstractServiceDescription> T getServiceDescription(Execution execution) {
        return (T) execution.getEnvironmentValue(SERVICE_DESCRIPTION_KEY);
    }

    @Override
    protected void onStart() throws KeeperException {
        super.onStart();
        this.terminate();

        T serviceDescription = this.readServiceDescription(this.serviceId, this.resultType);
        this.setEnvironmentValue(SERVICE_DESCRIPTION_KEY, serviceDescription);
    }
}
