package com.petcloud.user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 举报帖子DTO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostReportDTO {

    /**
     * 举报类型: spam-垃圾信息, abuse-辱骂, inappropriate-不当内容, other-其他
     */
    private String reasonType;

    /**
     * 举报原因（必填）
     */
    @NotBlank(message = "举报原因不能为空")
    @Size(max = 200, message = "举报原因长度不能超过200字符")
    private String reason;
}
