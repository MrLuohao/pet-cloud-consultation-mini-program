package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.CommunityPostReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 帖子举报Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface CommunityPostReportMapper extends BaseMapper<CommunityPostReport> {

    /**
     * 检查用户是否已举报过该帖子
     *
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 是否存在举报记录
     */
    @Select("SELECT COUNT(*) > 0 FROM community_post_report WHERE post_id = #{postId} AND user_id = #{userId}")
    boolean hasReported(@Param("postId") Long postId, @Param("userId") Long userId);
}
