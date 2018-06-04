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
package com.indoqa.zookeeper.config.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public abstract class AbstractServiceDescription {

    private String id;
    private String name;
    private String description;
    private Map<String, String> properties = new HashMap<>();
    private Map<String, ServiceInstance> instances = new HashMap<>();

    public void addInstance(ServiceInstance instance) {
        this.instances.put(instance.getName(), instance);
    }

    public final String getDescription() {
        return this.description;
    }

    public final String getId() {
        return this.id;
    }

    public Map<String, ServiceInstance> getInstances() {
        return this.instances;
    }

    public final String getName() {
        return this.name;
    }

    public final Map<String, String> getProperties() {
        return this.properties;
    }

    public final Optional<String> getProperty(String propertyName) {
        return Optional.ofNullable(this.properties.get(propertyName));
    }

    public void onRead() {
        for (Entry<String, ServiceInstance> eachEntry : this.instances.entrySet()) {
            eachEntry.getValue().setName(eachEntry.getKey());
        }
    }

    public final void setDescription(String description) {
        this.description = description;
    }

    public final void setId(String id) {
        this.id = id;
    }

    public void setInstances(Map<String, ServiceInstance> instances) {
        this.instances = instances;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public final void setProperty(String propertyName, String propertyValue) {
        this.properties.put(propertyName, propertyValue);
    }
}
