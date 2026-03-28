# Project Context Handoff

更新时间：2026-03-28

## 项目
- 名称：PetCloud Consultation
- 工作区：`/Volumes/Beelink/software/pet-cloud-consultation-mini-program`
- 仓库结构：`apps/frontend`、`apps/backend/pet-cloud-consultation`、`design`、`docs`、`memory`
- 前端形态：微信小程序原生 `WXML / WXSS / JS`
- 后端目录：`apps/backend/pet-cloud-consultation`
- 当前主目标：继续推进原型设计、页面结构确认与 schema 同步，不以现有代码作为完成依据

## 当前阶段
- 整体阶段：原型设计阶段
- 消费端：`design/frontendMobileScreens.pen` 与 `apps/frontend/docs/design-schemas/` 持续推进中，已有较多正式页面与 schema，但前端代码大多仍未按新版设计闭环
- 管理后台：`design/backendManagement.pen` 已进入正式补画阶段，但仍属于原型设计推进，不进入后台前端实现
- 后端：尚未进入正式实现阶段；当前仓库中的 Java / SQL 代码只可视为预研、草稿或结构占位，不作为当前项目进度或完成度依据

## 核心入口
- 总规则入口：`docs/project-rules.md`
- 全局项目规则：`docs/rules/global-project-rules.md`
- 设计源规则：`docs/rules/design-source-rules.md`
- 管理后台设计规则：`docs/rules/backend-management-design-rules.md`
- 管理后台产品规则：`docs/rules/backend-management-product-rules.md`
- 前端实现规则：`docs/rules/frontend-implementation-rules.md`
- 后端开发规则：`docs/rules/backend-development-rules.md`
- 消费端正式设计源：`design/frontendMobileScreens.pen`
- 管理后台正式设计源：`design/backendManagement.pen`
- schema 规则入口：`apps/frontend/docs/design-schemas/README.md`
- 页面状态矩阵：`docs/plans/frontend-page-status-matrix.md`

## 当前设计事实
- 消费端设计源 `design/frontendMobileScreens.pen`
  - 当前保存版本含 59 个顶层画板
  - 按当前状态矩阵口径理解为：
    - 58 个正式页面画板
    - 1 个辅助流程提示画板：`X6bge / 订单取消流程提示`
  - 宠物档案链、问诊链、社区详情链、私信链、beauty 链、订单售后链等新版页面都已进入正式画板范围
- 管理后台设计源 `design/backendManagement.pen`
  - 当前已有 6 个顶层正式后台页面：
    - `运营总览 Dashboard`
    - `商品内容配置中心 Product Content Config`
    - `社区审核中心 Community Moderation`
    - `订单履约中心`
    - `用户 360 运营台`
    - `首页精选配置 / 发布排期`
  - 当前仍属于后台原型设计阶段，不应据此判断后台实现已开始

## Schema 与路由现状
- 当前 `apps/frontend/docs/design-schemas/` 下共有 57 个 `*.page-schema.json`
- 当前小程序 `apps/frontend/miniprogram/app.json` 中共注册 60 个页面路由
- 当前核心事实不是“页面大量缺设计”，而是“新版 `.pen + schema` 已扩展，但路由、旧代码和新版结构还没有完全闭环”
- 明确已存在 schema 但尚未建立前端页面文件或路由承接的重点页面包括：
  - `pages/pet/profile/index`
  - `pages/order/cancelled-detail`
  - `pages/order/list`
  - `pages/order/logistics-detail`
  - `pages/order/after-sales/apply`
  - `pages/order/after-sales/progress`
  - `pages/order/after-sales/complete`
- 宠物档案链当前口径：
  - `pet-profile.page-schema.json` 已存在，对应 `H6Xma / 单宠物健康档案页 Pet Health Profile`
  - `pages/pet/profile/index` 仍未创建，不能把宠物档案链视为前端已闭环
  - `pages/pet/list`、`pages/pet/edit`、`pages/pet/timeline/index` 当前代码仍主要承接旧实现
- 问诊链当前口径：
  - `consultation-doctor-list`、`doctor-detail`、`create`、`chat`、`list`、`urgent` 均已有正式 schema，且对应路由已注册
  - 但当前这些页面代码仍以旧版列表页 / 会话页 / 表单页结构为主，不能视为新版前端已完成

## 当前仓库事实
- 工作区当前存在较多未提交改动，主要集中在以下几类：
  - `design/*.pen`
  - `apps/frontend/docs/design-schemas/*.page-schema.json`
  - `docs/project-rules.md`
  - `docs/rules/*.md`
  - `docs/plans/frontend-page-status-matrix.md`
  - `memory/project-context-handoff.md`
- 管理后台规则已拆分为三层：
  - 画布 / 视觉 / 结构：`docs/rules/backend-management-design-rules.md`
  - 页面内容语义 / 模块职责：`docs/rules/backend-management-product-rules.md`
  - 后端实现契约：`docs/rules/backend-development-rules.md`
- 当前仍存在 `.DS_Store` 等无关未跟踪文件
- 后端目录、Maven 工程、SQL 文件虽然存在，但当前阶段不以这些文件判断“后端已实现”

## 当前阻塞
- 当前首要阻塞不是后端实现，而是原型确认与设计契约闭环
- 主要阻塞点包括：
  - 新版 `.pen` 已覆盖更多链路，但部分关键页面仍未建立页面文件承接
  - 已有路由的页面中，大量仍是旧版代码，尚未按当前 schema 收敛
  - 如果现在继续参考旧代码或旧摘要，很容易误判“某条链路已经完成”

## 下一步建议
- 继续以 `.pen -> schema` 为主线推进原型阶段，不把现有后端代码当作正式实现任务
- 如果继续补齐消费端原型闭环，优先看这些真实缺口：
  - `pages/pet/profile/index`
  - `pages/order/cancelled-detail`
  - `pages/order/list`
  - `pages/order/logistics-detail`
  - `pages/order/after-sales/*`
- 如果继续做页面复核，优先判断“已有路由但还是旧实现”的页面是否需要进入下一轮 schema 对齐，而不是重复补画
- 如果继续推进后台原型，应先按最新三层规则继续补 `design/backendManagement.pen`
- 等用户明确确认原型冻结后，再进入前端正式落代码与后端接口/数据契约实现阶段

## 显式过期上下文说明
- 任何把“仓库里已有 Java / SQL 代码”直接等同于“后端实现中”或“后端已完成”的旧结论，现已失效
- 任何把“已有页面路由或旧页面文件”直接等同于“新版设计已完成落地”的旧结论，现已失效
- 如后续出现旧 handoff、旧截图、旧 schema、旧代码与当前 `.pen` 冲突，统一以当前 `.pen` 和当前规则文件为准

## 下次优先读取入口
1. `docs/project-rules.md`
2. `docs/rules/global-project-rules.md`
3. `docs/rules/design-source-rules.md`
4. `docs/rules/backend-management-design-rules.md`
5. `docs/rules/backend-management-product-rules.md`
6. `docs/rules/frontend-implementation-rules.md`
7. `docs/rules/backend-development-rules.md`
8. `docs/plans/frontend-page-status-matrix.md`
9. `apps/frontend/docs/design-schemas/README.md`
10. `memory/project-context-handoff.md`
