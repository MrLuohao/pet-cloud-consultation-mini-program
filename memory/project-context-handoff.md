# Project Context Handoff

更新时间：2026-03-28

## 项目
- 名称：PetCloud Consultation
- 工作区：`/Volumes/Beelink/software/pet-cloud-consultation-mini-program`
- 仓库结构：`apps/frontend`、`apps/backend/pet-cloud-consultation`、`design`、`docs`、`memory`
- 前端：微信小程序原生 `WXML / WXSS / JS`
- 后端：Java 21 + Spring Boot 3.2.5 多模块服务
- 当前阶段：设计源继续推进中；前端进入“新版 `.pen` 已扩展，beauty 链已收缩为 2 页正式链路，当前重点转为页面代码按 schema 承接”的同步阶段

## 核心入口
- 总规则入口：`docs/project-rules.md`
- 全局项目规则：`docs/rules/global-project-rules.md`
- 设计源规则：`docs/rules/design-source-rules.md`
- 前端实现规则：`docs/rules/frontend-implementation-rules.md`
- 后端开发规则：`docs/rules/backend-development-rules.md`
- 正式设计源：`design/frontendMobileScreens.pen`
- schema 规则入口：`apps/frontend/docs/design-schemas/README.md`
- 页面状态矩阵：`docs/plans/frontend-page-status-matrix.md`

## 当前 `.pen` 事实
- 主设计源：`design/frontendMobileScreens.pen`
  - 当前顶层共 46 个 frame，其中：
    - 45 个正式页面画板
    - 1 个辅助流程提示 frame：`X6bge / 订单取消流程提示`
  - 2026-03-26 ~ 2026-03-27 明确可见的新增/变更顶层画板包括：
    - `WA7xQ / 新增宠物页 Pet Add`
    - `11vrw / 编辑宠物页 Pet Edit`
    - `zqXpi / 单宠物成长档案页 Pet Growth Profile`
    - `2fI4W / 健康记录列表页 Health Record List`
    - `7YI5K / 新增健康记录页 Health Record Create`
    - `kabmm / 编辑健康记录页 Health Record Edit`
    - `wfnjI / 健康提醒页 Health Reminder`
    - `Y55fu / 问诊医生列表页 Consultation Doctor List`
    - `0akq8 / 医生详情页 Consultation Doctor Detail`
    - `oKwYh / 发起问诊页 Consultation Create`
    - `ob35v / 问诊会话页 Consultation Chat`
    - `TO5jB / 问诊列表页 Consultation List`
    - `WP23x / 紧急问诊页 Urgent Consultation`
    - `WnUMs / 社区详情页 Community Detail`
    - `RKyhI / 社区搜索页 Community Search`
    - `gDeKf / 社区话题页 Community Topic`
    - `GFqFg / 社区发布页 Community Publish`
    - `dtmAQ / 私信会话页 Message Conversations`
    - `hHmNO / 私信聊天页 Message Chat`
    - `X6bge / 订单取消流程提示`
  - 2026-03-28 已落盘并保留的 beauty 链画板包括：
    - `Mpxrb / 护理首页 Beauty Care Home`
    - `ex1sD / 附近机构搜索页 Beauty Nearby Search`
  - 2026-03-28 已从正式 `.pen` 删除的 beauty 旧分叉画板包括：
    - `jExWV / 护理专题页 Beauty Care Topic`
    - `OF4tv / 附近机构列表页 Beauty Nearby List`
    - `tyOZN / 机构详情页 Beauty Place Detail`
  - 仍保持可用且与现有 schema 对得上的核心顶层画板包括：首页、商城、社区主入口、消息、我的、AI 诊断、商品详情、购物车、结算、支付方式、支付验证、支付密码、支付成功、登录、地址列表、地址编辑、待支付订单详情、已完成订单详情、订单评价、取消订单弹层、取消态订单详情、宠物列表、单宠物健康档案页
- 后台设计源：`design/backendManagement.pen`
  - 当前仍不能作为后台管理端开发依据

