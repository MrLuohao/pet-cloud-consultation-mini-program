package com.petcloud.map.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petcloud.common.core.exception.BusinessException;
import com.petcloud.map.application.config.AmapProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AmapClientImplTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldWrapChineseAddressRequestFailureAsBusinessException() {
        AmapProperties properties = new AmapProperties();
        properties.setApiKey("test-key");
        properties.setBaseUrl("https://127.0.0.1:9");
        AmapClientImpl client = new AmapClientImpl(properties, objectMapper);

        assertThrows(BusinessException.class, () -> client.geocode("深圳市南山区科技园", null));
    }
}
