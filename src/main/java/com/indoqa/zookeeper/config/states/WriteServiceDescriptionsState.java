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

import static com.indoqa.zookeeper.config.utils.ReflectionHelper.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.zookeeper.KeeperException;

import com.indoqa.zookeeper.AbstractZooKeeperState;
import com.indoqa.zookeeper.config.model.AbstractServiceDescription;
import com.indoqa.zookeeper.config.utils.ReflectionHelper;

public class WriteServiceDescriptionsState<T extends AbstractServiceDescription> extends AbstractZooKeeperState {

    private Collection<? extends T> serviceDescriptions;

    public WriteServiceDescriptionsState(Collection<? extends T> serviceDescriptions) {
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

        if (isArray(type)) {
            return;
        }

        this.writeObject(path, object);
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
        this.writeValue(targetPath, "");
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
        String serializedValue = ReflectionHelper.getSerializedValue(value);
        this.writeValue(targetPath, serializedValue);
    }

    private void writeValue(String path, String value) throws KeeperException {
        this.setData(path, value.getBytes(UTF_8), -1);
    }
}
