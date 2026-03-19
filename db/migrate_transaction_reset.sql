-- ============================================================
-- 交易域重置迁移
-- 日期：2026-03-15
-- 目标：以 .pen 交易模型为准，重置购物车/结算/支付/地址相关结构
-- ============================================================

USE `pet_cloud_db`;

-- ============================================================
-- 0. 清理历史测试数据
-- ============================================================
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `shopping_cart`;
TRUNCATE TABLE `order_item`;
TRUNCATE TABLE `payment_record`;
TRUNCATE TABLE `order_info`;
TRUNCATE TABLE `user_coupon`;
TRUNCATE TABLE `user_address`;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 1. product 扩展店铺和默认规格
-- ============================================================
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'product' AND column_name = 'shop_id');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `product` ADD COLUMN `shop_id` VARCHAR(64) DEFAULT NULL COMMENT ''店铺标识'' AFTER `tag`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'product' AND column_name = 'shop_name');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `product` ADD COLUMN `shop_name` VARCHAR(100) DEFAULT NULL COMMENT ''店铺名称'' AFTER `shop_id`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'product' AND column_name = 'service_text');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `product` ADD COLUMN `service_text` VARCHAR(100) DEFAULT NULL COMMENT ''服务文案'' AFTER `shop_name`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'product' AND column_name = 'default_spec');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `product` ADD COLUMN `default_spec` VARCHAR(100) DEFAULT NULL COMMENT ''默认规格'' AFTER `service_text`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE `product`
SET `shop_id` = COALESCE(`shop_id`, 'official'),
    `shop_name` = COALESCE(`shop_name`, '伴宠云诊自营'),
    `service_text` = COALESCE(`service_text`, '包邮 · 正品保障'),
    `default_spec` = COALESCE(`default_spec`, '默认规格');

