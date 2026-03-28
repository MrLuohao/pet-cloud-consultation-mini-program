# Frontend Page Status Matrix

更新时间：2026-03-28（已同步原型设计阶段口径）

适用范围：`apps/frontend/miniprogram/app.json` 中已注册的前端页面，以及当前已存在正式 schema 但尚未注册到 `app.json` 的关键候选页。

说明：

- 本文档统一使用以下字段：
  - `设计链路状态`：
    - `已画schema`：已有正式 `.pen` 且 schema 与当前 `.pen` 保持一致
    - `已画待schema`：已有正式 `.pen`，但 schema 缺失、过期或需按当前 `.pen` 重新刷新
    - `已画未实现`：已有正式 `.pen + schema`，但前端路由或页面实现未闭环
    - `待复核`：旧 schema 或旧摘要与当前 `.pen` 已不一致，需先确认页面是否仍按独立链路保留
    - `有代码无schema`：页面已注册并已有代码，但尚未进入正式新版 `.pen -> schema` 链路
    - `完全未画`：当前没有正式可开发设计源
  - `开发进度状态`：
    - 当前字段统一按“消费端原型承接进度”理解，不代表正式前端开发完成度，也不代表后端实现进度
    - `已完成`
      - 仅表示当前原型链路已基本闭环或已确认，不表示正式开发交付完成
    - `开发中`
      - 表示当前正在进行原型承接或页面收口
    - `未完成`
      - 表示已有设计、路由或旧代码，但当前原型尚未按新版 schema 闭环
    - `未开始`
      - 表示已有设计对象，但尚未建立页面承接或尚未进入本轮原型收口
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
- 本轮基于 2026-03-28 当前已保存 `.pen` 重核；当前顶层共 59 个 frame，其中 58 个为正式页面画板，`X6bge / 订单取消流程提示` 为辅助流程提示 frame。
- 本表只跟踪“已进入正式 schema 链路的消费端页面”，不把辅助提示稿或纯参考画板混入独立页面开发清单。
- 强一致性核对结果：
  - 当前 schema 文件总数为 57
  - 其中 56 个正式页面 schema 与 1 个辅助流程 schema 已与当前 `.pen` 顶层画板或变体画板对齐
  - 其中 `pet-edit.page-schema.json` 通过 `meta.variantFrameIds` 同时覆盖 `11vrw / 编辑宠物页 Pet Edit` 与 `WA7xQ / 新增宠物页 Pet Add`
  - 其中 `health-edit.page-schema.json` 通过 `meta.variantFrameIds` 同时覆盖 `7YI5K / 新增健康记录页 Health Record Create` 与 `kabmm / 编辑健康记录页 Health Record Edit`
  - 当前 58 个正式页面画板都已进入 schema 覆盖范围
  - 辅助流程画板 `X6bge / 订单取消流程提示` 现已补齐对应 `order-cancel-flow-hint.page-schema.json`，但它仍只作为流程说明，不纳入独立页面开发清单
- 当前仍处于原型设计阶段：
  - 现有前端旧代码只能作为原型承接现状参考，不作为“页面已完成开发”的证据
  - 仓库中已有的后端 Java / SQL 代码不计入本表进度判断
- 当前前台设计阶段的核心问题，已不是“大量缺正式页面”，而是“已画页面的链路复核、体验统一、路由接入与代码承接闭环”。
- 最近已完成并已体现在当前 `.pen` 的画布优化批次包括：
  - 用户辅助链：`设置页`、`会员中心页`、`优惠券页（已合并优惠券中心 / 我的优惠券）`、`我的收藏页`、`每日任务页`、`意见反馈页`、`全部消息页`
  - 订单链：`订单列表页`、`物流详情页`、`申请售后页`、`售后进度页`、`售后完成页`、`订单详情页多个状态`、`金额明细模块统一优化`
  - 支付 / 取消中间态：`支付方式弹窗`、`支付验证层`、`支付密码输入`、`取消订单原因弹层`
  - 宠物档案链：`我的宠物列表`、`单宠物健康档案页`、`单宠物成长档案页`
