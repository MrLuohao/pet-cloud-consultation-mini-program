package com.petcloud.user.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 消息中心VO
 *
 * @author luohao
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageCenterVO {

    /**
     * 页面快捷入口
     */
    private List<QuickEntryVO> quickEntries;

    /**
     * 未读汇总
     */
    private UnreadSummaryVO unreadSummary;

    /**
     * 总未读数
     */
    private Integer totalUnreadCount;

    /**
     * 最近会话列表
     */
    private List<ConversationVO> recentConversations;

    /**
     * AI助手未读数
     */
    private Integer aiUnreadCount;

    /**
     * 咨询未读数
     */
    private Integer consultationUnreadCount;

    /**
     * 系统通知统计
     */
    private NotificationCounts notificationCounts;

    /**
     * 最近系统通知
     */
    private List<MessageVO> recentNotifications;

    /**
     * 系统通知列表
     */
    private List<MessageVO> systemNotifications;

    /**
     * 页面事件槽位
     */
    private List<EventSlotVO> eventSlots;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickEntryVO {

        private String key;

        private String title;

        private String subtitle;

        private String iconKey;

        private Integer unreadCount;

        private String navigateUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnreadSummaryVO {

        private Integer totalUnreadCount;

        private Integer conversationUnreadCount;

        private Integer notificationUnreadCount;

        private Integer aiUnreadCount;

        private Integer consultationUnreadCount;

        private Integer customerServiceUnreadCount;
    }

    /**
     * 系统通知统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationCounts {
        /**
         * 订单通知数
         */
        private Integer orderCount;

        /**
         * 活动通知数
         */
        private Integer activityCount;

        /**
         * 系统消息数
         */
        private Integer systemCount;

        /**
         * 互动消息数
         */
        private Integer interactionCount;

        /**
         * 总数
         */
        private Integer totalCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventSlotVO {

        private String key;

        private String title;

        private String description;

        private Integer unreadCount;

        private Boolean enabled;
    }
}
