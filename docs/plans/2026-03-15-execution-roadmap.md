# 2026-03-15 执行路线总文档

更新时间：2026-03-15

适用范围：当前项目下一次重启对话后的直接接力执行。

这份文档的目标不是讨论“应该做什么”，而是明确：

- 现在已经做完了什么
- 现在还有什么没做
- 下一次接手必须先读什么
- 下一次接手必须按什么顺序继续
- 哪些规则不能再被重新讨论或被旧代码带偏

---

## 1. 重启对话后的必读顺序

下一次进入项目时，必须按下面顺序建立上下文：

1. `docs/project-rules.md`
2. 本文档
3. `docs/frontend-design-principles.md`
4. `apps/frontend/docs/design-schemas/README.md`
5. `docs/backend-integration-tracking.md`
6. `docs/product-image-generation-guidelines.md`
7. `docs/plans/2026-03-15-page-design-gap-remediation-plan.md`

结论：

- `.pen` 画布仍然是唯一视觉事实源
- 本文档是“当前执行状态”和“下次怎么继续”的事实源
- 代码永远排在这些文档之后

---

## 2. 当前已经锁定的硬规则

以下规则已经确认，后续不能被旧代码、个人偏好或“先跑通再说”的思路推翻：

### 2.1 页面实现规则

- 所有前端页面必须遵循 `.pen -> schema -> code -> 截图校验`
- 不允许凭印象复刻 `.pen`
- 不允许没做截图校验就宣称“1:1 还原”
- `.pen` 中的系统状态栏只作为参考，不得把 `9:41 / 5G / 电量` 直接画死进 WXML
- 微信小程序默认使用原生导航栏，不得随意改成 `navigationStyle: custom`

### 2.2 视觉规则

- 首页、社区、我的、AI 诊断属于品牌内容页：紫白系、轻粉感、柔和品牌氛围
- 商城、商品详情、购物车、结算、支付、地址属于交易确认页：暖灰白、米白、低饱和中性色
- 不做强促销电商风
- 不做花哨运营风
- 不做“一个页面一个视觉系统”

### 2.3 业务与数据规则

- 商品详情页的规格、亮点文案、故事段落、使用建议必须由后端结构化字段驱动
- 商品详情直购链路必须透传用户当前选择的规格
- `imageUrls` 只作为商品图库，不得冒充详情正文结构字段
- 社区普通宠物帖头部必须是一个完整身份块：
  - 头像
  - 宠物名
  - 品种 · 年龄
  - 个性签名
- 社区普通宠物帖不得把年龄拆成第一行右侧胶囊

---

## 3. 当前已经完成的内容

以下内容已经做完，下一次接手时不要重复推翻或重做。

### 3.1 文档与规则基线

以下核心文档已经建立并可直接作为后续工作的约束：

- `docs/project-rules.md`
- `docs/frontend-design-principles.md`
- `docs/backend-integration-tracking.md`
- `docs/product-image-generation-guidelines.md`
- `apps/frontend/docs/design-schemas/README.md`
- `apps/frontend/docs/design-schemas/page-schema.contract.json`

这些文档已经明确：

- `.pen` 为唯一视觉事实源
- 不能再渲染伪系统状态栏
- 商品详情必须走结构化后端字段
- 交易链路必须保留规格快照
- 商品图与商品介绍图必须按统一风格和尺寸规则生产

### 3.2 已建立 page schema 的页面

以下页面已经建立 schema 中间层：

- `home.page-schema.json`
- `shop.page-schema.json`
- `news.page-schema.json`
- `user.page-schema.json`
- `diagnosis.page-schema.json`
- `community.page-schema.json`
- `product-detail.page-schema.json`
- `cart.page-schema.json`
- `cart-empty.page-schema.json`
- `checkout.page-schema.json`
- `payment-method-modal.page-schema.json`
- `payment-verification.page-schema.json`
- `payment-password-entry.page-schema.json`
- `pay-success.page-schema.json`
- `login.page-schema.json`
- `address-list.page-schema.json`
- `address-edit.page-schema.json`
- `order-detail.page-schema.json`
- `completed-order-detail.page-schema.json`
- `cancel-order-modal.page-schema.json`
- `order-review.page-schema.json`
- `pet-list.page-schema.json`
- `pet-profile.page-schema.json`

注意：

- “已有 schema”不等于“全都已经对最新 `.pen` 新增画布做过最终截图复核”
- 但这些页面已经进入正式设计链路，不再是纯旧页面

### 3.3 已完成的重点前端落地与修正

