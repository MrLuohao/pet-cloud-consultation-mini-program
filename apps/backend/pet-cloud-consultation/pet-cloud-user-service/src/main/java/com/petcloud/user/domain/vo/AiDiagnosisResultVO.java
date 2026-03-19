package com.petcloud.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI诊断结果VO
 * 包含诊断结果和访客剩余次数信息
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDiagnosisResultVO {

    /**
     * 诊断结果
     */
    private String result;

    /**
     * 是否为登录用户
     */
    private Boolean isLoggedIn;

    /**
     * 剩余免费次数（仅访客有效）
     */
    private Integer remainingCount;

    /**
     * 是否已达到限制（仅访客有效）
     */
    private Boolean limitReached;
}
