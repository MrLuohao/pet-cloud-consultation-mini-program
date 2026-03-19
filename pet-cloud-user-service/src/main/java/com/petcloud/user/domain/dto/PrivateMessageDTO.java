package com.petcloud.user.domain.dto;

import com.petcloud.user.domain.enums.PrivateMessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送私信DTO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivateMessageDTO {

    /**
     * 接收者ID（必填）
     */
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    /**
     * 消息内容（必填）
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 500, message = "消息内容长度不能超过500字符")
    private String content;

    /**
     * 消息类型: text-文本, image-图片, voice-语音
     */
    @Builder.Default
    private String msgType = PrivateMessageType.DEFAULT_CODE;
}
