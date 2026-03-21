# Frontend Page Status Matrix

更新时间：2026-03-21

适用范围：`apps/frontend/miniprogram/app.json` 中已注册的前端页面。

说明：

- 本文档统一使用以下字段：
  - `设计链路状态`：
    - `已画已schema`：已有正式 `.pen` 且已有对应 schema
    - `已画未实现`：已有正式 `.pen + schema`，但前端路由或页面实现未闭环
    - `有代码无schema`：页面已注册并已有代码，但尚未进入正式新版 `.pen -> schema` 链路
    - `完全未画`：当前没有正式可开发设计源
  - `开发进度状态`：
    - `已完成`
    - `开发中`
    - `未完成`
    - `未开始`
  - `前端接入状态`：
    - `已接入`
    - `接入中`
    - `未接入`
- 优先级含义：
  - `P0`：主链路缺口，优先补
  - `P1`：高频能力，建议尽快补
  - `P2`：辅助能力，可后置
  - `Blocked`：当前被设计源缺失阻塞
- 所有判断统一以当前 `.pen` 最新实现和 schema 目录事实为准。
- 后续执行要求：
  - 每次页面状态变化后，都要同步更新本文档
  - 至少同步 `设计链路状态 / 开发进度状态 / 前端接入状态 / 备注`

## 一、主链路与已入正式设计链路页面

| 页面路径 | 对应 schema | 设计链路状态 | 开发进度状态 | 前端接入状态 | 优先级 | 说明 |
| --- | --- | --- | --- | --- | --- | --- |
| `pages/index/index` | `home.page-schema.json` | 已画已schema | 未完成 | 已接入 | P1 | 首页已进入正式设计链路 |
| `pages/shop/shop` | `shop.page-schema.json` | 已画已schema | 未完成 | 已接入 | P1 | 商城首页已入链路 |
| `pages/news/news` | `news.page-schema.json` | 已画已schema | 未完成 | 已接入 | P1 | 消息主页已入链路 |
| `pages/user/user` | `user.page-schema.json` | 已画已schema | 未完成 | 已接入 | P1 | 我的主页已入链路 |
| `pages/login/login` | `login.page-schema.json` | 已画已schema | 未完成 | 已接入 | P1 | 登录页已入链路 |
| `pages/diagnosis/diagnosis` | `diagnosis.page-schema.json` | 已画已schema | 未完成 | 已接入 | P1 | AI 诊断页已入链路 |
| `pages/cart/cart` | `cart.page-schema.json` | 已画已schema | 未完成 | 已接入 | P1 | 购物车页已入链路 |
| `pages/product/detail` | `product-detail.page-schema.json` | 已画已schema | 未完成 | 已接入 | P1 | 商品详情页已入链路 |
| `pages/order/confirm` | `checkout.page-schema.json` | 已画已schema | 未完成 | 已接入 | P1 | 结算页已入链路 |
| `pages/order/detail` | `order-detail.page-schema.json` | 已画已schema | 未完成 | 已接入 | P1 | 订单详情主态已入链路 |
| `pages/address/list` | `address-list.page-schema.json` | 已画已schema | 未完成 | 已接入 | P1 | 地址列表已入链路 |
| `pages/address/edit` | `address-edit.page-schema.json` | 已画已schema | 未完成 | 已接入 | P1 | 地址编辑已入链路 |
| `pages/order/review` | `order-review.page-schema.json` | 已画已schema | 未完成 | 已接入 | P1 | 订单评价页已入链路 |
| `pages/order/pay-result/index` | `pay-success.page-schema.json` | 已画已schema | 未完成 | 已接入 | P1 | 支付成功页已入链路 |
| `pages/community/tab` | `community.page-schema.json` | 已画已schema | 未完成 | 已接入 | P1 | 当前社区主入口更接近该 schema 承接页 |
| `pages/pet/list` | `pet-list.page-schema.json` | 已画已schema | 未完成 | 已接入 | P0 | 新版宠物列表已切成 `健康档案 / 成长档案` 双入口，schema 已同步 |
| `pages/pet/edit` | `pet-edit.page-schema.json` | 已画已schema | 未完成 | 已接入 | P0 | `pet-edit` 已补双态正式设计链路：编辑态保持单页卡片表单，新增态补了轻量品种横滑图卡，当前待按新版 schema 重写旧向导式实现 |

## 二、已画已schema，但实现链路仍未闭环

