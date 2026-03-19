-- ============================================================
-- 商品详情结构化配置迁移
-- 目标：
-- 1. product 表新增规格组与详情内容结构化字段
-- 2. shopping_cart 唯一键升级为 user + product + spec_label
-- ============================================================

SET @col_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'product'
    AND column_name = 'spec_groups_json'
);
SET @sql = IF(
  @col_exists = 0,
  'ALTER TABLE `product` ADD COLUMN `spec_groups_json` LONGTEXT DEFAULT NULL COMMENT ''规格组配置(JSON)'' AFTER `default_spec`',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
  SELECT COUNT(*)
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'product'
    AND column_name = 'detail_content_json'
);
SET @sql = IF(
  @col_exists = 0,
  'ALTER TABLE `product` ADD COLUMN `detail_content_json` LONGTEXT DEFAULT NULL COMMENT ''详情内容配置(JSON)'' AFTER `spec_groups_json`',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'shopping_cart'
    AND index_name = 'uk_user_product'
);
SET @sql = IF(
  @idx_exists > 0,
  'ALTER TABLE `shopping_cart` DROP INDEX `uk_user_product`',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'shopping_cart'
    AND index_name = 'uk_user_product_spec'
);
SET @sql = IF(
  @idx_exists = 0,
  'ALTER TABLE `shopping_cart` ADD UNIQUE KEY `uk_user_product_spec` (`user_id`, `product_id`, `spec_label`)',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
