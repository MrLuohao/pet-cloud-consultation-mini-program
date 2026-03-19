# 页面设计补齐与重构计划

## 1. 背景

当前项目的统一规则已经明确：

- `.pen` 画布是视觉与交互唯一事实源
- 页面实现必须遵循 `.pen -> schema -> code -> 截图校验`
- 不允许用旧代码风格反向定义新设计

基于当前仓库扫描结果，项目已经具备一批核心页面的 `.pen` 画板与 page schema，但仍存在大量页面没有进入正式设计链路，或者虽然已有代码实现，但视觉语言明显偏离当前项目主题。

本计划用于统一后续页面补画、重画、schema 补齐与代码落地顺序，避免逐页临时决策。

## 2. 当前事实源

### 2.1 当前 `.pen` 已有顶层页面

文件：`design/frontendMobileScreens.pen`

已存在的核心画板：

- 首页优化版 Home Style
- 商城页轻量优化版 Shop Style
- 社区页面优化版 Social Style
- 消息中心 Community Style
- 我的页面优化版 Profile Style
- AI健康诊断 Professional Style
- 商品详情页 Poster Style
- 购物车页 Curated Cart Style
- 购物车页 Empty State
- 结算页 Summary Confirm Style
- 支付方式弹窗 Payment Method Modal
- 支付验证层 Payment Verification
- 支付成功 Success Feedback
- 地址管理页 Address List
- 地址编辑页 Address Edit
- 单宠物健康档案页 Pet Health Profile

### 2.2 当前已有 page schema

目录：`apps/frontend/docs/design-schemas`

已具备 schema 的页面：

- home
- shop
- news
- user
- diagnosis
- cart
- product-detail
- checkout
- payment-method-modal
- payment-verification
- payment-password-entry
- pay-success
- community
- login
- address-list
- address-edit
- order-detail
- completed-order-detail
- cancel-order-modal
- order-review
- pet-list
- pet-profile

### 2.3 当前统一设计基调

根据 `docs/project-rules.md` 与 `docs/frontend-design-principles.md`，项目统一设计基调如下：

- 简约
- 柔和
- 克制
- 有呼吸感
- 高级感来自秩序与细节，而不是装饰堆砌

场景分为两条主线：

- 品牌内容页：紫白系、轻粉感、柔和品牌氛围
- 交易确认页：暖灰白、米白、低饱和中性色

### 2.4 当前执行状态补记（2026-03-15）

本计划最初用于说明“后续怎么补页面”，但现在项目已经进入执行中段，因此必须以当前状态理解，而不是按全文的未来时态理解。

当前已经完成的关键前置工作：

- 已建立统一规则文档：
  - `docs/project-rules.md`
  - `docs/frontend-design-principles.md`
  - `docs/backend-integration-tracking.md`
  - `docs/product-image-generation-guidelines.md`
- 已建立 schema 中间层与 contract：
  - `apps/frontend/docs/design-schemas/page-schema.contract.json`
- 已具备 schema 的页面：
  - `home`
  - `shop`
  - `news`
  - `user`
  - `diagnosis`
  - `community`
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
- 已完成一批重点 UI 修正：
  - 商城恢复双列商品流
  - 商城顶部分类改为横向可滑动，并保留胶囊指示
  - 商城悬浮购物车回到右下角且可拖动
  - 社区普通宠物帖头像已并入身份信息块
  - 商品详情页底部操作条已改为整体连体结构
  - 页面不再伪造系统状态栏文本
- 已完成商品详情后端结构化能力与规格透传链路：
  - `specGroups`
  - `highlights`
  - `storySections`
  - `usageNote`
  - `specLabel/specLabels`

当前仍未完成的关键点：

- 画布中新增的页面还未重新做全量 Pencil MCP 扫描
- 第一阶段四个页面：
  - `login`
  - `address/list`
  - `address/edit`
  - `order/detail`
  仍未全部进入“最新 `.pen` -> schema -> code -> 截图校验”的闭环
- `address-picker` 旧契约已退出 schema 清单，地址链路后续统一按 `address-list + address-edit` 理解
- `pet-profile.page-schema.json` 已补齐，但页面路由与实现仍待承接
- 大量二级页仍停留在旧实现或未正式设计链路状态
- 商品管理后台尚未提供结构化商品详情编辑能力
- 数据库迁移脚本已写，但下一次继续时仍需确认是否已实际执行到当前 MySQL

结论：

- 本计划仍然有效
- 但当前阶段已经不是“从零设计”，而是“按既定规则继续补齐缺口”

### 2.5 当前执行状态补记（2026-03-16）

本轮对话已经继续完成一批关键画布细化，后续不得再按旧方案回退理解。

已完成的新设计收敛如下：

