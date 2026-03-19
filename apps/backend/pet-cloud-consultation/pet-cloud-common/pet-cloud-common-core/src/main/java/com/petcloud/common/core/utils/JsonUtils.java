package com.petcloud.common.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * JSON 工具类
 *
 * @author luohao
 */
public class JsonUtils {
    private static final ObjectMapper MAPPER = createObjectMapper();

    /**
     * 创建并配置 ObjectMapper
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 注册Java8时间模块（线程安全）
        mapper.registerModule(new JavaTimeModule());

        // 基础配置
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // 使用线程安全的 StdDateFormat 替代 SimpleDateFormat
        mapper.setDateFormat(new StdDateFormat());

        return mapper;
    }

    /**
     * 获取 ObjectMapper 实例
     */
    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    /**
     * 将对象转换为 JSON 字符串
     */
    public static String toString(Object object) {
        return callWith(() -> {
                    try {
                        return MAPPER.writeValueAsString(object);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                },
                "JSON序列化失败 [Object -> String]",
                object);
    }

    /**
     * 反转义 JSON 字符串
     */
    public static String unescape(String json) {
        return toObject(json, String.class);
    }

    /**
     * 转义 JSON 字符串
     */
    public static String escape(String json) {
        return toString(json);
    }

    /**
     * 将对象转换为指定类型
     */
    public static <T> T toObject(Object object, Class<T> type) {
        return callWith(() -> toObject(toString(object), type),
                "JSON序列化失败 [Object -> Class]",
                object);
    }

    /**
     * 将 JSON 字符串转换为指定类型
     */
    public static <T> T toObject(String json, Class<T> type) {
        return callWith(() -> {
                    try {
                        return MAPPER.readValue(json, type);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                },
                "JSON反序列化失败 [String -> Class]",
                json);
    }

    /**
     * 将 JSON 字符串转换为指定类型（支持泛型）
     */
    public static <T> T toObject(String json, TypeReference<T> typeReference) {
        return callWith(() -> {
                    try {
                        return MAPPER.readValue(json, typeReference);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                },
                "JSON反序列化失败 [String -> TypeReference]",
                json);
    }

    /**
     * 将 JSON 字符串转换为指定类型（支持复杂类型）
     */
    public static <T> T toObject(String json, JavaType javaType) {
        return callWith(() -> {
                    try {
                        return MAPPER.readValue(json, javaType);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                },
                "JSON反序列化失败 [String -> JavaType]",
                json);
    }

    /**
     * 将对象转换为列表
     */
    public static <T> List<T> toList(Object obj, Class<T> elementType) {
        return callWith(() -> MAPPER.convertValue(
                        obj,
                        MAPPER.getTypeFactory().constructCollectionType(List.class, elementType)
                ),
                "JSON转换失败 [Object -> List]",
                obj
        );
    }

    /**
     * 将 JSON 字符串转换为列表
     */
    public static <T> List<T> toList(String json, Class<T> elementType) {
        return callWith(() -> {
                    try {
                        return MAPPER.readValue(
                                json,
                                MAPPER.getTypeFactory().constructCollectionType(List.class, elementType)
                        );
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                },
                "JSON反序列化失败 [String -> List]",
                json
        );
    }

    /**
     * 将对象转换为Map
     */
    public static <K, V> Map<K, V> toMap(Object obj, Class<K> keyType, Class<V> valueType) {
        return callWith(() -> MAPPER.convertValue(
                        obj,
                        MAPPER.getTypeFactory().constructMapType(Map.class, keyType, valueType)
                ),
                "JSON转换失败 [Object -> Map]",
                obj
        );
    }

    /**
     * 将 JSON 字符串转换为Map
     */
    public static <K, V> Map<K, V> toMap(String json, Class<K> keyType, Class<V> valueType) {
        return callWith(() -> {
                    try {
                        return MAPPER.readValue(
                                json,
                                MAPPER.getTypeFactory().constructMapType(Map.class, keyType, valueType)
                        );
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                },
                "JSON反序列化失败 [String -> Map]",
                json
        );
    }

    /**
     * 将 JSON 字符串转换为 Map<String, String>
     */
    public static Map<String, String> toMapSs(String json) {
        return toMap(json, String.class, String.class);
    }

    /**
     * 将 JSON 字符串转换为 Map<String, Object>
     */
    public static Map<String, Object> toMapSo(String json) {
        return toMap(json, String.class, Object.class);
    }

    /**
     * 将对象转换为字节数组
     */
    public static byte[] toBytes(Object obj) {
        return callWith(() -> {
                    try {
                        return MAPPER.writeValueAsBytes(obj);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                },
                "JSON序列化失败 [Object -> Bytes]",
                obj
        );
    }

    /**
     * 安全执行操作并处理异常
     */
    private static <T> T callWith(Supplier<T> action, String errorMsg, Object contextObj) {
        if (contextObj == null) {
            return null;
        }

        try {
            return action.get();
        } catch (Exception e) {
            throw new JsonException(errorMsg + " | 输入数据: " + contextObj, e);
        }
    }

    /**
     * JSON 处理异常类
     */
    public static class JsonException extends RuntimeException {
        public JsonException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
