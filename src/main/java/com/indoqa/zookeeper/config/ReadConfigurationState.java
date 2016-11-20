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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import com.indoqa.zookeeper.AbstractZooKeeperState;

public class ReadConfigurationState extends AbstractZooKeeperState {

    private static final String PLACEHOLDER_START = "${zk:";
    private static final String PLACEHOLDER_END = "}";

    private final String basePath;

    private final Map<String, String> encounteredPlaceholders = new HashMap<>();

    public ReadConfigurationState(String basePath) {
        super("Read properties from " + basePath);

        this.basePath = basePath;
    }

    private static String asString(byte[] data) {
        try {
            return new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Charset 'UTF-8' is not available!", e);
        }
    }

    private static boolean hasData(byte[] data) {
        return data != null && data.length > 0;
    }

    @Override
    protected void onStart() throws KeeperException {
        super.onStart();

        // the execution should stop after we're done
        this.terminate();

        Map<String, Object> properties = this.readProperties();
        this.setEnvironmentValue("properties", properties);
    }

    private String fillPlaceholders(String propertyValue) throws KeeperException {
        int startIndex = propertyValue.indexOf(PLACEHOLDER_START);
        if (startIndex == -1) {
            return propertyValue;
        }

        int endIndex = propertyValue.indexOf(PLACEHOLDER_END, startIndex);
        String path = propertyValue.substring(startIndex + PLACEHOLDER_START.length(), endIndex);

        String value = this.encounteredPlaceholders.get(path);
        if (value == null) {
            value = this.getPropertyValue(path);
            this.encounteredPlaceholders.put(path, value);
        }

        StringBuilder stringBuilder = new StringBuilder(propertyValue);
        stringBuilder.replace(startIndex, endIndex + PLACEHOLDER_END.length(), value);
        return this.fillPlaceholders(stringBuilder.toString());
    }

    private String getPropertyValue(String path) throws KeeperException {
        Stat stat = new Stat();
        byte[] data = this.getData(path, stat);

        if (hasData(data)) {
            return asString(data);
        } else if (stat.getNumChildren() == 0) {
            // if this node is a leaf, but has no data, we'll interpret this as an empty value
            return "";
        }

        return null;
    }

    private String getRelativePath(String path) {
        if (path.startsWith(this.basePath)) {
            return path.substring(this.basePath.length() + 1);
        }

        return path;
    }

    private Map<String, Object> readProperties() throws KeeperException {
        Map<String, Object> result = new HashMap<>();

        if (!this.exists(this.basePath)) {
            this.logger.error("Base path '{}' does not exist.", this.basePath);
            return result;
        }

        this.logger.info("Reading properties at '{}'.", this.basePath);

        // perform a breadth-first search for all properties under the base path
        List<String> paths = new ArrayList<>();
        paths.add(this.basePath);

        while (!paths.isEmpty()) {
            String path = paths.remove(0);

            List<String> children = this.getChildren(path);
            for (String eachChild : children) {
                String childPath = combinePath(path, eachChild);
                paths.add(childPath);

                this.readProperty(childPath, result);
            }
        }

        this.logger.info("Found {} property value(s).", result.size());

        return result;
    }

    private void readProperty(String propertyPath, Map<String, Object> properties) throws KeeperException {
        String propertyName = this.getRelativePath(propertyPath);

        String propertyValue = this.getPropertyValue(propertyPath);
        propertyValue = this.fillPlaceholders(propertyValue);
        properties.put(propertyName, propertyValue);
    }
}
