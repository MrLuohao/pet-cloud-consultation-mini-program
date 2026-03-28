# Pet Cloud Consultation Mini Program

伴宠云诊是一套围绕宠物健康、轻服务与交易链路打造的统一项目仓库。  
它将消费端小程序、后端服务和 Pencil 设计源整合到同一个代码库中，目标不是单纯把功能“做出来”，而是让产品体验、设计语言、接口能力和实现落地始终保持一致。

这个仓库适合承接三类工作：

- 消费端页面设计与实现
- 宠物健康、社区、商城、交易链相关后端开发
- 基于 `.pen` 设计源的页面还原、迭代与验收

## 项目概览

项目当前由三条主线组成：

- 消费端前端：微信小程序原生 `WXML / WXSS / JS`
- 后端服务：Java 21 + Spring Boot 3.2.5 多模块
- 设计源：Pencil `.pen` 文件，作为页面视觉与交互最高事实源

项目核心理念很明确：

`先看设计源，再按契约和规则落地代码。`

这意味着：

- 页面实现不能凭印象复刻
- 旧代码优先级低于已确认设计
- 后端接口需要服务页面最终形态，而不是反向限制页面

## 仓库结构

```text
.
├── apps/
│   ├── frontend/
│   └── backend/
│       └── pet-cloud-consultation/
├── design/
├── docs/
│   └── rules/
└── memory/
```

各目录职责如下：

- `apps/frontend/`
  消费端小程序代码与页面契约，页面 schema 位于 `apps/frontend/docs/design-schemas/`
- `apps/backend/pet-cloud-consultation/`
  后端父工程与各业务模块，数据库唯一建表 SQL 位于 `apps/backend/pet-cloud-consultation/db/pet_cloud_db.sql`
- `design/`
  Pencil 设计源，`frontendMobileScreens.pen` 是消费端页面设计主文件
- `docs/`
  项目统一规则与说明文档
- `memory/`
  项目续接摘要，适合新会话快速恢复上下文

## 当前产品范围

当前项目已经覆盖并持续迭代以下核心模块：

- 首页与宠物身份入口
- 社区内容流与宠物身份社交
- AI 健康诊断
- 商城与商品详情
- 购物车、结算、支付验证、支付成功
- 地址管理与地址编辑
- 宠物列表与单宠物健康档案链路

整体产品风格不是传统强运营电商，也不是纯工具型后台，而是：

`柔和、克制、轻服务、轻社交的宠物产品体验。`

## 如何开始

第一次接手项目，建议按这个顺序阅读：

1. `design/frontendMobileScreens.pen`
2. `docs/project-rules.md`
3. `docs/rules/design-source-rules.md`
4. `docs/rules/backend-management-design-rules.md`
5. `docs/rules/backend-management-product-rules.md`
6. `docs/rules/frontend-implementation-rules.md`
7. `docs/rules/backend-development-rules.md`
8. `apps/frontend/docs/design-schemas/README.md`
9. `memory/project-context-handoff.md`

如果你要做前端页面：

- 先读设计源
- 再读 schema
- 再改代码

如果你要做后端能力：

- 先确认当前页面交互和字段语义
- 再判断接口、VO、数据库是否需要配合

如果你要做管理后台画布：

- 先读 `design/backendManagement.pen`
- 再读 `docs/rules/design-source-rules.md`
- 再读 `docs/rules/backend-management-design-rules.md`
- 再读 `docs/rules/backend-management-product-rules.md`
- 确认后台定位、导航分组、首页形态后再继续补画

## 规则入口

根级 README 只负责介绍项目，不承担详细规则正文。  
当前统一规则入口如下：

- `docs/project-rules.md`
  项目总规则入口与优先级定义
- `docs/rules/design-source-rules.md`
  设计源、画布、商品图与设计协作规则
- `docs/rules/backend-management-design-rules.md`
  管理后台画布定位、信息架构、视觉基调与配置化设计规则
- `docs/rules/backend-management-product-rules.md`
  管理后台模块职责、看板语义、对象详情与工作台产品规则
- `docs/rules/frontend-implementation-rules.md`
  前端实现、视觉还原、小程序约束规则
- `docs/rules/backend-development-rules.md`
  后端开发、数据库、鉴权与接口协作规则

## 当前实现事实

几个对开发最重要的当前事实：

- 设计源优先级高于现有代码
- `apps/frontend/docs/design-schemas/` 是页面契约目录
- 地址链路当前按“地址管理页 + 地址编辑页”理解
- 后端 Maven 根目录在 `apps/backend/pet-cloud-consultation/pom.xml`
- 后端唯一保留的建表 SQL 是 `apps/backend/pet-cloud-consultation/db/pet_cloud_db.sql`

## 一句话理解这个仓库

这是一个把设计、前端和后端统一收口后的宠物健康与轻服务产品仓库，目标是让每一次页面迭代和接口调整，都能围绕同一份产品事实源协同推进。
