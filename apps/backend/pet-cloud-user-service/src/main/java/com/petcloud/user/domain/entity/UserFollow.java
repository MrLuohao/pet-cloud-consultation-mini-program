package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户关注关系实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_follow")
public class UserFollow extends BaseEntity {

    /**
     * 关注者ID
     */
    @TableField("follower_id")
    private Long followerId;

    /**
     * 被关注者ID
     */
    @TableField("following_id")
    private Long followingId;
}
