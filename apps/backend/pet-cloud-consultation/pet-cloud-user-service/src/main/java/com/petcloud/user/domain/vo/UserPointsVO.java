package com.petcloud.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户积分VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPointsVO {

    /**
     * 总积分
     */
    private Integer total;

    /**
     * 已使用积分
     */
    private Integer used;

    /**
     * 可用积分
     */
    private Integer available;

    /**
     * 用户等级
     */
    private Integer level;
}
