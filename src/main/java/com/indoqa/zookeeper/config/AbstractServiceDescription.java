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

public class AbstractServiceDescription {

    private String id;
    private String name;
    private String description;
    private Map<String, String> properties;

    public final String getDescription() {
        return this.description;
    }

    public final String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public final Map<String, String> getProperties() {
        return this.properties;
    }

    public final void setDescription(String description) {
        this.description = description;
    }

    public final void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public final void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public final void setProperty(String propertyName, String propertyValue) {
        this.properties.put(propertyName, propertyValue);
    }
}