## schema 与设计源同步状态
- 当前 page schema 数量：43
- 强一致性核对结果：
  - 当前 43 个 schema 文件已与当前已落盘 `.pen` 顶层画板对齐
  - 其中 `pet-edit.page-schema.json` 通过 `meta.variantFrameIds` 同时覆盖 `11vrw / 编辑宠物页 Pet Edit` 与 `WA7xQ / 新增宠物页 Pet Add`
  - 其中 `health-edit.page-schema.json` 通过 `meta.variantFrameIds` 同时覆盖 `7YI5K / 新增健康记录页 Health Record Create` 与 `kabmm / 编辑健康记录页 Health Record Edit`
  - 当前 45 个正式页面画板都已进入 schema 覆盖范围
- 已确认与当前 `.pen` 顶层 frameId 一致的关键 schema 包括：
  - `home.page-schema.json`
  - `shop.page-schema.json`
  - `news.page-schema.json`
  - `user.page-schema.json`
  - `diagnosis.page-schema.json`
  - `cart.page-schema.json`
  - `checkout.page-schema.json`
  - `address-list.page-schema.json`
  - `address-edit.page-schema.json`
  - `order-detail.page-schema.json`
  - `completed-order-detail.page-schema.json`
  - `cancel-order-modal.page-schema.json`
  - `cancelled-order-detail.page-schema.json`
  - `health-list.page-schema.json`
  - `health-edit.page-schema.json`
  - `health-reminder.page-schema.json`
  - `consultation-doctor-list.page-schema.json`
  - `consultation-doctor-detail.page-schema.json`
  - `consultation-create.page-schema.json`
  - `consultation-chat.page-schema.json`
  - `consultation-list.page-schema.json`
  - `consultation-urgent.page-schema.json`
  - `community-detail.page-schema.json`
  - `community-search.page-schema.json`
  - `community-topic.page-schema.json`
  - `community-publish.page-schema.json`
  - `message-conversations.page-schema.json`
  - `message-chat.page-schema.json`
  - `beauty.page-schema.json`
  - `beauty-booking.page-schema.json`
  - `pet-list.page-schema.json`
  - `pet-profile.page-schema.json`
- 已在本轮按当前 `.pen` 刷新的关键 schema：
  - `health-list.page-schema.json`
    - 当前 `frameId` 为 `2fI4W`
    - 已承接健康记录列表页的筛选、摘要卡、记录卡与底部双动作
  - `health-edit.page-schema.json`
    - 当前主 `frameId` 为 `kabmm`
    - `meta.variantFrameIds.create = 7YI5K`
    - 已把新增态与编辑态统一收敛到单 schema
  - `health-reminder.page-schema.json`
    - 当前 `frameId` 为 `wfnjI`
    - 已承接提醒摘要、筛选提醒、提醒列表与底部双动作
  - `consultation-doctor-list.page-schema.json`
    - 当前 `frameId` 为 `Y55fu`
    - 已承接在线问诊入口、科室筛选、医生卡片与底部双动作
  - `consultation-doctor-detail.page-schema.json`
    - 当前 `frameId` 为 `0akq8`
    - 已承接医生可信度信息、咨询方式选择与用户反馈卡
  - `consultation-create.page-schema.json`
    - 当前 `frameId` 为 `oKwYh`
    - 已承接医生卡、宠物切换、问诊方式、症状描述与提交动作
  - `consultation-chat.page-schema.json`
    - 当前 `frameId` 为 `ob35v`
    - 已承接会话头卡、消息流、加急切换提示与输入栏
  - `consultation-list.page-schema.json`
    - 当前 `frameId` 为 `TO5jB`
    - 已承接咨询摘要、状态 tabs、咨询卡片与底部双动作
  - `consultation-urgent.page-schema.json`
    - 当前 `frameId` 为 `WP23x`
    - 已承接加急派单头卡、危险症状 chips、紧急描述与提交流程
  - `community-detail.page-schema.json`
    - 当前 `frameId` 为 `WnUMs`
    - 已承接发帖主卡、评论面板与底部回复栏
  - `community-search.page-schema.json`
    - 当前 `frameId` 为 `RKyhI`
    - 已承接搜索输入、结果模式切换、最近搜索与帖子/话题结果卡
  - `community-topic.page-schema.json`
    - 当前 `frameId` 为 `gDeKf`
    - 已承接话题摘要头卡、话题帖子卡与参与话题 CTA
  - `community-publish.page-schema.json`
    - 当前 `frameId` 为 `GFqFg`
    - 已承接动态编辑器、媒体上传、关联信息与底部发布按钮
  - `message-conversations.page-schema.json`
    - 当前 `frameId` 为 `dtmAQ`
    - 已承接未读摘要头卡与会话列表
  - `message-chat.page-schema.json`
    - 当前 `frameId` 为 `hHmNO`
    - 已承接双侧消息流、图片消息、提示条与底部输入栏
  - `pet-edit.page-schema.json`
    - 当前主 `frameId` 为 `11vrw`
    - `meta.variantFrameIds.create = WA7xQ`
    - 已把新增态与编辑态统一收敛到单 schema
  - `pet-timeline.page-schema.json`
    - 当前 `frameId` 为 `zqXpi`
    - 已改为承接“单宠物成长档案页 Pet Growth Profile”而非旧时间轴页
  - `beauty.page-schema.json`
    - 当前 `frameId` 为 `Mpxrb`
    - 已承接后台可配置护理热点首页与单一附近机构 CTA
  - `beauty-booking.page-schema.json`
    - 当前 `frameId` 为 `ex1sD`
    - 已承接单页附近机构搜索与地图式地址结果列表
