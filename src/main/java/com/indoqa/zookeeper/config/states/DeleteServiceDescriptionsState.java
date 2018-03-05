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

import java.util.List;

import org.apache.zookeeper.KeeperException;

import com.indoqa.zookeeper.AbstractZooKeeperState;

public class DeleteServiceDescriptionsState extends AbstractZooKeeperState {

    public DeleteServiceDescriptionsState() {
        super("Delete Service Descriptions");
    }

    @Override
    protected void onStart() throws KeeperException {
        super.onStart();
        this.terminate();

        this.ensureNodeExists("/");

        List<String> children = this.getChildren("/");
        for (String eachChild : children) {
            this.logger.info("Deleting service description '{}' ...", eachChild);

            try {
                this.deleteNodeStructure(combinePath("/", eachChild));
            } catch (KeeperException.BadArgumentsException e) {
                this.logger.error("Caught '{}'. Did you try to delete the zookeeper node?", e.getMessage(), e);
                break;
            }
        }
    }
}