- 宠物列表页 `我的宠物列表 My Pets`：`cKCx8`
  - 已从普通卡片列表改为“画廊式大卡流”
  - 主卡完整展示，下一张卡从底部露出并带轻微重叠与高斯模糊感
  - 下一张卡不是缩略卡，而是与主卡同构的下一状态
  - 主卡已收敛为：
    - 封面只负责视觉识别
    - 中下部承载姓名、性别、品种年龄
    - 底部只保留一个主 CTA `查看档案`
  - 已删除的旧表达包括：
    - 重复信息胶囊
    - 冗余提示文案
    - 不清晰的小胶囊次按钮
  - 2026-03-17 已完成当前前端第一轮对齐：
    - `pages/pet/list` 已改为主卡 + 下一张露出预览卡结构
    - 已移除旧标题 `我的宠物档案`
    - 已移除 `pet-type-badge` 与 `成长档案` 旧 CTA
    - 已补 `apps/frontend/docs/design-schemas/pet-list.page-schema.json`
    - 已补回归测试：
      - `tests/frontend/pet-page-schema.test.js`
      - `tests/frontend/pet-list-redesign.test.js`
  - 当前仍保留的待验项：
    - 需要在微信开发者工具或真机确认主卡与下一张卡的层叠比例
    - 当前 `查看档案` 仍暂时复用现有 `/pages/pet/edit`，单宠物独立档案页仍待后续阶段补齐
- 登录页 `登录页 Brand Welcome Login`：`52ZbF`
  - 已去除顶部独立欢迎卡，收敛为单主卡结构
  - 已去除英文眉标题
  - `欢迎登录伴宠云诊` 已移入登录卡并居中
  - 已删除头像中间的装饰性符号
  - 当前画布仅保留最必要的登录主路径表达
- 地址编辑页 `地址编辑页 Address Edit`：`MRUGD`
  - 已按现有前端实现补入地图搜索框
  - 搜索框位置已确认在 acquire card 内、地图预览上方
  - 本轮只同步了搜索框本体，没有在 `.pen` 中展开联想结果列表

本轮同步确认的实现约束：

- 登录页头像昵称回填必须遵循当前微信能力：
  - 进入页时优先回填本地缓存或后端已有资料
  - 头像通过 `chooseAvatar` 获取
  - 昵称通过 `input type=\"nickname\"` 获取
  - 登录时统一提交保存
- 地址编辑页地图搜索能力已是正式交互组成部分，后续 schema 和代码实现必须与当前前端结构保持一致

### 2.5 交易链修正执行补记（2026-03-16）

本轮对话已从“页面补设计缺口”进入“按 `.pen` 修交易链细节与状态流”的执行阶段，以下内容视为已完成的阶段性结果。

本节属于执行记录与阶段性进展，长期有效的规则与硬约束仍以以下文档为准：

- `docs/project-rules.md`

本轮已明确的验收基准：

- 交易链相关页面以当前 `.pen` 为唯一验收标准，不以旧实现或旧页面结构为准
- 核对节点统一为：
  - 结算页 `0fx0p`
  - 支付方式弹窗 `Dff1y`
  - Face ID 验证层 `Ef00X`
  - 支付密码输入态 `1bX6Z`
  - 支付成功反馈 `MeD2O`
  - 地址编辑页 `MRUGD`

本轮已完成的前端修正：

- 地址编辑页已补齐搜索框，形成：
  - 搜索地点
  - 地图预览
  - 粘贴完整地址识别
  - 表单修正与最终保存
  的完整两段式输入链路
- 地址编辑页已实现：
  - 选择搜索建议后同步地图、地区和详细地址
  - 粘贴完整地址后回填联系人、手机号、省市区和详细地址
  - 用户手动修改地址后，保存前重新 geocode，防止沿用旧坐标
  - 若缺少最新地图确认结果，则阻止保存并提示继续搜索或选点
- 结算页地址信息卡已向 `.pen` 收紧：
  - 联系人和手机号改为紧凑同排
  - 减少旧实现中过大的纵向割裂感
- 支付验证文案已改为系统单行提示，不再拆成错位的多段信息
- 支付成功态已改为 `.pen` 对应的系统反馈卡片，而不是旧的详情页式成功页
- 支付成功后跳转方式已改为 `redirectTo`，用于消除旧页面栈导致的“停在成功态/卡死”问题

本轮已完成的稳定性修正：

- 地址搜索建议已增加高德接口限流保护：
  - 少于 2 个字不发请求
  - 260ms 防抖后再请求
  - 搜索结果缺字段时使用兜底文案，避免渲染 `undefined`
- 已确认此前用户遇到的 `CUQPS_HAS_EXCEEDED_THE_LIMIT` 属于高频输入触发建议接口导致的 QPS 超限，不应继续按“地图能力未接通”理解

本轮新增与更新的验证：

- 已更新测试：
  - `apps/frontend/tests/address-edit-map-review-flow.test.js`
- 已通过的测试：
  - `node apps/frontend/tests/address-edit-map-review-flow.test.js`
  - `node apps/frontend/tests/address-payment-presenter.test.js`
  - `node apps/frontend/tests/payment-result-page.test.js`
- 已通过语法校验：
  - `node --check apps/frontend/miniprogram/pages/address/edit.js`
  - `node --check apps/frontend/miniprogram/pages/order/confirm.js`
  - `node --check apps/frontend/miniprogram/pages/order/pay-result/index.js`

