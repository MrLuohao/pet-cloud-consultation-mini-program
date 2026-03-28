# Frontend Implementation Rules

适用范围：`apps/frontend/` 下全部小程序实现与页面契约。

## 一、分层边界

- 本文件只定义消费端前端实现规则，不定义页面设计风格与视觉基调。
- 消费端页面视觉、图片、导航栏与交互风格规则，统一以下沉到 `docs/rules/frontend-mobile-design-rules.md` 为准。
- `.pen` 作为事实源的优先级与同步规则，统一以下沉到 `docs/rules/design-source-rules.md` 为准。

## 二、核心流程

- 页面实现统一遵循：`.pen -> schema -> code -> screenshot verification`。
- 所有前端契约与实现都必须以 `.pen` 的最新版本为准；如果 `.pen` 已更新，schema 和代码必须跟着更新，绝对不允许继续沿用旧 `.pen` 内容，更不允许用旧内容去覆盖新的 `.pen` 状态。
- 前端页面契约唯一目录：`apps/frontend/docs/design-schemas/`。
- 未进入 schema 的页面，不应直接凭印象写 WXML / WXSS / JS。
- schema 生成与刷新必须通过 Pencil 官方 MCP / pencli 对应工具直接读取 `.pen` 节点结构完成，禁止用视觉识别、截图理解或人工看图方式替代。

## 三、schema 与代码实现约束

- WXML 结构顺序必须服从 schema `sections`。
- WXSS 优先服从 schema `tokens` 与布局规则。
- JS 只负责把 API payload 转换成页面状态，不应擅自扩展视觉块。
- 如果代码需要新增视觉模块，先改 schema，再改代码。
- 未确认前，不得自行新增、删除、重排模块。
- 若 `.pen` 已定义弹层、转场、支付验证、地图交互、成功反馈，前端不得退化实现。

## 四、小程序实现边界

- 默认使用微信原生导航栏。
- 未确认前，不得启用 `navigationStyle: custom` 重做导航。
- 禁止在代码中伪造系统状态栏时间、信号、电量。
- 如果必须因为平台能力、性能、合规或安全限制偏离当前 `.pen`，必须先确认，再进入实现。

## 五、验收与同步要求

- 没有截图对照，不得宣称 1:1 还原。
- 截图只承担验收职责，不承担 schema 主生成职责。
- 视觉断言优先于“看起来差不多”。
- 页面契约、实现边界或关键交互变化后，必须在同一轮同步更新相关 schema 与协作文档。