- 上述“已完成”仅指 `.pen` / schema 层的原型推进结果，不等同于消费端代码或后端实现已经完成
- 本轮确认新增或明确进入正式画板链路的页面包括：
  - 健康记录链：`pages/health/list`、`pages/health/edit`、`pages/health/reminder/index`
  - 问诊链：`pages/consultation/doctor-list`、`pages/consultation/doctor-detail`、`pages/consultation/create`、`pages/consultation/chat`、`pages/consultation/list`、`pages/consultation/urgent/index`
  - 社区详情链：`pages/community/detail`、`pages/community/search`、`pages/community/topic`、`pages/community/publish`
  - 私信链：`pages/message/conversations`、`pages/message/chat`
  - 宠物链变更：`pages/pet/edit`、`pages/pet/timeline/index`
  - Beauty 链：`pages/beauty/beauty`、`pages/beauty/booking/index`
- 本轮对“代码是否已真正承接当前画布”的抽样复核结论：
  - 已注册且页面文件完整，但仍需参考当前画布更新的重点页面共 27 个：
    `pages/pet/list`、`pages/pet/edit`、`pages/pet/timeline/index`、`pages/order/pending-review`、`pages/health/list`、`pages/health/edit`、`pages/health/reminder/index`、`pages/consultation/doctor-list`、`pages/consultation/doctor-detail`、`pages/consultation/create`、`pages/consultation/chat`、`pages/consultation/list`、`pages/consultation/urgent/index`、`pages/community/detail`、`pages/community/search`、`pages/community/topic`、`pages/community/publish`、`pages/message/conversations`、`pages/message/chat`、`pages/news/all-messages/index`、`pages/beauty/beauty`、`pages/beauty/booking/index`、`pages/vip/index`、`pages/settings/settings`、`pages/collection/list`、`pages/task/index`、`pages/feedback/feedback`
  - 已注册且在当前原型承接中相对最接近正式链路的重点页面：`pages/order/detail`
  - 已注册但路由语义仍需先收敛的页面：`pages/coupon/center`、`pages/coupon/my`
  - 当前尚未注册到 `app.json`，但已具备正式 `.pen + schema` 的页面 / 候选状态承接包括：
    `pages/pet/profile/index`、`pages/order/cancelled-detail`、`候选：pages/order/list`、`候选：pages/order/logistics-detail`、`候选：pages/order/after-sales/apply`、`候选：pages/order/after-sales/progress`、`候选：pages/order/after-sales/complete`、`候选：pages/order/detail` 的售后处理中状态分支
- 后续执行要求：
  - 每次页面状态变化后，都要同步更新本文档
  - 至少同步 `设计链路状态 / 开发进度状态 / 前端接入状态 / 备注`
  - 在提出“接下来做什么”之前，必须先核对本表与最近已完成优化批次，避免把已优化页面重复当作待补页面。

## 一、主链路与当前仍可直接承接的正式设计链路页面

