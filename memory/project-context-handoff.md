# Project Context Handoff

更新时间：2026-03-21

## 项目
- 名称：PetCloud Consultation
- 工作区：`/Users/luohao/Desktop/pet-cloud-consultation-mini-program`
- 仓库结构：`apps/frontend`、`apps/backend/pet-cloud-consultation`、`design`、`docs`、`memory`
- 前端：微信小程序原生 `WXML / WXSS / JS`
- 后端：Java 21 + Spring Boot 3.2.5 多模块服务
- 当前阶段：开发中 + 验证中，且上下文同步不完整

## 核心入口
- 总规则入口：`docs/project-rules.md`
- 全局项目规则：`docs/rules/global-project-rules.md`
- 设计源规则：`docs/rules/design-source-rules.md`
- 前端实现规则：`docs/rules/frontend-implementation-rules.md`
- 后端开发规则：`docs/rules/backend-development-rules.md`
- 正式设计源：`design/frontendMobileScreens.pen`
- schema 规则入口：`apps/frontend/docs/design-schemas/README.md`
- 页面状态矩阵：`docs/plans/2026-03-21-frontend-page-status-matrix.md`

## `.pen` 状态
- 主设计源：`design/frontendMobileScreens.pen`
  - 当前顶层正式画布数：29
  - 已覆盖主消费端页面：首页、商城、社区、消息、我的、AI 诊断、商品详情、购物车、结算、支付方式、支付验证、支付密码、支付成功、登录、地址列表、地址编辑、订单详情、取消订单弹层、取消态订单详情、订单评价、宠物列表、诊断档案页、成长档案页、完整诊断报告页、宠物编辑页，以及宠物编辑的新增态变体
  - 顶层未发现 `draft / todo / wip / 待完善` 命名，但并不代表全部已完成截图验收
- 后台设计源：`design/backendManagement.pen`
  - 当前只有 1 个空白顶层 `Frame`
  - 不能作为后台管理端开发依据

## 已进入正式设计链路的页面契约
- 当前 page schema 数量：27
- 具体清单与执行状态统一见 `docs/plans/2026-03-21-frontend-page-status-matrix.md`

## 已知失配与过期点
- `address-picker` 旧契约已从 schema 目录移除
  - 当前地址链路应统一按 `address-list` + `address-edit` 理解
  - 仍引用 `address-picker / D5qTR` 的计划或摘要都应视为过期
  - 已补到当前 schema 清单与地址链最新事实
  - 需结合当前 schema 数量、最新 `.pen` 画布与代码改动重新校准

## 当前仓库事实
- 小程序 `app.json` 当前注册页面：60 个
- 当前 page schema 数量：27 个
- 说明：大量页面仍停留在旧实现或未进入 `.pen + schema` 链路
- 页面执行优先级、完成度、前端接入状态统一记录在 `docs/plans/2026-03-21-frontend-page-status-matrix.md`
- 宠物列表页 `pages/pet/list`
  - 已有 `pet-list.page-schema.json`
  - `cKCx8` 已正式落稳为新版“我的宠物”单主卡浏览页
  - 当前画布特征：右上角轻量 `新增`、单主卡大头照、照片分页与宠物分页分层、底部轻量宠物缩略导航、主卡内 `健康档案 / 成长档案` 双入口
  - `pet-list.page-schema.json` 已按新版 `cKCx8` 重新同步
  - schema 已将宠物主卡动作拆为 `/pages/pet/profile/index` 与 `/pages/pet/timeline/index` 两条档案入口，后续代码承接应以此为准
- 单宠物健康档案页 `.pen`
  - `frameId = H6Xma`
  - 已放在 `cKCx8` 右侧，作为宠物主链路下一屏
  - 当前已正式落稳为新版“诊断档案”页
  - 当前画布特征：身份卡轻入口编辑资料、首屏重点诊断主卡、轻量日常健康概览、重要诊断历史、历史模块右上角轻量 `查看更多`、底部 `再次发起诊断`
  - `pet-profile.page-schema.json` 已按新版 `H6Xma` 重新同步
- 宠物成长档案页 `.pen`
  - `frameId = cAZG0`
  - 已放在 `H6Xma` 右侧，作为宠物主链路中的“成长档案”分支
  - 当前已正式建立为“成长回忆相册”式页面
  - 当前画布特征：封面故事卡、成长节点、最近回忆、回忆模块右上角轻量 `查看更多`、底部 `新增成长记录`
  - `pet-timeline.page-schema.json` 已新增并按当前 `cAZG0` 结构同步
- 完整诊断报告页 `.pen`
  - `frameId = K1C5b`
  - 已放在 `cAZG0` 右侧，作为“查看完整诊断”的正式承接页
  - 当前已建立为“AI 原始报告 + 线下诊疗补充 + 结果对照”的完整报告页
  - 当前画布特征：报告头部、首屏摘要卡、本次诊断依据、AI 详细判断、线下诊疗补充、结果对照、底部 `补充线下记录`
  - `diagnosis-detail.page-schema.json` 已新增并按当前 `K1C5b` 结构同步