- 已在本轮移除的旧 schema：
  - `diagnosis-detail.page-schema.json`
    - 原因：当前 `.pen` 顶层已无对应正式画板 `K1C5b`
    - 处理：不再保留为独立 schema，不再作为前端候选独立页面
  - `beauty-detail.page-schema.json`
    - 原因：最新 `.pen` 已不再保留独立护理专题页
    - 处理：从正式 schema 目录移除
  - `beauty-booking-list.page-schema.json`
    - 原因：最新 `.pen` 已把搜索结果并回 `pages/beauty/booking/index`
    - 处理：从正式 schema 目录移除
  - `beauty-booking-detail.page-schema.json`
    - 原因：最新 `.pen` 已不再保留独立机构详情页
    - 处理：从正式 schema 目录移除
- 已落盘正式 `.pen` 的消费端页面都已有对应 schema；剩余差距集中在页面代码承接与截图验收

## 当前仓库事实
- 小程序 `app.json` 当前注册页面：60 个
- 当前 page schema 数量：43 个
- 工作区当前存在未提交改动：
  - 规则文件已在本轮收敛更新：`docs/project-rules.md`、`docs/rules/global-project-rules.md`、`docs/rules/design-source-rules.md`、`docs/rules/frontend-implementation-rules.md`、`docs/rules/backend-development-rules.md`
  - 后端数据库口径已进一步更新：`BaseEntity` 默认不再携带 `is_deleted`；当前仅 `community_post`、`community_comment` 保留软删除；`private_conversation` 已改为 `user1_hidden_time / user2_hidden_time` 按用户维度隐藏会话
  - 后端有一轮进行中的 controller 分层/包路径重组改动，集中在 `apps/backend/pet-cloud-consultation/pet-cloud-shop-service` 与 `apps/backend/pet-cloud-consultation/pet-cloud-user-service`
  - 前端 schema / 状态文档本轮已更新：保留并重写 `beauty.page-schema.json`、`beauty-booking.page-schema.json`，删除 `beauty-detail.page-schema.json`、`beauty-booking-list.page-schema.json`、`beauty-booking-detail.page-schema.json`；并同步刷新 `apps/frontend/docs/design-schemas/README.md`、`docs/plans/frontend-page-status-matrix.md`、`memory/project-context-handoff.md`
  - 仍有 `.DS_Store` 类杂项未跟踪文件
  - 前端 `apps/frontend/miniprogram/utils/api.js` 里的 `MapAPI` 目前只暴露 `geocode / reverseGeocode / searchSuggest`，beauty 链真正落代码前还需要补 `GET /api/map/poi/nearby` 适配
