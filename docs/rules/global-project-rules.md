# Global Project Rules

适用范围：整个项目的协作、优先级、同步与交接规则。

## 唯一事实源优先级

后续所有判断统一按下面顺序执行：

1. 相关正式 `.pen` 的最新版本
2. `docs/rules/global-project-rules.md`
3. `docs/rules/design-source-rules.md`
4. `docs/rules/frontend-mobile-design-rules.md`
5. `docs/rules/backend-management-design-rules.md`
6. `docs/rules/backend-management-product-rules.md`
7. `docs/rules/frontend-implementation-rules.md`
8. `docs/rules/backend-development-rules.md`
9. `apps/frontend/docs/design-schemas/README.md`
10. 现有代码

说明：

- `docs/project-rules.md` 是规则导航页，不作为业务事实源参与冲突仲裁。
- 只在与当前任务相关的规则文件之间比较优先级，不把无关规则文件强行并入同一层判断。

## 全局协作铁律

- `.pen` 不是参考图，而是项目最高优先级事实源。
- 所有内容必须以 `.pen` 的最新实现为准。
- 一旦 `.pen` 已形成新的正式版本，后续补画、补 schema、补代码时必须站在新版本上继续推进。
- 绝对不允许用旧内容、旧截图、旧口头结论、旧 schema 去覆盖或回退新的 `.pen`。
- 代码要服务设计，后端要适配确认后的页面结构，不能让旧实现反向定义新页面。

## 文档职责分层

- `docs/project-rules.md`
  - 只做规则总入口、分类说明、建议阅读顺序。
- `docs/rules/global-project-rules.md`
  - 只放全局协作规则、同步规则、交接边界。
- `docs/rules/design-source-rules.md`
  - 只放 `.pen` 作为事实源的协作规则。
- `docs/rules/frontend-mobile-design-rules.md`
  - 只放消费端页面设计规则。
- `docs/rules/backend-management-design-rules.md`
  - 只放后台页面形态、视觉系统与设计协作规则。
- `docs/rules/backend-management-product-rules.md`
  - 只放后台信息架构、模块职责与页面语义规则。
- `docs/rules/frontend-implementation-rules.md`
  - 只放前端实现规则。
- `docs/rules/backend-development-rules.md`
  - 只放后端开发规则与稳定实现契约。
- `docs/plans/*.md`
  - 只放计划、状态矩阵、执行进度。
- `memory/project-context-handoff.md`
  - 只放现状、进度、阻塞、下一步和入口引用，不定义长期规则。

## 文档与进度实时同步规则

后续本项目中只要发生以下任一变化，必须在同一轮工作内同步更新对应项目文件，不能等到“之后再补”：

- `.pen` 设计发生修改
- 页面 schema、交互规则、实现约束发生修改
- 新计划建立、旧计划拆分、阶段顺序调整
- 任务状态发生变化：开始进行、完成、阻塞、延期、转为优化中
- 新增项目规则、变更既有规则、废弃旧规则

同步要求：

- 全局规则体系变化：更新 `docs/project-rules.md` 与 `docs/rules/global-project-rules.md`
- 如果只是某一类规则正文调整，优先更新对应细则文件；只有入口、分工或阅读顺序变化时才同步修改 `docs/project-rules.md`
- 设计源协作规则变化：更新 `docs/rules/design-source-rules.md`
- 消费端设计规则变化：更新 `docs/rules/frontend-mobile-design-rules.md`
- 管理后台设计规则变化：更新 `docs/rules/backend-management-design-rules.md`
- 管理后台产品规则变化：更新 `docs/rules/backend-management-product-rules.md`
- 前端实现规则变化：更新 `docs/rules/frontend-implementation-rules.md`
- 后端开发规则变化：更新 `docs/rules/backend-development-rules.md`
- 后台页面执行状态变化：更新 `docs/plans/backend-management-page-status-matrix.md`
- 前端页面执行状态变化：更新 `docs/plans/frontend-page-status-matrix.md`
- 如果本轮变更会影响下次接手判断：同步更新 `memory/project-context-handoff.md`

## Handoff 边界

`memory/project-context-handoff.md` 只负责记录：

- 当前事实
- 当前进度
- 当前阻塞
- 最近变更
- 下一步建议
- 下次优先读取入口

明确不负责：

- 定义长期规则
- 复制规则正文
- 替代 `docs/project-rules.md` 或 `docs/rules/*`

如果 handoff 中出现规则性约束，应当抽回规则文件，handoff 只保留入口引用和当前现状。
