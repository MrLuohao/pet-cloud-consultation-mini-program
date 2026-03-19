package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.CommunityPostCollect;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 帖子收藏Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface CommunityPostCollectMapper extends BaseMapper<CommunityPostCollect> {

    /**
     * 物理删除收藏记录（绕过逻辑删除）
     *
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 删除的行数
     */
    @Delete("DELETE FROM community_post_collect WHERE post_id = #{postId} AND user_id = #{userId}")
    int physicalDelete(@Param("postId") Long postId, @Param("userId") Long userId);
}
