package com.petcloud.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 可预约时间段VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableSlotVO {

    /**
     * 时间段，如 "09:00-11:00"
     */
    private String slot;

    /**
     * 是否可预约
     */
    private Boolean available;

    /**
     * 剩余可预约数量
     */
    private Integer remaining;
}
