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

import static com.indoqa.zookeeper.config.ReflectionHelper.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.zookeeper.KeeperException;

import com.indoqa.zookeeper.AbstractZooKeeperState;

public class CreateServiceDescriptionsState extends AbstractZooKeeperState {

    private List<? extends AbstractServiceDescription> serviceDescriptions;

    public CreateServiceDescriptionsState(List<? extends AbstractServiceDescription> serviceDescriptions) {
        super("Create Service Descriptions");
        this.serviceDescriptions = serviceDescriptions;
    }

    @Override
    protected void onStart() throws KeeperException {
        this.terminate();
        this.ensureNodeExists("/");

        for (AbstractServiceDescription eachServiceDescription : this.serviceDescriptions) {
            this.create(eachServiceDescription);
        }
    }

    private void create(AbstractServiceDescription serviceDescription) throws KeeperException {
        this.logger.info("Creating service description '{}' ...", serviceDescription.getId());

        String servicePath = combinePath("/", serviceDescription.getId());
        this.write(servicePath, serviceDescription, serviceDescription.getClass());
    }

    @SuppressWarnings("unchecked")
    private void write(String path, Object object, Type type) throws KeeperException {
        this.ensureNodeExists(path);

        if (object == null) {
            this.writeNull(path);
            return;
        }

        if (isSimpleType(type)) {
            this.writeSimpleValue(path, object);
            return;
        }

        if (Map.class.isInstance(object)) {
            this.writeMapValue(path, (Map<String, ?>) object, type);
            return;
        }

        if (Collection.class.isInstance(object)) {
            this.writeCollectionValue(path, (Collection<?>) object, type);
            return;
        }

        this.writeObject(path, object);
    }

    private void writeBooleanValue(String path, Boolean value) throws KeeperException {
        this.writeStringValue(path, value.toString());
    }

    private void writeCollectionValue(String path, Collection<?> value, Type type) throws KeeperException {
        ParameterizedType parameterizedType = (ParameterizedType) type;
        Type valueType = parameterizedType.getActualTypeArguments()[0];

        int index = 0;
        for (Object eachValue : value) {
            String valuePath = combinePath(path, String.valueOf(index));
            this.write(valuePath, eachValue, valueType);

            index++;
        }
    }

    private void writeDateValue(String targetPath, Date value) throws KeeperException {
        this.writeStringValue(targetPath, value.toInstant().toString());
    }

    private void writeMapValue(String path, Map<String, ?> value, Type type) throws KeeperException {
        if (!isCompatibleMap(type)) {
            throw new IllegalArgumentException("Incompatible map type " + type);
        }

        ParameterizedType parameterizedType = (ParameterizedType) type;
        Type valueType = parameterizedType.getActualTypeArguments()[1];

        for (Entry<String, ?> eachEntry : value.entrySet()) {
            String valuePath = combinePath(path, eachEntry.getKey());
            this.write(valuePath, eachEntry.getValue(), valueType);
        }
    }

    private void writeNull(String targetPath) throws KeeperException {
        this.writeStringValue(targetPath, "");
    }

    private void writeNumberValue(String path, Number value) throws KeeperException {
        this.writeStringValue(path, value.toString());
    }

    private void writeObject(String path, Object object) throws KeeperException {
        Class<?> currentClass = object.getClass();
        while (currentClass != null) {
            for (Field eachDeclaredField : currentClass.getDeclaredFields()) {
                String valuePath = combinePath(path, eachDeclaredField.getName());
                Object value = getFieldValue(eachDeclaredField, object);
                this.write(valuePath, value, eachDeclaredField.getGenericType());
            }

            currentClass = currentClass.getSuperclass();
        }
    }

    private void writeSimpleValue(String targetPath, Object value) throws KeeperException {
        if (value == null) {
            this.writeStringValue(targetPath, "");
            return;
        }

        if (value instanceof String) {
            this.writeStringValue(targetPath, (String) value);
            return;
        }

        if (value instanceof Number) {
            this.writeNumberValue(targetPath, (Number) value);
            return;
        }

        if (value instanceof Boolean) {
            this.writeBooleanValue(targetPath, (Boolean) value);
            return;
        }

        if (value instanceof Date) {
            this.writeDateValue(targetPath, (Date) value);
            return;
        }

        throw new IllegalArgumentException("Unhandled value type " + value.getClass());
    }

    private void writeStringValue(String path, String value) throws KeeperException {
        this.setData(path, value.getBytes(UTF_8), -1);
    }
}
