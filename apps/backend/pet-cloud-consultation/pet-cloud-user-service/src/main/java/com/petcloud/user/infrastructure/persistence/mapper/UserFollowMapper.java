package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.UserFollow;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户关注关系Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {

    /**
     * 物理删除关注记录（绕过逻辑删除）
     *
     * @param followerId  关注者ID
     * @param followingId 被关注者ID
     * @return 删除的行数
     */
    @Delete("DELETE FROM user_follow WHERE follower_id = #{followerId} AND following_id = #{followingId}")
    int physicalDelete(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    /**
     * 统计粉丝数量
     *
     * @param userId 用户ID
     * @return 粉丝数量
     */
    @Select("SELECT COUNT(*) FROM user_follow WHERE following_id = #{userId} AND is_deleted = 0")
    int countFollowers(@Param("userId") Long userId);

    /**
     * 统计关注数量
     *
     * @param userId 用户ID
     * @return 关注数量
     */
    @Select("SELECT COUNT(*) FROM user_follow WHERE follower_id = #{userId} AND is_deleted = 0")
    int countFollowings(@Param("userId") Long userId);
}
