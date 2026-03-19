package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.ArticleLike;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 文章点赞Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface ArticleLikeMapper extends BaseMapper<ArticleLike> {

    /**
     * 物理删除点赞记录（绕过逻辑删除）
     *
     * @param articleId 文章ID
     * @param userId 用户ID
     * @return 删除的行数
     */
    @Delete("DELETE FROM article_like WHERE article_id = #{articleId} AND user_id = #{userId}")
    int physicalDelete(@Param("articleId") Long articleId, @Param("userId") Long userId);
}
