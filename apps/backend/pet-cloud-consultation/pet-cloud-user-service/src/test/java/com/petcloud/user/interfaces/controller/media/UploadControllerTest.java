package com.petcloud.user.interfaces.controller.media;

import com.petcloud.common.core.exception.RespType;
import com.petcloud.common.core.response.Response;
import com.petcloud.common.web.utils.FileUploadUtil;
import com.petcloud.common.web.utils.UserContextHolderWeb;
import com.petcloud.user.interfaces.controller.media.UploadController;
import com.petcloud.user.infrastructure.feign.MediaServiceClient;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadControllerTest {

    @Mock
    private FileUploadUtil fileUploadUtil;

    @Mock
    private UserContextHolderWeb userContextHolderWeb;

    @Mock
    private MediaServiceClient mediaServiceClient;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private UploadController uploadController;

    @Test
    void shouldReturnParameterErrorWhenMediaTypeUnsupported() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "note.txt",
                "text/plain",
                "hello".getBytes()
        );
        when(fileUploadUtil.uploadMediaFile(file)).thenThrow(new IllegalArgumentException("不支持的文件类型: text/plain"));

        Response<?> response = uploadController.uploadMedia(file, "diagnosis", request);

        assertFalse(response.isSuccess());
        assertEquals(RespType.PARAMETER_ERROR.getCode(), response.getCode());
        assertEquals("不支持的文件类型: text/plain", response.getMsg());
    }
}
