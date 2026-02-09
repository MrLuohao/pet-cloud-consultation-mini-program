package com.petcloud.common.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Bean工具类 - 支持多种属性拷贝模式
 *
 * @author luohao
 */
public class BeanUtils {

    private static final Map<String, List<Field>> FIELD_CACHE = new HashMap<>();

    /**
     * 将源对象属性值拷贝到目标对象，相同属性名赋值后，其他属性置空
     */
    public static void copyPropertiesWithNullify(Object source, Object target, boolean ignoreNull) {
        if (source == null || target == null) {
            return;
        }

        List<Field> sourceFields = getFields(source.getClass());
        List<Field> targetFields = getFields(target.getClass());

        Map<String, Field> targetFieldMap = new HashMap<>();
        for (Field field : targetFields) {
            field.setAccessible(true);
            targetFieldMap.put(field.getName(), field);
        }

        // 第一步：拷贝匹配属性
        for (Field sourceField : sourceFields) {
            sourceField.setAccessible(true);
            Field targetField = targetFieldMap.get(sourceField.getName());
            if (targetField != null && isAssignable(sourceField, targetField)) {
                try {
                    Object value = sourceField.get(source);
                    if (!ignoreNull || value != null) {
                        targetField.set(target, value);
                    }
                } catch (IllegalAccessException e) {
                    // 静默处理
                }
            }
        }

        // 第二步：将未匹配属性置空
        for (Field targetField : targetFields) {
            targetField.setAccessible(true);
            boolean found = sourceFields.stream().anyMatch(
                    f -> f.getName().equals(targetField.getName())
            );
            if (!found) {
                try {
                    targetField.set(target, null);
                } catch (IllegalAccessException e) {
                    // 静默处理
                }
            }
        }
    }

    /**
     * 保留模式拷贝：只覆盖相同属性名的值，保持目标对象其他属性不变
     */
    public static void copyPropertiesPreserve(Object source, Object target, boolean ignoreNull) {
        if (source == null || target == null) {
            return;
        }

        List<Field> sourceFields = getFields(source.getClass());
        Map<String, Field> targetFieldMap = getFieldMap(target.getClass());

        for (Field sourceField : sourceFields) {
            sourceField.setAccessible(true);
            Field targetField = targetFieldMap.get(sourceField.getName());
            if (targetField != null && isAssignable(sourceField, targetField)) {
                try {
                    Object value = sourceField.get(source);
                    if (!ignoreNull || value != null) {
                        targetField.set(target, value);
                    }
                } catch (IllegalAccessException e) {
                    // 静默处理
                }
            }
        }
    }

    /**
     * 创建新实例拷贝：将源对象属性值拷贝到目标类的新实例
     */
    public static <T> T copyToNewInstance(Object source, Class<T> targetClass) {
        if (source == null || targetClass == null) {
            return null;
        }

        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            copyPropertiesPreserve(source, target, true);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("创建目标对象失败", e);
        }
    }

    // ====== 私有工具方法 ======

    private static List<Field> getFields(Class<?> clazz) {
        String key = clazz.getName();
        if (FIELD_CACHE.containsKey(key)) {
            return FIELD_CACHE.get(key);
        }

        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            Arrays.stream(current.getDeclaredFields())
                    .filter(f -> !Modifier.isStatic(f.getModifiers()))
                    .forEach(fields::add);
            current = current.getSuperclass();
        }

        FIELD_CACHE.put(key, fields);
        return fields;
    }

    private static Map<String, Field> getFieldMap(Class<?> clazz) {
        Map<String, Field> map = new HashMap<>();
        for (Field field : getFields(clazz)) {
            field.setAccessible(true);
            map.put(field.getName(), field);
        }
        return map;
    }

    private static boolean isAssignable(Field sourceField, Field targetField) {
        Class<?> sourceType = sourceField.getType();
        Class<?> targetType = targetField.getType();

        if (sourceType.isPrimitive() || targetType.isPrimitive()) {
            return isPrimitiveCompatible(sourceType, targetType);
        }

        return targetType.isAssignableFrom(sourceType);
    }

    private static boolean isPrimitiveCompatible(Class<?> srcType, Class<?> dstType) {
        if (srcType == int.class || srcType == Integer.class) {
            return dstType == int.class || dstType == long.class ||
                    dstType == double.class || dstType == float.class;
        }
        if (srcType == long.class || srcType == Long.class) {
            return dstType == long.class || dstType == double.class;
        }
        if (srcType == double.class || srcType == Double.class) {
            return dstType == double.class;
        }
        if (srcType == float.class || srcType == Float.class) {
            return dstType == float.class || dstType == double.class;
        }
        return false;
    }
}
