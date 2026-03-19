package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.UserPet;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户宠物Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface UserPetMapper extends BaseMapper<UserPet> {
}
