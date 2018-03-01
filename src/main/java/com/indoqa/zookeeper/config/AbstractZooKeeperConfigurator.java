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
package com.indoqa.zookeeper.config;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indoqa.zookeeper.Execution;
import com.indoqa.zookeeper.StateExecutor;
import com.indoqa.zookeeper.config.model.AbstractServiceDescription;
import com.indoqa.zookeeper.config.states.DeleteServiceDescriptionsState;
import com.indoqa.zookeeper.config.states.ReadServiceDescriptionsState;
import com.indoqa.zookeeper.config.states.WriteServiceDescriptionsState;

public abstract class AbstractZooKeeperConfigurator<T extends AbstractServiceDescription> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractZooKeeperConfigurator.class);

    private final String connectString;
    private final Class<T> serviceDescriptionType;

    protected AbstractZooKeeperConfigurator(String connectString, Class<T> serviceDescriptionType) {
        this.connectString = connectString;
        this.serviceDescriptionType = serviceDescriptionType;
    }

    protected void deleteServiceDescriptions() {
        LOGGER.info("Deleting service descriptions ...");

        try (StateExecutor stateExecutor = new StateExecutor(this.connectString)) {
            Execution execution = stateExecutor.executeState(new DeleteServiceDescriptionsState());
            stateExecutor.waitForTermination(execution);
        }
    }

    protected Collection<T> readServiceDescriptions() {
        LOGGER.info("Reading service descriptions ...");

        try (StateExecutor stateExecutor = new StateExecutor(this.connectString)) {
            Execution execution = stateExecutor.executeState(new ReadServiceDescriptionsState<>(this.serviceDescriptionType));
            stateExecutor.waitForTermination(execution);

            return ReadServiceDescriptionsState.getServiceDescriptions(execution);
        }
    }

    protected void writeServiceDescriptions(Collection<? extends T> serviceDescriptions) {
        LOGGER.info("Writing service descriptions ...");

        try (StateExecutor stateExecutor = new StateExecutor(this.connectString)) {
            Execution execution = stateExecutor.executeState(new WriteServiceDescriptionsState<>(serviceDescriptions));
            stateExecutor.waitForTermination(execution);
        }
    }
}