本轮仍保留的待验事项：

- 仍需在微信开发者工具或真机上做一次截图级对照验收，重点核对：
  - 支付方式弹窗选项排版
  - Face ID 验证层图标与文案居中
  - 支付密码输入态数字与底部键盘居中
  - 取消订单原因弹层的高度、选中态与双按钮比例
  - 完成态订单详情页的绿色状态条、实付信息卡和底部动作排版
  - 订单评价页多商品卡片的间距、评分星级和上传框尺寸
  - 地址编辑页搜索建议下拉的真实遮挡关系与输入体验
- 若再次出现地图搜索异常，应优先区分：
  - 是否为旧构建缓存未刷新
  - 是否为高德 key 配额或环境配置问题
  - 是否为页面实现重新引入了无防抖请求

## 3. 问题分类

本次需要处理的页面分为三类。

### 3.1 A 类：缺少 `.pen` 设计基准的页面

这些页面虽然在 `app.json` 中已存在，但当前没有对应正式画板，也没有进入 `.pen -> schema -> code` 链路。

- `pages/login/login`
- `pages/course/course`
- `pages/course/detail/index`
- `pages/course/player/index`
- `pages/beauty/beauty`
- `pages/beauty/detail/index`
- `pages/beauty/booking/index`
- `pages/beauty/booking-list/index`
- `pages/beauty/booking-detail/index`
- `pages/consultation/doctor-list`
- `pages/consultation/doctor-detail`
- `pages/consultation/create`
- `pages/consultation/chat`
- `pages/consultation/list`
- `pages/consultation/urgent/index`
- `pages/pet/list`
- `pages/pet/edit`
- `pages/pet/timeline/index`
- `pages/health/list`
- `pages/health/edit`
- `pages/health/reminder/index`
- `pages/address/list`
- `pages/address/edit`
- `pages/community/detail`
- `pages/community/search`
- `pages/community/topic`
- `pages/community/publish`
- `pages/message/conversations`
- `pages/message/chat`
- `pages/user/profile`
- `pages/user/followers`
- `pages/user/followings`
- `pages/search/result`
- `pages/collection/list`
- `pages/vip/index`
- `pages/settings/settings`
- `pages/feedback/feedback`
- `pages/product/reviews`
- `pages/order/detail`
- `pages/order/pending-review`
- `pages/order/review`
- `pages/coupon/center`
- `pages/coupon/my`
- `pages/shop/subscription/index`
- `pages/recommend/detail`
- `pages/chat/chat`
- `pages/task/index`
- `pages/insurance/index`
- `pages/news/all-messages/index`

### 3.2 B 类：已有代码，但主题严重不符的页面

这些页面通常存在以下问题：

- 仍在使用旧蓝紫渐变 `#667eea / #764ba2`
- 仍保留“Apple 风格”或泛模板式视觉
- 仍是通用表单页、运营页或活动页语言
- 与当前 `.pen` 的轻描边、低饱和、大圆角、柔和克制体系不一致

重点重画名单：

- 登录页 `pages/login/login`
- 保险页 `pages/insurance/index`
- 任务页 `pages/task/index`
- 地址编辑页 `pages/address/edit`
- 推荐详情 `pages/recommend/detail`
- 课程首页 `pages/course/course`
- 课程详情 `pages/course/detail/index`
- 美容首页 `pages/beauty/beauty`
- 美容预约 `pages/beauty/booking/index`
- 订单详情 `pages/order/detail`
- 咨询创建页 `pages/consultation/create`
- 收藏页 `pages/collection/list`
- 商品评价页 `pages/product/reviews`
- 优惠券页 `pages/coupon/my`

### 3.3 C 类：已有较新实现，但还未进入正式设计体系的页面

这些页面已经开始使用全局 token 或较新的结构写法，但仍缺失 `.pen` 画板与 schema，应在后续补齐。

- 消息会话 `pages/message/conversations`
- 消息私聊 `pages/message/chat`
- 粉丝 `pages/user/followers`
- 关注 `pages/user/followings`
- 用户资料扩展页 `pages/user/profile`

## 4. 页面建设优先级

### 4.1 优先级 A：先补齐产品主路径

第一批优先新画：

- 登录页 `pages/login/login`
- 地址管理页 `pages/address/list`
- 地址编辑确认页 `pages/address/edit`
- 订单详情页 `pages/order/detail`

目标：

- 补齐进入产品与交易闭环缺口
- 建立后续交易页扩展的视觉模板
- 让地址链路与现有 `Address Picker` 统一

### 4.2 优先级 B：补齐内容与服务业务线

- 课程线
- 美容线

目标：

- 去除旧运营模板风格
- 统一到“轻内容 + 轻服务”的宠物产品语言

### 4.3 优先级 C：补齐问诊与宠物资产线

- 问诊线补充页
- 宠物档案线
- 健康管理线

目标：

- 建立宠物身份、健康资产、问诊服务之间的连续感
- 与首页“我的宠物”“健康提醒”“AI诊断”形成一致体验

### 4.4 优先级 D：补齐生态与二级页

