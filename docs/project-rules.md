# Project Rules

适用范围：`/Volumes/Beelink/software/pet-cloud-consultation-mini-program`

这份文档只作为规则总入口、阅读导航与职责分层说明，不承接具体规则正文，也不作为业务事实冲突时的仲裁文件。

## 1. 规则分工

### 全局项目规则

- `docs/rules/global-project-rules.md`

负责：

- 唯一事实源优先级
- 全局协作铁律
- 文档同步规则
- handoff 职责边界

### 设计源协作规则

- `docs/rules/design-source-rules.md`

负责：

- `.pen` 作为事实源的优先级
- `.pen -> schema -> code` 的顺序
- 画布协作边界
- 结构化读取要求

### 消费端设计规则

- `docs/rules/frontend-mobile-design-rules.md`

负责：

- 消费端页面视觉基调
- 场景色调与图片规则
- 小程序导航栏设计约定
- 消费端页面结构与交互风格

### 管理后台设计规则

- `docs/rules/backend-management-design-rules.md`

负责：

- 后台 Web 页面骨架
- 工作台 / 列表 / 抽屉 / 图表的设计约束
- 后台视觉系统与组件系统
- 后台设计与实现协作边界

### 管理后台产品规则

- `docs/rules/backend-management-product-rules.md`

负责：

- 管理后台 V1 信息架构
- 模块职责与页面语义
- 宏观看板与对象处理闭环
- 视图、导出、跳转等后台产品行为

### 前端实现规则

- `docs/rules/frontend-implementation-rules.md`

负责：

- 前端实现流程
- schema -> code 的约束
- 小程序平台限制
- 验收标准

### 后端开发规则

- `docs/rules/backend-development-rules.md`

负责：

- Java / 数据库规范
- 后端接口协同边界
- 结构化字段契约
- 指标口径、阈值、SLA
- 审计日志、导出与权限边界

## 2. 执行与进度文件分工

### 消费端页面状态与执行进度

- `docs/plans/frontend-page-status-matrix.md`

负责：

- 页面优先级
- 完成度
- 前端接入状态
- 页面执行顺序

### 管理后台页面状态与执行进度

- `docs/plans/backend-management-page-status-matrix.md`

负责：

- 后台页面优先级
- 补画进度
- 规则对齐状态
- 实现准备状态
- 页面执行顺序

### 前端 schema 规则

- `apps/frontend/docs/design-schemas/README.md`
- `apps/frontend/docs/design-schemas/page-schema.contract.json`

负责：

- schema 目录规范
- schema 契约格式
- `.pen -> schema -> code -> verification` 的实现口径

### 交接摘要

- `memory/project-context-handoff.md`

负责：

- 当前现状
- 当前阻塞
- 最近变更
- 下一步建议
- 下次优先读取入口

不负责：

- 定义长期规则
- 重复复制规则正文

## 3. 建议阅读顺序

1. 按任务读取对应正式 `.pen`
   - 消费端：`design/frontendMobileScreens.pen`
   - 后台：`design/backendManagement.pen`
2. `docs/rules/global-project-rules.md`
3. `docs/rules/design-source-rules.md`
4. `docs/rules/frontend-mobile-design-rules.md`
5. `docs/rules/backend-management-design-rules.md`
6. `docs/rules/backend-management-product-rules.md`
7. `docs/rules/frontend-implementation-rules.md`
8. `docs/rules/backend-development-rules.md`
9. `docs/plans/backend-management-page-status-matrix.md`
10. `docs/plans/frontend-page-status-matrix.md`
11. `apps/frontend/docs/design-schemas/README.md`
12. `memory/project-context-handoff.md`

说明：

- 如果只是想知道“规则分工和去哪里看”，先读本文件。
- 如果遇到具体业务、设计、前端、后端判断冲突，以对应细则文件为准，而不是以本文件正文为准。

## 4. 当前目录建议理解

- `docs/project-rules.md`
  - 只看“规则分工”和“入口”
- `docs/rules/`
  - 只看长期规则
- `docs/plans/`
  - 只看计划与状态
- `memory/`
  - 只看交接摘要
