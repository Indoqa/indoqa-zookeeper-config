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

import static org.springframework.util.StringUtils.isEmpty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MutablePropertySources;

import com.indoqa.zookeeper.StateExecutor;
import com.indoqa.zookeeper.config.states.RegisterInstanceZooKeeperState;
import com.indoqa.zookeeper.config.utils.ZooKeeperRegistrationException;

public final class ZooKeeperRegistrationUtils {

    private static final String PROP_ZK_CONNECT_STRING = "zookeeper.connect-string";
    private static final String PROP_ZK_SESSION_TIMEOUT = "zookeeper.session-timeout";
    private static final String DEFAULT_ZK_SESSION_TIMEOUT = "5000";

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperRegistrationUtils.class);

    private ZooKeeperRegistrationUtils() {
        // hide utility class constructor
    }

    /**
     * Register an application's services with ZooKeeper.
     */
    public static void registerZooKeeperServices(ConfigurableApplicationContext applicationContext, String... servicesNames) {
        checkServiceNames(servicesNames);

        String connectString = applicationContext.getEnvironment().getProperty(PROP_ZK_CONNECT_STRING);
        if (isEmpty(connectString)) {
            LOGGER.warn("The property '{}' is not set or empty. The application will NOT register with ZooKeeper.",
                PROP_ZK_CONNECT_STRING);
            return;
        }

        int sessionTimeout = getSessionTimeout(applicationContext);

        StateExecutor stateExecutor = new StateExecutor(connectString, sessionTimeout);
        registerStateExecutor(applicationContext, stateExecutor);

        MutablePropertySources propertySources = applicationContext.getEnvironment().getPropertySources();
        for (String eachServiceName : servicesNames) {
            registerService(stateExecutor, eachServiceName);
            addPropertySource(stateExecutor, propertySources, eachServiceName);
        }
    }

    private static void addPropertySource(StateExecutor stateExecutor, MutablePropertySources propertySources, String serviceName) {
        propertySources.addLast(new ZooKeeperPropertySource(stateExecutor, "/" + serviceName + "/properties"));
    }

    private static void checkServiceNames(String[] servicesNames) {
        if (servicesNames == null || servicesNames.length == 0) {
            throw new ZooKeeperRegistrationException(
                "There was no service name passed that should be used to register the application.");
        }
    }

    private static int getSessionTimeout(ConfigurableApplicationContext applicationContext) {
        try {
            String value = applicationContext.getEnvironment().getProperty(PROP_ZK_SESSION_TIMEOUT);

            if (isEmpty(value)) {
                value = DEFAULT_ZK_SESSION_TIMEOUT;
            }

            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ZooKeeperRegistrationException("Error while parsing the value of property " + PROP_ZK_SESSION_TIMEOUT, e);
        }
    }

    private static void registerService(StateExecutor stateExecutor, String serviceName) {
        stateExecutor.executeState(new RegisterInstanceZooKeeperState(serviceName));
    }

    private static void registerStateExecutor(ConfigurableApplicationContext applicationContext, StateExecutor stateExecutor) {
        applicationContext.getBeanFactory().registerSingleton(stateExecutor.getClass().getName(), stateExecutor);
    }
}