| 页面路径 | 对应 schema | 设计链路状态 | 开发进度状态 | 前端接入状态 | 优先级 | 说明 |
| --- | --- | --- | --- | --- | --- | --- |
| `pages/index/index` | `home.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | 首页已进入正式设计链路 |
| `pages/shop/shop` | `shop.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | 商城首页已入链路 |
| `pages/news/news` | `news.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | 消息主页已入链路 |
| `pages/user/user` | `user.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | 我的主页已入链路 |
| `pages/login/login` | `login.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | 登录页已入链路 |
| `pages/diagnosis/diagnosis` | `diagnosis.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | AI 诊断页已入链路 |
| `pages/cart/cart` | `cart.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | 购物车页已入链路 |
| `pages/product/detail` | `product-detail.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | 商品详情页已入链路 |
| `pages/order/confirm` | `checkout.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | 结算页已入链路 |
| `pages/order/detail` | `order-detail.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | 订单详情主态已入链路；对应辅助链路提示画板 `X6bge` 也已单独补齐 `order-cancel-flow-hint.page-schema.json`；当前只可视为原型承接相对更接近闭环，不表示正式开发完成 |
| `pages/address/list` | `address-list.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | 地址列表已入链路 |
| `pages/address/edit` | `address-edit.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | 地址编辑已入链路 |
| `pages/order/review` | `order-review.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | 订单评价页已入链路 |
| `pages/order/pay-result/index` | `pay-success.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | 支付成功页已入链路 |
| `pages/community/tab` | `community.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | 当前社区主入口更接近该 schema 承接页 |
| `pages/community/detail` | `community-detail.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | schema 已承接 `WnUMs / 社区详情页`，但当前页面仍是旧的重功能版详情实现 |
| `pages/community/search` | `community-search.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | schema 已承接 `RKyhI / 社区搜索页`，但当前页面仍是旧的搜索历史/热门话题/结果 tabs 结构 |
| `pages/community/topic` | `community-topic.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | schema 已承接 `gDeKf / 社区话题页`，但当前页面仍是通用话题头部 + feed 结构 |
| `pages/community/publish` | `community-publish.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | schema 已承接 `GFqFg / 社区发布页`，但当前页面仍是大量自定义 picker/modal 的旧发布流程 |
| `pages/pet/list` | `pet-list.page-schema.json` | 已画schema | 未完成 | 已接入 | P0 | schema 仍与当前 `.pen` 对齐，但当前代码仍只有单个“查看档案”入口，尚未真正承接 `健康档案 / 成长档案` 双入口 |
| `pages/pet/edit` | `pet-edit.page-schema.json` | 已画schema | 未完成 | 已接入 | P0 | schema 已刷新为 `11vrw / 编辑宠物页` + `WA7xQ / 新增宠物页` 双态，但当前页面代码仍是旧分步向导式实现 |
| `pages/pet/timeline/index` | `pet-timeline.page-schema.json` | 已画schema | 未完成 | 已接入 | P0 | schema 已刷新到 `zqXpi / 单宠物成长档案页 Pet Growth Profile`，但当前页面代码仍是旧健康时间线实现 |
| `pages/health/list` | `health-list.page-schema.json` | 已画schema | 未完成 | 已接入 | P0 | schema 已承接 `2fI4W / 健康记录列表页`，但当前页面代码仍是通用 CRUD 列表结构 |
| `pages/health/edit` | `health-edit.page-schema.json` | 已画schema | 未完成 | 已接入 | P0 | schema 已统一覆盖 `7YI5K / 新增健康记录页` 与 `kabmm / 编辑健康记录页`，但当前页面代码仍是旧表单实现 |
| `pages/health/reminder/index` | `health-reminder.page-schema.json` | 已画schema | 未完成 | 已接入 | P0 | schema 已承接 `wfnjI / 健康提醒页`，但当前页面代码仍是列表 + FAB + 弹窗表单旧实现 |
| `pages/consultation/doctor-list` | `consultation-doctor-list.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | schema 已承接 `Y55fu / 问诊医生列表页`，但当前页面代码仍是旧科室筛选 + 医生卡片结构 |
| `pages/consultation/doctor-detail` | `consultation-doctor-detail.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | schema 已承接 `0akq8 / 医生详情页`，但当前实现仍是旧详情页结构且 reviews 承接不完整 |
| `pages/consultation/create` | `consultation-create.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | schema 已承接 `oKwYh / 发起问诊页`，但当前页面仍是常规表单 + 图片上传实现 |
| `pages/consultation/chat` | `consultation-chat.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | schema 已承接 `ob35v / 问诊会话页`，但当前仍是泛聊天页，且输入绑定存在不一致 |
| `pages/consultation/list` | `consultation-list.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | schema 已承接 `TO5jB / 问诊列表页`，但当前仍是简单 tab + 列表卡片结构 |
| `pages/consultation/urgent/index` | `consultation-urgent.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | schema 已承接 `WP23x / 紧急问诊页`，但当前仍是旧的强视觉紧急通道实现 |
| `pages/beauty/beauty` | `beauty.page-schema.json` | 已画schema | 未完成 | 已接入 | P2 | schema 已承接 `Mpxrb / 护理首页 Beauty Care Home`；页面已收缩为“后台可配置护理热点内容 + 单一附近找机构 CTA” |
| `pages/beauty/booking/index` | `beauty-booking.page-schema.json` | 已画schema | 未完成 | 已接入 | P2 | schema 已承接 `ex1sD / 附近机构搜索页 Beauty Nearby Search`；页面已改为单页搜索承接，结果直接在本页按地图式地址列表展示，前端还需补 `/api/map/poi/nearby` 适配 |
| `pages/message/conversations` | `message-conversations.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | schema 已承接 `dtmAQ / 私信会话页`，但当前页面仍是基础会话列表底稿 |
| `pages/message/chat` | `message-chat.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | schema 已承接 `hHmNO / 私信聊天页`，但当前页面仍只支持基础文字气泡聊天 |
| `pages/news/all-messages/index` | `all-messages.page-schema.json` | 已画schema | 未完成 | 已接入 | P1 | schema 已承接 `R5qEl / 全部消息页 All Messages`，但当前页面代码仍未承接“会话 / 订单 / 活动 / 系统”统一收口结构 |
| `pages/vip/index` | `vip-center.page-schema.json` | 已画schema | 未完成 | 已接入 | P2 | schema 已承接 `meeAN / 会员中心页 VIP Center`，当前页面代码仍是旧 VIP 展示结构 |
| `pages/settings/settings` | `settings.page-schema.json` | 已画schema | 未完成 | 已接入 | P2 | schema 已承接 `tPmtP / 设置页 Settings`，当前页面代码仍未按“资料设置 + 账号与安全 + 消息提醒”新版结构实现 |
| `pages/collection/list` | `collection.page-schema.json` | 已画schema | 未完成 | 已接入 | P2 | schema 已承接 `gM8gn / 我的收藏页 My Collection`，当前页面代码仍是旧收藏列表实现 |
| `pages/task/index` | `daily-tasks.page-schema.json` | 已画schema | 未完成 | 已接入 | P2 | schema 已承接 `bVCp2 / 每日任务页 Daily Tasks`，当前页面代码仍未承接积分激励与任务卡层级 |
| `pages/feedback/feedback` | `feedback.page-schema.json` | 已画schema | 未完成 | 已接入 | P2 | schema 已承接 `orfEX / 意见反馈页 Feedback`，当前页面代码仍是旧反馈表单实现 |

## 二、已有 schema，但前端路由或实现仍未闭环

| 页面路径 | 对应 schema | 设计链路状态 | 开发进度状态 | 前端接入状态 | 优先级 | 说明 |
| --- | --- | --- | --- | --- | --- | --- |
| `pages/pet/profile/index` | `pet-profile.page-schema.json` | 已画未实现 | 未开始 | 未接入 | P0 | 单宠物健康档案页 `.pen + schema` 仍有效，但路由和页面文件尚未建立；卡内“查看完整诊断”仅保留为流程占位，不得再落成独立 `pages/diagnosis/detail` 路由 |
| `候选：pages/order/list` | `order-list.page-schema.json` | 已画未实现 | 未开始 | 未接入 | P1 | 订单列表页已有正式 `.pen + schema`，但当前 `app.json` 尚未注册独立路由；更可能作为 `pages/order/detail` 的列表化拆分页或后续新增路由 |
| `候选：pages/order/logistics-detail` | `logistics-detail.page-schema.json` | 已画未实现 | 未开始 | 未接入 | P1 | 物流详情页已有正式 `.pen + schema`，应从待收货订单卡或订单详情进入；当前 `app.json` 未注册 |
| `候选：pages/order/after-sales/apply` | `after-sales-apply.page-schema.json` | 已画未实现 | 未开始 | 未接入 | P1 | 申请售后页已有正式 `.pen + schema`，应从已完成订单或订单详情进入；当前 `app.json` 未注册 |
| `候选：pages/order/after-sales/progress` | `after-sales-progress.page-schema.json` | 已画未实现 | 未开始 | 未接入 | P1 | 售后进度页已有正式 `.pen + schema`，应承接审核中状态；当前 `app.json` 未注册 |
| `候选：pages/order/after-sales/complete` | `after-sales-complete.page-schema.json` | 已画未实现 | 未开始 | 未接入 | P1 | 售后完成页已有正式 `.pen + schema`，应承接退款完成状态；当前 `app.json` 未注册 |
| `候选：pages/order/detail` 的售后态分支 | `after-sales-order-detail.page-schema.json` | 已画未实现 | 未开始 | 未接入 | P1 | `2FO7j / 订单详情页 After-Sales State` 是订单详情的售后处理中状态画板，更适合作为现有 `pages/order/detail` 的状态分支，而不是立即再拆一条新路由 |
| `pages/order/cancelled-detail` | `cancelled-order-detail.page-schema.json` | 已画未实现 | 未开始 | 未接入 | P0 | 取消态订单详情已有新版 `.pen + schema`，前端尚未承接 |
| `pages/order/pending-review` | `completed-order-detail.page-schema.json` | 待复核 | 未完成 | 已接入 | P1 | 当前路由命名与 schema 语义仍存在偏差；页面代码也是旧的“待评价列表页”实现，需先确认它是否继续承接 `Completed Order Detail` |
| `pages/coupon/center` | `coupon-hub.page-schema.json` | 待复核 | 未完成 | 已接入 | P2 | 最新 `.pen` 已将“优惠券中心 / 我的优惠券”合并为统一 `ROr8Y / 优惠券页 Coupon Hub`，旧 route 是否保留需在接入前收敛 |
| `pages/coupon/my` | `coupon-hub.page-schema.json` | 待复核 | 未完成 | 已接入 | P2 | 同上，当前更适合收敛为单一路由或同页不同入口态 |
| `payment-method-modal` | `payment-method-modal.page-schema.json` | 已画未实现 | 未开始 | 未接入 | P1 | 有正式 schema，但当前不是 `app.json` 独立页面 |
| `payment-verification` | `payment-verification.page-schema.json` | 已画未实现 | 未开始 | 未接入 | P1 | 有正式 schema，但当前不是 `app.json` 独立页面 |
| `payment-password-entry` | `payment-password-entry.page-schema.json` | 已画未实现 | 未开始 | 未接入 | P1 | 有正式 schema，但当前不是 `app.json` 独立页面 |
| `cancel-order-modal` | `cancel-order-modal.page-schema.json` | 已画未实现 | 未开始 | 未接入 | P1 | 有正式 schema，但当前不是 `app.json` 独立页面 |
| `cart-empty` | `cart-empty.page-schema.json` | 已画未实现 | 未开始 | 未接入 | P2 | 空态设计已存在，需并入购物车实现分支 |

## 三、有代码和路由，但尚未进入正式新版设计链路

| 页面路径 | 设计链路状态 | 开发进度状态 | 前端接入状态 | 优先级 | 说明 |
| --- | --- | --- | --- | --- | --- |
| `pages/course/course` | 有代码无schema | 未完成 | 已接入 | P2 | 课程列表未纳入新版设计链路 |
| `pages/beauty/detail/index` | 有代码无schema | 未完成 | 已接入 | P2 | 最新 `.pen` 已移除独立护理专题页，当前旧路由应并入内容阅读承接，不再保留独立页面链路 |
| `pages/recommend/detail` | 有代码无schema | 未完成 | 已接入 | P2 | 推荐详情未纳入新版设计链路 |
| `pages/chat/chat` | 有代码无schema | 未完成 | 已接入 | P2 | 老聊天页未纳入新版设计链路 |
| `pages/user/profile` | 有代码无schema | 未完成 | 已接入 | P2 | 个人资料页未纳入新版设计链路 |
| `pages/search/result` | 有代码无schema | 未完成 | 已接入 | P2 | 搜索结果页未纳入新版设计链路 |
| `pages/product/reviews` | 有代码无schema | 未完成 | 已接入 | P1 | 商品评价列表未纳入新版设计链路 |
| `pages/course/detail/index` | 有代码无schema | 未完成 | 已接入 | P2 | 课程详情未纳入新版设计链路 |
| `pages/course/player/index` | 有代码无schema | 未完成 | 已接入 | P2 | 课程播放页未纳入新版设计链路 |
| `pages/insurance/index` | 有代码无schema | 未完成 | 已接入 | P2 | 保险页未纳入新版设计链路 |
| `pages/beauty/booking-list/index` | 有代码无schema | 未完成 | 已接入 | P2 | 最新 `.pen` 已移除独立结果列表页，搜索结果改为在 `pages/beauty/booking/index` 内直接展示 |
| `pages/beauty/booking-detail/index` | 有代码无schema | 未完成 | 已接入 | P2 | 最新 `.pen` 已移除独立机构详情页，当前链路不再保留单独详情承接 |
| `pages/shop/subscription/index` | 有代码无schema | 未完成 | 已接入 | P2 | 商城订阅页未纳入新版设计链路 |
| `pages/community/index` | 有代码无schema | 未完成 | 已接入 | P1 | 社区入口旧页仍存在，但新版主链更偏 `community/tab` |
| `pages/user/followers` | 有代码无schema | 未完成 | 已接入 | P2 | 粉丝页未纳入新版设计链路 |
| `pages/user/followings` | 有代码无schema | 未完成 | 已接入 | P2 | 关注页未纳入新版设计链路 |

## 四、建议的下一步排序

说明：

- 以下排序统一按“消费端原型阶段”理解，不代表正式开发排期
- 后台管理端已进入独立原型设计阶段，现统一由 `docs/plans/backend-management-page-status-matrix.md` 跟踪，不属于本表范围

### P0

1. 优先识别“真实缺口”而不是重复补画：`pages/pet/profile/index`、`pages/order/cancelled-detail`、`payment-method-modal`、`payment-verification`、`payment-password-entry`、`cancel-order-modal`
2. 已完成本轮画布 / schema 优化的订单链、支付中间态、宠物档案链，默认先停止继续补画，除非用户明确要求继续细调
3. 如果仍处于 `.pen` 设计推进阶段，下一条更适合整体复核的高价值真实链路是：`问诊链`

### P1

1. 问诊链整链复核：`医生列表`、`医生详情`、`发起问诊`、`问诊会话`、`问诊列表`、`紧急问诊`
2. 社区详情链整链复核：`详情`、`搜索`、`话题`、`发布`
3. 私信链整链复核：`会话列表`、`聊天`

### P2

1. beauty 链按新 schema 复核：`护理首页`、`附近机构搜索`
2. 课程、保险、推荐详情等“有代码无正式新版设计链路”页面
3. 社交关系页：`粉丝 / 关注`，仅在入口与业务语义重新确认后再推进
4. 设置、收藏、反馈、优惠券等本轮已优化辅助页，默认不再重复提报为待补页面

## 五、维护规则

- 新页面只要进入正式设计链路，就要同步更新本表
- 页面状态发生变化时，至少同步更新：
  - 设计链路状态
  - 开发进度状态
  - 前端接入状态
  - 对应 schema
  - 优先级
  - 备注说明
- 如果项目仍处于原型设计阶段，`开发进度状态` 统一按“原型承接进度”填写，不把历史代码或后端草稿代码视为完成依据
- 如果某个页面被确认废弃、合并或改路由，也必须在本表中显式改掉，避免继续误判
