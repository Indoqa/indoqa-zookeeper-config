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

import java.util.Map;

import org.springframework.core.env.MapPropertySource;

import com.indoqa.zookeeper.Execution;
import com.indoqa.zookeeper.StateExecutor;

public class ZooKeeperPropertySource extends MapPropertySource {

    public ZooKeeperPropertySource(StateExecutor stateExecutor, String basePath) {
        super(getName(basePath), loadProperties(stateExecutor, basePath));
    }

    public ZooKeeperPropertySource(String connectString, int sessionTimeout, String basePath) {
        super(getName(basePath), loadProperties(connectString, sessionTimeout, basePath));
    }

    private static String getName(String basePath) {
        return "ZooKeeper properties @ " + basePath;
    }

    private static Map<String, Object> loadProperties(StateExecutor stateExecutor, String basePath) {
        Execution execution = stateExecutor.executeState(new ReadConfigurationState(basePath));
        stateExecutor.waitForTermination(execution);

        return execution.getEnvironmentValue("properties");
    }

    private static Map<String, Object> loadProperties(String connectString, int sessionTimeout, String basePath) {
        try (StateExecutor stateExecutor = new StateExecutor(connectString, sessionTimeout)) {
            return loadProperties(stateExecutor, basePath);
        }
    }
}
