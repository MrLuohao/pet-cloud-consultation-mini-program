# Project Context Handoff

更新时间：2026-03-19

## 项目
- 名称：PetCloud Consultation
- 工作区：`/Users/luohao/Desktop/pet-cloud-consultation-mini-program`
- 仓库结构：`apps/frontend`、`apps/backend/pet-cloud-consultation`、`design`、`docs`、`memory`、`tests`
- 前端：微信小程序原生 `WXML / WXSS / JS`
- 后端：Java 21 + Spring Boot 3.2.5 多模块服务
- 当前阶段：开发中 + 验证中，且上下文同步不完整

## 持续生效规则
- `.pen` 是最高优先级事实源，代码优先级最低。
- 前端必须遵循 `.pen -> schema -> code -> 截图校验`，且优先使用 Pencil 官方 MCP 工具链。
- 未确认前不得自行新增、删除、重排模块，不得擅自简化弹层、转场、支付验证、地图选点和成功反馈。
- 微信小程序默认保留原生导航栏，不得伪造系统状态栏时间、信号、电量。
- 设计、计划、schema、实现状态变化后，必须同轮同步规则文档、计划文档、前后端跟踪和本 handoff。

## 核心入口
- 规则入口：`docs/project-rules.md`
- 设计源规则：`docs/rules/design-source-rules.md`
- 前端实现规则：`docs/rules/frontend-implementation-rules.md`
- 后端开发规则：`docs/rules/backend-development-rules.md`
- 设计源：`design/frontendMobileScreens.pen`
- schema 规则：`apps/frontend/docs/design-schemas/README.md`

## `.pen` 状态
- 主设计源：`design/frontendMobileScreens.pen`
  - 当前顶层正式画布数：23
  - 已覆盖主消费端页面：首页、商城、社区、消息、我的、AI 诊断、商品详情、购物车、结算、支付方式、支付验证、支付密码、支付成功、登录、地址列表、地址编辑、订单详情、取消订单弹层、订单评价、宠物列表、单宠物健康档案页
  - 顶层未发现 `draft / todo / wip / 待完善` 命名，但并不代表全部已完成截图验收
- 后台设计源：`design/backendManagement.pen`
  - 当前只有 1 个空白顶层 `Frame`
  - 不能作为后台管理端开发依据

## 已进入正式设计链路的页面契约
- 当前 page schema 数量：23
- 已存在 schema：
  - `home`
  - `shop`
  - `community`
  - `news`
  - `user`
  - `diagnosis`
  - `product-detail`
  - `cart`
  - `cart-empty`
  - `checkout`
  - `payment-method-modal`
  - `payment-verification`
  - `payment-password-entry`
  - `pay-success`
  - `login`
  - `address-list`
  - `address-edit`
  - `order-detail`
  - `completed-order-detail`
  - `cancel-order-modal`
  - `order-review`
  - `pet-list`
  - `pet-profile`

## 已知失配与过期点
- `pet-profile.page-schema.json` 已补齐
  - 当前已指向 `frameId = H6Xma`
  - 但 `pages/pet/profile/index` 路由与页面实现仍不存在
- `address-picker` 旧契约已从 schema 目录移除
  - 当前地址链路应统一按 `address-list` + `address-edit` 理解
  - 仍引用 `address-picker / D5qTR` 的计划或摘要都应视为过期
  - 已补到当前 schema 清单与地址链最新事实
  - 需结合当前 schema 数量、最新 `.pen` 画布与代码改动重新校准

## 当前仓库事实
- 小程序 `app.json` 当前注册页面：60 个
- 当前 page schema 数量：23 个
- 说明：大量页面仍停留在旧实现或未进入 `.pen + schema` 链路
- 宠物列表页 `pages/pet/list`
  - 已有 `pet-list.page-schema.json`
  - 当前 `查看档案` 仍未切到独立 `pet-profile` 路由
- 单宠物健康档案页 `.pen`
  - `frameId = H6Xma`
  - 已放在 `cKCx8` 右侧，作为宠物主链路下一屏
  - 当前颜色结论：保留浅紫白版本，不采用更暖米白方案
  - 当前已进入 `schema` 阶段，但仍未进入 `route -> code` 阶段
- 地址链路
  - 当前正式契约是 `address-list.page-schema.json` + `address-edit.page-schema.json`
  - 旧的 `address-picker.page-schema.json` 已移除，避免继续指向不存在的 `D5qTR`
- 订单相关
  - `cancel-order-modal.page-schema.json`
  - `completed-order-detail.page-schema.json`
  - `order-review.page-schema.json`
  - 以上 3 个 schema 已新增，但仍缺截图级验收
- 前端子仓库当前未提交改动：
  - 当前实际工作区状态需以 `git -C PetCloudConsultation_Frontend status --short` 现查为准
  - 本轮同步前观察到的显式改动仅剩 IDE 文件
- 后端子仓库当前未提交改动：
  - 当前实际工作区状态需以 `git -C pet-cloud-consultation status --short` 现查为准
  - 本轮同步前观察到的显式改动为 `API.md` 与后端 handoff

## 后端协同要点
- 后端必须让位于已确认原型交互，不允许旧接口反向定义新页面。
- 交易链联调建议同时拉起：
  - `pet-cloud-user-service`
  - `pet-cloud-shop-service`
  - `pet-cloud-map-service`
  - `pet-cloud-media-service`
- Java 校验默认要求显式切换 Java 21。

## 当前阻塞
- 并非所有目标页都具备“最新 `.pen` + 最新 schema + 截图验收”完整闭环。
- 后台管理端没有可用设计源。
- `pet-profile` 仍停留在 `.pen` 与 schema 已完成、路由/实现未开始。
- 多份计划与跟踪文档未完全同步到 2026-03-19 的仓库事实。

## 下一步建议
- 如果继续做消费端前端页面：
  - 先确认目标页是否同时具备最新 `.pen` 与最新 schema
  - 若缺任一项，先补设计或刷新 schema
- 如果继续做交易链：
  - 先补微信开发者工具或真机截图级对照验收，再宣称完成
- 如果继续做宠物相关页：
  - 下一步优先注册 `pages/pet/profile/index` 路由并承接页面实现
- 如果继续做地址相关页：
  - 继续以 `address-list` / `address-edit` 两页契约推进，不再恢复 `address-picker`
- 如果继续做后台页：
  - 先补后台 `.pen`，禁止直接编码
- 如果继续做上下文治理：
  - 优先同步 `docs/project-rules.md`、`docs/rules/*` 与 `memory/project-context-handoff.md`

## 下次会话优先读取
1. `docs/project-rules.md`
2. `memory/project-context-handoff.md`
3. `design/frontendMobileScreens.pen`
4. `apps/frontend/docs/design-schemas/README.md`

## 维护要求
- 只要本轮工作影响下次接手判断，就更新本文件。
- 至少同步：
  - 当前进行中的任务
  - 新增/修改的 `.pen`、schema、代码、规则、计划
  - 最新阻塞
  - 最合理的下一步
