package com.petcloud.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.petcloud.common.database.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 社区话题实体类
 *
 * @author luohao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("community_topic")
public class CommunityTopic extends BaseEntity {

    @TableField("name")
    private String name;

    @TableField("post_count")
    private Integer postCount;

    @TableField("is_hot")
    private Integer isHot;
}