- 社区补充页
- 消息补充页
- 用户关系页
- 收藏、设置、反馈、VIP、优惠券、订阅、任务、保险等二级页

目标：

- 统一所有长尾页面设计语言
- 为后续 schema 化和批量还原打基础

## 5. 页面与风格归属

### 5.1 品牌内容页体系

适用页面：

- 登录
- 课程线
- 社区补充页
- 问诊线
- 宠物档案线
- 健康管理线
- 用户关系页
- 消息会话与私聊

视觉原则：

- 使用品牌紫白体系
- 强调亲和、柔和与内容感
- 控制彩色面积，避免大面积强渐变
- 保持轻卡片、轻阴影、轻描边

### 5.2 交易确认页体系

适用页面：

- 地址管理
- 地址编辑确认
- 订单详情
- 优惠券
- 订阅购
- 保险
- 评价

视觉原则：

- 以暖灰白、米白为基底
- 强化可信感、稳定感、完成感
- 避免电商促销风和活动页语言

## 6. 新画布参考关系

### 6.1 登录页

参考：

- 首页 `kJvqm`
- 我的页 `S9U5p`

方向：

- 弱装饰欢迎区
- 品牌感登录入口
- 用户信息完善作为轻量后续动作，不做“模板式登录页”

### 6.1.1 登录页补记（2026-03-16）

登录页本轮已进一步做减法，当前最新结论如下：

- 顶部独立欢迎区不再保留
- 画布结构收敛为单主卡，而不是“欢迎卡 + 登录卡”双层结构
- 登录标题直接放入主卡内部并居中
- 英文眉标题已删除
- 头像区只保留干净的头像容器与角标，不再保留头像中央装饰性符号
- 欢迎说明文案已删除，不再使用解释性副标题堆叠情绪

后续如果继续优化登录页，优先做：

- 头像区细节精修
- 协议区弱化
- 登录卡内微调

不要再做：

- 恢复顶部欢迎卡
- 恢复英文品牌眉标题
- 在头像主体中央加入无意义图形或胶囊

### 6.2 地址管理页

参考：

- 地址管理页 `13Bdg`
- 结算页 `0fx0p`

方向：

- 地址列表为结果容器
- 强化当前默认地址与可切换地址
- 与交易场景保持统一暖灰白体系

### 6.3 地址编辑确认页

参考：

- 地址编辑页 `MRUGD`

方向：

- 以地图/识别结果回填为前提
- 用户主要做校正与确认，不是从零填写表单

### 6.3.1 地址链路重组补记（2026-03-15）

当前地址链路已经在 `.pen` 中完成新一轮重构，后续实现时不要再按本节原始字面理解为“三页并存”。

当前最新结论：

- 地址链路按“两页版”实现
- `我的地址 Address List` 负责：
  - 地址选择
  - 地址管理
  - 默认地址主卡
  - 其他地址列表
  - 从订单进入时直接选中并返回
- `编辑地址 Address Edit` 负责：
  - 联系人
  - 手机号
  - 地区
  - 详细地址
  - 默认地址设置
  - 地图定位辅助
  - 粘贴完整地址自动识别
- 原来的“选择收货地址 / 地图定位地址”独立主页面已不再保留为主流程页面
- 地图定位与自动识别属于 `编辑地址` 的能力，而不是独立流程页
- 自动识别与地图回填后，最终保存的数据以编辑页最终字段值为准
- 地图定位无法可靠识别门牌号、房间号，例如 `1702`，此类信息必须允许用户手动补全
- `编辑地址` 的详细地址区应提示用户：
  - 可手动补充门牌号等信息
  - 也可通过粘贴完整地址辅助识别

当前 `.pen` 关键画板：

- 地址管理页 Address List：`13Bdg`
- 地址编辑页 Address Edit：`MRUGD`

### 6.3.3 地址编辑页搜索框补记（2026-03-16）

地址编辑页本轮已按当前前端实现同步搜索能力到画布，当前结论如下：

- 搜索框属于 `编辑地址` 页内正式结构，不是实现时再决定的临时控件
- 放置顺序固定为：
  1. `完善收货资料 / 地图选点`
  2. 地图搜索框
  3. 地图预览
  4. 粘贴完整地址自动识别
- 当前 `.pen` 中已补入搜索框本体，文案为：
  - `搜索地点，地图将自动切换`
- 下拉联想列表是否在 `.pen` 中展开，应按当次设计任务决定：
  - 如仅需同步页面静态结构，搜索框本体即可
  - 如需补齐完整交互态，再单独补联想结果层

实现侧应继续保持与当前前端一致：

- 输入搜索关键词后触发实时检索
- 选中地点后回填地图与地址信息
- 不得把搜索框移到地图下方或表单区中段

### 6.3.4 宠物列表页补记（2026-03-16）

虽然宠物列表页不属于当前交易主链路，但本轮已经完成高优先级视觉收敛，后续不得按旧“普通列表卡”理解。

当前 `.pen` 关键画板：

- 我的宠物列表 My Pets：`cKCx8`

当前最新结论：

