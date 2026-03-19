# 伴宠云诊后端API接口文档

> **文档版本**: v1.0
> **更新日期**: 2026-02-12
> **维护规则**: 每次有接口更新时，必须同步更新本文档

---

## 基础信息

| 服务 | 端口 | 说明 |
|------|------|------|
| 用户服务 | 8117 | pet-cloud-user-service |
| 商城服务 | 8118 | pet-cloud-shop-service |

**认证方式**: JWT Bearer Token 或 `X-User-Id` 请求头

---

## 请求头说明

### 公开接口
无需携带认证信息

### 需要认证的接口
```
Authorization: Bearer {JWT Token}
# 或
X-User-Id: {用户ID}
```

---

## 响应格式

### 统一响应格式
```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

### 错误响应
```json
{
  "success": false,
  "code": 401,
  "message": "未登录或登录已过期",
  "data": null
}
```

---

## 认证模块 (`/api/auth`)

### 1. 微信小程序登录
- **接口**: `POST /api/auth/login`
- **认证**: 不需要
- **请求体**:
```json
{
  "code": "微信登录code",
  "nickname": "用户昵称（可选）",
  "avatarUrl": "头像URL（可选）",
  "gender": 0
}
```
- **响应**:
```json
{
  "success": true,
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "JWT Token",
    "userId": 123,
    "nickname": "宠物主人",
    "avatarUrl": "https://...",
    "isNewUser": false
  }
}
```

### 2. 刷新Token
- **接口**: `POST /api/auth/refresh`
- **认证**: 需要Token
- **请求头**: `Authorization: Bearer {token}`
- **响应**: `String` (新Token)

### 3. 获取用户信息
- **接口**: `GET /api/auth/userinfo`
- **认证**: 需要Token
- **响应**: `LoginVO`

### 4. 更新用户信息
- **接口**: `PUT /api/auth/userinfo`
- **认证**: 需要Token
- **查询参数**:
  - `nickname` (可选): 用户昵称
  - `avatarUrl` (可选): 头像URL
  - `gender` (可选): 性别
- **响应**: `Void`

### 5. 登出
- **接口**: `POST /api/auth/logout`
- **认证**: 需要Token
- **响应**: `Void`

---

## 用户中心模块 (`/api/user`)

### 1. 获取宠物列表
- **接口**: `GET /api/user/pets`
- **认证**: 需要Token
- **响应**: `List<UserPetVO>`

### 2. 添加宠物
- **接口**: `POST /api/user/pet`
- **认证**: 需要Token
- **查询参数**:
  - `petName`: 宠物名称
  - `petType`: 类型 (1:狗 / 2:猫 / 3:其他)
  - `breed`: 品种
  - `gender`: 性别
  - `birthday`: 生日
  - `weight`: 体重
- **响应**: `Long` (宠物ID)

### 3. 获取收货地址列表
- **接口**: `GET /api/user/address`
- **认证**: 需要Token
- **响应**: `List<UserAddressVO>`

### 4. 添加收货地址
- **接口**: `POST /api/user/address`
- **认证**: 需要Token
- **查询参数**:
  - `contactName`: 联系人姓名
  - `contactPhone`: 联系电话
  - `province`: 省份
  - `city`: 城市
  - `district`: 区县
  - `detailAddress`: 详细地址
  - `isDefault`: 是否默认地址
- **响应**: `Long` (地址ID)

### 5. 更新收货地址
- **接口**: `PUT /api/user/address/{id}`
- **认证**: 需要Token
- **路径参数**: `id` - 地址ID
- **查询参数**: 同添加地址（全部可选）
- **响应**: `Void`

---

## 宠物管理模块 (`/api/pet`)

### 1. 获取宠物列表
- **接口**: `GET /api/pet/list`
- **认证**: 需要Token (`X-User-Id`)
- **响应**: `List<UserPetVO>`

### 2. 获取宠物详情
- **接口**: `GET /api/pet/{id}`
- **认证**: 需要Token (`X-User-Id`)
- **路径参数**: `id` - 宠物ID
- **响应**: `UserPetVO`

### 3. 创建宠物
- **接口**: `POST /api/pet/create`
- **认证**: 需要Token (`X-User-Id`)
- **请求体**:
```json
{
  "name": "宠物名称",
  "type": 1,
  "breed": "品种",
  "gender": 1,
  "birthday": "2020-01-01",
  "weight": 5.5,
  "avatarUrl": "头像URL",
  "healthStatus": "健康状况"
}
```
- **响应**: `Long` (宠物ID)

### 4. 更新宠物
- **接口**: `PUT /api/pet/update`
- **认证**: 需要Token (`X-User-Id`)
- **请求体**:
```json
{
  "id": 1,
  "name": "宠物名称",
  "type": 1,
  "breed": "品种",
  "gender": 1,
  "birthday": "2020-01-01",
  "weight": 5.5,
  "avatarUrl": "头像URL",
  "healthStatus": "健康状况"
}
```
- **响应**: `Void`

### 5. 删除宠物
- **接口**: `DELETE /api/pet/delete`
- **认证**: 需要Token (`X-User-Id`)
- **查询参数**: `petId` - 宠物ID
- **响应**: `Void`

---

## 地址管理模块 (`/api/address`)

### 1. 获取地址列表
- **接口**: `GET /api/address/list`
- **认证**: 需要Token (`X-User-Id`)
- **响应**: `List<UserAddressVO>`

### 2. 获取地址详情
- **接口**: `GET /api/address/{id}`
- **认证**: 需要Token (`X-User-Id`)
- **路径参数**: `id` - 地址ID
- **响应**: `UserAddressVO`

### 3. 创建地址
- **接口**: `POST /api/address/create`
- **认证**: 需要Token (`X-User-Id`)
- **请求体**:
```json
{
  "contactName": "张三",
  "contactPhone": "13800138000",
  "province": "北京市",
  "city": "北京市",
  "district": "朝阳区",
  "detailAddress": "某某街道123号",
  "isDefault": 1
}
```
- **响应**: `Long` (地址ID)

### 4. 更新地址
- **接口**: `PUT /api/address/update`
- **认证**: 需要Token (`X-User-Id`)
- **请求体**: 同创建地址
- **响应**: `Void`

### 5. 删除地址
- **接口**: `DELETE /api/address/delete`
- **认证**: 需要Token (`X-User-Id`)
- **查询参数**: `addressId` - 地址ID
- **响应**: `Void`

### 6. 设置默认地址
- **接口**: `PUT /api/address/default`
- **认证**: 需要Token (`X-User-Id`)
- **查询参数**: `addressId` - 地址ID
- **响应**: `Void`

### 7. 获取默认地址
- **接口**: `GET /api/address/default`
- **认证**: 需要Token (`X-User-Id`)
- **响应**: `UserAddressVO`

---

## 健康档案模块 (`/api/health`)

### 1. 获取健康档案列表
- **接口**: `GET /api/health/list`
- **认证**: 需要Token (`X-User-Id`)
- **响应**: `List<HealthRecordVO>`

### 2. 获取指定宠物的健康档案
- **接口**: `GET /api/health/pet/{petId}`
- **认证**: 需要Token (`X-User-Id`)
- **路径参数**: `petId` - 宠物ID
- **响应**: `List<HealthRecordVO>`

### 3. 创建健康档案
- **接口**: `POST /api/health/create`
- **认证**: 需要Token (`X-User-Id`)
- **请求体**:
```json
{
  "petId": 1,
  "recordType": 1,
  "title": "疫苗接种",
  "content": "狂犬疫苗",
  "hospitalName": "某某宠物医院",
  "doctorName": "李医生",
  "recordDate": "2024-01-01",
  "nextDate": "2025-01-01",
  "images": "https://..."
}
```
- **响应**: `Long` (档案ID)

### 4. 更新健康档案
- **接口**: `PUT /api/health/update`
- **认证**: 需要Token (`X-User-Id`)
- **请求体**:
```json
{
  "id": 1,
  "recordType": 1,
  "title": "疫苗接种",
  "content": "狂犬疫苗",
  "hospitalName": "某某宠物医院",
  "doctorName": "李医生",
  "recordDate": "2024-01-01",
  "nextDate": "2025-01-01",
  "images": "https://..."
}
```
- **响应**: `Void`

### 5. 删除健康档案
- **接口**: `DELETE /api/health/delete`
- **认证**: 需要Token (`X-User-Id`)
- **查询参数**: `recordId` - 档案ID
- **响应**: `Void`

---

## 咨询模块 (`/api/consultation`)

### 1. 创建咨询
- **接口**: `POST /api/consultation/create`
- **认证**: 需要Token (`X-User-Id`)
- **请求体**:
```json
{
  "petId": 1,
  "doctorId": 1,
  "type": 1,
  "description": "宠物最近食欲不振",
  "images": "https://..."
}
```
- **响应**: `Long` (咨询ID)

### 2. 获取我的咨询列表
- **接口**: `GET /api/consultation/list`
- **认证**: 需要Token (`X-User-Id`)
- **响应**: `List<ConsultationVO>`

### 3. 获取咨询详情
- **接口**: `GET /api/consultation/{id}`
- **认证**: 需要Token (`X-User-Id`)
- **路径参数**: `id` - 咨询ID
- **响应**: `ConsultationVO`

### 4. 发送消息
- **接口**: `POST /api/consultation/message`
- **认证**: 需要Token (`X-User-Id`)
- **请求体**:
```json
{
  "consultationId": 1,
  "messageType": 1,
  "content": "医生你好",
  "mediaUrl": "https://..."
}
```
- **响应**: `Long` (消息ID)

### 5. 获取聊天记录
- **接口**: `GET /api/consultation/{id}/messages`
- **认证**: 需要Token (`X-User-Id`)
- **路径参数**: `id` - 咨询ID
- **响应**: `List<ConsultationMessageVO>`

### 6. 完成咨询
- **接口**: `PUT /api/consultation/{id}/finish`
- **认证**: 需要Token (`X-User-Id`)
- **路径参数**: `id` - 咨询ID
- **响应**: `Void`

### 7. 取消咨询
- **接口**: `PUT /api/consultation/{id}/cancel`
- **认证**: 需要Token (`X-User-Id`)
- **路径参数**: `id` - 咨询ID
- **响应**: `Void`

---

## 医生模块 (`/api/doctor`)

### 1. 获取医生列表
- **接口**: `GET /api/doctor/list`
- **认证**: 不需要
- **查询参数**:
  - `department` (可选): 科室名称
- **响应**: `List<DoctorVO>`

### 2. 获取医生详情
- **接口**: `GET /api/doctor/{id}`
- **认证**: 不需要
- **路径参数**: `id` - 医生ID
- **响应**: `DoctorVO`

### 3. 获取科室列表
- **接口**: `GET /api/doctor/departments`
- **认证**: 不需要
- **响应**: `List<String>`

---

## 消息模块 (`/api/messages/notifications`)

### 1. 获取消息列表
- **接口**: `GET /api/messages/notifications`
- **认证**: 需要Token
- **响应**: `List<MessageVO>`

### 2. 标记消息已读
- **接口**: `PUT /api/messages/notifications/{id}/read`
- **认证**: 需要Token
- **路径参数**: `id` - 消息ID
- **响应**: `Void`

### 3. 全部标记已读
- **接口**: `PUT /api/messages/notifications/read-all`
- **认证**: 需要Token
- **响应**: `Void`

### 4. 获取未读数量
- **接口**: `GET /api/messages/notifications/unread-count`
- **认证**: 需要Token
- **响应**: `Long`

---

## 文章模块 (`/api/article`)

### 1. 获取文章列表
- **接口**: `GET /api/article/list`
- **认证**: 不需要
- **响应**: `List<ArticleVO>`

### 2. 新增文章
- **接口**: `POST /api/article/create`
- **认证**: 需要Token
- **请求体**:
```json
{
  "title": "文章标题",
  "coverUrl": "https://...",
  "summary": "文章摘要",
  "content": "文章内容",
  "tag": "健康,指南",
  "status": 1
}
```
- **响应**: `Long` (文章ID)
- **验证规则**:
  - `title`: 必填，最大200字符
  - `content`: 必填
  - `coverUrl`: 最大500字符
  - `summary`: 最大500字符
  - `tag`: 最大100字符
  - `status`: 0=草稿, 1=已发布

### 3. 获取文章详情
- **接口**: `GET /api/article/{id}`
- **认证**: 可选（登录后显示点赞/收藏状态）
- **路径参数**: `id` - 文章ID
- **响应**: `ArticleVO`

### 4. 点赞文章
- **接口**: `POST /api/article/{id}/like`
- **认证**: 需要Token
- **路径参数**: `id` - 文章ID
- **响应**: `Void`

### 5. 收藏文章
- **接口**: `POST /api/article/{id}/collect`
- **认证**: 需要Token
- **路径参数**: `id` - 文章ID
- **响应**: `Void`

---

## 课程模块 (`/api/course`)

### 1. 获取课程列表
- **接口**: `GET /api/course/list`
- **认证**: 不需要
- **响应**: `List<CourseVO>`

### 2. 获取课程详情
- **接口**: `GET /api/course/{id}`
- **认证**: 不需要
- **路径参数**: `id` - 课程ID
- **响应**: `CourseVO`

---

## 美容服务模块 (`/api/beauty`)

### 1. 获取门店列表
- **接口**: `GET /api/beauty/stores`
- **认证**: 不需要
- **响应**: `List<BeautyStoreVO>`

### 2. 获取门店详情
- **接口**: `GET /api/beauty/store/{id}`
- **认证**: 不需要
- **路径参数**: `id` - 门店ID
- **响应**: `BeautyStoreVO`

### 3. 创建预约
- **接口**: `POST /api/beauty/booking`
- **认证**: 需要Token
- **请求体**:
```json
{
  "storeId": 1,
  "petId": 1,
  "bookingDate": "2024-03-20",
  "bookingTime": "10:00-12:00",
  "services": "洗护,美容",
  "remark": "备注"
}
```
- **响应**: `Long` (预约ID)

---

## AI服务模块 (`/api/ai`)

### 1. 宠物AI健康诊断
- **接口**: `POST /api/ai/diagnosis`
- **认证**: 不需要
- **请求体**:
```json
{
  "petType": 1,
  "symptoms": "狗狗最近食欲不振，精神萎靡"
}
```
- **响应**: `String` (AI诊断建议)

### 2. AI聊天 - 通义千问
- **接口**: `POST /api/chat/qwen3Max`
- **认证**: 不需要
- **请求体**: `String` (用户消息)
- **响应**: `String` (AI回复)

### 3. AI聊天 - DeepSeek V3
- **接口**: `POST /api/chat/deepSeekV3`
- **认证**: 不需要
- **请求体**: `String` (用户消息)
- **响应**: `String` (AI回复)

### 4. AI聊天 - DeepSeek V3 流式
- **接口**: `POST /api/chat/deepSeekV3/streaming`
- **认证**: 不需要
- **请求体**: `String` (用户消息)
- **响应**: `SseEmitter` (流式响应)

### 5. AI图像生成
- **接口**: `POST /api/image/text`
- **认证**: 不需要
- **请求体**: `String` (图像描述)
- **响应**: `String` (图像URL)

### 6. AI图像生成 V2
- **接口**: `POST /api/image/textV2`
- **认证**: 不需要
- **请求体**: `String` (图像描述)
- **响应**: `ImageSynthesisResult`

### 7. AI图像编辑
- **接口**: `POST /api/image/imageEdit`
- **认证**: 不需要
- **请求体**:
```json
{
  "imageUrl": "https://...",
  "prompt": "编辑描述"
}
```
- **响应**: `List<String>` (编辑后的图像URL列表)

---

## 商城模块 - 商品 (`/api/product`)

### 1. 获取商品分类
- **接口**: `GET /api/product/categories`
- **认证**: 不需要
- **响应**: `List<ProductCategoryVO>`

### 2. 获取商品列表
- **接口**: `GET /api/product/list`
- **认证**: 不需要
- **查询参数**:
  - `categoryId` (可选): 分类ID
- **响应**: `List<ProductVO>`

### 3. 获取商品详情
- **接口**: `GET /api/product/{id}`
- **认证**: 不需要
- **路径参数**: `id` - 商品ID
- **响应**: `ProductVO`

### 4. 获取商品详情（含评价）
- **接口**: `GET /api/product/{id}/detail`
- **认证**: 不需要
- **路径参数**: `id` - 商品ID
- **响应**: `ProductDetailVO`

### 5. 获取商品评价列表
- **接口**: `GET /api/product/{id}/reviews`
- **认证**: 不需要
- **路径参数**: `id` - 商品ID
- **查询参数**:
  - `page` (可选，默认1): 页码
  - `size` (可选，默认10): 每页数量
- **响应**: `List<ProductReviewVO>`

### 6. 提交商品评价
- **接口**: `POST /api/product/review`
- **认证**: 需要Token (`X-User-Id`)
- **请求体**:
```json
{
  "orderItemId": 1,
  "productId": 1,
  "rating": 5,
  "content": "商品很好",
  "images": "https://..."
}
```
- **响应**: `Void`

### 6.1 获取商品评价统计
- **接口**: `GET /api/product/{id}/reviews/summary`
- **认证**: 不需要
- **路径参数**: `id` - 商品ID
- **响应**: `ReviewSummaryVO`
```json
{
  "total": 10,
  "goodCount": 8,
  "badCount": 2,
  "withImagesCount": 5,
  "avgRating": 4.5
}
```

---

## 商城模块 - 搜索 (`/api/search`)

### 1. 搜索商品
- **接口**: `GET /api/search/products`
- **认证**: 不需要
- **查询参数**:
  - `keyword`: 搜索关键词
- **响应**: `List<ProductVO>`

### 2. 获取热门搜索词
- **接口**: `GET /api/search/hot`
- **认证**: 不需要
- **响应**: `List<String>`

---

## 商城模块 - 购物车 (`/api/cart`)

### 1. 获取购物车列表
- **接口**: `GET /api/cart/list`
- **认证**: 需要Token
- **响应**: `List<CartVO>`

### 2. 添加商品到购物车
- **接口**: `POST /api/cart/add`
- **认证**: 需要Token
- **查询参数**:
  - `productId`: 商品ID
  - `quantity` (可选，默认1): 数量
- **响应**: `Long` (购物车ID)

### 3. 更新购物车商品数量
- **接口**: `PUT /api/cart/update`
- **认证**: 需要Token
- **查询参数**:
  - `cartId`: 购物车ID
  - `quantity`: 数量
- **响应**: `Void`

### 4. 删除购物车商品
- **接口**: `DELETE /api/cart/delete`
- **认证**: 需要Token
- **查询参数**: `cartId` - 购物车ID
- **响应**: `Void`

### 5. 清空购物车
- **接口**: `DELETE /api/cart/clear`
- **认证**: 需要Token
- **响应**: `Void`

### 6. 获取购物车商品数量
- **接口**: `GET /api/cart/count`
- **认证**: 需要Token
- **响应**: `Integer`

---

## 商城模块 - 优惠券 (`/api/coupon`)

### 1. 获取可领取优惠券列表
- **接口**: `GET /api/coupon/list`
- **认证**: 需要Token
- **响应**: `List<CouponVO>`

### 2. 获取我的优惠券
- **接口**: `GET /api/coupon/my`
- **认证**: 需要Token
- **查询参数**:
  - `status` (可选): 优惠券状态
- **响应**: `List<UserCouponVO>`

### 3. 领取优惠券
- **接口**: `POST /api/coupon/receive`
- **认证**: 需要Token
- **查询参数**: `couponId` - 优惠券ID
- **响应**: `Boolean`

### 4. 获取订单可用优惠券
- **接口**: `GET /api/coupon/available`
- **认证**: 需要Token
- **查询参数**: `totalAmount` - 订单总金额
- **响应**: `List<UserCouponVO>`

---

## 商城模块 - 订单 (`/api/order`)

### 1. 获取订单确认页信息
- **接口**: `POST /api/order/confirm`
- **认证**: 需要Token
- **请求体**:
```json
{
  "productIds": [1, 2],
  "quantities": [1, 2],
  "cartIds": [1, 2]
}
```
- **响应**: `OrderConfirmVO`

### 2. 提交订单
- **接口**: `POST /api/order/submit`
- **认证**: 需要Token
- **请求体**:
```json
{
  "productIds": [1, 2],
  "quantities": [1, 2],
  "addressId": 1,
  "couponId": 1,
  "remark": "备注"
}
```
- **响应**: `Long` (订单ID)

### 3. 获取订单列表
- **接口**: `GET /api/order/list`
- **认证**: 需要Token
- **查询参数**:
  - `status` (可选): 订单状态
- **响应**: `List<OrderDetailVO>`

### 4. 获取订单详情
- **接口**: `GET /api/order/{id}`
- **认证**: 需要Token
- **路径参数**: `id` - 订单ID
- **响应**: `OrderDetailVO`

### 5. 取消订单
- **接口**: `PUT /api/order/cancel`
- **认证**: 需要Token
- **查询参数**: `orderId` - 订单ID
- **响应**: `Void`

### 6. 确认收货
- **接口**: `PUT /api/order/confirm-receive`
- **认证**: 需要Token
- **查询参数**: `orderId` - 订单ID
- **响应**: `Void`

### 7. 支付订单
- **接口**: `POST /api/order/pay`
- **认证**: 需要Token
- **查询参数**: `orderId` - 订单ID
- **响应**: `Boolean`

### 8. 获取各状态订单数量
- **接口**: `GET /api/order/count`
- **认证**: 需要Token
- **响应**: `Object`

---

## 健康检查 (`/v1/health`)

### 1. 服务健康检查
- **接口**: `GET /v1/health/`
- **认证**: 不需要
- **响应**: `String` (健康状态)

---

## 内部接口（微服务间调用）

### 用户服务内部接口 (`/api/internal/user`)

#### 1. 批量获取用户简要信息
- **接口**: `POST /api/internal/user/batch`
- **认证**: 不需要（内部接口）
- **请求体**: `List<Long>` (用户ID列表)
- **响应**: `Map<Long, UserBriefVO>` (用户ID -> 用户信息映射)

#### 2. 获取单个用户简要信息
- **接口**: `GET /api/internal/user/{userId}`
- **认证**: 不需要（内部接口）
- **路径参数**: `userId` - 用户ID
- **响应**: `UserBriefVO`

---

## 任务模块 (`/api/task`)

### 1. 获取今日任务列表
- **接口**: `GET /api/task/today`
- **认证**: 需要Token (`X-User-Id`)
- **响应**: `List<TaskVO>`
```json
[
  {
    "id": 1,
    "code": "DAILY_SIGN",
    "name": "每日签到",
    "desc": "登录小程序完成签到",
    "icon": "📅",
    "points": 10,
    "completed": false,
    "type": 1
  }
]
```

### 2. 完成任务
- **接口**: `POST /api/task/{taskId}/complete`
- **认证**: 需要Token (`X-User-Id`)
- **路径参数**: `taskId` - 任务ID
- **响应**: `Integer` (获得的积分)

### 3. 获取用户积分
- **接口**: `GET /api/task/points`
- **认证**: 需要Token (`X-User-Id`)
- **响应**: `UserPointsVO`
```json
{
  "total": 150,
  "used": 0,
  "available": 150,
  "level": 1
}
```

### 4. 获取积分历史
- **接口**: `GET /api/task/history`
- **认证**: 需要Token (`X-User-Id`)
- **查询参数**:
  - `page` (可选，默认1): 页码
  - `size` (可选，默认10): 每页数量
- **响应**: `List<PointsHistoryVO>`
```json
[
  {
    "id": 1,
    "points": 10,
    "balance": 150,
    "type": 1,
    "typeDesc": "任务奖励",
    "remark": "完成任务: 每日签到",
    "createTime": "2026-02-17 10:30:00"
  }
]
```

---

## 管理员模块 (`/v1/safety`)

### 1. 管理员登录
- **接口**: `POST /v1/safety/login`
- **认证**: 不需要
- **请求体**:
```json
{
  "username": "admin",
  "password": "password"
}
```
- **响应**: `Response<?>`

---

## API 更新日志

| 日期 | 版本 | 更新内容 | 操作人 |
|------|------|---------|--------|
| 2026-02-17 | v1.1 | 新增任务模块API（/api/task） | - |
| 2026-02-12 | v1.0 | 初始化文档，整理所有现有接口 | - |

---

## 附录

### 常用状态码

| Code | Message | 说明 |
|------|---------|------|
| 200 | 操作成功 | 请求成功 |
| 401 | 未登录或登录已过期 | 需要重新登录 |
| 404 | 资源不存在 | 请求的资源不存在 |
| 500 | 服务器内部错误 | 服务器异常 |

### PetType 类型说明

| 值 | 说明 |
|----|------|
| 1 | 狗 |
| 2 | 猫 |
| 3 | 其他 |

### 订单状态说明

| 值 | 说明 |
|----|------|
| 0 | 待支付 |
| 1 | 待发货 |
| 2 | 待收货 |
| 3 | 已完成 |
| 4 | 已取消 |

### 任务类型说明

| 值 | 说明 |
|----|------|
| 1 | 每日任务 |
| 2 | 每周任务 |
| 3 | 一次性任务 |

### 积分变动类型说明

| 值 | 说明 |
|----|------|
| 1 | 任务奖励 |
| 2 | 签到奖励 |
| 3 | 兑换消耗 |
| 4 | 其他 |
