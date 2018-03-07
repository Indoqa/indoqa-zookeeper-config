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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoNodeException;

import com.indoqa.zookeeper.AbstractZooKeeperState;
import com.indoqa.zookeeper.config.model.AbstractServiceDescription;
import com.indoqa.zookeeper.config.utils.ReflectionHelper;

public abstract class AbstractReadServiceDescriptonZooKeeperState<T extends AbstractServiceDescription>
        extends AbstractZooKeeperState {

    protected AbstractReadServiceDescriptonZooKeeperState(String name) {
        super(name);
    }

    @SuppressWarnings("unchecked")
    protected T readServiceDescription(String serviceId, Class<T> resultType) throws KeeperException {
        this.logger.info("Reading service description '{}' ...", serviceId);

        String path = combinePath("/", serviceId);
        if (!this.exists(path)) {
            return null;
        }

        return (T) this.read(path, resultType);
    }

    private Object read(String path, Type type) throws KeeperException {
        if (ReflectionHelper.isSimpleType(type)) {
            return this.readSimpleValue(path, type);
        }

        if (ReflectionHelper.isMap(type)) {
            return this.readMap(path, type);
        }

        if (ReflectionHelper.isCollection(type)) {
            return this.readCollection(path, type);
        }

        if (ReflectionHelper.isArray(type)) {
            return this.readArray(path, type);
        }

        return this.readObject(path, type);
    }

    private Object readArray(String path, Type type) throws KeeperException {
        Class<?> valueType = ((Class<?>) type).getComponentType();

        List<String> children = this.getChildren(path);
        Object result = Array.newInstance(valueType, children.size());

        int index = 0;
        for (String eachChild : children) {
            Object childValue = this.read(combinePath(path, eachChild), valueType);
            Array.set(result, index, childValue);
            index++;
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private Object readCollection(String path, Type type) throws KeeperException {
        Collection<Object> result = (Collection<Object>) ReflectionHelper.createInstance(type);

        Type valueType = ((ParameterizedType) type).getActualTypeArguments()[0];

        List<String> children = this.getChildren(path);
        for (String eachChild : children) {
            Object childValue = this.read(combinePath(path, eachChild), valueType);
            result.add(childValue);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private Object readMap(String path, Type type) throws KeeperException {
        Map<String, Object> result = (Map<String, Object>) ReflectionHelper.createInstance(type);

        Type valueType = ((ParameterizedType) type).getActualTypeArguments()[1];

        List<String> children = this.getChildren(path);
        for (String eachChild : children) {
            Object childValue = this.read(combinePath(path, eachChild), valueType);
            result.put(eachChild, childValue);
        }

        return result;
    }

    private Object readObject(String path, Type type) throws KeeperException {
        this.logger.debug("ReadObject {} {}", path, type.getTypeName());
        Object result = ReflectionHelper.createInstance(type);

        Class<?> currentClass = result.getClass();
        while (currentClass != null) {
            for (Field eachDeclaredField : currentClass.getDeclaredFields()) {
                String valuePath = combinePath(path, eachDeclaredField.getName());
                Object value = this.read(valuePath, eachDeclaredField.getGenericType());
                ReflectionHelper.setFieldValue(eachDeclaredField, result, value);
            }

            currentClass = currentClass.getSuperclass();
        }

        return result;
    }

    private Object readSimpleValue(String path, Type type) throws KeeperException {
        String serializedvalue = this.readValue(path);
        return ReflectionHelper.getDeserializedValue(serializedvalue, type);
    }

    private String readValue(String path) throws KeeperException {
        try {
            byte[] data = this.getData(path, null);
            if (data == null || data.length == 0) {
                return "";
            }

            return new String(data, UTF_8);
        } catch (NoNodeException e) {
            this.logger.error("Could not find node '{}'.", path, e);
            return null;
        }
    }
}
