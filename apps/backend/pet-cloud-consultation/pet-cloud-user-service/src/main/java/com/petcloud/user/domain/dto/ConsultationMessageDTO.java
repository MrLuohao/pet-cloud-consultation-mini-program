package com.petcloud.user.domain.dto;

import lombok.Data;

/**
 * 咨询消息请求DTO
 *
 * @author luohao
 */
@Data
public class ConsultationMessageDTO {
    private Long consultationId;
    private Integer messageType;
    private String content;
    private String mediaUrl;
}
