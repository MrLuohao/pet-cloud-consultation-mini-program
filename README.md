# WeChat Mini Program Dev

这个仓库当前的工作方式不是“先看代码怎么写”，而是：

`先看 .pen 设计源，再按规则与 schema 落地代码。`

## 必读顺序

新对话、新成员接手、或开始任何页面设计与开发前，统一按下面顺序阅读：

1. `design/frontendMobileScreens.pen`
2. `docs/project-rules.md`
3. `docs/frontend-design-principles.md`
4. `apps/frontend/docs/design-schemas/README.md`
5. `docs/backend-integration-tracking.md`

## 当前唯一事实源

- `.pen` 画布是最终视觉与交互基准
- `docs/project-rules.md` 是项目统一规则入口
- `page schema` 是页面实现契约
- 现有代码优先级低于已确认设计

## 硬规则

- 开发必须 1:1 还原 `.pen`
- 不允许自行新增、删除、重排模块
- 不允许擅自简化弹层、转场、验证流程、地图交互、特效反馈
- 页面实现必须走 `.pen -> schema -> 代码 -> 截图校验`
- 如果必须偏离 `.pen`，只能因为平台能力、性能、合规或安全限制，并且必须先确认

## 当前保留的核心文档

- `docs/project-rules.md`
- `docs/frontend-design-principles.md`
- `docs/backend-integration-tracking.md`
- `docs/product-image-generation-guidelines.md`
- `apps/frontend/docs/design-schemas/README.md`
- `apps/frontend/docs/design-schemas/page-schema.contract.json`

## 当前重点链路

当前重点交易链路按以下顺序理解：

1. 结算页
2. 支付方式弹窗
3. 支付验证层
4. 支付成功反馈
5. 地址管理 / 地址编辑

其中：

- 支付方式是覆盖在结算页上的底部弹层
- 支付验证与支付成功应保留系统级反馈感
- 地址链路当前按“地址管理页选择 + 地址编辑页补全”两页版理解

## 仓库结构

- `design/`: Pencil `.pen` 设计源
- `docs/`: 项目核心规则与协同文档
- `apps/frontend/`: 小程序前端与 schema 契约
- `apps/backend/`: 后端服务
