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

import java.io.IOException;
import java.time.Instant;
import java.util.*;

import org.apache.curator.test.TestingCluster;
import org.apache.zookeeper.KeeperException;
import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.indoqa.zookeeper.AbstractZooKeeperState;
import com.indoqa.zookeeper.Execution;
import com.indoqa.zookeeper.StateExecutor;
import com.indoqa.zookeeper.config.ServiceDescription.Setting;
import com.indoqa.zookeeper.config.model.AbstractServiceDescription;
import com.indoqa.zookeeper.config.model.ServiceInstance;
import com.indoqa.zookeeper.config.states.ReadServiceDescriptionState;
import com.indoqa.zookeeper.config.states.WriteServiceDescriptionsState;

public class ServiceDescriptionStateTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDescriptionStateTest.class);
    private static final int CONNECT_TIMEOUT = 30000;

    private static TestingCluster testingCluster;

    @AfterClass
    public static void afterClass() throws IOException {
        LOGGER.info("Stopping test cluster");
        testingCluster.stop();
        wait(1000);
        testingCluster.close();
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        LOGGER.info("Starting test cluster");
        testingCluster = new TestingCluster(3);
        testingCluster.start();
        wait(1000);
    }

    private static void wait(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            LOGGER.error("Failed to wait", e);
        }
    }

    @Before
    public void before() {
        try (StateExecutor stateExecutor = new StateExecutor(testingCluster.getConnectString(), CONNECT_TIMEOUT)) {
            Execution execution = stateExecutor.executeState(new DeleteAllZooKeeperState());
            stateExecutor.waitForTermination(execution);
        }
    }

    @Test
    public void test() {
        ServiceDescription serviceDescription = this.createServiceDescription();

        try (StateExecutor stateExecutor = new StateExecutor(testingCluster.getConnectString(), CONNECT_TIMEOUT)) {
            Execution execution = stateExecutor.executeState(new WriteServiceDescriptionsState<>(Arrays.asList(serviceDescription)));
            stateExecutor.waitForTermination(execution);

            execution = stateExecutor
                .executeState(new ReadServiceDescriptionState<>(serviceDescription.getId(), ServiceDescription.class));
            stateExecutor.waitForTermination(execution);

            AbstractServiceDescription readServiceDescription = ReadServiceDescriptionState.getServiceDescription(execution);

            Assertions.assertThat(serviceDescription).isEqualToComparingFieldByFieldRecursively(readServiceDescription);
        }
    }

    @Test
    public void testWrongId() {
        ServiceDescription serviceDescription = this.createServiceDescription();

        try (StateExecutor stateExecutor = new StateExecutor(testingCluster.getConnectString(), CONNECT_TIMEOUT)) {
            Execution execution = stateExecutor.executeState(new WriteServiceDescriptionsState<>(Arrays.asList(serviceDescription)));
            stateExecutor.waitForTermination(execution);

            execution = stateExecutor.executeState(new ReadServiceDescriptionState<>("unknown-id", ServiceDescription.class));
            stateExecutor.waitForTermination(execution);

            ServiceDescription readServiceDescription = ReadServiceDescriptionState.getServiceDescription(execution);
            Assertions.assertThat(readServiceDescription).isNull();
        }
    }

    private ServiceDescription createServiceDescription() {
        ServiceDescription serviceDescription = new ServiceDescription();

        serviceDescription.setId("id");
        serviceDescription.setDescription("description");
        serviceDescription.setLastAccess(new Date());
        serviceDescription.setName("name");
        serviceDescription.setTotalCount(12);
        serviceDescription.setType("type");
        serviceDescription.setUrl("url");
        serviceDescription.setFolds(new boolean[] {true, true, false, true});
        serviceDescription.setSettings(new Setting[] {Setting.create("setting-a", 3), Setting.create("setting-b", -4)});

        Set<String> dependencies = new HashSet<>();
        dependencies.add("dependency-1");
        dependencies.add("dependency-2");
        serviceDescription.setDependencies(dependencies);

        Map<String, String> links = new HashMap<>();
        links.put("link-1", "url-1");
        links.put("link-2", "url-2");
        serviceDescription.setLinks(links);

        ServiceInstance instance = new ServiceInstance();
        instance.setName("instance-1");
        instance.setSession("session-1", Instant.now());
        serviceDescription.addInstance(instance);

        Map<String, String> properties = new HashMap<>();
        properties.put("property-1", "value-1");
        properties.put("property-2", "value-2");
        properties.put("property-3", "value-3");
        serviceDescription.setProperties(properties);

        return serviceDescription;
    }

    private static class DeleteAllZooKeeperState extends AbstractZooKeeperState {

        public DeleteAllZooKeeperState() {
            super("Delete All");
        }

        @Override
        protected void onStart() throws KeeperException {
            super.onStart();
            this.terminate();

            List<String> children = this.getChildren("/");
            for (String eachChild : children) {
                if (eachChild.equals("zookeeper")) {
                    continue;
                }

                String childPath = combinePath(eachChild);
                this.logger.info("Deleting '{}'", childPath);
                this.deleteNodeStructure(childPath);
            }
        }
    }
}
