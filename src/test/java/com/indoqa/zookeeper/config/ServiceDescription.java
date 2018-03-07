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

import java.util.*;

import com.indoqa.zookeeper.config.model.AbstractServiceDescription;

public class ServiceDescription extends AbstractServiceDescription {

    private String type;

    private String url;
    private Set<String> dependencies = new TreeSet<>();
    private Map<String, String> links = new HashMap<>();

    private int totalCount;

    private Date lastAccess;

    private boolean[] folds;
    private Setting[] settings;

    public Set<String> getDependencies() {
        return this.dependencies;
    }

    public boolean[] getFolds() {
        return this.folds;
    }

    public Date getLastAccess() {
        return this.lastAccess;
    }

    public Map<String, String> getLinks() {
        return this.links;
    }

    public Setting[] getSettings() {
        return this.settings;
    }

    public int getTotalCount() {
        return this.totalCount;
    }

    public String getType() {
        return this.type;
    }

    public String getUrl() {
        return this.url;
    }

    public void setDependencies(Set<String> dependencies) {
        this.dependencies = dependencies;
    }

    public void setFolds(boolean[] folds) {
        this.folds = folds;
    }

    public void setLastAccess(Date lastAccess) {
        this.lastAccess = lastAccess;
    }

    public void setLinks(Map<String, String> links) {
        this.links = links;
    }

    public void setSettings(Setting[] settings) {
        this.settings = settings;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static class Setting {

        private String name;

        private int value;

        public static Setting create(String name, int value) {
            Setting result = new Setting();

            result.setName(name);
            result.setValue(value);

            return result;
        }

        public String getName() {
            return this.name;
        }

        public int getValue() {
            return this.value;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}
