package com.petcloud.user.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 会员开通/续费请求DTO
 */
@Data
public class VipSubscribeDTO {

    /**
     * 套餐ID
     */
    @NotBlank(message = "套餐ID不能为空")
    private String planId;

    /**
     * 支付确认标识（前端模拟支付完成后传 true）
     */
    private Boolean payConfirmed;
}
