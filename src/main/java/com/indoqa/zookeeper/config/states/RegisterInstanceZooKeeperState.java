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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.InetAddress;
import java.time.Instant;
import java.util.UUID;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NodeExistsException;

import com.indoqa.zookeeper.AbstractZooKeeperState;
import com.indoqa.zookeeper.config.utils.ReflectionHelper;
import com.indoqa.zookeeper.config.utils.ZooKeeperRegistrationException;

public class RegisterInstanceZooKeeperState extends AbstractZooKeeperState {

    private final String serviceId;

    public RegisterInstanceZooKeeperState(String serviceId) {
        super("Register Instance for '" + serviceId + "'.");
        this.serviceId = serviceId;
    }

    @Override
    protected void onStart() throws KeeperException {
        super.onStart();

        String hostName = this.getHostName();
        String sessionName = "0x" + Long.toHexString(this.zooKeeper.getSessionId());
        this.logger.info("Registering instance '{}' @ session '{}' ...", hostName, sessionName);

        String instancesPath = combinePath(this.serviceId, "instances");
        if (!this.exists(instancesPath)) {
            throw new ZooKeeperRegistrationException("The path '" + instancesPath
                + "' does not exist. Check if ZooKeeper contains the service description '" + this.serviceId + "'.");
        }

        String instancePath = combinePath(instancesPath, hostName, "sessions");
        this.ensureNodeExists(instancePath);

        try {
            String sessionNode = combinePath(instancePath, sessionName);
            byte[] value = ReflectionHelper.getSerializedValue(Instant.now()).getBytes(UTF_8);
            this.createNode(sessionNode, value, CreateMode.EPHEMERAL);
        } catch (NodeExistsException e) {
            this.logger.debug("Session node already exists.", e);
            // this can happen if we restarted without losing the session -> nothing to do
        }
    }

    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            this.logger.error("Could not determine host name.", e);
            return "UNKNOWN-" + UUID.randomUUID().toString();
        }
    }
}
