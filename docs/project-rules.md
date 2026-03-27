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

### 画布 / `.pen` 规则

- `docs/rules/design-source-rules.md`

负责：

- `.pen` 设计阶段的行为约束
- 画布修改优先级
- 最新 `.pen` 覆盖旧内容的铁律
- schema 必须基于 Pencil MCP / pencli 结构化读取
- 画布阶段的产品设计判断标准

### 前端实现规则

- `docs/rules/frontend-implementation-rules.md`

负责：

- 前端实现流程
- schema -> code 的约束
- 小程序平台限制
- 验收标准
- 前端视觉与交互落地边界

### 后端开发规则

- `docs/rules/backend-development-rules.md`

负责：

- Java / 数据库规范
- 后端接口协同边界
- 结构化字段契约
- 交易链与业务字段口径

## 2. 执行与进度文件分工

### 页面状态与执行进度

- `docs/plans/frontend-page-status-matrix.md`

负责：

- 页面优先级
- 完成度
- 前端接入状态
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

1. `design/frontendMobileScreens.pen`
2. `docs/rules/global-project-rules.md`
3. `docs/rules/design-source-rules.md`
4. `docs/rules/frontend-implementation-rules.md`
5. `docs/rules/backend-development-rules.md`
6. `docs/plans/frontend-page-status-matrix.md`
7. `apps/frontend/docs/design-schemas/README.md`
8. `memory/project-context-handoff.md`

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
