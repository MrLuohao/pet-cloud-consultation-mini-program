-- ============================================
-- 伴宠云诊 - 完整数据库表创建脚本
-- 创建时间: 2025
-- 说明: 所有表统一使用 pet_cloud_db 数据库
-- ============================================

-- 创建/使用主数据库
CREATE DATABASE IF NOT EXISTS `pet_cloud_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `pet_cloud_db`;

-- ============================================
-- 用户服务表
-- ============================================

-- 小程序用户表
CREATE TABLE IF NOT EXISTS `wx_user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `openid` VARCHAR(100) NOT NULL UNIQUE COMMENT '微信OpenID',
  `unionid` VARCHAR(100) DEFAULT NULL COMMENT '微信UnionID',
  `nickname` VARCHAR(100) DEFAULT NULL COMMENT '昵称',
  `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `gender` TINYINT DEFAULT 0 COMMENT '性别: 0-未知 1-男 2-女',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
  `is_vip` TINYINT DEFAULT 0 COMMENT '是否会员: 0-否 1-是',
  `vip_level` VARCHAR(20) DEFAULT NULL COMMENT '会员等级',
  `vip_start_time` DATETIME DEFAULT NULL COMMENT '会员开始时间',
  `vip_expire_time` DATETIME DEFAULT NULL COMMENT '会员到期时间',
  `vip_saving_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '会员累计已省金额',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(100) DEFAULT NULL COMMENT '修改人',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识: 0-正常 1-删除',
  INDEX `idx_openid` (`openid`),
  INDEX `idx_unionid` (`unionid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小程序用户表';

-- 会员订单表
CREATE TABLE IF NOT EXISTS `vip_order` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `plan_id` VARCHAR(32) NOT NULL COMMENT '套餐ID',
  `plan_name` VARCHAR(50) NOT NULL COMMENT '套餐名称',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
  `duration_days` INT NOT NULL COMMENT '有效天数',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-待支付 1-已支付 2-已取消',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `expire_time` DATETIME DEFAULT NULL COMMENT '到期时间',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(100) DEFAULT NULL COMMENT '修改人',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识',
  INDEX `idx_user_id` (`user_id`),
  UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员订单表';

-- 用户宠物表
CREATE TABLE IF NOT EXISTS `user_pet` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(50) NOT NULL COMMENT '宠物名称',
  `type` TINYINT NOT NULL COMMENT '类型: 1-狗 2-猫 3-其他',
  `breed` VARCHAR(50) DEFAULT NULL COMMENT '品种',
  `gender` TINYINT DEFAULT 0 COMMENT '性别: 0-未知 1-公 2-母',
  `birthday` DATE DEFAULT NULL COMMENT '生日',
  `weight` DECIMAL(5,2) DEFAULT NULL COMMENT '体重(kg)',
  `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '宠物头像',
  `health_status` VARCHAR(200) DEFAULT NULL COMMENT '健康状况',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(100) DEFAULT NULL COMMENT '修改人',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识',
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户宠物表';

-- 收货地址表
CREATE TABLE IF NOT EXISTS `user_address` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `contact_name` VARCHAR(50) NOT NULL COMMENT '联系人',
  `contact_phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
  `province` VARCHAR(50) NOT NULL COMMENT '省份',
  `city` VARCHAR(50) NOT NULL COMMENT '城市',
  `district` VARCHAR(50) NOT NULL COMMENT '区/县',
  `detail_address` VARCHAR(200) NOT NULL COMMENT '详细地址',
  `is_default` TINYINT DEFAULT 0 COMMENT '是否默认: 0-否 1-是',
  `longitude` DECIMAL(11,6) DEFAULT NULL COMMENT '经度',
  `latitude` DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
  `business_area` VARCHAR(100) DEFAULT NULL COMMENT '商圈',
  `door_no` VARCHAR(100) DEFAULT NULL COMMENT '门牌号',
  `raw_text` VARCHAR(500) DEFAULT NULL COMMENT '原始粘贴文本',
  `parsed_name` VARCHAR(50) DEFAULT NULL COMMENT '解析联系人',
  `parsed_phone` VARCHAR(20) DEFAULT NULL COMMENT '解析手机号',
  `map_address` VARCHAR(300) DEFAULT NULL COMMENT '地图地址描述',
  `address_tag` VARCHAR(50) DEFAULT NULL COMMENT '地址标签',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(100) DEFAULT NULL COMMENT '修改人',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识',
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- 课程表
CREATE TABLE IF NOT EXISTS `course` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(100) NOT NULL COMMENT '课程标题',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '课程描述',
  `cover_url` VARCHAR(500) DEFAULT NULL COMMENT '封面图',
  `lesson_count` INT DEFAULT 0 COMMENT '课时数量',
  `student_count` INT DEFAULT 0 COMMENT '学习人数',
  `price` DECIMAL(10,2) DEFAULT 0 COMMENT '价格',
  `tag` VARCHAR(50) DEFAULT NULL COMMENT '标签: 入门/进阶/热门',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-下架 1-上架',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(100) DEFAULT NULL COMMENT '修改人',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 美容门店表
CREATE TABLE IF NOT EXISTS `beauty_store` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '门店名称',
  `cover_url` VARCHAR(500) DEFAULT NULL COMMENT '封面图',
  `rating` DECIMAL(2,1) DEFAULT 0 COMMENT '评分',
  `distance` VARCHAR(20) DEFAULT NULL COMMENT '距离',
  `address` VARCHAR(200) NOT NULL COMMENT '地址',
  `tags` VARCHAR(200) DEFAULT NULL COMMENT '服务标签（逗号分隔）',
  `latitude` DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
  `longitude` DECIMAL(11,6) DEFAULT NULL COMMENT '经度',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `business_hours` VARCHAR(100) DEFAULT NULL COMMENT '营业时间',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-关闭 1-营业',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(100) DEFAULT NULL COMMENT '修改人',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='美容门店表';

-- 美容预约表
CREATE TABLE IF NOT EXISTS `beauty_booking` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `store_id` BIGINT NOT NULL COMMENT '门店ID',
  `pet_id` BIGINT DEFAULT NULL COMMENT '宠物ID',
  `booking_date` DATE NOT NULL COMMENT '预约日期',
  `booking_time` VARCHAR(10) NOT NULL COMMENT '预约时间段',
  `services` VARCHAR(200) DEFAULT NULL COMMENT '服务项目（逗号分隔）',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待确认 1-已确认 2-已完成 3-已取消',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(100) DEFAULT NULL COMMENT '修改人',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_store_id` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='美容预约表';

-- 消息表
CREATE TABLE IF NOT EXISTS `message` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `type` VARCHAR(20) NOT NULL COMMENT '类型: system/consult/order/activity',
  `title` VARCHAR(100) NOT NULL COMMENT '标题',
  `content` VARCHAR(500) NOT NULL COMMENT '内容',
  `extra_data` VARCHAR(500) DEFAULT NULL COMMENT '额外数据(JSON)',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(100) DEFAULT NULL COMMENT '修改人',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- 文章表
CREATE TABLE IF NOT EXISTS `article` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(100) NOT NULL COMMENT '文章标题',
  `cover_url` VARCHAR(500) DEFAULT NULL COMMENT '封面图',
  `summary` VARCHAR(200) DEFAULT NULL COMMENT '摘要',
  `content` TEXT NOT NULL COMMENT '文章内容',
  `tag` VARCHAR(50) DEFAULT NULL COMMENT '标签',
  `view_count` INT DEFAULT 0 COMMENT '浏览量',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `collect_count` INT DEFAULT 0 COMMENT '收藏数',
  `comment_count` INT DEFAULT 0 COMMENT '评论数',
  `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-草稿 1-已发布',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(100) DEFAULT NULL COMMENT '修改人',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- 文章点赞记录表
CREATE TABLE IF NOT EXISTS `article_like` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `article_id` BIGINT NOT NULL COMMENT '文章ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(100) DEFAULT NULL COMMENT '修改人',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识',
  UNIQUE KEY `uk_article_user` (`article_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章点赞记录表';

-- 文章收藏记录表
CREATE TABLE IF NOT EXISTS `article_collect` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `article_id` BIGINT NOT NULL COMMENT '文章ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(100) DEFAULT NULL COMMENT '修改人',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识',
  UNIQUE KEY `uk_article_user` (`article_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章收藏记录表';

-- 医生表
CREATE TABLE IF NOT EXISTS `doctor` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '医生姓名',
  `avatar` VARCHAR(255) COMMENT '头像',
  `title` VARCHAR(50) COMMENT '职称',
  `specialty` VARCHAR(100) COMMENT '专长',
  `department` VARCHAR(50) COMMENT '科室',
  `experience` INT COMMENT '从业年限',
  `description` TEXT COMMENT '医生简介',
  `hospital_name` VARCHAR(100) COMMENT '所属医院',
  `consultation_fee` DECIMAL(10,2) DEFAULT 0 COMMENT '咨询费',
  `rating` DECIMAL(3,2) DEFAULT 5.0 COMMENT '评分',
  `consultation_count` INT DEFAULT 0 COMMENT '咨询次数',
  `tags` VARCHAR(200) COMMENT '标签（逗号分隔）',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(100) DEFAULT NULL COMMENT '修改人',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生信息';

-- 咨询记录表
CREATE TABLE IF NOT EXISTS `consultation` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(64) NOT NULL UNIQUE COMMENT '咨询单号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `user_nickname` VARCHAR(50) COMMENT '用户昵称',
  `user_avatar` VARCHAR(255) COMMENT '用户头像',
  `pet_id` BIGINT COMMENT '宠物ID',
  `pet_name` VARCHAR(50) COMMENT '宠物名称',
  `pet_type` TINYINT COMMENT '宠物类型：1狗 2猫 3其他',
  `doctor_id` BIGINT NOT NULL COMMENT '医生ID',
  `doctor_name` VARCHAR(50) COMMENT '医生姓名',
  `doctor_avatar` VARCHAR(255) COMMENT '医生头像',
  `type` TINYINT NOT NULL COMMENT '类型：1图文 2视频',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0待接单 1进行中 2已完成 3已取消',
  `description` TEXT COMMENT '病情描述',
  `images` JSON COMMENT '病情图片',
  `fee` DECIMAL(10,2) COMMENT '咨询费',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `accept_time` DATETIME COMMENT '接单时间',
  `finish_time` DATETIME COMMENT '完成时间',
  `cancel_time` DATETIME COMMENT '取消时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(100) DEFAULT NULL COMMENT '修改人',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_doctor_id` (`doctor_id`),
  INDEX `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='咨询记录';

-- 咨询聊天记录表
CREATE TABLE IF NOT EXISTS `consultation_message` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `consultation_id` BIGINT NOT NULL COMMENT '咨询ID',
  `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
  `sender_type` TINYINT NOT NULL COMMENT '发送者类型：1用户 2医生',
  `sender_name` VARCHAR(50) COMMENT '发送者名称',
  `sender_avatar` VARCHAR(255) COMMENT '发送者头像',
  `message_type` TINYINT DEFAULT 1 COMMENT '消息类型：1文字 2图片 3语音',
  `content` TEXT COMMENT '消息内容',
  `media_url` VARCHAR(500) COMMENT '媒体文件URL',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX `idx_consultation_id` (`consultation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='咨询聊天记录';

-- 健康档案表
CREATE TABLE IF NOT EXISTS `health_record` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `pet_id` BIGINT NOT NULL COMMENT '宠物ID',
  `pet_name` VARCHAR(50) COMMENT '宠物名称',
  `record_type` VARCHAR(20) NOT NULL COMMENT '记录类型：vaccine,checkup,medicine,surgery,other',
  `title` VARCHAR(100) COMMENT '标题',
  `content` TEXT COMMENT '详细内容',
  `hospital_name` VARCHAR(100) COMMENT '医院名称',
  `doctor_name` VARCHAR(50) COMMENT '医生姓名',
  `record_date` DATE COMMENT '记录日期',
  `next_date` DATE COMMENT '下次日期（如疫苗下次接种）',
  `images` JSON COMMENT '相关图片',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(100) DEFAULT NULL COMMENT '修改人',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识',
  INDEX `idx_pet_id` (`pet_id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物健康档案';

-- 系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `config_key` VARCHAR(50) NOT NULL UNIQUE COMMENT '配置键',
  `config_value` TEXT COMMENT '配置值',
  `description` VARCHAR(200) COMMENT '描述',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(100) DEFAULT NULL COMMENT '修改人',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置';

-- ============================================
-- 商城服务表
-- ============================================

-- 商品分类表
CREATE TABLE IF NOT EXISTS `product_category` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `icon` VARCHAR(50) DEFAULT NULL COMMENT '图标',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(100) DEFAULT NULL COMMENT '修改人',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识: 0-正常 1-删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 商品表
CREATE TABLE IF NOT EXISTS `product` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `cover_url` VARCHAR(500) DEFAULT NULL COMMENT '封面图',
  `image_urls` TEXT DEFAULT NULL COMMENT '商品图片（JSON数组）',
  `summary` VARCHAR(200) DEFAULT NULL COMMENT '商品简介',
  `price` DECIMAL(10,2) NOT NULL COMMENT '商品价格',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
  `stock` INT DEFAULT 0 COMMENT '库存',
  `sales` INT DEFAULT 0 COMMENT '销量',
  `rating` DECIMAL(3,2) DEFAULT 5.0 COMMENT '评分',
  `review_count` INT DEFAULT 0 COMMENT '评价数',
  `tag` VARCHAR(50) DEFAULT NULL COMMENT '标签: 热门/新品/推荐',
  `shop_id` VARCHAR(64) DEFAULT NULL COMMENT '店铺标识',
  `shop_name` VARCHAR(100) DEFAULT NULL COMMENT '店铺名称',
  `service_text` VARCHAR(100) DEFAULT NULL COMMENT '服务文案',
  `default_spec` VARCHAR(100) DEFAULT NULL COMMENT '默认规格',
  `spec_groups_json` LONGTEXT DEFAULT NULL COMMENT '规格组配置(JSON)',
  `detail_content_json` LONGTEXT DEFAULT NULL COMMENT '详情内容配置(JSON)',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-下架 1-上架',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(100) DEFAULT NULL COMMENT '修改人',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识',
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 订单表
CREATE TABLE IF NOT EXISTS `order_info` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
  `receiver_name` VARCHAR(50) COMMENT '收货人',
  `receiver_phone` VARCHAR(20) COMMENT '收货电话',
  `receiver_address` VARCHAR(500) COMMENT '收货地址',
  `remark` VARCHAR(500) COMMENT '订单备注',
  `coupon_id` BIGINT COMMENT '使用的优惠券ID',
  `coupon_discount` DECIMAL(10,2) DEFAULT 0 COMMENT '优惠券优惠金额',
  `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待付款 1-待发货 2-待收货 3-已完成 4-已取消',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `ship_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `receive_time` DATETIME DEFAULT NULL COMMENT '收货时间',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(100) DEFAULT NULL COMMENT '修改人',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识',
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单状态流转历史表
CREATE TABLE IF NOT EXISTS `order_status_history` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(32) DEFAULT NULL COMMENT '订单号',
  `from_status` TINYINT DEFAULT NULL COMMENT '原状态',
  `to_status` TINYINT DEFAULT NULL COMMENT '目标状态',
  `action` VARCHAR(50) NOT NULL COMMENT '动作标识',
  `operator_type` VARCHAR(30) DEFAULT NULL COMMENT '操作人类型',
  `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(100) DEFAULT NULL COMMENT '操作人名称',
  `logistics_company` VARCHAR(50) DEFAULT NULL COMMENT '物流公司',
  `tracking_no` VARCHAR(64) DEFAULT NULL COMMENT '物流单号',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `operate_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(100) DEFAULT NULL COMMENT '修改人',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识',
  KEY `idx_order_status_history_order` (`order_id`),
  KEY `idx_order_status_history_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单状态历史';

-- 订单商品表
CREATE TABLE IF NOT EXISTS `order_item` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `cover_url` VARCHAR(500) DEFAULT NULL COMMENT '商品封面',
  `shop_id` VARCHAR(64) DEFAULT NULL COMMENT '店铺标识',
  `shop_name` VARCHAR(100) DEFAULT NULL COMMENT '店铺名称',
  `service_text` VARCHAR(100) DEFAULT NULL COMMENT '服务文案',
  `spec_snapshot` VARCHAR(100) DEFAULT NULL COMMENT '规格快照',
  `sku_snapshot` VARCHAR(100) DEFAULT NULL COMMENT 'SKU快照',
  `price` DECIMAL(10,2) NOT NULL COMMENT '商品单价',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '商品原价',
  `quantity` INT NOT NULL COMMENT '购买数量',
  `subtotal` DECIMAL(10,2) NOT NULL COMMENT '小计',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(100) DEFAULT NULL COMMENT '修改人',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识',
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品表';

-- 购物车表
CREATE TABLE IF NOT EXISTS `shopping_cart` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `shop_id` VARCHAR(64) DEFAULT NULL COMMENT '店铺标识',
  `shop_name` VARCHAR(100) DEFAULT NULL COMMENT '店铺名称',
  `service_text` VARCHAR(100) DEFAULT NULL COMMENT '服务文案',
  `spec_label` VARCHAR(100) DEFAULT NULL COMMENT '规格说明',
  `sku_code` VARCHAR(64) DEFAULT NULL COMMENT 'SKU编码',
  `selected` TINYINT NOT NULL DEFAULT 1 COMMENT '是否选中',
  `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态 active/invalid',
  `price_snapshot` DECIMAL(10,2) DEFAULT NULL COMMENT '价格快照',
  `original_price_snapshot` DECIMAL(10,2) DEFAULT NULL COMMENT '原价快照',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(100) DEFAULT NULL COMMENT '修改人',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识',
  INDEX `idx_user_id` (`user_id`),
  UNIQUE KEY `uk_user_product_spec` (`user_id`, `product_id`, `spec_label`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车';

-- 商品评价表
CREATE TABLE IF NOT EXISTS `product_review` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `order_item_id` BIGINT COMMENT '订单项ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `user_nickname` VARCHAR(50) COMMENT '用户昵称',
  `user_avatar` VARCHAR(255) COMMENT '用户头像',
  `rating` INT NOT NULL COMMENT '评分1-5',
  `content` TEXT COMMENT '评价内容',
  `images` JSON COMMENT '评价图片',
  `reply_content` TEXT COMMENT '商家回复',
  `reply_time` DATETIME COMMENT '回复时间',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(100) DEFAULT NULL COMMENT '修改人',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识',
  INDEX `idx_product_id` (`product_id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价';

-- 优惠券表
CREATE TABLE IF NOT EXISTS `coupon` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '优惠券名称',
  `type` TINYINT NOT NULL COMMENT '类型：1满减券 2折扣券',
  `discount_amount` DECIMAL(10,2) COMMENT '减免金额',
  `discount_rate` DECIMAL(5,2) COMMENT '折扣率',
  `min_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '最低使用金额',
  `max_discount` DECIMAL(10,2) COMMENT '最大优惠金额(折扣券用)',
  `total_count` INT NOT NULL COMMENT '发行总量',
  `received_count` INT DEFAULT 0 COMMENT '已领取数量',
  `used_count` INT DEFAULT 0 COMMENT '已使用数量',
  `valid_days` INT NOT NULL COMMENT '有效天数',
  `start_time` DATETIME COMMENT '生效时间',
  `end_time` DATETIME COMMENT '失效时间',
  `description` VARCHAR(200) COMMENT '使用说明',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(100) DEFAULT NULL COMMENT '修改人',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券';

-- 用户优惠券表
CREATE TABLE IF NOT EXISTS `user_coupon` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `coupon_id` BIGINT NOT NULL COMMENT '优惠券ID',
  `coupon_name` VARCHAR(100) COMMENT '优惠券名称',
  `coupon_type` TINYINT COMMENT '类型：1满减券 2折扣券',
  `discount_amount` DECIMAL(10,2) COMMENT '减免金额',
  `discount_rate` DECIMAL(5,2) COMMENT '折扣率',
  `min_amount` DECIMAL(10,2) COMMENT '最低使用金额',
  `max_discount` DECIMAL(10,2) COMMENT '最大优惠金额',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0未使用 1已使用 2已过期',
  `use_time` DATETIME COMMENT '使用时间',
  `order_id` BIGINT COMMENT '订单ID',
  `expire_time` DATETIME NOT NULL COMMENT '过期时间',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(100) DEFAULT NULL COMMENT '修改人',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_coupon_id` (`coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券';

-- 支付记录表
CREATE TABLE IF NOT EXISTS `payment_record` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
  `payment_method` VARCHAR(20) COMMENT '支付方式：wechat,alipay,bank,credit',
  `payment_channel` VARCHAR(20) DEFAULT NULL COMMENT '支付渠道',
  `verify_type` VARCHAR(20) DEFAULT NULL COMMENT '验证方式',
  `status_detail` VARCHAR(100) DEFAULT NULL COMMENT '状态说明',
  `client_scene` VARCHAR(30) DEFAULT NULL COMMENT '客户端场景',
  `transaction_id` VARCHAR(64) COMMENT '第三方交易号',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0待支付 1已支付 2已退款',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `pay_time` DATETIME COMMENT '支付时间',
  `refund_time` DATETIME COMMENT '退款时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(100) DEFAULT NULL COMMENT '修改人',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '删除标识',
  INDEX `idx_order_id` (`order_id`),
  INDEX `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录';

-- ============================================
-- 测试数据
-- ============================================

-- 商品分类测试数据
INSERT INTO `product_category` (`name`, `icon`, `sort_order`) VALUES
('食品', '🍖', 1),
('用品', '🧸', 2),
('玩具', '🎾', 3),
('保健品', '💊', 4);

-- 添加缺失的分类记录（用于搜索测试）
INSERT INTO `product_category` (`id`, `name`, `icon`, `sort_order`, `status`, `create_time`, `modify_time`, `is_deleted`)
VALUES
(7, '宠物玩具', '/icon/toy.png', 7, 1, NOW(), NOW(), 0),
(8, '宠物清洁', '/icon/clean.png', 8, 1, NOW(), NOW(), 0)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 商品测试数据
INSERT INTO `product` (`category_id`, `name`, `cover_url`, `summary`, `price`, `original_price`, `sales`, `stock`, `tag`) VALUES
(1, '进口天然猫粮 2kg', '/uploads/products/cat_food.jpg', '天然无谷配方，营养均衡', 199.00, 299.00, 23000, 500, 'hot'),
(2, '智能饮水机', '/uploads/products/water_fountain.jpg', '智能补水，水质过滤', 299.00, 399.00, 12000, 200, 'new'),
(3, '狗狗咬胶玩具', '/uploads/products/dog_toy.jpg', '耐咬无毒，清洁牙齿', 49.00, 69.00, 56000, 100, 'hot'),
(4, '宠物复合维生素', '/uploads/products/vitamins.jpg', '全面营养，增强体质', 128.00, 168.00, 8900, 150, ''),
(5, '全自动猫砂盆', '/uploads/products/litter_box.jpg', '自动铲砂，无味无菌', 899.00, 1299.00, 6700, 50, ''),
(6, '宠物除臭剂', '/uploads/products/deodorizer.jpg', '天然植物，安全无害', 79.00, 99.00, 15000, 300, ''),
(7, '狗狗飞盘', '/uploads/products/frisbee.jpg', '柔软耐用，互动性强', 35.00, 49.00, 32000, 200, ''),
(8, '营养化毛膏', '/uploads/products/hairball_paste.jpg', '温和配方，不油腻', 68.00, 88.00, 9800, 100, '');

-- 课程测试数据
INSERT INTO `course` (`title`, `description`, `lesson_count`, `student_count`, `price`, `tag`) VALUES
('幼犬基础训练课', '从零开始训练您的狗狗，建立良好习惯', 12, 23000, 0, '入门'),
('狗狗行为矫正', '解决吠叫、扑人等行为问题', 8, 15000, 99, '进阶'),
('猫咪好习惯养成', '让猫咪养成良好生活习惯', 10, 32000, 0, '热门'),
('宠物社交训练', '让宠物学会与其他动物友好相处', 6, 8900, 69, '实战');

-- 美容门店测试数据
INSERT INTO `beauty_store` (`name`, `rating`, `distance`, `address`, `tags`) VALUES
('萌宠美容馆', 4.9, '1.2km', '朝阳区建国路88号', '洗护,美容,寄养'),
('精致宠护中心', 4.8, '2.5km', '海淀区中关村大街100号', 'SPA,染色,造型'),
('爱宠护理店', 4.7, '3.1km', '丰台区西三环南路66号', '洗护,美容');

-- 文章测试数据
INSERT INTO `article` (`title`, `summary`, `content`, `tag`, `publish_time`) VALUES
('春季宠物护理指南', '专家建议的春季健康管理方案，让爱宠健康度过换季期', '春季是宠物健康管理的重要时期。随着气温回升，宠物的新陈代谢逐渐旺盛，同时也是各种寄生虫和细菌活跃的季节。\n\n首先，要注意宠物的日常清洁。春季宠物容易掉毛，需要增加梳毛频率，促进血液循环，减少毛发打结。\n\n其次，要注重驱虫工作。春季是寄生虫繁殖的高峰期，建议每月进行一次体内外驱虫，保障宠物健康。\n\n此外，适当增加户外活动时间，让宠物多晒太阳，增强免疫力。但要注意避免花粉过敏等问题。\n\n最后，定期带宠物到宠物医院进行健康检查，及时发现并处理潜在的健康问题。', '护理', '2024-03-15 10:00:00'),
('新宠优惠套餐', '首次体验洗护+驱虫8折优惠，给爱宠最好的呵护', '欢迎新用户加入伴宠云诊！我们为新用户准备了超值优惠套餐。\n\n套餐一：首次洗护体验\n• 精致洗澡 + 精修毛发 + 耳部清洁\n• 原价198元，新用户专享价仅需99元\n• 适用：小型犬、猫咪\n\n套餐二：基础驱虫套餐\n• 体内外驱虫一次\n• 赠送健康检查一次\n• 原价168元，新用户专享价仅需128元\n\n活动时间：2024年3月1日-3月31日\n\n每人限购一次，数量有限，先到先得！', '活动', '2024-03-10 14:00:00');

-- 医生测试数据
INSERT INTO `doctor` (`name`, `avatar`, `title`, `specialty`, `department`, `experience`, `description`, `hospital_name`, `consultation_fee`, `rating`, `consultation_count`, `tags`) VALUES
('张医生', 'https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?w=200', '主治医师', '犬猫常见疾病,皮肤病,内科', '内科', 10, '擅长小动物内科疾病诊治，拥有丰富的临床经验', '伴宠宠物医院', 50.00, 4.9, 1520, '耐心,专业'),
('李医生', 'https://images.unsplash.com/photo-1582750433449-648ed127bb54?w=200', '副主任医师', '外科手术,骨科,急诊', '外科', 15, '资深外科专家，擅长复杂骨科手术', '爱宠动物医院', 80.00, 4.8, 980, '经验丰富,技术精湛'),
('王医生', 'https://images.unsplash.com/photo-1594824476967-48c8b964273f?w=200', '主治医师', '皮肤病,过敏,耳病', '皮肤科', 8, '专注于宠物皮肤病诊治，对各类过敏有深入研究', '萌宠专科医院', 60.00, 4.7, 850, '皮肤专家'),
('赵医生', 'https://images.unsplash.com/photo-1537368910025-700350fe46c7?w=200', '主任医师', '心脏病,肾病,老年病', '专科', 20, '资深专家，擅长宠物老年病及慢性病管理', '伴宠宠物医院', 100.00, 5.0, 620, '权威,资深'),
('刘医生', 'https://images.unsplash.com/photo-1559839734-2b71ea197ec2?w=200', '主治医师', '眼科,口腔科', '专科', 12, '专注宠物眼科和口腔疾病诊治', '爱宠动物医院', 70.00, 4.9, 740, '细致,认真');

-- 优惠券测试数据
INSERT INTO `coupon` (`name`, `type`, `discount_amount`, `discount_rate`, `min_amount`, `total_count`, `valid_days`, `start_time`, `end_time`, `description`, `status`) VALUES
('新用户专享券', 1, 20.00, NULL, 99.00, 10000, 30, '2024-01-01 00:00:00', '2024-12-31 23:59:59', '新用户首单满99减20', 1),
('满199减30', 1, 30.00, NULL, 199.00, 5000, 15, '2024-01-01 00:00:00', '2024-12-31 23:59:59', '全场通用，满199减30', 1),
('9折优惠券', 2, NULL, 0.90, 50.00, 3000, 7, '2024-01-01 00:00:00', '2024-12-31 23:59:59', '全场通用，立享9折', 1),
('满299减50', 1, 50.00, NULL, 299.00, 2000, 15, '2024-01-01 00:00:00', '2024-12-31 23:59:59', '大额优惠，满299减50', 1);

-- 系统配置测试数据
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES
('hot_search_keywords', '猫粮,狗粮,驱虫,疫苗,宠物医院,美容,洗护,玩具', '热门搜索关键词'),
('service_phone', '400-123-4567', '客服电话'),
('about_us', '伴宠云诊致力于为宠物主人提供便捷的在线问诊和优质商品服务', '关于我们'),
('user_agreement', '用户协议内容...', '用户协议'),
('privacy_policy', '隐私政策内容...', '隐私政策');

-- ============================================
-- 数据库迁移脚本 (用于已存在的数据库)
-- ============================================

-- 为 consultation 表添加缺失的字段 (兼容 MySQL 语法)
-- 使用存储过程安全地添加列（如果不存在）
DROP PROCEDURE IF EXISTS add_column_if_not_exists;
DELIMITER //
CREATE PROCEDURE add_column_if_not_exists()
BEGIN
    -- 添加 creator_id 列
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'consultation'
        AND COLUMN_NAME = 'creator_id'
    ) THEN
        ALTER TABLE consultation ADD COLUMN creator_id BIGINT DEFAULT NULL COMMENT '创建人ID' AFTER fee;
    END IF;

    -- 添加 creator_name 列
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'consultation'
        AND COLUMN_NAME = 'creator_name'
    ) THEN
        ALTER TABLE consultation ADD COLUMN creator_name VARCHAR(100) DEFAULT NULL COMMENT '创建人' AFTER creator_id;
    END IF;
END //
DELIMITER ;
CALL add_column_if_not_exists();
DROP PROCEDURE IF EXISTS add_column_if_not_exists;

-- 更新商品库存 (修复购物车测试问题)
UPDATE product SET stock = 100 WHERE id = 3 AND stock < 10;

-- ============================================
-- 补充表（从数据库导出 - 2026-02-26）
-- ============================================

-- 管理员表
CREATE TABLE IF NOT EXISTS `admin` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `gender` tinyint NOT NULL DEFAULT '0' COMMENT '性别(0未知/1男/2女)',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `mask_phone` varchar(20) DEFAULT NULL COMMENT '脱敏手机号(如138****1234)',
  `encrypted_phone` varchar(255) DEFAULT NULL COMMENT '加密手机号',
  `salt` varchar(32) DEFAULT NULL COMMENT '盐',
  `encrypted_password` varchar(255) NOT NULL COMMENT '密码MD5(密码+盐)',
  `unionid` varchar(100) DEFAULT NULL COMMENT '微信UnionID',
  `id_card` varchar(255) DEFAULT NULL COMMENT '身份证号',
  `encrypted_id_card` varchar(255) DEFAULT NULL COMMENT '加密身份证号',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `encrypted_payment_password` varchar(255) DEFAULT NULL COMMENT '加密支付密码',
  `status` tinyint DEFAULT '0' COMMENT '状态(0禁用/1启用)',
  `creator_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` bigint DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` varchar(50) DEFAULT NULL COMMENT '修改人姓名',
  `modify_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `phone` (`phone`),
  KEY `idx_gender` (`gender`),
  KEY `idx_unionid` (`unionid`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员表';

-- AI聊天记录表
CREATE TABLE IF NOT EXISTS `ai_chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `conversation_id` bigint NOT NULL COMMENT '会话ID',
  `role` varchar(20) NOT NULL COMMENT '角色: user/assistant',
  `content` text NOT NULL COMMENT '消息内容',
  `model_type` varchar(20) DEFAULT NULL COMMENT '模型类型: qwen/deepseek',
  `creator_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建人名称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` bigint DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` varchar(50) DEFAULT NULL COMMENT '修改人名称',
  `modify_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除: 0否 1是',
  PRIMARY KEY (`id`),
  KEY `idx_user_conversation` (`user_id`,`conversation_id`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI聊天记录';

-- 文章评论表
CREATE TABLE IF NOT EXISTS `article_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `user_nickname` varchar(100) DEFAULT NULL COMMENT '用户昵称',
  `user_avatar` varchar(500) DEFAULT NULL COMMENT '用户头像',
  `content` varchar(500) NOT NULL COMMENT '评论内容',
  `parent_id` bigint DEFAULT NULL COMMENT '父评论ID（用于回复）',
  `reply_to_user_id` bigint DEFAULT NULL COMMENT '回复目标用户ID',
  `reply_to_nickname` varchar(100) DEFAULT NULL COMMENT '回复目标用户昵称',
  `like_count` int DEFAULT '0' COMMENT '点赞数',
  `creator_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `creator_name` varchar(100) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` bigint DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` varchar(100) DEFAULT NULL COMMENT '修改人姓名',
  `modify_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文章评论表';

-- 会话列表
CREATE TABLE IF NOT EXISTS `conversation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` varchar(20) NOT NULL COMMENT '会话类型: ai_chat/doctor_consultation/customer_service',
  `target_id` bigint DEFAULT NULL COMMENT '目标ID(医生ID等)',
  `target_name` varchar(50) NOT NULL COMMENT '目标名称',
  `target_avatar` varchar(255) DEFAULT NULL COMMENT '目标头像',
  `last_message` varchar(200) DEFAULT NULL COMMENT '最后一条消息内容',
  `last_message_time` datetime DEFAULT NULL COMMENT '最后消息时间',
  `unread_count` int DEFAULT '0' COMMENT '未读消息数',
  `is_pinned` tinyint DEFAULT '0' COMMENT '是否置顶: 0否 1是',
  `status` varchar(20) DEFAULT 'active' COMMENT '状态: active/archived/deleted',
  `creator_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建人名称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` bigint DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` varchar(50) DEFAULT NULL COMMENT '修改人名称',
  `modify_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除: 0否 1是',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_type` (`user_id`,`type`),
  KEY `idx_last_message_time` (`last_message_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会话列表';

-- 积分流水表
CREATE TABLE IF NOT EXISTS `points_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `points` int NOT NULL COMMENT '变动积分（正数增加，负数减少）',
  `balance` int NOT NULL COMMENT '变动后余额',
  `type` tinyint NOT NULL COMMENT '类型: 1-任务奖励 2-签到奖励 3-兑换消耗 4-其他',
  `related_id` bigint DEFAULT NULL COMMENT '关联ID（任务ID、订单ID等）',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注说明',
  `creator_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` bigint DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` varchar(50) DEFAULT NULL COMMENT '修改人姓名',
  `modify_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分流水表';

-- 商品收藏表
CREATE TABLE IF NOT EXISTS `product_collection` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(200) DEFAULT NULL COMMENT '商品名称',
  `product_image` varchar(500) DEFAULT NULL COMMENT '商品图片',
  `product_price` decimal(10,2) DEFAULT NULL COMMENT '商品价格',
  `creator_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` bigint DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` varchar(50) DEFAULT NULL COMMENT '修改人姓名',
  `modify_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`,`product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品收藏表';

-- 评价点赞表
CREATE TABLE IF NOT EXISTS `product_review_like` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `review_id` bigint NOT NULL COMMENT '评价ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review_user` (`review_id`,`user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_review_id` (`review_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价点赞表';

-- 满减活动表
CREATE TABLE IF NOT EXISTS `promotion` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '活动名称',
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '类型：1满减',
  `threshold` decimal(10,2) NOT NULL COMMENT '满减门槛金额',
  `discount` decimal(10,2) NOT NULL COMMENT '优惠金额',
  `start_time` varchar(30) NOT NULL COMMENT '开始时间',
  `end_time` varchar(30) NOT NULL COMMENT '结束时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0禁用 1启用',
  `description` varchar(500) DEFAULT NULL COMMENT '活动描述',
  `creator_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` bigint DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` varchar(50) DEFAULT NULL COMMENT '修改人姓名',
  `modify_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除：0否 1是',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='满减活动表';

-- 任务定义表
CREATE TABLE IF NOT EXISTS `task_definition` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_code` varchar(50) NOT NULL COMMENT '任务编码，唯一标识',
  `task_name` varchar(100) NOT NULL COMMENT '任务名称',
  `task_desc` varchar(500) DEFAULT NULL COMMENT '任务描述',
  `task_icon` varchar(50) DEFAULT NULL COMMENT '任务图标（emoji或图标名）',
  `task_type` tinyint DEFAULT '1' COMMENT '任务类型: 1-每日任务 2-每周任务 3-一次性任务',
  `points` int NOT NULL DEFAULT '0' COMMENT '完成奖励积分',
  `sort_order` int DEFAULT '0' COMMENT '排序顺序',
  `status` tinyint DEFAULT '1' COMMENT '状态: 0-禁用 1-启用',
  `creator_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` bigint DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` varchar(50) DEFAULT NULL COMMENT '修改人姓名',
  `modify_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除: 0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_code` (`task_code`),
  KEY `idx_status` (`status`),
  KEY `idx_task_type` (`task_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务定义表';

-- 用户积分表
CREATE TABLE IF NOT EXISTS `user_points` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `total_points` int NOT NULL DEFAULT '0' COMMENT '总积分',
  `used_points` int NOT NULL DEFAULT '0' COMMENT '已使用积分',
  `level` int DEFAULT '1' COMMENT '用户等级',
  `creator_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` bigint DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` varchar(50) DEFAULT NULL COMMENT '修改人姓名',
  `modify_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除: 0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户积分表';

-- 用户任务记录表
CREATE TABLE IF NOT EXISTS `user_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `task_id` bigint NOT NULL COMMENT '任务定义ID',
  `task_code` varchar(50) NOT NULL COMMENT '任务编码',
  `points` int NOT NULL DEFAULT '0' COMMENT '获得积分',
  `status` tinyint DEFAULT '1' COMMENT '状态: 0-未完成 1-已完成',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `task_date` date NOT NULL COMMENT '任务日期（用于每日任务）',
  `creator_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `creator_name` varchar(50) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` bigint DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` varchar(50) DEFAULT NULL COMMENT '修改人姓名',
  `modify_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_task_date` (`user_id`,`task_id`,`task_date`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_task_date` (`task_date`),
  KEY `idx_user_task_date` (`user_id`,`task_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户任务记录表';

-- ============================================
-- 补充表结束
-- ============================================