- 页面定位已从“普通管理列表”提升为“宠物档案画廊页”
- 主卡必须完整展示
- 下一张卡必须是同构大卡，只露出顶部一部分，形成向上接管的滚动逻辑
- 允许轻微重叠与高斯模糊，但必须保证当前主卡完整可读
- 封面区不再重复显示状态、品种等信息
- 主卡底部只保留一个主动作 `查看档案`
- 不得再恢复多胶囊、多文字说明、多条操作入口的旧方向

### 6.3.2 GPT-5 Codex 执行提示词

以下提示词可直接复制给 GPT-5 Codex，用于按当前最新 `.pen` 修改前端与必要的后端适配：

```text
Role
你是资深全栈工程师兼设计落地负责人。你的任务是基于当前仓库里已经确认的 .pen 画布，修改微信小程序前端和必要的后端/接口适配，使实现严格符合最新设计和交互链路。不要按旧代码理解产品，旧代码优先级低于已确认设计。

Task Objective
完成以下目标：
1. 将地址链路重构为“两页版”：
   - 我的地址：选择 + 管理
   - 编辑地址：新增 / 编辑 + 地图定位 + 粘贴识别 + 手动补全
2. 不再保留“选择收货地址/地图定位地址”作为独立主流程页面。
3. 将支付验证链路补完整，在 Face ID 验证后增加“支付密码输入态”。
4. 前端、必要的接口适配、状态流转、返回路径、页面文案和视觉结构都要与当前 .pen 一致。
5. 最终输出真实修改，不要只给建议。

Context/Input
项目根目录：/Users/luohao/Desktop/pet-cloud-consultation-mini-program

必须先阅读这些文件，按顺序执行：
1. /Users/luohao/Desktop/pet-cloud-consultation-mini-program/README.md
2. /Users/luohao/Desktop/pet-cloud-consultation-mini-program/docs/project-rules.md
3. /Users/luohao/Desktop/pet-cloud-consultation-mini-program/docs/frontend-design-principles.md
4. /Users/luohao/Desktop/pet-cloud-consultation-mini-program/apps/frontend/docs/design-schemas/README.md

设计源文件：
- /Users/luohao/Desktop/pet-cloud-consultation-mini-program/design/frontendMobileScreens.pen

当前已确认的关键画布节点：
- 地址管理页 Address List：13Bdg
- 地址编辑页 Address Edit：MRUGD
- 结算页 Summary Confirm Style：0fx0p
- 支付验证层 Payment Verification：Ef00X
- 支付密码输入 Payment Password Entry：1bX6Z
- 支付成功 Success Feedback：MeD2O

关键产品结论：
1. 地址链路现在是两页版，不是三页版。
2. 结算页点地址，进入“我的地址”。
3. “我的地址”负责：
   - 默认地址主卡
   - 其他地址列表
   - 从订单进入时可直接选中并返回订单
   - 点编辑进入“编辑地址”
   - 点新增进入“编辑地址”
4. “编辑地址”负责：
   - 联系人
   - 手机号
   - 地区
   - 详细地址
   - 默认地址开关
   - 地图定位辅助
   - 粘贴完整地址自动识别
5. 地图定位和粘贴识别是“编辑地址”的能力，不再是独立主页面。
6. 自动识别/地图回填只是辅助输入，最终保存的数据以编辑页上最终可见字段值为准。
7. 地图定位无法可靠识别门牌号/房间号，例如“1702”，这类细节必须让用户手动补充。
8. 详细地址区域要保留轻提示，提示用户可以手动补充，也可以通过粘贴完整地址识别。
9. 支付验证链路必须是：
   - 支付方式弹窗
   - Face ID 验证态
   - 支付密码输入态
   - 支付成功
10. 支付密码输入态底部键盘最后一行必须是：
   - 左：Face ID
   - 中：0
   - 右：删除

需要重点修改的前端页面
- /Users/luohao/Desktop/pet-cloud-consultation-mini-program/apps/frontend/miniprogram/pages/address/list
- /Users/luohao/Desktop/pet-cloud-consultation-mini-program/apps/frontend/miniprogram/pages/address/edit
- /Users/luohao/Desktop/pet-cloud-consultation-mini-program/apps/frontend/miniprogram/pages/order/confirm
- 支付验证/支付成功相关页面，如果项目中已有对应实现，也一起对齐

需要重点核对的后端/接口能力
- 地址列表、地址详情、地址新增、地址更新
- 地址解析能力：粘贴完整地址后拆出姓名、手机号、省市区、详细地址
- 地图定位/地理编码能力：根据地图位置回填省市区和街道信息
- 地址保存时以页面最终字段值为准，不以自动识别原始结果为准

Constraints
1. .pen 是唯一视觉与交互事实源，必须 1:1 落地。
2. 不允许因为实现方便退回普通表单页或保留旧的地图独立页主流程。
3. 不允许让旧代码结构反向定义新设计。
4. 地址管理页和地址编辑页必须保持同一套暖灰白、米白、低饱和中性色的交易页语言。
5. 编辑地址页要有简约高级感，不能只是“整齐排版的普通表单”。
6. 允许保留现有后端接口命名和大体结构，但若能力缺失，补最小必要改动。
7. 如果已有独立“地图定位地址/选择收货地址”页面代码，允许降级为内部状态、复用组件，或从主流程移除；不要继续作为主入口保留。
8. 先检查是否已有可复用逻辑，再修改，不要无意义重写。
9. 所有实现必须遵循：.pen -> schema -> code -> 截图校验。
10. 不要输出文档计划，不要只分析，直接实施修改并验证。

Required Behavior Details
地址链路：
- 结算页地址区域点击后，进入“我的地址”页。
- 如果 from=order，从“我的地址”选择地址后可返回结算页。
- “我的地址”页默认地址主卡和其他地址列表要与设计一致。
- “编辑地址”页支持三种输入来源：
  - 手动填写
  - 地图定位
  - 粘贴完整地址自动识别
- 粘贴识别后，需要把识别出的姓名、手机号、省市区、详细地址回填到表单。
- 地图定位后，需要更新地区和地址字段，但不要假装能可靠识别房间号。
- 例如“1702”这类门牌号必须允许用户手动补。
- 保存成功后，统一回到“我的地址”页。
- “编辑地址”页中“详细地址”附近要明确提示：门牌号等请手动补充，可通过粘贴完整地址辅助识别。

支付链路：
- “改用支付密码”必须进入独立的支付密码输入态。
- 支付密码输入态要有 6 位密码位和数字键盘。
- 底行是 Face ID / 0 / 删除。
- 支付成功页保持在支付密码输入态之后。

Implementation Steps
1. 先读取上述规则文件与 .pen。
2. 核对当前前端页面、现有跳转关系、现有地址 API 和支付相关逻辑。
3. 修改 address/list，使其符合最新“我的地址”画布和交互。
4. 修改 address/edit，使其吸收地图定位和粘贴识别能力，并符合最新“编辑地址”画布。
5. 处理 order/confirm 到 address/list 的交互回流。
6. 如后端缺少地址解析或地理编码能力，做最小必要补齐。
7. 修改支付验证相关页面，补全支付密码输入态。
8. 补或更新对应 schema，至少覆盖 address-list、address-edit、payment-password-entry，如项目当前流程要求。
9. 用测试和截图验证，不要跳过验证。

Output Format
按这个格式回复：
1. 你发现的当前实现与设计差异
2. 你准备修改的前端点
3. 你准备修改的后端点
4. 实际完成的修改
5. 验证结果
6. 仍存在的风险或待确认项

Acceptance Criteria
1. 地址链路只剩“我的地址 + 编辑地址”两页主流程。
2. “选择收货地址/地图定位地址”不再作为独立主流程页存在。
3. 编辑地址页能承载地图定位与粘贴识别。
4. 自动回填存在，但最终保存以页面最终字段为准。
5. 房间号等细粒度信息不会被错误地假设为地图自动识别结果。
6. 详细地址区域有轻提示，提示手动补充和粘贴识别。
7. 支付密码输入态已补齐，并有完整键盘与密码位。
8. 前端视觉结构与 .pen 对齐。
9. 相关测试通过，且给出验证证据。
10. 不要在未验证前声称“已完成”。
```

