package com.petcloud.user.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 积分历史VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointsHistoryVO {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 变动积分（正数增加，负数减少）
     */
    private Integer points;

    /**
     * 变动后余额
     */
    private Integer balance;

    /**
     * 类型: 1-任务奖励 2-签到奖励 3-兑换消耗 4-其他
     */
    private Integer type;

    /**
     * 类型描述
     */
    private String typeDesc;

    /**
     * 备注说明
     */
    private String remark;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
