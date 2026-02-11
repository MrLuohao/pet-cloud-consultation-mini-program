-- ============================================================
-- 伴宠云诊 - 一键执行全量迁移脚本
-- 包含 Sprint1/Sprint2/Sprint4 所有新增表和字段
-- 所有语句均使用 IF NOT EXISTS / IF NOT EXISTS 保证幂等
-- ============================================================

-- ============================================================
-- 1. course 表扩展（讲师信息 + 章节JSON）
-- ============================================================
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'course' AND column_name = 'instructor_name');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `course` ADD COLUMN `instructor_name` VARCHAR(50) DEFAULT NULL COMMENT ''讲师姓名'' AFTER `sort_order`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'course' AND column_name = 'instructor_avatar');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `course` ADD COLUMN `instructor_avatar` VARCHAR(255) DEFAULT NULL COMMENT ''讲师头像'' AFTER `instructor_name`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'course' AND column_name = 'instructor_bio');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `course` ADD COLUMN `instructor_bio` VARCHAR(500) DEFAULT NULL COMMENT ''讲师简介'' AFTER `instructor_avatar`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'course' AND column_name = 'chapters');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `course` ADD COLUMN `chapters` JSON DEFAULT NULL COMMENT ''章节列表JSON'' AFTER `instructor_bio`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- 2. doctor 表扩展（在线状态 + 平均响应时长）
-- ============================================================
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'doctor' AND column_name = 'online_status');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `doctor` ADD COLUMN `online_status` TINYINT DEFAULT 0 COMMENT ''在线状态: 0离线 1在线'' AFTER `sort_order`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'doctor' AND column_name = 'avg_response_minutes');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `doctor` ADD COLUMN `avg_response_minutes` DECIMAL(10,2) DEFAULT NULL COMMENT ''平均响应时长（分钟）'' AFTER `online_status`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- 3. consultation 表扩展（紧急标识）
-- ============================================================
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'consultation' AND column_name = 'is_urgent');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `consultation` ADD COLUMN `is_urgent` TINYINT DEFAULT 0 COMMENT ''是否紧急: 0普通 1紧急''', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- 4. beauty_booking 表扩展（服务照片）
-- ============================================================
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'beauty_booking' AND column_name = 'before_photo');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `beauty_booking` ADD COLUMN `before_photo` VARCHAR(500) NULL COMMENT ''服务前照片URL'' AFTER `status`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'beauty_booking' AND column_name = 'after_photo');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `beauty_booking` ADD COLUMN `after_photo` VARCHAR(500) NULL COMMENT ''服务后照片URL'' AFTER `before_photo`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'beauty_booking' AND column_name = 'service_photos');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `beauty_booking` ADD COLUMN `service_photos` TEXT NULL COMMENT ''服务过程照片JSON数组'' AFTER `after_photo`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- 5. 新建 beauty_service 表（美容服务项目）
-- ============================================================
CREATE TABLE IF NOT EXISTS `beauty_service` (
    `id`              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `store_id`        BIGINT       NOT NULL             COMMENT '门店ID',
    `name`            VARCHAR(100) NOT NULL             COMMENT '服务名称',
    `description`     VARCHAR(500) NULL                 COMMENT '服务描述',
    `suitable_weight` VARCHAR(50)  NULL                 COMMENT '适合体重范围',
    `duration`        INT          NULL                 COMMENT '服务时长（分钟）',
    `price`           DECIMAL(10,2) NULL                COMMENT '服务价格',
    `status`          TINYINT      NOT NULL DEFAULT 1   COMMENT '状态(0下架/1上架)',
    `sort_order`      INT          NOT NULL DEFAULT 0   COMMENT '排序权重',
    `creator_id`      BIGINT       NULL,
    `creator_name`    VARCHAR(50)  NULL,
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `modifier_id`     BIGINT       NULL,
    `modifier_name`   VARCHAR(50)  NULL,
    `modify_time`     DATETIME     NULL ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`      TINYINT      NOT NULL DEFAULT 0,
    INDEX `idx_store_id` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='美容服务项目表';

-- ============================================================
-- 6. 新建 beauty_booking_log 表（预约操作日志）
-- ============================================================
CREATE TABLE IF NOT EXISTS `beauty_booking_log` (
    `id`            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `booking_id`    BIGINT       NOT NULL             COMMENT '预约ID',
    `status`        TINYINT      NOT NULL             COMMENT '状态值',
    `status_text`   VARCHAR(20)  NULL                 COMMENT '状态说明',
    `operator_id`   BIGINT       NULL                 COMMENT '操作人ID',
    `remark`        VARCHAR(200) NULL                 COMMENT '备注',
    `creator_id`    BIGINT       NULL,
    `creator_name`  VARCHAR(50)  NULL,
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `modifier_id`   BIGINT       NULL,
    `modifier_name` VARCHAR(50)  NULL,
    `modify_time`   DATETIME     NULL,
    `is_deleted`    TINYINT      NOT NULL DEFAULT 0,
    INDEX `idx_booking_id` (`booking_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='美容预约操作日志表';

-- ============================================================
-- 7. 新建 course_progress 表（课程学习进度）
-- ============================================================
CREATE TABLE IF NOT EXISTS `course_progress` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`       BIGINT       NOT NULL                COMMENT '用户ID',
    `course_id`     BIGINT       NOT NULL                COMMENT '课程ID',
    `chapter_id`    VARCHAR(50)  DEFAULT NULL             COMMENT '当前章节ID',
    `progress`      INT          DEFAULT 0               COMMENT '进度百分比 0-100',
    `watch_seconds` INT          DEFAULT 0               COMMENT '已观看秒数',
    `is_completed`  TINYINT      DEFAULT 0               COMMENT '是否完成: 0否 1是',
    `creator_id`    BIGINT       DEFAULT NULL,
    `creator_name`  VARCHAR(100) DEFAULT NULL,
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `modifier_id`   BIGINT       DEFAULT NULL,
    `modifier_name` VARCHAR(100) DEFAULT NULL,
    `modify_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`    TINYINT      DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_course` (`user_id`, `course_id`, `is_deleted`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程学习进度';

-- ============================================================
-- 8. 新建 course_review 表（课程评价）
-- ============================================================
CREATE TABLE IF NOT EXISTS `course_review` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `course_id`     BIGINT       NOT NULL                COMMENT '课程ID',
    `user_id`       BIGINT       NOT NULL                COMMENT '用户ID',
    `user_nickname` VARCHAR(100) DEFAULT NULL             COMMENT '用户昵称',
    `rating`        TINYINT      NOT NULL DEFAULT 5       COMMENT '评分 1-5',
    `content`       VARCHAR(500) DEFAULT NULL             COMMENT '评价内容',
    `creator_id`    BIGINT       DEFAULT NULL,
    `creator_name`  VARCHAR(100) DEFAULT NULL,
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `modifier_id`   BIGINT       DEFAULT NULL,
    `modifier_name` VARCHAR(100) DEFAULT NULL,
    `modify_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`    TINYINT      DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_course` (`user_id`, `course_id`, `is_deleted`),
    INDEX `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程评价';

-- ============================================================
-- 9. 新建 consultation_review 表（咨询评价）
-- ============================================================
CREATE TABLE IF NOT EXISTS `consultation_review` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `consultation_id` BIGINT       NOT NULL                COMMENT '咨询ID',
    `doctor_id`       BIGINT       NOT NULL                COMMENT '医生ID',
    `user_id`         BIGINT       NOT NULL                COMMENT '用户ID',
    `user_nickname`   VARCHAR(100) DEFAULT NULL             COMMENT '用户昵称',
    `rating`          TINYINT      NOT NULL DEFAULT 5       COMMENT '评分 1-5',
    `is_good`         TINYINT      DEFAULT 1                COMMENT '是否好评: 0否 1是',
    `content`         VARCHAR(500) DEFAULT NULL             COMMENT '评价内容',
    `creator_id`      BIGINT       DEFAULT NULL,
    `creator_name`    VARCHAR(100) DEFAULT NULL,
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `modifier_id`     BIGINT       DEFAULT NULL,
    `modifier_name`   VARCHAR(100) DEFAULT NULL,
    `modify_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`      TINYINT      DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_consultation` (`consultation_id`, `is_deleted`),
    INDEX `idx_doctor_id` (`doctor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='咨询评价';

-- ============================================================
-- 10. 新建 health_reminder 表（健康提醒）
-- ============================================================
CREATE TABLE IF NOT EXISTS `health_reminder` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`       BIGINT       NOT NULL                COMMENT '用户ID',
    `pet_id`        BIGINT       NOT NULL                COMMENT '宠物ID',
    `pet_name`      VARCHAR(50)  DEFAULT NULL             COMMENT '宠物名称',
    `reminder_type` VARCHAR(20)  NOT NULL                 COMMENT '类型: vaccine,checkup,medicine,deworming,other',
    `title`         VARCHAR(100) NOT NULL                 COMMENT '提醒标题',
    `remind_date`   DATE         NOT NULL                 COMMENT '提醒日期',
    `is_done`       TINYINT      DEFAULT 0                COMMENT '是否完成: 0否 1是',
    `note`          VARCHAR(255) DEFAULT NULL             COMMENT '备注',
    `creator_id`    BIGINT       DEFAULT NULL,
    `creator_name`  VARCHAR(100) DEFAULT NULL,
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `modifier_id`   BIGINT       DEFAULT NULL,
    `modifier_name` VARCHAR(100) DEFAULT NULL,
    `modify_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`    TINYINT      DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_user_pet` (`user_id`, `pet_id`),
    INDEX `idx_remind_date` (`remind_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康提醒';

-- ============================================================
-- 11. 新建 community_post 表（社区动态）
-- ============================================================
CREATE TABLE IF NOT EXISTS `community_post` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `user_id`       BIGINT       NOT NULL                 COMMENT '发布者用户ID',
    `content`       TEXT         NOT NULL                 COMMENT '动态内容',
    `media_urls`    JSON         DEFAULT NULL             COMMENT '媒体URL数组（JSON）',
    `media_type`    VARCHAR(10)  DEFAULT NULL             COMMENT '媒体类型: image/video',
    `pet_id`        BIGINT       DEFAULT NULL             COMMENT '关联宠物ID',
    `like_count`    INT          NOT NULL DEFAULT 0       COMMENT '点赞数',
    `comment_count` INT          NOT NULL DEFAULT 0       COMMENT '评论数',
    `is_deleted`    TINYINT      NOT NULL DEFAULT 0       COMMENT '删除标识',
    `creator_id`    BIGINT       DEFAULT NULL,
    `creator_name`  VARCHAR(100) DEFAULT NULL,
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `modifier_id`   BIGINT       DEFAULT NULL,
    `modifier_name` VARCHAR(100) DEFAULT NULL,
    `modify_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_id`     (`user_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区动态';

-- ============================================================
-- 12. 新建 community_post_like 表（动态点赞）
-- ============================================================
CREATE TABLE IF NOT EXISTS `community_post_like` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `post_id`       BIGINT       NOT NULL                 COMMENT '动态ID',
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
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区动态点赞';

-- ============================================================
-- 13. 新建 community_comment 表（动态评论）
-- ============================================================
CREATE TABLE IF NOT EXISTS `community_comment` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `post_id`       BIGINT       NOT NULL                 COMMENT '动态ID',
    `user_id`       BIGINT       NOT NULL                 COMMENT '评论者用户ID',
    `content`       VARCHAR(500) NOT NULL                 COMMENT '评论内容',
    `is_deleted`    TINYINT      NOT NULL DEFAULT 0       COMMENT '删除标识',
    `creator_id`    BIGINT       DEFAULT NULL,
    `creator_name`  VARCHAR(100) DEFAULT NULL,
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `modifier_id`   BIGINT       DEFAULT NULL,
    `modifier_name` VARCHAR(100) DEFAULT NULL,
    `modify_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_post_id`     (`post_id`),
    INDEX `idx_user_id`     (`user_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区动态评论';

-- ============================================================
-- 14. 新建 community_topic 表（话题）
-- ============================================================
CREATE TABLE IF NOT EXISTS `community_topic` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    `name`          VARCHAR(50)  NOT NULL                 COMMENT '话题名称',
    `post_count`    INT          NOT NULL DEFAULT 0       COMMENT '动态数量',
    `is_hot`        TINYINT      NOT NULL DEFAULT 0       COMMENT '是否热门',
    `creator_id`    BIGINT       DEFAULT NULL,
    `creator_name`  VARCHAR(100) DEFAULT NULL,
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `modifier_id`   BIGINT       DEFAULT NULL,
    `modifier_name` VARCHAR(100) DEFAULT NULL,
    `modify_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`    TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    INDEX `idx_is_hot` (`is_hot`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区话题';

-- ============================================================
-- 15. 新建 product_subscription 表（商品订阅，shop-service 库）
-- 注意：此表属于 shop-service 的数据库，如果两个服务共用同一个库则直接执行
-- ============================================================
CREATE TABLE IF NOT EXISTS `product_subscription` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`         BIGINT       NOT NULL                COMMENT '用户ID',
    `product_id`      BIGINT       NOT NULL                COMMENT '商品ID',
    `sku_id`          BIGINT       DEFAULT NULL             COMMENT 'SKU ID',
    `quantity`        INT          NOT NULL DEFAULT 1       COMMENT '数量',
    `cycle_days`      INT          NOT NULL                 COMMENT '配送周期（天）',
    `address_id`      BIGINT       NOT NULL                 COMMENT '收货地址ID',
    `status`          TINYINT      NOT NULL DEFAULT 0       COMMENT '状态: 0正常 1暂停 2取消',
    `next_order_date` DATE         DEFAULT NULL             COMMENT '下次下单日期',
    `discount_rate`   DECIMAL(5,2) DEFAULT 0.90             COMMENT '折扣率',
    `creator_id`      BIGINT       DEFAULT NULL,
    `creator_name`    VARCHAR(100) DEFAULT NULL,
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `modifier_id`     BIGINT       DEFAULT NULL,
    `modifier_name`   VARCHAR(100) DEFAULT NULL,
    `modify_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`      TINYINT      DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_user_id`    (`user_id`),
    INDEX `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品订阅';

-- ============================================================
-- 完成
-- ============================================================
SELECT '全量迁移执行完毕' AS result;