-- ============================================================
-- 2. shopping_cart 扩展快照字段
-- ============================================================
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'shopping_cart' AND column_name = 'shop_id');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `shopping_cart` ADD COLUMN `shop_id` VARCHAR(64) DEFAULT NULL COMMENT ''店铺标识'' AFTER `quantity`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'shopping_cart' AND column_name = 'shop_name');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `shopping_cart` ADD COLUMN `shop_name` VARCHAR(100) DEFAULT NULL COMMENT ''店铺名称'' AFTER `shop_id`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'shopping_cart' AND column_name = 'service_text');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `shopping_cart` ADD COLUMN `service_text` VARCHAR(100) DEFAULT NULL COMMENT ''服务文案'' AFTER `shop_name`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'shopping_cart' AND column_name = 'spec_label');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `shopping_cart` ADD COLUMN `spec_label` VARCHAR(100) DEFAULT NULL COMMENT ''规格说明'' AFTER `service_text`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'shopping_cart' AND column_name = 'sku_code');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `shopping_cart` ADD COLUMN `sku_code` VARCHAR(64) DEFAULT NULL COMMENT ''SKU编码'' AFTER `spec_label`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'shopping_cart' AND column_name = 'selected');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `shopping_cart` ADD COLUMN `selected` TINYINT NOT NULL DEFAULT 1 COMMENT ''是否选中'' AFTER `sku_code`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'shopping_cart' AND column_name = 'status');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `shopping_cart` ADD COLUMN `status` VARCHAR(20) NOT NULL DEFAULT ''active'' COMMENT ''状态 active/invalid'' AFTER `selected`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'shopping_cart' AND column_name = 'price_snapshot');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `shopping_cart` ADD COLUMN `price_snapshot` DECIMAL(10,2) DEFAULT NULL COMMENT ''价格快照'' AFTER `status`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'shopping_cart' AND column_name = 'original_price_snapshot');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `shopping_cart` ADD COLUMN `original_price_snapshot` DECIMAL(10,2) DEFAULT NULL COMMENT ''原价快照'' AFTER `price_snapshot`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- 3. order_item 扩展订单快照字段
-- ============================================================
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'order_item' AND column_name = 'shop_id');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `order_item` ADD COLUMN `shop_id` VARCHAR(64) DEFAULT NULL COMMENT ''店铺标识'' AFTER `cover_url`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'order_item' AND column_name = 'shop_name');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `order_item` ADD COLUMN `shop_name` VARCHAR(100) DEFAULT NULL COMMENT ''店铺名称'' AFTER `shop_id`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'order_item' AND column_name = 'service_text');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `order_item` ADD COLUMN `service_text` VARCHAR(100) DEFAULT NULL COMMENT ''服务文案'' AFTER `shop_name`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'order_item' AND column_name = 'spec_snapshot');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `order_item` ADD COLUMN `spec_snapshot` VARCHAR(100) DEFAULT NULL COMMENT ''规格快照'' AFTER `service_text`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'order_item' AND column_name = 'sku_snapshot');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `order_item` ADD COLUMN `sku_snapshot` VARCHAR(100) DEFAULT NULL COMMENT ''SKU快照'' AFTER `spec_snapshot`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'order_item' AND column_name = 'original_price');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `order_item` ADD COLUMN `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT ''商品原价'' AFTER `price`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- 4. payment_record 扩展支付草稿字段
-- ============================================================
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'payment_record' AND column_name = 'payment_channel');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `payment_record` ADD COLUMN `payment_channel` VARCHAR(20) DEFAULT NULL COMMENT ''支付渠道'' AFTER `payment_method`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'payment_record' AND column_name = 'verify_type');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `payment_record` ADD COLUMN `verify_type` VARCHAR(20) DEFAULT NULL COMMENT ''验证方式'' AFTER `payment_channel`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'payment_record' AND column_name = 'status_detail');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `payment_record` ADD COLUMN `status_detail` VARCHAR(100) DEFAULT NULL COMMENT ''状态说明'' AFTER `verify_type`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'payment_record' AND column_name = 'client_scene');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `payment_record` ADD COLUMN `client_scene` VARCHAR(30) DEFAULT NULL COMMENT ''客户端场景'' AFTER `status_detail`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_length = (
  SELECT CHARACTER_MAXIMUM_LENGTH
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'order_status_history'
    AND column_name = 'order_no'
  LIMIT 1
);
SET @sql = IF(
  @col_length IS NOT NULL AND @col_length < 64,
  'ALTER TABLE `order_status_history` MODIFY COLUMN `order_no` VARCHAR(64) DEFAULT NULL COMMENT ''订单号''',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- 5. user_address 扩展地图和解析字段
-- ============================================================
SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user_address' AND column_name = 'longitude');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `user_address` ADD COLUMN `longitude` DECIMAL(11,6) DEFAULT NULL COMMENT ''经度'' AFTER `is_default`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user_address' AND column_name = 'latitude');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `user_address` ADD COLUMN `latitude` DECIMAL(10,6) DEFAULT NULL COMMENT ''纬度'' AFTER `longitude`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user_address' AND column_name = 'business_area');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `user_address` ADD COLUMN `business_area` VARCHAR(100) DEFAULT NULL COMMENT ''商圈'' AFTER `latitude`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user_address' AND column_name = 'door_no');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `user_address` ADD COLUMN `door_no` VARCHAR(100) DEFAULT NULL COMMENT ''门牌号'' AFTER `business_area`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user_address' AND column_name = 'raw_text');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `user_address` ADD COLUMN `raw_text` VARCHAR(500) DEFAULT NULL COMMENT ''原始粘贴文本'' AFTER `door_no`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user_address' AND column_name = 'parsed_name');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `user_address` ADD COLUMN `parsed_name` VARCHAR(50) DEFAULT NULL COMMENT ''解析联系人'' AFTER `raw_text`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user_address' AND column_name = 'parsed_phone');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `user_address` ADD COLUMN `parsed_phone` VARCHAR(20) DEFAULT NULL COMMENT ''解析手机号'' AFTER `parsed_name`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user_address' AND column_name = 'map_address');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `user_address` ADD COLUMN `map_address` VARCHAR(300) DEFAULT NULL COMMENT ''地图地址描述'' AFTER `parsed_phone`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'user_address' AND column_name = 'address_tag');
SET @sql = IF(@col_exists = 0, 'ALTER TABLE `user_address` ADD COLUMN `address_tag` VARCHAR(50) DEFAULT NULL COMMENT ''地址标签'' AFTER `map_address`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