当前已经明确处理过的重点页面和细节包括：

#### 宠物列表页

- `pages/pet/list` 已按 `cKCx8` 从旧普通列表改为“画廊式大卡流”
- 页面标题已收敛为 `宠物档案`
- 主卡与下一张预览卡已改为同构大卡关系，不再是连续列表卡
- 已移除旧的 emoji 类型徽标与 `成长档案` 优先 CTA
- 已新增 `pet-list.page-schema.json` 作为当前画布契约
- 仍需在开发者工具或真机上做一次截图级对照，确认卡片露出比例与层叠关系

#### 单宠物健康档案页

- 已于 2026-03-17 在 `frontendMobileScreens.pen` 中补出正式画板 `H6Xma`
- 画板名：`单宠物健康档案页 Pet Health Profile`
- 当前放置于 `cKCx8` 右侧，作为宠物主链路的下一屏
- 页面结构已收敛为：
  - 轻身份头部
  - 强健康摘要卡
  - 轻提醒概览
  - 健康记录时间线
  - 底部动作区
- 当前颜色方案已经定稿为：
  - 浅紫白体系
  - 保留品牌紫主 CTA
  - 不采用更暖的 luxury 试验配色
- 当前状态：
  - `.pen` 已定稿
  - `pet-profile.page-schema.json` 已补
  - 尚未进入 `route -> code` 阶段

#### 商城页

- 商品流恢复为双列错落布局，不再退化成单列
- 顶部分类改为横向可滑动
- 分类项保持稳定宽度，避免分类过多时字体被压得过小
- 分类上方保留小胶囊指示
- 悬浮购物车默认落在右下角，并可拖动

#### 社区页

- 普通宠物帖头像已并入身份信息块，不再悬空单独占一行
- 年龄不再放在第一行右侧，改回第二行 `品种 · 年龄`
- 个性签名放在头像右侧信息块内，不再出现左侧大空白

#### 商品详情页

- 顶部不再渲染伪系统状态栏
- 底部操作条已做成一个整体，不再出现中间缝隙
- 底部操作条贴合底部安全区
- 商品详情页已支持后端返回的结构化规格与正文内容

#### 订单取消弹层

- `pages/order/detail` 已按 `4XhHR` 将取消订单链路从原生确认框改为底部原因弹层
- 已补 `cancel-order-modal.page-schema.json`
- 已接入三个原因项、可选补充说明区和双按钮联动
- 当前后端仍复用既有 `PUT /api/order/cancel`，未新增取消原因字段
- 仍需微信开发者工具或真机确认弹层高度、遮罩与底部按钮比例

#### 订单详情完成态

- `pages/order/detail` 已补 `7rGoU` 完成态契约与状态分支
- 已新增 `completed-order-detail.page-schema.json`
- 完成态状态卡已切到绿色完成态，金额卡标题已改为 `实付信息`
- 完成态底部操作已切为 `申请售后 / 去评价`
- 当前“申请售后”仍为前端占位提示，后端售后链路未在本轮补齐
- 仍需微信开发者工具或真机确认完成态卡片层级、底部按钮比例和绿色状态条长度

#### 订单评价页

- `pages/order/review` 已按 `BBaGr` 从旧单商品评价表单改为整单多商品评价页
- 已新增 `order-review.page-schema.json`
- 每个商品现在都有独立的星级、评价文案和上传区
- `pages/order/detail` 与 `pages/order/pending-review` 已统一改为传递 `itemsData` 进入评价页
- 提交逻辑仍复用既有 `POST /api/product/review`，按订单项逐条提交
- 仍需微信开发者工具或真机确认多卡片间距、上传框尺寸和底部提交栏比例

### 3.4 已完成的商品详情后端能力

商品详情已经预留并接通以下结构化字段：

- `specGroups`
- `highlights`
- `storySections`
- `usageNote`

对应后端基础能力已经补上：

- `Product` 实体新增：
  - `spec_groups_json`
  - `detail_content_json`
- `ProductDetailVO` 已暴露：
  - `specGroups`
  - `highlights`
  - `storySections`
  - `usageNote`
- 商品详情服务已支持解析上述 JSON 配置

### 3.5 已完成的交易链路规格透传

当前规格链路已经贯通：

1. 商品详情页选择规格
2. 加入购物车时传 `specLabel`
3. 立即购买时传 `specLabels`
4. 订单确认页接收并继续透传 `specLabels`
5. 订单提交时保留规格快照
6. 购物车合并逻辑按 `user + product + specLabel` 区分，而不是只按商品合并

