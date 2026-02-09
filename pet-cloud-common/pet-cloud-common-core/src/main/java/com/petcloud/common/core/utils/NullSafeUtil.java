package com.petcloud.common.core.utils;

import java.util.*;

/**
 * 空安全工具类
 *
 * @author luohao
 */
public class NullSafeUtil {
    private NullSafeUtil() {
    }

    public static Optional<String> notNullString(String source) {
        return source != null &&
                !source.isBlank() &&
                !"null".equalsIgnoreCase(source) ?
                Optional.of(source) : Optional.empty();
    }

    public static Optional<String> string(String source) {
        return source != null && !source.isBlank() ?
                Optional.of(source) : Optional.empty();
    }

    public static <T> Optional<T> ofNullable(T source) {
        return Optional.ofNullable(source);
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] array(T[] source) {
        return source != null ? source : (T[]) new Object[0];
    }

    public static <T> Collection<T> collection(Collection<T> source) {
        return source != null ? source : Collections.emptyList();
    }

    public static <T> List<T> list(List<T> source) {
        return source != null ? source : Collections.emptyList();
    }

    public static <T> List<T> toList(List<T> source) {
        return source != null ? new ArrayList<>(source) : new ArrayList<>();
    }

    public static <T> List<T> toList(Collection<T> source) {
        return source != null ?
                (source instanceof List ? (List<T>) source : new ArrayList<>(source)) :
                new ArrayList<>();
    }

    public static <T> Set<T> set(Set<T> source) {
        return source != null ? source : Collections.emptySet();
    }

    public static <T> Set<T> toHashSet(Set<T> source) {
        return source != null ? new HashSet<>(source) : new HashSet<>();
    }

    public static <K, V> Map<K, V> map(Map<K, V> source) {
        return source != null ? source : Collections.emptyMap();
    }

    public static <K, V> Map<K, V> toHashMap(Map<K, V> source) {
        return source != null ? new HashMap<>(source) : new HashMap<>();
    }
}