- 已存在但尚未承接的页面事实：
  - `pages/pet/profile/index` 仍未建路由与页面文件
  - `pages/order/cancelled-detail` 仍未建路由与页面文件
- 已有代码但仍明显承接旧设计的页面：
  - `pages/pet/list` 当前代码仍只有单个“查看档案”入口，未按正式 `.pen + schema` 承接 `健康档案 / 成长档案` 双入口
  - `pages/pet/edit` 仍是旧的分步向导式实现
  - `pages/pet/timeline/index` 仍是旧健康时间轴实现
  - `pages/health/list`、`pages/health/edit`、`pages/health/reminder/index` 虽已注册且页面文件完整，但当前仍是通用 CRUD / 弹窗式旧实现
  - `pages/consultation/doctor-list`、`pages/consultation/doctor-detail`、`pages/consultation/create`、`pages/consultation/chat`、`pages/consultation/list`、`pages/consultation/urgent/index` 虽已注册且页面文件完整，但当前仍是旧问诊页结构，尚未按正式画布收敛
  - `pages/community/detail`、`pages/community/search`、`pages/community/topic`、`pages/community/publish` 虽已注册且页面文件完整，但当前仍是旧社区实现
  - `pages/message/conversations`、`pages/message/chat` 虽已注册且页面文件完整，但当前仍是通用私信/聊天底稿
  - `pages/beauty/beauty`、`pages/beauty/booking/index` 虽已注册且页面文件完整，但当前仍是旧门店/预约链实现，尚未承接新的“护理热点内容 + 单页附近机构搜索”语义
  - `pages/beauty/detail/index`、`pages/beauty/booking-list/index`、`pages/beauty/booking-detail/index` 当前虽仍有代码与路由，但已退出最新正式设计链路，应后续并入或裁撤
- `X6bge / 订单取消流程提示` 是当前 `.pen` 中新增的链路提示画板，但还不是 `app.json` 独立页面

## 当前接入判断
- 已注册且页面文件完整，但仍需参考当前画布更新的页面：
  - `pages/pet/list`
  - `pages/pet/edit`
  - `pages/pet/timeline/index`
  - `pages/order/pending-review`
  - `pages/health/list`
  - `pages/health/edit`
  - `pages/health/reminder/index`
  - `pages/consultation/doctor-list`
  - `pages/consultation/doctor-detail`
  - `pages/consultation/create`
  - `pages/consultation/chat`
  - `pages/consultation/list`
  - `pages/consultation/urgent/index`
  - `pages/community/detail`
  - `pages/community/search`
  - `pages/community/topic`
  - `pages/community/publish`
  - `pages/message/conversations`
  - `pages/message/chat`
  - `pages/beauty/beauty`
  - `pages/beauty/booking/index`
- 已注册且当前实现至少基本承接正式链路的页面：
  - `pages/order/detail`
- 当前压根还没接入的独立页面：
  - `pages/pet/profile/index`
  - `pages/order/cancelled-detail`
- 当前属于弹层/中间态、不是 `app.json` 独立页面的设计对象：
  - `payment-method-modal`
  - `payment-verification`
  - `payment-password-entry`
  - `cancel-order-modal`
  - `cart-empty`
  - `X6bge / 订单取消流程提示`

## 已知过期点
- 旧 handoff 中“顶层正式画板数 29”已失效，当前实际为 44
- 旧 handoff 中把全部 44 个顶层 frame 都视为正式页面也不够精确；当前应按 `43 个正式页面画板 + 1 个辅助流程提示 frame` 理解
- 旧 handoff 使用的工作区路径 `/Users/luohao/Desktop/...` 已失效
- 旧 schema 中残留的 `/Users/luohao/Desktop/WeChat_MiniProgramDev/desgin/frontendMobileScreens.pen` 也已失效，当前应统一指向活动工作区中的 `.pen` 文件
- 旧 handoff 中把 `diagnosis-detail` 继续当作独立 schema / 独立页面候选的结论已失效
- 旧 handoff 中把 `pet-edit`、`pet-timeline` 记为 schema 漂移、把 `WA7xQ` 记为无 schema 的结论已失效
- 旧矩阵中把健康记录链、问诊链、社区详情链、私信链都归为“有代码无schema”，已经不准确；当前事实是这些页面都已补 schema，问题集中在代码仍未按 schema 收敛
- 旧上下文中把 beauty 链视为“平台美容交易/预约链”的结论已失效；当前已重定义为“后台可配置护理热点内容 + 单页附近机构搜索链”