这意味着：

- 不同商品规格可以被后台独立配置
- 不同规格可以独立加入购物车
- 订单里可以保留真实规格快照
- 不会再出现“用户选了 A，提交订单时仍按默认规格结算”的错误

### 3.6 已完成的数据库与迁移脚本准备

已经补好的数据库脚本包括：

- `apps/backend/pet_cloud_db.sql`
  - `product` 表新增：
    - `spec_groups_json`
    - `detail_content_json`
  - `shopping_cart` 唯一键升级为：
    - `uk_user_product_spec (user_id, product_id, spec_label)`
- `apps/backend/db/migrate_product_detail_config.sql`
  - 幂等迁移脚本
- `apps/backend/db/migrate_all.sql`
  - 已同步追加上述迁移逻辑

注意：

- 这些脚本已经写好
- 但“是否已实际执行到当前 MySQL 实例”必须在下一次继续时再次确认

### 3.7 已完成的验证

当前已经跑过并通过的验证包括：

#### 前端

- `node apps/frontend/tests/product-detail-config.test.js`
- `node apps/frontend/tests/transaction-flow.test.js`
- `node --check apps/frontend/miniprogram/pages/product/detail.js`
- `node --check apps/frontend/miniprogram/pages/order/confirm.js`
- `node --check apps/frontend/miniprogram/utils/api.js`

#### 后端

在 Java 21 环境下已通过：

- `CartServiceImplTest`
- `OrderServiceImplCheckoutTest`
- `ProductDetailServiceImplConfigTest`

运行方式：

```bash
env JAVA_HOME=/opt/homebrew/opt/openjdk@21 PATH=/opt/homebrew/opt/openjdk@21/bin:$PATH \
  mvn -q -pl pet-cloud-shop-service \
  -Dtest=CartServiceImplTest,OrderServiceImplCheckoutTest,ProductDetailServiceImplConfigTest test
```

注意：

- 当前机器默认 `java` 不是 21
- 后端 Maven 验证时必须显式切到 `/opt/homebrew/opt/openjdk@21`

---

## 4. 当前还没有完成的内容

以下内容仍未完成，下一次继续时必须按优先级推进。

### 4.1 还没有完成的首要前置动作

- 画布里已经新增了新的页面画布
- 这些新增画布还没有重新做完整 Pencil MCP 扫描、schema 抽取和代码接力

所以，下一次继续之前，必须先做：

1. 打开当前最新 `.pen`
2. 读取新增顶层画板
3. 重新确认哪些旧 schema 需要刷新
4. 不允许在没读最新画布前继续写页面代码

### 4.2 还没有进入正式设计链路的页面

完整待补列表以 `docs/plans/2026-03-15-page-design-gap-remediation-plan.md` 为准。

当前最重要的是以下第一批页面仍未完成正式链路闭环：

- `pages/login/login`
- `pages/address/list`
- `pages/address/edit`
- `pages/order/detail`

这四个页面的目标不是“修旧页面”，而是：

- 先用最新 `.pen` 确认画板
- 再补 schema
- 再落 WXML / WXSS / JS
- 最后截图校验

### 4.3 页面 backlog 仍然很大

以下页面家族仍未完成：

#### 服务业务线

- 课程线
- 美容线
- 问诊线

#### 宠物与健康资产线

- 宠物档案
- 健康管理
- 健康提醒

补记：

- “单宠物健康档案页”设计稿已不再属于空缺项
- 当前真正未完成的是：
  - `pages/pet/profile/index` 路由与实现

#### 生态与二级页

- 收藏
- 优惠券
- VIP
- 设置
- 反馈
- 任务
- 保险
- 搜索结果
- 推荐详情
- 关系页（关注/粉丝/资料）
- 社区补充页
- 消息补充页

详细列表不要重新口述，以 `docs/plans/2026-03-15-page-design-gap-remediation-plan.md` 为准。

### 4.4 商品管理后台还没有做

虽然商品详情结构化字段已经接通，但“后台管理端的可视化维护能力”还没有实现。

当前缺的不是底层字段，而是管理层：

- 还没有真正的商品管理后台表单
- 还没有针对 `specGroups` 的编辑器
- 还没有针对 `highlights / storySections / usageNote` 的编辑器
- 还没有后台发布流程去稳定维护这套 JSON 结构

结论：

- 现在前端和后端都已经能吃这些字段
- 但后台运营侧仍缺真正可用的管理界面

### 4.5 数据库实际迁移执行状态还要确认

