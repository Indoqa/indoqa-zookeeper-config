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
package com.indoqa.zookeeper.config.utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

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
        SIMPLE_TYPE_NAMES.add(Instant.class.getName());
    }

    private static final Map<String, Supplier<Object>> CREATORS = new HashMap<>();
    static {
        CREATORS.put(List.class.getName(), ArrayList::new);
        CREATORS.put(Set.class.getName(), HashSet::new);
        CREATORS.put(Collection.class.getName(), ArrayList::new);
        CREATORS.put(Map.class.getName(), HashMap::new);
    }

    private static final Map<String, Function<String, ? extends Object>> DESERIALIZERS = new HashMap<>();
    static {
        DESERIALIZERS.put(boolean.class.getName(), Boolean::valueOf);
        DESERIALIZERS.put(char.class.getName(), value -> value.charAt(0));
        DESERIALIZERS.put(short.class.getName(), Short::valueOf);
        DESERIALIZERS.put(int.class.getName(), Integer::valueOf);
        DESERIALIZERS.put(long.class.getName(), Long::valueOf);
        DESERIALIZERS.put(float.class.getName(), Float::valueOf);
        DESERIALIZERS.put(double.class.getName(), Double::valueOf);

        DESERIALIZERS.put(Boolean.class.getName(), Boolean::valueOf);
        DESERIALIZERS.put(Character.class.getName(), value -> value.charAt(0));
        DESERIALIZERS.put(Short.class.getName(), Short::valueOf);
        DESERIALIZERS.put(Integer.class.getName(), Integer::valueOf);
        DESERIALIZERS.put(Long.class.getName(), Long::valueOf);
        DESERIALIZERS.put(Float.class.getName(), Float::valueOf);
        DESERIALIZERS.put(Double.class.getName(), Double::valueOf);

        DESERIALIZERS.put(Date.class.getName(), value -> Date.from(Instant.parse(value)));
        DESERIALIZERS.put(Instant.class.getName(), value -> Instant.parse(value));

        DESERIALIZERS.put(String.class.getName(), Function.identity());
    }

    private ReflectionHelper() {
        // hide utility class constructor
    }

    public static Object createInstance(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return createInstance(parameterizedType.getRawType());
        }

        if (type instanceof Class) {
            Class<?> classType = (Class<?>) type;
            Supplier<Object> supplier = CREATORS.get(classType.getName());
            if (supplier != null) {
                return supplier.get();
            }

            try {
                return classType.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate type " + type, e);
            }
        }

        throw new IllegalArgumentException("Cannot instantiate type " + type);
    }

    public static Object getDeserializedValue(String value, Type type) {
        if (type instanceof ParameterizedType) {
            return getDeserializedValue(value, ((ParameterizedType) type).getRawType());
        }

        if (type instanceof Class) {
            Class<?> classType = (Class<?>) type;

            Function<String, ? extends Object> converter = DESERIALIZERS.get(classType.getName());
            if (converter != null) {
                return converter.apply(value);
            }
        }

        throw new IllegalArgumentException("Cannot convert value '" + value + "' to type " + type);
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

    public static String getSerializedValue(Object value) {
        if (value instanceof String) {
            return (String) value;
        }

        if (value instanceof Date) {
            return getSerializedValue(((Date) value).toInstant());
        }

        return String.valueOf(value);
    }

    public static boolean isArray(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return isArray(parameterizedType.getRawType());
        }

        return type instanceof Class && ((Class<?>) type).isArray();
    }

    public static boolean isCollection(Type type) {
        if (type instanceof ParameterizedType) {
            return isCollection(((ParameterizedType) type).getRawType());
        }

        if (type instanceof Class) {
            return Collection.class.isAssignableFrom((Class<?>) type);
        }

        return false;
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

    public static boolean isList(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return isList(parameterizedType.getRawType());
        }

        return type instanceof Class && List.class.isAssignableFrom((Class<?>) type);
    }

    public static boolean isMap(Type type) {
        if (type instanceof ParameterizedType) {
            return isMap(((ParameterizedType) type).getRawType());
        }

        if (type instanceof Class) {
            return Map.class.isAssignableFrom((Class<?>) type);
        }

        return false;
    }

    public static boolean isSimpleType(String typeName) {
        return SIMPLE_TYPE_NAMES.contains(typeName);
    }

    public static boolean isSimpleType(Type type) {
        return isSimpleType(type.getTypeName());
    }

    public static void setFieldValue(Field field, Object object, Object value) {
        try {
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            throw new RuntimeException("Could not set value of field " + field + " to " + value, e);
        } finally {
            field.setAccessible(false);
        }
    }
}