### 6.4 订单详情页

参考：

- 结算页 `0fx0p`
- 支付成功 `MeD2O`
- 商品详情 `2QVFo`

方向：

- 订单状态清晰
- 商品、金额、地址、时间、售后动作有明确层级
- 操作区克制，不做强运营按钮墙

### 6.4.3 取消订单原因弹层补记（2026-03-17）

当前 `.pen` 关键画板：

- 取消订单原因弹层 Cancel Order Reason Modal：`4XhHR`

当前已完成的实现收敛：

- `pages/order/detail` 已将取消订单链路从原生 `wx.showModal` 改为页面内底部弹层
- 已新增 `apps/frontend/docs/design-schemas/cancel-order-modal.page-schema.json`
- 已接入三个原因项：
  - `不想买了`
  - `收货信息需要调整`
  - `想重新选择商品`
- 已接入可选补充说明输入区与双按钮：
  - `暂不取消`
  - `确认取消`
- 当前确认取消后仍复用既有 `PUT /api/order/cancel`
- 本轮未新增后端取消原因字段，先保持最小必要改动

本轮新增验证：

- `node tests/frontend/cancel-order-modal.test.js`
- `node apps/frontend/tests/order-cancel-modal-state.test.js`
- `node --check apps/frontend/miniprogram/pages/order/detail.js`

当前仍保留的待验项：

- 需在微信开发者工具或真机确认弹层高度、遮罩强度和底部 joint action 的视觉比例

### 6.4.4 订单详情完成态补记（2026-03-17）

当前 `.pen` 关键画板：

- 订单详情页 Completed Order Detail：`7rGoU`

当前已完成的实现收敛：

