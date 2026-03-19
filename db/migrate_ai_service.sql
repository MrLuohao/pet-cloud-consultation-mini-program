-- ============================================
-- AI service / diagnosis / media asset 基础表
-- 创建时间: 2026-03-15
-- ============================================

USE `pet_cloud_db`;

CREATE TABLE IF NOT EXISTS `ai_task` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `task_no` VARCHAR(64) NOT NULL COMMENT '任务编号',
  `task_type` VARCHAR(50) NOT NULL COMMENT '任务类型',
  `biz_type` VARCHAR(50) DEFAULT NULL COMMENT '业务类型',
  `biz_id` BIGINT DEFAULT NULL COMMENT '业务ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
  `guest_device_hash` VARCHAR(128) DEFAULT NULL COMMENT '游客设备哈希',
  `model_provider` VARCHAR(50) DEFAULT NULL COMMENT '模型提供方',
  `model_name` VARCHAR(100) DEFAULT NULL COMMENT '模型名称',
  `prompt_version` VARCHAR(50) DEFAULT NULL COMMENT 'Prompt版本',
  `template_version` VARCHAR(50) DEFAULT NULL COMMENT '模板版本',
  `status` VARCHAR(30) NOT NULL COMMENT '任务状态',
  `input_snapshot` LONGTEXT DEFAULT NULL COMMENT '输入快照',
  `output_snapshot` LONGTEXT DEFAULT NULL COMMENT '输出快照',
  `error_message` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
  `latency_ms` BIGINT DEFAULT NULL COMMENT '耗时毫秒',
  `trace_id` VARCHAR(64) DEFAULT NULL COMMENT '追踪ID',
  `creator_id` BIGINT DEFAULT NULL,
  `creator_name` VARCHAR(100) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `modifier_id` BIGINT DEFAULT NULL,
  `modifier_name` VARCHAR(100) DEFAULT NULL,
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  UNIQUE KEY `uk_ai_task_no` (`task_no`),
  KEY `idx_ai_task_user` (`user_id`),
  KEY `idx_ai_task_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI任务表';

CREATE TABLE IF NOT EXISTS `diagnosis_record` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `task_id` BIGINT NOT NULL COMMENT 'AI任务ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
  `pet_id` BIGINT DEFAULT NULL COMMENT '宠物ID',
  `guest_device_hash` VARCHAR(128) DEFAULT NULL COMMENT '游客设备哈希',
  `symptom_tags_json` JSON DEFAULT NULL COMMENT '症状标签',
  `symptom_description` TEXT NOT NULL COMMENT '症状描述',
  `risk_level` VARCHAR(20) DEFAULT NULL COMMENT '风险等级',
  `summary` VARCHAR(500) DEFAULT NULL COMMENT '诊断摘要',
  `possible_causes_json` JSON DEFAULT NULL COMMENT '可能原因',
  `care_suggestions_json` JSON DEFAULT NULL COMMENT '护理建议',
  `next_actions_json` JSON DEFAULT NULL COMMENT '后续动作',
  `observation_table_json` JSON DEFAULT NULL COMMENT '观察表格',
  `should_consult_doctor` TINYINT DEFAULT 0 COMMENT '是否建议就医',
  `status` VARCHAR(20) DEFAULT 'observing' COMMENT '病历状态',
  `diagnosis_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '诊断时间',
  `creator_id` BIGINT DEFAULT NULL,
  `creator_name` VARCHAR(100) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `modifier_id` BIGINT DEFAULT NULL,
  `modifier_name` VARCHAR(100) DEFAULT NULL,
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  KEY `idx_diag_task` (`task_id`),
  KEY `idx_diag_user_pet` (`user_id`, `pet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI诊断记录';

CREATE TABLE IF NOT EXISTS `diagnosis_extracted_info` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `record_id` BIGINT NOT NULL COMMENT '诊断记录ID',
  `primary_symptoms_json` JSON DEFAULT NULL COMMENT '主要症状',
  `duration_text` VARCHAR(100) DEFAULT NULL COMMENT '持续时间',
  `severity` VARCHAR(50) DEFAULT NULL COMMENT '严重程度',
  `suspected_issues_json` JSON DEFAULT NULL COMMENT '疑似问题',
  `affected_parts_json` JSON DEFAULT NULL COMMENT '影响部位',
  `follow_up_focus_json` JSON DEFAULT NULL COMMENT '复查重点',
  `extract_version` VARCHAR(50) DEFAULT NULL COMMENT '提取版本',
  `creator_id` BIGINT DEFAULT NULL,
  `creator_name` VARCHAR(100) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `modifier_id` BIGINT DEFAULT NULL,
  `modifier_name` VARCHAR(100) DEFAULT NULL,
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  KEY `idx_diag_extract_record` (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI诊断关键信息提取';

CREATE TABLE IF NOT EXISTS `media_asset` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `asset_no` VARCHAR(64) NOT NULL COMMENT '资产编号',
  `owner_type` VARCHAR(50) DEFAULT NULL COMMENT '归属类型',
  `owner_id` BIGINT DEFAULT NULL COMMENT '归属ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
  `media_type` VARCHAR(20) NOT NULL COMMENT '媒体类型',
  `url` VARCHAR(500) NOT NULL COMMENT '文件URL',
  `mime_type` VARCHAR(100) DEFAULT NULL COMMENT 'mime类型',
  `file_size` BIGINT DEFAULT NULL COMMENT '文件大小',
  `upload_status` VARCHAR(30) DEFAULT 'uploaded' COMMENT '上传状态',
  `moderation_status` VARCHAR(20) DEFAULT 'pass' COMMENT '审核状态',
  `risk_tags_json` JSON DEFAULT NULL COMMENT '风险标签',
  `reason` VARCHAR(255) DEFAULT NULL COMMENT '审核说明',
  `creator_id` BIGINT DEFAULT NULL,
  `creator_name` VARCHAR(100) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `modifier_id` BIGINT DEFAULT NULL,
  `modifier_name` VARCHAR(100) DEFAULT NULL,
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  UNIQUE KEY `uk_media_asset_no` (`asset_no`),
  KEY `idx_media_user` (`user_id`),
  KEY `idx_media_status` (`moderation_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='媒体资产表';

CREATE TABLE IF NOT EXISTS `featured_content_draft` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `draft_date` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '草稿日期',
  `source_type` VARCHAR(30) NOT NULL COMMENT '来源类型',
  `source_id` BIGINT NOT NULL COMMENT '来源ID',
  `title` VARCHAR(120) NOT NULL COMMENT '标题',
  `summary` VARCHAR(300) DEFAULT NULL COMMENT '摘要',
  `tag` VARCHAR(50) DEFAULT NULL COMMENT '标签',
  `reason_label` VARCHAR(100) DEFAULT NULL COMMENT '推荐理由',
  `cover_url` VARCHAR(500) DEFAULT NULL COMMENT '封面',
  `ranking_score` INT DEFAULT 0 COMMENT '排序分',
  `status` VARCHAR(30) DEFAULT 'draft' COMMENT '草稿状态',
  `creator_id` BIGINT DEFAULT NULL,
  `creator_name` VARCHAR(100) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `modifier_id` BIGINT DEFAULT NULL,
  `modifier_name` VARCHAR(100) DEFAULT NULL,
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` TINYINT DEFAULT 0,
  KEY `idx_featured_draft_date` (`draft_date`),
  KEY `idx_featured_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页精选草稿';
