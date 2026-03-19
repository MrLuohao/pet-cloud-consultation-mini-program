package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.PrivateConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 私信会话Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface PrivateConversationMapper extends BaseMapper<PrivateConversation> {

    /**
     * 根据两个用户ID查询会话
     *
     * @param user1Id 用户1ID（较小的ID）
     * @param user2Id 用户2ID（较大的ID）
     * @return 会话
     */
    @Select("SELECT * FROM private_conversation WHERE user1_id = #{user1Id} AND user2_id = #{user2Id} AND is_deleted = 0")
    PrivateConversation findByUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    /**
     * 增加用户1的未读数
     *
     * @param conversationId 会话ID
     * @return 更新的行数
     */
    @Update("UPDATE private_conversation SET unread_1 = unread_1 + 1, last_time = NOW() WHERE id = #{conversationId}")
    int incrementUnread1(@Param("conversationId") Long conversationId);

    /**
     * 增加用户2的未读数
     *
     * @param conversationId 会话ID
     * @return 更新的行数
     */
    @Update("UPDATE private_conversation SET unread_2 = unread_2 + 1, last_time = NOW() WHERE id = #{conversationId}")
    int incrementUnread2(@Param("conversationId") Long conversationId);

    /**
     * 清除用户1的未读数
     *
     * @param conversationId 会话ID
     * @return 更新的行数
     */
    @Update("UPDATE private_conversation SET unread_1 = 0 WHERE id = #{conversationId}")
    int clearUnread1(@Param("conversationId") Long conversationId);

    /**
     * 清除用户2的未读数
     *
     * @param conversationId 会话ID
     * @return 更新的行数
     */
    @Update("UPDATE private_conversation SET unread_2 = 0 WHERE id = #{conversationId}")
    int clearUnread2(@Param("conversationId") Long conversationId);
}
