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
    @Select("SELECT * FROM private_conversation WHERE user1_id = #{user1Id} AND user2_id = #{user2Id}")
    PrivateConversation findByUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    /**
     * 发送消息后更新会话状态
     *
     * @param conversationId 会话ID
     * @param lastMessage    最后一条消息
     * @param unread1        用户1未读数
     * @param unread2        用户2未读数
     * @return 更新的行数
     */
    @Update("""
            UPDATE private_conversation
            SET last_message = #{lastMessage},
                last_time = NOW(),
                unread_1 = #{unread1},
                unread_2 = #{unread2},
                user1_hidden_time = NULL,
                user2_hidden_time = NULL
            WHERE id = #{conversationId}
            """)
    int updateAfterSend(@Param("conversationId") Long conversationId,
                        @Param("lastMessage") String lastMessage,
                        @Param("unread1") Integer unread1,
                        @Param("unread2") Integer unread2);

    /**
     * 隐藏用户1视角会话
     *
     * @param conversationId 会话ID
     * @return 更新的行数
     */
    @Update("UPDATE private_conversation SET user1_hidden_time = NOW(), unread_1 = 0 WHERE id = #{conversationId}")
    int hideByUser1(@Param("conversationId") Long conversationId);

    /**
     * 隐藏用户2视角会话
     *
     * @param conversationId 会话ID
     * @return 更新的行数
     */
    @Update("UPDATE private_conversation SET user2_hidden_time = NOW(), unread_2 = 0 WHERE id = #{conversationId}")
    int hideByUser2(@Param("conversationId") Long conversationId);

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
