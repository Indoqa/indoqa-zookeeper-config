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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.zookeeper.KeeperException;

import com.indoqa.zookeeper.Execution;
import com.indoqa.zookeeper.config.model.AbstractServiceDescription;

public class ReadServiceDescriptionsState<T extends AbstractServiceDescription>
        extends AbstractReadServiceDescriptonZooKeeperState<T> {

    private static final String SERVICE_DESCRIPTIONS_KEY = "service-descriptions";

    private final Class<T> resultType;

    public ReadServiceDescriptionsState(Class<T> type) {
        super("Read Service Descriptions");

        this.resultType = type;
    }

    public static <T extends AbstractServiceDescription> Collection<T> getServiceDescriptions(Execution execution) {
        return execution.getEnvironmentValues(SERVICE_DESCRIPTIONS_KEY);
    }

    @Override
    protected void onStart() throws KeeperException {
        // terminate this execution as soon as this state is completed
        this.terminate();

        List<T> serviceDescriptions = new ArrayList<>();
        this.setEnvironmentValues(SERVICE_DESCRIPTIONS_KEY, serviceDescriptions);

        for (String eachServiceId : this.getChildren("/")) {
            T serviceDescription = this.readServiceDescription(eachServiceId, this.resultType);

            if (serviceDescription != null) {
                serviceDescriptions.add(serviceDescription);
            }
        }
    }
}
