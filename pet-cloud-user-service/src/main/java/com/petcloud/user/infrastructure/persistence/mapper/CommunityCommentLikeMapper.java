package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.CommunityCommentLike;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 评论点赞Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface CommunityCommentLikeMapper extends BaseMapper<CommunityCommentLike> {

    /**
     * 物理删除点赞记录（绕过逻辑删除）
     *
     * @param commentId 评论ID
     * @param userId    用户ID
     * @return 删除的行数
     */
    @Delete("DELETE FROM community_comment_like WHERE comment_id = #{commentId} AND user_id = #{userId}")
    int physicalDelete(@Param("commentId") Long commentId, @Param("userId") Long userId);
}