| 页面路径 | 对应 schema | 设计链路状态 | 开发进度状态 | 前端接入状态 | 优先级 | 说明 |
| --- | --- | --- | --- | --- | --- | --- |
| `pages/pet/profile/index` | `pet-profile.page-schema.json` | 已画未实现 | 未开始 | 未接入 | P0 | 诊断档案页 `.pen + schema` 已完成，但路由和页面文件尚未建立 |
| `pages/diagnosis/detail` | `diagnosis-detail.page-schema.json` | 已画未实现 | 未开始 | 未接入 | P0 | 完整 AI 诊断报告页 `.pen + schema` 已建立，但当前路由尚未注册到 `app.json` |
| `pages/pet/timeline/index` | `pet-timeline.page-schema.json` | 已画未实现 | 未完成 | 接入中 | P0 | 成长档案正式画布与 schema 已建立，但现有代码仍是旧的健康时间线实现，需按新设计重写 |
| `pages/order/cancelled-detail` | `cancelled-order-detail.page-schema.json` | 已画未实现 | 未开始 | 未接入 | P0 | 取消态订单详情已有新版 `.pen + schema`，前端尚未承接 |
| `pages/order/pending-review` | `completed-order-detail.page-schema.json` | 已画未实现 | 未完成 | 已接入 | P1 | 当前路由命名与 schema 语义存在偏差，需复核承接关系 |
| `payment-method-modal` | `payment-method-modal.page-schema.json` | 已画未实现 | 未开始 | 未接入 | P1 | 有正式 schema，但当前不是 `app.json` 独立页面 |
| `payment-verification` | `payment-verification.page-schema.json` | 已画未实现 | 未开始 | 未接入 | P1 | 有正式 schema，但当前不是 `app.json` 独立页面 |
| `payment-password-entry` | `payment-password-entry.page-schema.json` | 已画未实现 | 未开始 | 未接入 | P1 | 有正式 schema，但当前不是 `app.json` 独立页面 |
| `cancel-order-modal` | `cancel-order-modal.page-schema.json` | 已画未实现 | 未开始 | 未接入 | P1 | 有正式 schema，但当前不是 `app.json` 独立页面 |
| `cart-empty` | `cart-empty.page-schema.json` | 已画未实现 | 未开始 | 未接入 | P2 | 空态设计已存在，需并入购物车实现分支 |

## 三、有代码和路由，但尚未进入正式新版设计链路

