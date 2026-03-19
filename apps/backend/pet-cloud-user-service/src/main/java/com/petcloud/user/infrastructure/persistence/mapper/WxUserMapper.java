package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.WxUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 微信用户Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface WxUserMapper extends BaseMapper<WxUser> {
}