- `pages/order/detail` 的 presenter 已增加完成态分支，不再沿用待支付态文案
- 已新增 `apps/frontend/docs/design-schemas/completed-order-detail.page-schema.json`
- 已完成的关键对齐包括：
  - 状态卡切换为 `已完成`
  - 状态提示切换为“订单已完成，如有需要可继续评价或查看售后服务。”
  - 状态条切换为绿色完成态
  - 金额卡标题切换为 `实付信息`
  - 信息卡内容切换为：
    - `订单编号`
    - `创建时间`
    - `支付时间`
    - `完成状态`
  - 底部动作切换为：
    - `申请售后`
    - `去评价`
- 当前“申请售后”先使用前端提示占位，后端售后入口本轮未补齐

本轮新增验证：

- `node apps/frontend/tests/login-order-detail.test.js`
- `node tests/frontend/completed-order-detail.test.js`
- `node --check apps/frontend/miniprogram/pages/order/detail.js`

当前仍保留的待验项：

- 需在微信开发者工具或真机确认完成态卡片高度、绿色状态条和底部 joint action 的视觉比例

### 6.4.5 订单评价页补记（2026-03-17）

当前 `.pen` 关键画板：

- 订单评价页 Order Review：`BBaGr`

当前已完成的实现收敛：

- `pages/order/review` 已从旧单商品评价表单重构为整单多商品评价页
- 已新增 `apps/frontend/docs/design-schemas/order-review.page-schema.json`
- 每个订单商品现在都有独立的：
  - 商品头部
  - 星级评分
  - 文本评价区
  - 图片上传区
- `pages/order/detail` 的 `去评价` 已改为把整单未评价商品通过 `itemsData` 直接带入评价页
- `pages/order/pending-review` 也已同步改为向评价页传 `itemsData`
- 提交时仍复用既有 `POST /api/product/review`，前端逐商品循环提交，不新增后端批量接口

本轮新增验证：

- `node tests/frontend/order-review-page.test.js`
- `node apps/frontend/tests/order-review-state.test.js`
- `node --check apps/frontend/miniprogram/pages/order/review.js`
- `node --check apps/frontend/miniprogram/pages/order/detail.js`
- `node --check apps/frontend/miniprogram/pages/order/pending-review.js`

当前仍保留的待验项：

- 需在微信开发者工具或真机确认多商品评价卡片间距、上传框尺寸和底部提交栏比例

### 6.4.1 支付链路补记（2026-03-15）

当前支付链路在 `.pen` 中已补成完整状态链，而不是只停留在 Face ID 验证入口。

当前最新结论：

- 支付链路应按以下顺序理解：
  - 支付方式弹窗 `Dff1y`
  - 支付验证层 Face ID 验证态 `Ef00X`
  - 支付密码输入态 `1bX6Z`
  - 支付成功 `MeD2O`
- `Payment Verification` 不仅要有 `改用支付密码` 入口，还必须有其后的真实输入状态
- `支付密码输入态` 必须保留：
  - 6 位密码位
  - 数字键盘
  - 删除键
  - 底行三列：`Face ID / 0 / 删除`
- 不能让底行左侧留空，否则视觉重心失衡
- `支付密码输入态` 不是新业务页，而是支付验证链路中的一个系统级状态

当前 `.pen` 关键画板：

- 支付方式弹窗 Payment Method Modal：`Dff1y`
- 支付验证层 Payment Verification：`Ef00X`
- 支付密码输入 Payment Password Entry：`1bX6Z`
- 支付成功 Success Feedback：`MeD2O`

### 6.4.2 GPT-5 Codex 支付链路执行提示词

以下提示词可直接复制给 GPT-5 Codex，用于按当前最新 `.pen` 补齐支付验证与支付密码输入态：

