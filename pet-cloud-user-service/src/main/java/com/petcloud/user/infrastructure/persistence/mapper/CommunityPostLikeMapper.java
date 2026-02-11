package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.CommunityPostLike;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 社区动态点赞Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface CommunityPostLikeMapper extends BaseMapper<CommunityPostLike> {

    /**
     * 物理删除点赞记录（绕过逻辑删除）
     *
     * @param postId 动态ID
     * @param userId 用户ID
     * @return 删除的行数
     */
    @Delete("DELETE FROM community_post_like WHERE post_id = #{postId} AND user_id = #{userId}")
    int physicalDelete(@Param("postId") Long postId, @Param("userId") Long userId);
}
