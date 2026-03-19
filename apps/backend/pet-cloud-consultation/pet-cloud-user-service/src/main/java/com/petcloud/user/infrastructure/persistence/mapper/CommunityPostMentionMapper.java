package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.CommunityPostMention;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 帖子@提及Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface CommunityPostMentionMapper extends BaseMapper<CommunityPostMention> {

    /**
     * 批量标记已读
     *
     * @param userId 用户ID
     * @return 更新的行数
     */
    @Update("UPDATE community_post_mention SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllAsRead(@Param("userId") Long userId);
}
