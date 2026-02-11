package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 积分流水实体类
 *
 * 注意：此表为流水记录表，不支持软删除，因此显式排除 is_deleted 字段
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("points_history")
public class PointsHistory extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 变动积分（正数增加，负数减少）
     */
    @TableField("points")
    private Integer points;

    /**
     * 变动后余额
     */
    @TableField("balance")
    private Integer balance;

    /**
     * 类型: 1-任务奖励 2-签到奖励 3-兑换消耗 4-其他
     */
    @TableField("type")
    private Integer type;

    /**
     * 关联ID（任务ID、订单ID等）
     */
    @TableField("related_id")
    private Long relatedId;

    /**
     * 备注说明
     */
    @TableField("remark")
    private String remark;

    /**
     * 此表为流水记录表，不需要软删除功能
     * 显式设置为 false，MyBatis-Plus 将不会查询此字段
     */
    @TableField(exist = false)
    private Integer isDeleted;

    /**
     * 类型枚举
     */
    public enum Type {
        TASK_REWARD(1, "任务奖励"),
        SIGN_REWARD(2, "签到奖励"),
        EXCHANGE(3, "兑换消耗"),
        OTHER(4, "其他");

        private final Integer code;
        private final String desc;

        Type(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}
