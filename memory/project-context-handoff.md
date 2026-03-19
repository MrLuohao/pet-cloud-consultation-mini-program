# Pet Cloud Consultation Handoff

更新日期：2026-03-19

## 项目摘要

- 仓库名称：`pet-cloud-consultation`
- 项目类型：Java 多模块后端聚合工程
- 核心栈：Java 21、Spring Boot 3.2.5、Spring Cloud OpenFeign、MyBatis-Plus
- 主要模块：`pet-cloud-user-service`、`pet-cloud-shop-service`、`pet-cloud-ai-service`、`pet-cloud-map-service`、`pet-cloud-media-service`

## 持续生效规则

- 编译和测试必须使用 Java 21。
- 代码注释必须写在代码上方，禁止写在行尾。
- 继承 `BaseEntity` 的实体必须遵守统一基础字段规范。
- 是否保留 `is_deleted` 必须按业务类型判断，事实记录和流水表通常不保留。
- 接口变更后必须同步更新 `API.md`。

## .pen 设计源状态

- 当前仓库未发现任何 `.pen` 文件。
- 当前工作区上游根目录已存在消费端设计源：
  - `/Users/luohao/Desktop/WeChat_MiniProgramDev/desgin/frontendMobileScreens.pen`
- 交易域计划明确要求“以 `.pen` 画布为唯一业务形态来源”。
- 后端子仓库内部虽然没有 vendored `.pen`，但继续推进交易域时应以上游工作区的最新 `.pen` 与 schema 契约为准。

## 当前计划状态

- 当前显式计划文件：`docs/plans/2026-03-15-transaction-domain-reset-design.md`
- 当前阶段判断：交易域重构已进入“后端模型已落地，上游设计契约已刷新，联调与验收闭环未完成”阶段

## 已完成事实

- 交易域迁移脚本已补齐：`db/migrate_transaction_reset.sql`
- 购物车已升级为页面读模型：`CartPageVO`
- 订单确认已升级为 checkout draft：`OrderConfirmVO`
- 提交订单已补充 `cartIds`、`paymentMethod`、`verificationType`
- 地址模型已补充地图和解析字段
- 交易域相关单测已覆盖关键字段和支付方式语义

## 当前阻塞

- 后端子仓库内部不直接携带 `.pen`，需要持续以上游工作区设计源为准
- 旧对话续接摘要此前缺失，本文件从当前仓库事实重新生成
- 当前交易链的地址部分已切换为“地址管理页 + 地址编辑页”两页主流程，旧“地址选择页”表述如果继续出现在计划或联调口径中会产生歧义
- 若后续继续推进交易域，需要确认迁移脚本是否已在目标数据库执行

## 文档同步状态

- `API.md` 已在 2026-03-19 对齐当前地址、购物车、订单接口事实
- 若控制器、DTO、VO 再发生变化，必须继续同步 `API.md`

## 下一步建议

1. 继续以上游工作区 `.pen` 与 page schema 为准，而不是在后端子仓库内部重新派生页面语义。
2. 用上游设计源重新核对购物车、结算、支付方式弹层、支付验证层、地址管理页、地址编辑页是否与当前后端模型一致。
3. 在确认设计无偏差后，执行受影响模块测试和编译。
4. 如需继续推进交易域，优先检查 `db/migrate_transaction_reset.sql` 的执行状态和测试数据清理状态。
