package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户积分实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_points")
public class UserPoints extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 总积分
     */
    @TableField("total_points")
    private Integer totalPoints;

    /**
     * 已使用积分
     */
    @TableField("used_points")
    private Integer usedPoints;

    /**
     * 用户等级
     */
    @TableField("level")
    private Integer level;
}
