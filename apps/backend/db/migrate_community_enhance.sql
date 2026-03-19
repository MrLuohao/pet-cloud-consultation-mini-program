-- ============================================================
-- 伴宠云诊 - 社区模块增强迁移脚本
-- 包含: 关注系统、收藏、分享、举报、私信、@提及等功能
-- 所有语句均使用 IF NOT EXISTS / 动态SQL 保证幂等
-- ============================================================

-- ============================================================
-- 1. 扩展 community_post 表（新增字段）
-- ============================================================

-- 添加 share_count 字段
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'community_post' AND column_name = 'share_count');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `community_post` ADD COLUMN `share_count` INT DEFAULT 0 COMMENT ''分享数'' AFTER `comment_count`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 添加 collect_count 字段
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'community_post' AND column_name = 'collect_count');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `community_post` ADD COLUMN `collect_count` INT DEFAULT 0 COMMENT ''收藏数'' AFTER `share_count`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 添加 is_pinned 字段（置顶）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'community_post' AND column_name = 'is_pinned');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `community_post` ADD COLUMN `is_pinned` TINYINT DEFAULT 0 COMMENT ''是否置顶: 0否 1是'' AFTER `is_deleted`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 添加 is_hot 字段（热门）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'community_post' AND column_name = 'is_hot');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `community_post` ADD COLUMN `is_hot` TINYINT DEFAULT 0 COMMENT ''是否热门: 0否 1是'' AFTER `is_pinned`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 添加 topic_id 字段（关联话题）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'community_post' AND column_name = 'topic_id');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `community_post` ADD COLUMN `topic_id` BIGINT DEFAULT NULL COMMENT ''话题ID'' AFTER `pet_id`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 添加置顶和热门索引
SET @idx_exists = (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'community_post' AND index_name = 'idx_pinned');
SET @sql = IF(@idx_exists = 0, 'CREATE INDEX `idx_pinned` ON `community_post` (`is_pinned`, `create_time`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists = (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'community_post' AND index_name = 'idx_hot');
SET @sql = IF(@idx_exists = 0, 'CREATE INDEX `idx_hot` ON `community_post` (`is_hot`, `create_time`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists = (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'community_post' AND index_name = 'idx_topic');
SET @sql = IF(@idx_exists = 0, 'CREATE INDEX `idx_topic` ON `community_post` (`topic_id`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- 2. 扩展 community_topic 表（新增字段）
-- ============================================================

-- 添加 icon 字段
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'community_topic' AND column_name = 'icon');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `community_topic` ADD COLUMN `icon` VARCHAR(50) DEFAULT ''📌'' COMMENT ''话题图标'' AFTER `name`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 添加 description 字段
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'community_topic' AND column_name = 'description');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `community_topic` ADD COLUMN `description` VARCHAR(200) DEFAULT NULL COMMENT ''话题描述'' AFTER `icon`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 添加 cover_url 字段
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'community_topic' AND column_name = 'cover_url');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `community_topic` ADD COLUMN `cover_url` VARCHAR(500) DEFAULT NULL COMMENT ''话题封面'' AFTER `description`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- 3. 新建 user_follow 表（用户关注关系）
-- ============================================================
CREATE TABLE IF NOT EXISTS `user_follow` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `follower_id`   BIGINT       NOT NULL                 COMMENT '关注者ID',
    `following_id`  BIGINT       NOT NULL                 COMMENT '被关注者ID',
    `creator_id`    BIGINT       DEFAULT NULL,
    `creator_name`  VARCHAR(100) DEFAULT NULL,
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `modifier_id`   BIGINT       DEFAULT NULL,
    `modifier_name` VARCHAR(100) DEFAULT NULL,
    `modify_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`    TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follow` (`follower_id`, `following_id`),
    INDEX `idx_follower` (`follower_id`),
    INDEX `idx_following` (`following_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注关系';

-- ============================================================
-- 4. 新建 community_post_collect 表（帖子收藏）
-- ============================================================
CREATE TABLE IF NOT EXISTS `community_post_collect` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `post_id`       BIGINT       NOT NULL                 COMMENT '帖子ID',
    `user_id`       BIGINT       NOT NULL                 COMMENT '用户ID',
    `creator_id`    BIGINT       DEFAULT NULL,
    `creator_name`  VARCHAR(100) DEFAULT NULL,
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `modifier_id`   BIGINT       DEFAULT NULL,
    `modifier_name` VARCHAR(100) DEFAULT NULL,
    `modify_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`    TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
    INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子收藏';