- 宠物编辑页 `.pen`
  - `frameId = FLS1j`
  - 已放在 `cAZG0` 右侧，作为宠物主链路中的“资料维护”页
  - 当前已正式建立为“编辑优先的单页卡片表单”页面，不再延续旧的分步向导式视觉
  - 当前画布特征：顶部轻说明、宠物身份预览卡、基础信息 / 成长信息 / 个性与照护备注三段表单卡、底部主保存按钮、底部弱危险区删除入口
  - `pet-edit.page-schema.json` 已新增，后续代码落地应直接按新 schema 替换旧向导式实现
- 宠物新增态变体 `.pen`
  - `frameId = CiXLc`
  - 已作为 `pet/edit` 的新增态正式变体加入同一设计链路
  - 当前画布特征：沿用编辑态卡片语言，但在基础信息区新增“常见品种”横向图片推荐卡，适配首次添加宠物的决策场景
  - `pet-edit.page-schema.json` 当前同时覆盖 `FLS1j` 编辑态与 `CiXLc` 新增态
- 地址链路
  - 当前正式契约是 `address-list.page-schema.json` + `address-edit.page-schema.json`
  - 旧的 `address-picker.page-schema.json` 已移除，避免继续指向不存在的 `D5qTR`
- 订单相关
  - 2026-03-20 已在 `.pen` 新增取消态订单详情变体：`yxr2Z / 订单详情页 Cancelled Order Detail`
  - 当前取消链路按 `HJ257 -> 4XhHR -> yxr2Z` 理解，其中 `yxr2Z` 内已补轻量 `订单已取消` 提示
  - `4XhHR` 仍只承担取消原因确认，不再承载取消成功结果页
  - `cancel-order-modal.page-schema.json`
  - `completed-order-detail.page-schema.json`
  - `order-review.page-schema.json`
  - `cancelled-order-detail.page-schema.json` 已新增，并按当前 `yxr2Z` 结构同步
  - 以上已存在 schema 仍缺截图级验收
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
- `pet-list` / `pet-profile` / `pet-timeline` / `diagnosis-detail` 的 `.pen` 与 schema 已完成同步，但宠物与诊断相关前端承接仍未完成。
- `pet-edit` 已补正式新版双态 `.pen + schema`，但前端页面代码仍是旧的分步向导实现。
- 订单取消链路的 `cancelled-order-detail` schema 已同步，但前端实现尚未开始。
- 多份计划与跟踪文档未完全同步到 2026-03-19 的仓库事实。

## 下一步建议
- 如果继续做消费端前端页面：
  - 先确认目标页是否同时具备最新 `.pen` 与最新 schema
  - 若缺任一项，先补设计或刷新 schema
- 如果继续做交易链：
  - 先让订单详情页承接 `HJ257 -> 4XhHR -> 轻提示 -> yxr2Z` 的原地切换逻辑
  - 最后补微信开发者工具或真机截图级对照验收，再宣称完成
- 如果继续做宠物相关页：
  - `pet-list` / `pet-profile` / `pet-timeline` / `pet-edit` 已完成新版 `.pen -> schema` 同步
  - 下一步应直接按新 schema 把 `pages/pet/edit` 改成“编辑态单页表单 + 新增态轻量品种图卡”的双态实现
  - 紧随其后再继续承接 `pages/pet/timeline/index` 与 `pages/pet/profile/index`，把宠物主链路补成完整闭环
- 如果继续做诊断相关页：
  - `diagnosis-detail.page-schema.json` 已建立
  - 下一步可继续补 `重要诊断历史查看更多` 与 `补充线下记录` 对应画布，再统一进入前端实现
- 如果继续做地址相关页：
  - 继续以 `address-list` / `address-edit` 两页契约推进，不再恢复 `address-picker`
- 如果继续做后台页：
  - 先补后台 `.pen`，禁止直接编码
- 如果继续做上下文治理：
  - 优先同步 `docs/project-rules.md`、`docs/rules/*` 与 `memory/project-context-handoff.md`

## 下次会话优先读取
1. `docs/project-rules.md`
2. `docs/rules/global-project-rules.md`
3. `memory/project-context-handoff.md`
4. `design/frontendMobileScreens.pen`
5. `docs/plans/2026-03-21-frontend-page-status-matrix.md`
6. `apps/frontend/docs/design-schemas/README.md`

## 维护要求
- 只要本轮工作影响下次接手判断，就更新本文件。
- 至少同步：
  - 当前进行中的任务
  - 新增/修改的 `.pen`、schema、代码、规则、计划
  - 最新阻塞
  - 最合理的下一步
