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

import java.net.InetAddress;
import java.util.UUID;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NodeExistsException;

import com.indoqa.zookeeper.AbstractZooKeeperState;

public class RegisterInstanceState extends AbstractZooKeeperState {

    private String serviceId;

    public RegisterInstanceState(String serviceId) {
        super("Register Instance for '" + serviceId + "'.");
        this.serviceId = serviceId;
    }

    @Override
    protected void onStart() throws KeeperException {
        String hostName = this.getHostName();
        String sessionName = "0x" + Long.toHexString(this.zooKeeper.getSessionId());
        this.logger.info("Registering instance '{}' @ session '{}' ...", hostName, sessionName);

        String instancesPath = combinePath(this.serviceId, "instances");
        if (!this.exists(instancesPath)) {
            throw new ZooKeeperRegistrationException(
                "The path '" + instancesPath + "' does not exist. Check if ZooKeeper contains the service description '"
                    + this.serviceId + "'.");
        }

        String instancePath = combinePath(instancesPath, hostName);
        this.ensureNodeExists(instancePath);

        String alivePath = combinePath(instancePath, sessionName);
        try {
            this.createNode(alivePath, new byte[0], CreateMode.EPHEMERAL);
        } catch (NodeExistsException e) {
            this.logger.debug("Alive-Node already exists.", e);
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

    public static class ZooKeeperRegistrationException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public ZooKeeperRegistrationException(String message) {
            super(message);
        }
    }
}