-- ============================================================
-- 5. 新建 community_post_share 表（帖子分享记录）
-- ============================================================
CREATE TABLE IF NOT EXISTS `community_post_share` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `post_id`       BIGINT       NOT NULL                 COMMENT '帖子ID',
    `user_id`       BIGINT       NOT NULL                 COMMENT '用户ID',
    `share_type`    VARCHAR(20)  DEFAULT 'wechat'          COMMENT '分享类型: wechat,moments,link',
    `creator_id`    BIGINT       DEFAULT NULL,
    `creator_name`  VARCHAR(100) DEFAULT NULL,
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `modifier_id`   BIGINT       DEFAULT NULL,
    `modifier_name` VARCHAR(100) DEFAULT NULL,
    `modify_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`    TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_post` (`post_id`),
    INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子分享记录';

-- ============================================================
-- 6. 新建 community_post_report 表（帖子举报）
-- ============================================================
CREATE TABLE IF NOT EXISTS `community_post_report` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `post_id`       BIGINT       NOT NULL                 COMMENT '帖子ID',
    `user_id`       BIGINT       NOT NULL                 COMMENT '举报用户ID',
    `reason`        VARCHAR(200) NOT NULL                 COMMENT '举报原因',
    `reason_type`   VARCHAR(50)  DEFAULT NULL             COMMENT '举报类型: spam,abuse,inappropriate,other',
    `status`        TINYINT      DEFAULT 0                COMMENT '状态: 0待处理 1已处理 2已驳回',
    `handler_id`    BIGINT       DEFAULT NULL             COMMENT '处理人ID',
    `handler_remark` VARCHAR(500) DEFAULT NULL             COMMENT '处理备注',
    `creator_id`    BIGINT       DEFAULT NULL,
    `creator_name`  VARCHAR(100) DEFAULT NULL,
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `modifier_id`   BIGINT       DEFAULT NULL,
    `modifier_name` VARCHAR(100) DEFAULT NULL,
    `modify_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`    TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_post` (`post_id`),
    INDEX `idx_user` (`user_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子举报';

-- ============================================================
-- 7. 新建 community_post_mention 表（@提及）
-- ============================================================
CREATE TABLE IF NOT EXISTS `community_post_mention` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `post_id`       BIGINT       NOT NULL                 COMMENT '帖子ID',
    `comment_id`    BIGINT       DEFAULT NULL             COMMENT '评论ID（如果是评论中的@）',
    `user_id`       BIGINT       NOT NULL                 COMMENT '被@的用户ID',
    `mention_by`    BIGINT       NOT NULL                 COMMENT '@发起者ID',
    `is_read`       TINYINT      DEFAULT 0                COMMENT '是否已读: 0否 1是',
    `creator_id`    BIGINT       DEFAULT NULL,
    `creator_name`  VARCHAR(100) DEFAULT NULL,
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `modifier_id`   BIGINT       DEFAULT NULL,
    `modifier_name` VARCHAR(100) DEFAULT NULL,
    `modify_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`    TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_user` (`user_id`),
    INDEX `idx_post` (`post_id`),
    INDEX `idx_mention_by` (`mention_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子@提及';

-- ============================================================
-- 8. 新建 private_conversation 表（私信会话）
-- ============================================================
CREATE TABLE IF NOT EXISTS `private_conversation` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `user1_id`      BIGINT       NOT NULL                 COMMENT '用户1 (较小的ID)',
    `user2_id`      BIGINT       NOT NULL                 COMMENT '用户2 (较大的ID)',
    `last_message`  VARCHAR(500) DEFAULT NULL             COMMENT '最后一条消息内容',
    `last_time`     DATETIME     DEFAULT NULL             COMMENT '最后消息时间',
    `unread_1`      INT          DEFAULT 0                COMMENT '用户1未读数',
    `unread_2`      INT          DEFAULT 0                COMMENT '用户2未读数',
    `creator_id`    BIGINT       DEFAULT NULL,
    `creator_name`  VARCHAR(100) DEFAULT NULL,
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `modifier_id`   BIGINT       DEFAULT NULL,
    `modifier_name` VARCHAR(100) DEFAULT NULL,
    `modify_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`    TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users` (`user1_id`, `user2_id`),
    INDEX `idx_user1` (`user1_id`),
    INDEX `idx_user2` (`user2_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信会话';

-- ============================================================
-- 9. 新建 private_message 表（私信消息）
-- ============================================================
CREATE TABLE IF NOT EXISTS `private_message` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `conversation_id` BIGINT       NOT NULL                 COMMENT '会话ID',
    `sender_id`       BIGINT       NOT NULL                 COMMENT '发送者ID',
    `receiver_id`     BIGINT       NOT NULL                 COMMENT '接收者ID',
    `content`         TEXT         NOT NULL                 COMMENT '消息内容',
    `msg_type`        VARCHAR(20)  DEFAULT 'text'           COMMENT '消息类型: text,image,voice',
    `is_read`         TINYINT      DEFAULT 0                COMMENT '是否已读: 0否 1是',
    `creator_id`      BIGINT       DEFAULT NULL,
    `creator_name`    VARCHAR(100) DEFAULT NULL,
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `modifier_id`     BIGINT       DEFAULT NULL,
    `modifier_name`   VARCHAR(100) DEFAULT NULL,
    `modify_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_conversation` (`conversation_id`),
    INDEX `idx_sender` (`sender_id`),
    INDEX `idx_receiver` (`receiver_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信消息';

-- ============================================================
-- 10. 扩展 community_comment 表（添加回复功能）
-- ============================================================

-- 添加 reply_to_id 字段（回复目标评论ID）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'community_comment' AND column_name = 'reply_to_id');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `community_comment` ADD COLUMN `reply_to_id` BIGINT DEFAULT NULL COMMENT ''回复目标评论ID'' AFTER `user_id`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 添加 reply_to_user_id 字段（回复目标用户ID）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'community_comment' AND column_name = 'reply_to_user_id');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `community_comment` ADD COLUMN `reply_to_user_id` BIGINT DEFAULT NULL COMMENT ''回复目标用户ID'' AFTER `reply_to_id`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 添加 like_count 字段（评论点赞数）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'community_comment' AND column_name = 'like_count');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `community_comment` ADD COLUMN `like_count` INT DEFAULT 0 COMMENT ''点赞数'' AFTER `content`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 添加 media_urls 字段（评论媒体URL数组）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'community_comment' AND column_name = 'media_urls');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `community_comment` ADD COLUMN `media_urls` JSON DEFAULT NULL COMMENT ''评论媒体URL数组'' AFTER `content`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 添加 media_type 字段（评论媒体类型）
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'community_comment' AND column_name = 'media_type');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `community_comment` ADD COLUMN `media_type` VARCHAR(10) DEFAULT NULL COMMENT ''评论媒体类型: image/video'' AFTER `media_urls`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- 11. 新建 community_comment_like 表（评论点赞）
-- ============================================================
CREATE TABLE IF NOT EXISTS `community_comment_like` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `comment_id`    BIGINT       NOT NULL                 COMMENT '评论ID',
    `user_id`       BIGINT       NOT NULL                 COMMENT '用户ID',
    `creator_id`    BIGINT       DEFAULT NULL,
    `creator_name`  VARCHAR(100) DEFAULT NULL,
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `modifier_id`   BIGINT       DEFAULT NULL,
    `modifier_name` VARCHAR(100) DEFAULT NULL,
    `modify_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`    TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
    INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞';

-- ============================================================
-- 12. 插入默认热门话题数据
-- ============================================================
INSERT IGNORE INTO `community_topic` (`name`, `icon`, `description`, `post_count`, `is_hot`, `create_time`) VALUES
('猫咪日常', '🐱', '分享你家猫咪的日常趣事', 0, 1, NOW()),
('狗狗训练', '🐕', '狗狗训练技巧和经验分享', 0, 1, NOW()),
('新手养宠', '🐾', '新手养宠指南和注意事项', 0, 1, NOW()),
('宠物健康', '🏥', '宠物健康知识和问题讨论', 0, 1, NOW()),
('晒晒我家萌宠', '📸', '晒出你家萌宠的美照', 0, 1, NOW()),
('宠物美食', '🍖', '宠物饮食和营养讨论', 0, 1, NOW()),
('领养代替购买', '❤️', '宠物领养故事和经验', 0, 1, NOW()),
('宠物用品测评', '⭐', '宠物用品使用心得和测评', 0, 1, NOW());

-- ============================================================
-- 完成
-- ============================================================
SELECT '社区模块增强迁移执行完毕' AS result;