当前风险点：

- 迁移脚本已写
- 但重启对话后必须确认当前 MySQL 实例是否真的已经执行过迁移

必须确认的点：

- `product.spec_groups_json` 是否存在
- `product.detail_content_json` 是否存在
- `shopping_cart` 的唯一键是否已经改成 `uk_user_product_spec`

### 4.6 后端文档定义但尚未全部落地的能力

`docs/backend-integration-tracking.md` 中仍有一批能力只完成了“文档定义”或“部分落地”，未全部收口。

典型未收口项包括：

- 首页宠物身份卡聚合能力的完整实现
- 多宠物切换规则的最终确认
- AI 诊断历史摘要 / 病历本沉淀能力
- AI 诊断结果关键信息抽取能力
- 社区专家帖是否拆为独立 `expert_post`
- 地址管理链路与地图选点回填的最终页面闭环

### 4.7 全量 1:1 还原还没有收口

当前不能宣称“全项目已完成 1:1 还原”，原因如下：

- 新增 `.pen` 画布还没全部同步
- 仍有大量页面没有 schema
- 已有 schema 的页面也需要在最新画布基础上继续做截图复核

结论：

- 目前是“核心规则已锁定、主链路已打通、若干关键页面已进入正式链路”
- 不是“所有页面都已经完成”

---

## 5. 下一次继续时的严格执行顺序

下一次重启对话后，直接按下面顺序推进，不要重新发散。

### Step 1：重新读取最新 `.pen`

必须先做：

- 查看当前 Pencil 编辑器状态
- 列出最新顶层画板
- 确认新增画布
- 对照已有 schema，找出需要新增或刷新的一批页面

### Step 2：优先推进第一批四个页面

按这个顺序：

1. `login`
2. `address/list`
3. `address/edit`
4. `order/detail`

执行要求：

- 先画布
- 再 schema
- 再代码
- 再截图

补记（2026-03-17）：

- `login / address-list / address-edit / order-detail` 这一批不再是唯一的宠物线首要前置
- 宠物线当前应新增一条并行优先项：
  - `pet-profile.page-schema.json` 已基于 `H6Xma` 补齐
  - 下一步直接进入 `pages/pet/profile/index` 的路由与实现承接

### Step 3：刷新核心交易链路相关画布

如果最新 `.pen` 中以下页面有改动，优先刷新 schema 和代码：

- 商品详情
- 购物车
- 结算
- 支付方式弹窗
- 支付验证
- 支付成功
- 地址管理
- 地址编辑

### Step 4：执行数据库确认与必要迁移

重点确认：

- 商品详情结构化字段是否已进库
- 购物车唯一键是否已升级

如未执行，补执行对应迁移脚本。

### Step 5：再进入后台管理能力

等以上页面与数据基础确认后，再做：

- 商品管理后台编辑能力
- 结构化规格与详情内容的管理表单

### Step 5.5：宠物线接续动作

如果继续推进宠物模块，下一步严格按下面顺序：

1. 补 `pages/pet/profile/index` 路由
2. 让 `pages/pet/list` 的 `查看档案` 从暂时复用页切到新档案页
3. 再决定是否继续重画健康提醒页、健康编辑页、成长时间线页

---

## 6. 下一次继续时必须复用的 MCP/实现流程

页面工作统一按下列 MCP 序列执行：

1. `get_editor_state(include_schema: true)`
2. `get_guidelines("code")`
3. `get_guidelines("mobile-app")`
4. `get_variables(filePath)`
5. `batch_get(nodeIds: [frameId], readDepth: 4+, resolveVariables: true)`
6. `get_screenshot(frameId)`

然后：

1. 生成或刷新 page schema
2. 对照 schema 改 WXML / WXSS / JS
3. 截图复核

禁止跳步。

---

## 7. 如果下一次只允许做一件事

只做这一件：

`先读取最新 .pen 新增画布，并把 login / address/list / address/edit / order/detail 的 schema 接起来。`

原因：

- 这是当前“规则完整但页面断层仍在”的最大缺口
- 也是重启对话后最容易因为失忆而重新走偏的地方

---

## 8. 一句话接力说明

当前项目不是从零开始，而是已经完成了：

- 核心规则锁定
- schema 链路建立
- 商城 / 社区 / 商品详情等关键页面的重要修正
- 商品详情结构化后端能力
- 规格透传交易链路

下一步不是回头再讨论规则，而是：

`先同步最新 .pen 画布，再按既定规则把剩余页面一批批补完。`
