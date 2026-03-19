package com.petcloud.common.core.exception;

import com.petcloud.common.core.response.IRespType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class BusinessExceptionTest {

    @Test
    void shouldFallbackToBusinessErrorWhenRespTypeIsNull() {
        BusinessException exception = new BusinessException((IRespType) null);

        assertSame(RespType.BUSINESS_ERROR, exception.getRespType());
        assertEquals(RespType.BUSINESS_ERROR.getMessage(), exception.getMessage());
    }

    @Test
    void shouldFormatCustomMessageWithArgs() {
        BusinessException exception = new BusinessException(RespType.PARAMETER_ERROR, "参数缺失: {}", "petId");

        assertSame(RespType.PARAMETER_ERROR, exception.getRespType());
        assertEquals("参数缺失: petId", exception.getMessage());
    }
}
