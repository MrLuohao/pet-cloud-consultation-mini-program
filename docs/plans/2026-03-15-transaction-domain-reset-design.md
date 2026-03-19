# 交易域重构设计

日期：2026-03-15

## 目标

以 `.pen` 画布为唯一业务形态来源，重做交易域后端模型，覆盖以下页面链路：

- 购物车页
- 结算页
- 支付方式弹层
- 支付验证层
- 地址选择页

本次不保留历史测试数据兼容，不以旧接口和旧库表为约束。

## 设计原则

- 接口以页面读模型为中心，而不是返回数据库薄表
- 购物车、结算、支付、地址各自具备独立聚合 VO
- 下单时必须保存商品快照，禁止后续依赖商品现态反推订单语义
- 地址支持地图选点和地址解析的扩展字段
- 支付草稿支持多支付方式和验证方式
- 历史测试交易数据全部清空，避免脏数据污染新模型

## 模型调整

### 购物车

- `shopping_cart` 扩展：
  - `shop_id`
  - `shop_name`
  - `service_text`
  - `spec_label`
  - `sku_code`
  - `selected`
  - `status`
  - `price_snapshot`
  - `original_price_snapshot`

- 接口返回 `CartPageVO`
  - `cartGroups`
  - `invalidItems`
  - `summary`

### 结算

- `OrderConfirmVO` 扩展为 checkout draft：
  - `items`
  - `address`
  - `availableCoupons`
  - `paymentMethods`
  - `selectedPaymentMethod`
  - `goodsAmount`
  - `freight`
  - `couponDiscount`
  - `payAmount`
  - `deliveryText`
  - `orderHint`

### 订单快照

- `order_item` 扩展：
  - `shop_id`
  - `shop_name`
  - `service_text`
  - `spec_snapshot`
  - `sku_snapshot`
  - `original_price`

### 支付

- `payment_record` 扩展：
  - `payment_channel`
  - `verify_type`
  - `status_detail`
  - `client_scene`

- 支持支付方式：
  - `wechat`
  - `alipay`
  - `bank`
  - `credit`

### 地址

- `user_address` 扩展：
  - `latitude`
  - `longitude`
  - `business_area`
  - `door_no`
  - `raw_text`
  - `parsed_name`
  - `parsed_phone`
  - `map_address`
  - `address_tag`

## 接口策略

- 直接升级现有交易接口返回模型
- 提交订单请求补充：
  - `cartIds`
  - `paymentMethod`
  - `verificationType`
- 保留原 URL，不保留旧薄模型语义

## 数据策略

- 执行重构迁移前清空以下历史测试数据：
  - `shopping_cart`
  - `order_item`
  - `order_info`
  - `payment_record`
  - `user_address`
  - `user_coupon`
- 删除项目内历史上传测试图片和视频

## 验证策略

- 先写单元测试锁定新模型
- 再执行库表迁移
- 最后跑受影响模块测试和编译