## 后端协同要点
- 后端必须让位于已确认原型交互，不允许旧接口反向定义新页面
- 交易链联调建议同时拉起：
  - `pet-cloud-user-service`
  - `pet-cloud-shop-service`
  - `pet-cloud-map-service`
  - `pet-cloud-media-service`
- Java 校验默认要求显式切换 Java 21

## 当前阻塞
- `.pen` 在 2026-03-26、2026-03-27 连续扩展后，schema 已追平，但页面代码没有同步跟上
- 多个已接入页面虽然路由和文件齐全，但实际代码仍停留在旧实现，不能视为已承接当前画布
- 健康记录链虽然已补 schema，但 `pages/health/list`、`pages/health/edit`、`pages/health/reminder/index` 代码仍是旧实现
- 问诊链虽然已补 schema，但 `pages/consultation/doctor-list`、`pages/consultation/doctor-detail`、`pages/consultation/create`、`pages/consultation/chat`、`pages/consultation/list`、`pages/consultation/urgent/index` 代码仍是旧实现
- 社区详情链虽然已补 schema，但 `pages/community/detail`、`pages/community/search`、`pages/community/topic`、`pages/community/publish` 代码仍是旧实现
- 私信链虽然已补 schema，但 `pages/message/conversations`、`pages/message/chat` 代码仍是旧实现
- beauty 链虽然已补 schema，但当前 2 个正式页面代码仍是旧门店/预约模型，且前端 `MapAPI` 尚未补 nearby 包装
- `pet-profile` 的“查看完整诊断”卡片虽然仍存在，但当前 `.pen` 没有给出独立详情页正式画板，因此前端不能自行扩展出 `pages/diagnosis/detail`
- 宠物主链路和订单取消链路仍未形成“最新 `.pen` + 最新 schema + 页面接入 + 截图验收”的闭环
- 后台管理端仍无可用正式设计源

## 下一步建议
- 如果继续做前端同步：
  - schema 层已完成收口，后续只允许按最新 schema 推进页面代码
- 如果继续做页面实现：
  - 优先补 `pages/pet/profile/index`
  - 然后补 `pages/order/cancelled-detail`
  - 再按刷新后的 schema 重写 `pages/pet/edit`、`pages/pet/timeline/index`
  - 紧接着重写 `pages/community/detail`、`pages/community/search`、`pages/community/topic`、`pages/community/publish`
  - 然后重写 `pages/message/conversations`、`pages/message/chat`
  - 如果切到 beauty 链：先补 `MapAPI.nearby`，再按新 schema 重写 `pages/beauty/beauty`、`pages/beauty/booking/index`，并同步处理 `pages/beauty/detail/index`、`pages/beauty/booking-list/index`、`pages/beauty/booking-detail/index` 的并入或下线
- 如果继续做验收：
  - 必须在 schema 刷新后重新做微信开发者工具或真机截图对照
- 如果继续做上下文治理：
  - 维持 `memory/project-context-handoff.md` 与 `docs/plans/frontend-page-status-matrix.md` 同轮更新

## 下次会话优先读取
1. `design/frontendMobileScreens.pen`
2. `docs/rules/global-project-rules.md`
3. `docs/project-rules.md`
4. `memory/project-context-handoff.md`
5. `docs/plans/frontend-page-status-matrix.md`
6. `apps/frontend/docs/design-schemas/README.md`

## 维护要求
- 只要本轮工作影响下次接手判断，就更新本文件
- 至少同步：
  - 当前进行中的任务
  - 新增/修改的 `.pen`、schema、代码、规则、计划
  - 最新阻塞
  - 最合理的下一步