| 页面路径 | 设计链路状态 | 开发进度状态 | 前端接入状态 | 优先级 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `pages/course/course` | 有代码无schema | 未完成 | 已接入 | P2 | 课程列表未纳入新版设计链路 |
| `pages/beauty/beauty` | 有代码无schema | 未完成 | 已接入 | P2 | 洗护/美容入口未纳入新版设计链路 |
| `pages/recommend/detail` | 有代码无schema | 未完成 | 已接入 | P2 | 推荐详情未纳入新版设计链路 |
| `pages/chat/chat` | 有代码无schema | 未完成 | 已接入 | P2 | 老聊天页未纳入新版设计链路 |
| `pages/user/profile` | 有代码无schema | 未完成 | 已接入 | P2 | 个人资料页未纳入新版设计链路 |
| `pages/coupon/center` | 有代码无schema | 未完成 | 已接入 | P2 | 优惠券中心未纳入新版设计链路 |
| `pages/coupon/my` | 有代码无schema | 未完成 | 已接入 | P2 | 我的优惠券未纳入新版设计链路 |
| `pages/consultation/doctor-list` | 有代码无schema | 未完成 | 已接入 | P1 | 问诊医生列表未纳入新版设计链路 |
| `pages/consultation/doctor-detail` | 有代码无schema | 未完成 | 已接入 | P1 | 问诊医生详情未纳入新版设计链路 |
| `pages/consultation/create` | 有代码无schema | 未完成 | 已接入 | P1 | 问诊创建页未纳入新版设计链路 |
| `pages/consultation/chat` | 有代码无schema | 未完成 | 已接入 | P1 | 问诊聊天页未纳入新版设计链路 |
| `pages/consultation/list` | 有代码无schema | 未完成 | 已接入 | P1 | 问诊列表未纳入新版设计链路 |
| `pages/health/list` | 有代码无schema | 未完成 | 已接入 | P0 | 健康记录主列表未纳入新版设计链路 |
| `pages/health/edit` | 有代码无schema | 未完成 | 已接入 | P0 | 健康记录编辑页未纳入新版设计链路 |
| `pages/search/result` | 有代码无schema | 未完成 | 已接入 | P2 | 搜索结果页未纳入新版设计链路 |
| `pages/collection/list` | 有代码无schema | 未完成 | 已接入 | P2 | 收藏列表未纳入新版设计链路 |
| `pages/task/index` | 有代码无schema | 未完成 | 已接入 | P2 | 任务页未纳入新版设计链路 |
| `pages/vip/index` | 有代码无schema | 未完成 | 已接入 | P2 | VIP 页未纳入新版设计链路 |
| `pages/settings/settings` | 有代码无schema | 未完成 | 已接入 | P2 | 设置页未纳入新版设计链路 |
| `pages/feedback/feedback` | 有代码无schema | 未完成 | 已接入 | P2 | 反馈页未纳入新版设计链路 |
| `pages/product/reviews` | 有代码无schema | 未完成 | 已接入 | P1 | 商品评价列表未纳入新版设计链路 |
| `pages/news/all-messages/index` | 有代码无schema | 未完成 | 已接入 | P1 | 全部消息列表未纳入新版设计链路 |
| `pages/beauty/detail/index` | 有代码无schema | 未完成 | 已接入 | P2 | 美容详情未纳入新版设计链路 |
| `pages/beauty/booking/index` | 有代码无schema | 未完成 | 已接入 | P2 | 美容预约未纳入新版设计链路 |
| `pages/beauty/booking-list/index` | 有代码无schema | 未完成 | 已接入 | P2 | 美容预约列表未纳入新版设计链路 |
| `pages/course/detail/index` | 有代码无schema | 未完成 | 已接入 | P2 | 课程详情未纳入新版设计链路 |
| `pages/course/player/index` | 有代码无schema | 未完成 | 已接入 | P2 | 课程播放页未纳入新版设计链路 |
| `pages/health/reminder/index` | 有代码无schema | 未完成 | 已接入 | P0 | 健康提醒页未纳入新版设计链路 |
| `pages/insurance/index` | 有代码无schema | 未完成 | 已接入 | P2 | 保险页未纳入新版设计链路 |
| `pages/beauty/booking-detail/index` | 有代码无schema | 未完成 | 已接入 | P2 | 美容预约详情未纳入新版设计链路 |
| `pages/consultation/urgent/index` | 有代码无schema | 未完成 | 已接入 | P1 | 紧急问诊入口未纳入新版设计链路 |
| `pages/shop/subscription/index` | 有代码无schema | 未完成 | 已接入 | P2 | 商城订阅页未纳入新版设计链路 |
| `pages/community/index` | 有代码无schema | 未完成 | 已接入 | P1 | 社区入口旧页仍存在，但新版主链更偏 `community/tab` |
| `pages/community/detail` | 有代码无schema | 未完成 | 已接入 | P1 | 社区详情未纳入新版设计链路 |
| `pages/community/search` | 有代码无schema | 未完成 | 已接入 | P1 | 社区搜索未纳入新版设计链路 |
| `pages/community/topic` | 有代码无schema | 未完成 | 已接入 | P1 | 社区话题未纳入新版设计链路 |
| `pages/community/publish` | 有代码无schema | 未完成 | 已接入 | P1 | 社区发布未纳入新版设计链路 |
| `pages/message/conversations` | 有代码无schema | 未完成 | 已接入 | P1 | 消息会话列表未纳入新版设计链路 |
| `pages/message/chat` | 有代码无schema | 未完成 | 已接入 | P1 | 消息聊天页未纳入新版设计链路 |
| `pages/user/followers` | 有代码无schema | 未完成 | 已接入 | P2 | 粉丝页未纳入新版设计链路 |
| `pages/user/followings` | 有代码无schema | 未完成 | 已接入 | P2 | 关注页未纳入新版设计链路 |

## 四、完全未画或被设计源阻塞

| 范围 | 设计链路状态 | 开发进度状态 | 前端接入状态 | 优先级 | 说明 |
| --- | --- | --- | --- | --- | --- |
| 后台管理端 | 完全未画 | 未开始 | 未接入 | Blocked | `design/backendManagement.pen` 当前仍为空白画板，禁止直接编码 |

## 五、建议的下一步排序

### P0

1. `pages/pet/profile/index`
2. `pages/order/cancelled-detail`
3. `pages/pet/edit`
4. `pages/pet/timeline/index`
5. `pages/health/list`
6. `pages/health/edit`
7. `pages/health/reminder/index`

### P1

1. 订单取消链与支付链中间态闭环
2. 社区详情 / 搜索 / 话题 / 发布
3. 消息会话列表 / 消息聊天
4. 问诊链：医生列表、医生详情、创建、会话、列表、紧急入口

### P2

1. 课程、洗护、保险、VIP、设置、收藏、反馈等辅助页
2. 社交关系页：粉丝 / 关注
3. 优惠券相关页

## 六、维护规则

- 新页面只要进入正式设计链路，就要同步更新本表。
- 页面状态发生变化时，至少同步更新：
  - 设计链路状态
  - 开发进度状态
  - 前端接入状态
  - 对应 schema
  - 优先级
  - 备注说明
- 如果某个页面被确认废弃、合并或改路由，也必须在本表中显式改掉，避免继续误判。