```text
Role
你是资深前端/后端联动工程师。你的任务是基于当前仓库中已确认的 .pen 画布，补齐支付验证链路，使微信小程序实现严格符合最新交互状态，而不是只停留在 Face ID 验证入口。

Task Objective
完成以下目标：
1. 将支付链路补齐为完整状态链：
   - 支付方式弹窗
   - Face ID 验证态
   - 支付密码输入态
   - 支付成功
2. 当前前端若只有“改用支付密码”入口，但没有真实密码输入态，必须补上。
3. 保证支付密码输入态与现有验证层视觉语言一致，保持系统级浮层感。
4. 必要时补齐前端状态流转与后端/接口参数适配，但优先做最小必要改动。

Context/Input
项目根目录：/Users/luohao/Desktop/pet-cloud-consultation-mini-program

必须先阅读：
1. /Users/luohao/Desktop/pet-cloud-consultation-mini-program/README.md
2. /Users/luohao/Desktop/pet-cloud-consultation-mini-program/docs/project-rules.md
3. /Users/luohao/Desktop/pet-cloud-consultation-mini-program/docs/frontend-design-principles.md
4. /Users/luohao/Desktop/pet-cloud-consultation-mini-program/apps/frontend/docs/design-schemas/README.md

设计源文件：
- /Users/luohao/Desktop/pet-cloud-consultation-mini-program/design/frontendMobileScreens.pen

当前已确认的关键画布节点：
- 支付方式弹窗 Payment Method Modal：Dff1y
- 支付验证层 Payment Verification：Ef00X
- 支付密码输入 Payment Password Entry：1bX6Z
- 支付成功 Success Feedback：MeD2O

关键产品结论：
1. Face ID 验证态不是终点，只是支付验证的第一种方式。
2. 点击“改用支付密码”后，必须进入真实的支付密码输入态。
3. 支付密码输入态必须包含：
   - 6 位密码位
   - 数字键盘 0-9
   - 删除键
   - Face ID 快捷回切
4. 键盘底行必须是：
   - 左：Face ID
   - 中：0
   - 右：删除
5. 不允许底行左侧留空格位。
6. 支付密码输入态之后才进入支付成功。

需要重点修改的前端页面
- 支付验证相关页面
- 支付成功相关页面
- 如项目中支付方式弹窗也需联动，顺带对齐

需要重点核对的后端/接口能力
- 支付验证方式切换参数
- 支付密码提交接口或模拟状态流
- 成功/失败结果回流

Constraints
1. .pen 是唯一视觉与交互事实源，必须 1:1 落地。
2. 不允许把支付密码输入态做成普通页面跳转感，要保留系统级浮层验证感。
3. 不允许只有“改用支付密码”入口而没有真实输入态。
4. 不允许把键盘底行做成留空占位。
5. 能复用现有支付验证逻辑时，优先复用。
6. 不要只做视觉占位，要让前端状态流转真实可运行。

Required Behavior Details
- 用户在 Face ID 验证态点击“改用支付密码”后，进入支付密码输入态。
- 支付密码输入态展示 6 位密码位与数字键盘。
- 输入 6 位后，进入成功态或按当前业务逻辑进入下一验证/提交步骤。
- 点击 Face ID 可回到 Face ID 验证态。
- 删除键可回退一位密码输入状态。
- 支付成功页在支付密码输入态之后出现，不应在密码态之前。

Implementation Steps
1. 读取规则文件与 .pen。
2. 核对当前支付验证实现是否只有 Face ID 态。
3. 补充支付密码输入态前端结构与状态切换。
4. 接入最小必要的密码输入交互逻辑。
5. 让成功页和验证页顺序与 .pen 一致。
6. 必要时补 schema，如项目当前实现链路要求。
7. 执行测试与截图验证。

Output Format
按这个格式回复：
1. 当前支付链路缺口
2. 前端修改点
3. 后端/接口适配点
4. 实际完成的修改
5. 验证结果
6. 风险或待确认项

Acceptance Criteria
1. 支付链路包含 Face ID 态和支付密码输入态两种验证方式。
2. “改用支付密码”后出现真实输入态。
3. 输入态有 6 位密码位和完整数字键盘。
4. 底行是 Face ID / 0 / 删除。
5. 支付成功位于支付密码输入态之后。
6. 前端状态流转真实可运行，不是纯视觉摆设。
7. 实现结果与 .pen 对齐，并有验证证据。
```

## 7. 第一阶段实施范围

第一阶段优先处理四个页面：

1. 登录页
2. 地址管理页
3. 地址编辑确认页
4. 订单详情页

每个页面都应按以下顺序执行：

1. 在 `.pen` 中新增或复制对应顶层画板
2. 完成画布结构与视觉定稿
3. 输出对应 page schema
4. 按 schema 落地 WXML / WXSS / JS
5. 做截图比对校验

补充说明：

- 这四个页面到现在仍然是最优先缺口
- 下一次重启对话后，应先读取最新 `.pen`，再继续这四个页面
- 不要因为商城、社区、商品详情已经推进了一部分，就跳过这一批基础页面

## 8. 执行约束

- 不允许跳过 `.pen` 直接改代码
- 不允许在未定稿前补 schema
- 不允许仅凭现有旧页面结构做视觉翻新
- 每个新增页面必须明确其所属体系：品牌内容页或交易确认页
- 如页面功能属于现有链路延伸，优先复用现有画布语言，不重新发明视觉系统
- 新增 `.pen` 画布出现后，必须先刷新画布盘点，再决定 schema 和代码工作
- 若页面使用微信原生导航栏，不得把系统状态栏文本和系统返回区画死进代码
- 商品详情与交易链路相关页面不得把规格写死在前端模板中
- 页面完成后必须保留“本页是否已按最新 `.pen` 截图复核”的状态记录

## 9. 当前接续顺序

下一次继续时，按下面顺序推进：

1. 先读取当前最新 `.pen`，确认新增顶层画板
2. 对照已有 schema，找出需要新增或刷新的页面
3. 优先完成第一阶段四个页面：
   - 登录页
   - 地址管理页
   - 地址编辑确认页
   - 订单详情页
4. 如果交易链路相关画布有变化，再刷新：
   - 商品详情
   - 购物车
   - 结算
   - 支付方式弹窗
   - 支付验证
   - 支付成功
   - 地址管理
   - 地址编辑
5. 再进入课程线、美容线、问诊线、宠物与健康资产线等剩余页面

补充说明：

- 更完整的“当前已完成 / 未完成 / 下次怎么接”说明，统一以 `docs/plans/2026-03-15-execution-roadmap.md` 为准
