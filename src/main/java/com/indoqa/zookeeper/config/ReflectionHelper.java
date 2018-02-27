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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public final class ReflectionHelper {

    private static final Set<String> SIMPLE_TYPE_NAMES = new HashSet<>();
    static {
        SIMPLE_TYPE_NAMES.add(String.class.getName());

        SIMPLE_TYPE_NAMES.add(boolean.class.getName());
        SIMPLE_TYPE_NAMES.add(char.class.getName());
        SIMPLE_TYPE_NAMES.add(short.class.getName());
        SIMPLE_TYPE_NAMES.add(int.class.getName());
        SIMPLE_TYPE_NAMES.add(long.class.getName());
        SIMPLE_TYPE_NAMES.add(float.class.getName());
        SIMPLE_TYPE_NAMES.add(double.class.getName());

        SIMPLE_TYPE_NAMES.add(Boolean.class.getName());
        SIMPLE_TYPE_NAMES.add(Character.class.getName());
        SIMPLE_TYPE_NAMES.add(Short.class.getName());
        SIMPLE_TYPE_NAMES.add(Integer.class.getName());
        SIMPLE_TYPE_NAMES.add(Long.class.getName());
        SIMPLE_TYPE_NAMES.add(Float.class.getName());
        SIMPLE_TYPE_NAMES.add(Double.class.getName());

        SIMPLE_TYPE_NAMES.add(Date.class.getName());
    }

    private ReflectionHelper() {
        // hide utility class constructor
    }

    public static Type getFieldType(Object object, String fieldName) {
        try {
            return object.getClass().getDeclaredField(fieldName).getGenericType();
        } catch (Exception e) {
            throw new RuntimeException("Could not find field '" + fieldName + "' on " + object, e);
        }
    }

    public static Object getFieldValue(Field field, Object target) {
        try {
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeException("Could not retrieve value of field " + field, e);
        } finally {
            field.setAccessible(false);
        }
    }

    public static boolean isCompatibleCollection(Type type) {
        ParameterizedType parameterizedType = (ParameterizedType) type;

        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (actualTypeArguments.length != 1) {
            throw new IllegalArgumentException("Expected 1 generic parameter, but found " + actualTypeArguments.length);
        }

        return isSimpleType(actualTypeArguments[0].getTypeName());
    }

    public static boolean isCompatibleMap(Type type) {
        if (!(type instanceof ParameterizedType)) {
            throw new IllegalArgumentException("Type of map should be a ParameterizedType.");
        }

        ParameterizedType parameterizedType = (ParameterizedType) type;

        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (actualTypeArguments.length != 2) {
            throw new IllegalArgumentException("Expected 2 generic parameters, but found " + actualTypeArguments.length);
        }

        return actualTypeArguments[0].getTypeName().equals(String.class.getName());
    }

    public static boolean isSimpleType(String typeName) {
        return SIMPLE_TYPE_NAMES.contains(typeName);
    }

    public static boolean isSimpleType(Type type) {
        return isSimpleType(type.getTypeName());
    }
}
