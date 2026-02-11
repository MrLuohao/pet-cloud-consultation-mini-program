package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.ArticleComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章评论Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface ArticleCommentMapper extends BaseMapper<ArticleComment> {
}
