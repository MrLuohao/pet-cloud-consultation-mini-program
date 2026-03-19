package com.petcloud.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcloud.user.domain.entity.PrivateMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 私信消息Mapper接口
 *
 * @author luohao
 */
@Mapper
public interface PrivateMessageMapper extends BaseMapper<PrivateMessage> {

    /**
     * 统计用户未读消息总数
     *
     * @param userId 用户ID
     * @return 未读消息数
     */
    @Select("SELECT COUNT(*) FROM private_message WHERE receiver_id = #{userId} AND is_read = 0 AND is_deleted = 0")
    int countUnreadByReceiver(@Param("userId") Long userId);

    /**
     * 批量标记会话消息为已读
     *
     * @param conversationId 会话ID
     * @param receiverId     接收者ID
     * @return 更新的行数
     */
    @Update("UPDATE private_message SET is_read = 1 WHERE conversation_id = #{conversationId} AND receiver_id = #{receiverId} AND is_read = 0")
    int markAsReadByConversation(@Param("conversationId") Long conversationId, @Param("receiverId") Long receiverId);
}
