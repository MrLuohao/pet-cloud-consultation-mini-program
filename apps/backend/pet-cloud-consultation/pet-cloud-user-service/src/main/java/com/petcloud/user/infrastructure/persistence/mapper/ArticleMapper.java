package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.Article;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}
