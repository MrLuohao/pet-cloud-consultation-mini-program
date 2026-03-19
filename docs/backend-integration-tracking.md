# 后端对接跟踪文档

更新时间：2026-03-19

## 当前同步补记（2026-03-19）

- 当前消费端 `.pen` 顶层正式画布数已是 23 个，且地址链与宠物链的契约状态已进一步明确：
  - 地址链路当前正式契约为：
    - `address-list.page-schema.json`
    - `address-edit.page-schema.json`
  - 旧的 `address-picker.page-schema.json` 已移除，不应再把 `D5qTR` 当作现行实现依据
  - 单宠物健康档案页当前已新增正式契约：
    - `apps/frontend/docs/design-schemas/pet-profile.page-schema.json`
    - `.pen` 节点：`H6Xma`
- 当前与后端协同最相关的结论：
  - 地址保存继续以编辑页最终字段值为准
  - 地图选点、搜索建议、粘贴识别都属于 `address-edit` 页内能力，不再是独立地址选择主流程
  - `pet-profile` 契约已补齐，但前端路由与页面实现尚未承接，后端此时不需要为“旧地址选择页”继续追加页面语义
- 当前文档与实现阶段判断：
  - 交易链和宠物链已经不是“只靠设计草案讨论”的阶段
  - 现在更准确的状态是“核心设计源和契约已刷新，但仍缺路由实现与截图验收闭环”

## 当前执行状态补记

- 首页、消息中心、商城、我的页面、社区首页、AI 诊断页已经进入 `.pen` 对照修正阶段
- 当前前端方向已从“功能先跑通”切换为“在不破坏现有接口链路的前提下贴近 `.pen` 形态”
- 已新增页面 schema 中间层试点：
  - `/Users/luohao/Desktop/pet-cloud-consultation-mini-program/apps/frontend/docs/design-schemas/page-schema.contract.json`
  - `/Users/luohao/Desktop/pet-cloud-consultation-mini-program/apps/frontend/docs/design-schemas/home.page-schema.json`
- 后续页面改造建议统一遵循：
  - Pencil MCP 抽取
  - 生成 page schema
  - 再落 WXML/WXSS/JS
- 旧上传与旧消息兼容接口已完成删除，但旧诊断兼容接口暂时仍需保留
- 保留原因：
  - 小程序诊断页当前仍通过 `AIAPI.getDiagnosisEntry` / `AIAPI.submitDiagnosis` 使用现有诊断入口链路
  - 测试脚本和部分文档仍引用 `/api/ai/diagnosis`

## 交易链当前状态补记（2026-03-16）

这轮交易链修正的长期规则与执行进展，分别以以下文档为准：

- 长期规则：`docs/project-rules.md`
- 执行进展：`docs/plans/2026-03-15-page-design-gap-remediation-plan.md`

从后端协同视角，当前需要持续遵守的约束如下：

- 交易链相关前端实现已明确按当前 `.pen` 节点联动：
  - 结算页 `0fx0p`
  - 支付方式弹窗 `Dff1y`
  - Face ID 验证层 `Ef00X`
  - 支付密码输入态 `1bX6Z`
  - 支付成功反馈 `MeD2O`
  - 地址管理页 `13Bdg`
  - 地址编辑页 `MRUGD`
- 地址保存必须以页面最终字段值为准，不能以地图自动识别结果、粘贴原文或历史坐标直接覆盖用户最后一次修正后的表单值。
- 当用户修改地区或详细地址后，前端会要求重新 geocode 或重新搜索/选点确认地址；后端应继续接受并保存前端最终提交的：
  - `province`
  - `city`
  - `district`
  - `detailAddress`
  - `latitude`
  - `longitude`
  - `mapAddress`
  - `rawText`
- 地址搜索建议当前依赖高德能力，前端已增加防抖与最小字数保护；若联调时再次出现建议搜索异常，应优先排查：
  - key 配额或 QPS 限流
  - 环境配置缺失
  - 服务端代理或封装层是否改变了请求频率
- 支付成功页当前已改为系统反馈态并在成功后自动回流；后端订单状态流转仍需保证：
  - 下单后可进入待付款/待发货
  - 发货后可进入待收货
  - 收货后可进入待评价
- 若继续做真实交易链联调，仍需同时拉起以下服务，避免把跨服务缺口误判成前端状态问题：
  - `pet-cloud-user-service`
  - `pet-cloud-shop-service`
  - `pet-cloud-map-service`
  - `pet-cloud-media-service`

## 宠物链当前状态补记（2026-03-19）

从前后端协同视角，宠物主链当前新增的事实如下：

- 宠物入口页 `cKCx8` 已经有正式 schema：`pet-list.page-schema.json`
- 单宠物健康档案页当前也已有正式 schema：`pet-profile.page-schema.json`
- `pet-profile` 当前约束的页面结构为：
  - 轻身份头部
  - 强健康摘要卡
  - 轻提醒概览
  - 健康记录时间线
  - 底部动作区
- 对后端现有能力的直接影响：
  - 需要继续保证 `GET /api/pets/{petId}` 可稳定提供身份信息
  - 需要继续保证 `GET /api/pets/{petId}/diagnosis-summary` / 同等聚合能力可承接摘要卡
  - 需要继续保证 `GET /api/pets/{petId}/medical-records` / 同等聚合能力可承接时间线
  - 若现阶段前端仍以现有 `PetAPI.getDetail / getTimeline`、`HealthAPI.getByPet / getReminders` 组合取数，后端无需为了旧页面兼容反向改坏聚合语义
- 当前未完成项：
  - `pages/pet/profile/index` 路由和页面实现仍未落地
  - 截图级验收仍未闭环

## 目的
这份文档用于承接 `.pen` 设计稿里已经确认的产品能力，提前沉淀前后端对接所需的核心字段、接口建议、边界说明和待确认事项。

使用原则：
- 每确认一个画布或关键模块后，增量更新本文件
- 先记录设计已经确定的业务能力，再细化接口
- 不追求一次性写全，优先避免后续联调遗漏

## 当前已确认画布
- 首页优化版 Home Style
- 商城页轻量优化版 Shop Style
- 社区页面优化版 Social Style
- 消息中心 Community Style
- 我的页面优化版 Profile Style
- AI健康诊断 Professional Style

## 统一产品原则
- 首页是“服务总览 + 宠物身份入口”
- 社区是“宠物身份社交”，优先体现宠物而不是主人
- AI健康诊断不是一次性工具，要逐步升级为“连续诊断 + 健康病历本”
- 页面风格统一使用浅背景、白卡片、紫色主强调，但功能页要体现不同任务气质

## 模块一：首页
### 已确认设计点
- 首页结构顺序：
  - 搜索
  - AI 诊断
  - 我的宠物
  - 常用服务
  - 健康提醒
  - 今日照护
  - 精选内容
- “我的宠物”是首页第二视觉焦点
- 宠物卡采用“宠物身份卡”表达，不做并列平铺
- 多宠物采用：
  - 单张主卡
  - 分页提示
  - 可滑动切换暗示
- 宠物卡展示：
  - 宠物名
  - 品种 · 年龄
  - 状态摘要

### 后端所需能力
- 获取首页宠物身份卡摘要
- 获取多宠物列表与当前默认宠物
- 获取健康提醒摘要
- 获取今日照护摘要
- 获取精选内容摘要列表

### 建议接口
- `GET /api/home/summary`
- `GET /api/pets/current-card`
- `GET /api/pets`
- `GET /api/health/reminders/summary`
- `GET /api/care/today-summary`
- `GET /api/content/featured`

### 首页宠物身份卡建议字段
```json
{
  "currentPetId": 101,
  "petCount": 3,
  "currentIndex": 1,
  "pets": [
    {
      "petId": 101,
      "name": "团子",
      "avatarUrl": "",
      "breed": "英短蓝猫",
      "ageText": "2岁3个月",
      "statusSummary": "今日状态稳定，已记录饮水、食欲与精神状态变化。"
    }
  ]
}
```

### 待确认
- 首页默认展示哪只宠物：最近活跃 / 主宠物 / 手动选择
- 多宠物切换顺序是否支持用户自定义

### 当前已落地约束
- 首页聚合中的 `featuredContents` 已不再直接读取文章表分页结果
- `GET /api/content/featured` 已提供公开只读的 published 内容列表
- 首页和公开精选接口当前都基于 `featured_content_publish` 读模型
- publish 内容当前关键字段包括：
  - `title`
  - `summary`
  - `tag`
  - `reasonLabel`
  - `coverUrl`
  - `targetPage`
  - `targetId`
  - `positionNo`
  - `startTime`
  - `endTime`
- 未来 admin 工作流当前仅做契约预留，不在小程序内直接使用：
  - `/api/admin/content/featured-drafts`
  - `/api/admin/content/featured-publish`

## 模块二：社区
### 已确认设计点
- 社区强调“宠物身份社交”
- 普通帖子优先展示宠物身份，而不是主人身份
- 社区帖子头部建议展示：
  - 宠物头像
  - 宠物名
  - 品种 · 年龄
  - 个性签名
- 专家/医生帖子保留专业身份，不强行套宠物人格逻辑

### 后端所需能力
- 社区帖子返回宠物身份信息
- 社区支持“宠物个性签名”字段
- 区分普通宠物帖与专家答疑帖
- 社区发布链路接入统一媒体资产审核，禁止未审核素材直接入帖

### 建议接口
- `GET /api/community/feed`
- `GET /api/community/posts`
- `GET /api/community/post/{postId}`
- `POST /api/community/post`

### 普通宠物帖建议字段
```json
{
  "postId": 9001,
  "postType": "pet_post",
  "author": {
    "userId": 1,
    "displayName": "宠物主人",
    "avatarUrl": "",
    "role": "user"
  },
  "pet": {
    "petId": 101,
    "name": "团子",
    "avatarUrl": "",
    "breed": "英短蓝猫",
    "ageText": "2岁3个月",
    "signature": "慢热小猫，喜欢晒太阳和安静陪伴。"
  },
  "content": "今天终于把饮食和作息慢慢调顺了...",
  "media": [],
  "likeCount": 128,
  "commentCount": 36
}
```

### 非宠物身份帖建议字段
```json
{
  "postId": 9002,
  "postType": "author_post",
  "author": {
    "role": "user",
    "displayName": "内容作者",
    "avatarUrl": ""
  },
  "content": "猫咪突然不爱喝水的时候..."
}
```

### 当前已落地约束
- `GET /api/community/feed` 已作为 feed 别名接入，复用 `/api/community/posts`
- feed/detail 返回兼容旧字段 `nickname/avatarUrl/petName`，同时新增 `author`、`pet`、`postType`
- `POST /api/community/post`、`PUT /api/community/post/{id}` 支持 `mediaAssetIds`
- 若传入 `mediaAssetIds`，服务端会通过 AI 内部接口校验 `availableForSubmit`
- 审核未通过时返回：`存在不可用的社区媒体: ...`

### 待确认
- 是否需要把“专家帖”从当前 `author_post` 继续细分为独立 `expert_post`
- 个性签名是否允许在发帖时临时覆盖宠物固定资料

## 模块三：AI 健康诊断
### 已确认设计点
- 页面定位：
  - 专业可信第一
  - 陪伴安心第二
  - 快速易用第三
- 页面结构已调整为：
  - 轻量标题说明
  - 健康档案摘要
  - 症状描述
  - 症状相关图片
  - 宠物基本信息
  - 诊断说明
  - CTA
- 症状标签单排展示
- 上传区支持最多 6 张图片
- 诊断页已加入“健康档案摘要”模块

### 后端所需能力
- 提交 AI 诊断
- 上传诊断图片
- 查询访客剩余次数
- 读取诊断历史摘要，用于本次协助诊断
- 保存每次诊断的关键记录，形成病历本
- 对大模型诊断结果做关键信息提取，只保留可沉淀的病历信息
- 对诊断图片做内容安全审核，命中血腥暴力等风险时禁止提交

### 建议接口
- `POST /api/ai/diagnosis`
- `POST /api/ai/diagnosis/images`
- `GET /api/ai/diagnosis/remaining-count`
- `GET /api/pets/{petId}/diagnosis-summary`
- `GET /api/pets/{petId}/medical-records`
- `POST /api/ai/diagnosis/{recordId}/extract-key-info`
- `POST /api/moderation/media-check`

### AI 诊断提交建议字段
```json
{
  "petId": 101,
  "petType": "cat",
  "petAgeMonths": 27,
  "symptomTags": ["食欲不振", "皮肤瘙痒"],
  "symptomDescription": "最近三天食欲下降，耳后和腹部有抓挠情况。",
  "imageUrls": ["https://.../1.jpg", "https://.../2.jpg"],
  "contextFromHistory": true
}
```

### AI 诊断结果建议字段
```json
{
  "recordId": 50001,
  "riskLevel": "medium",
  "summary": "存在轻中度皮肤敏感风险，建议先观察并补充清洁与饮食记录。",
  "possibleCauses": [
    "环境刺激",
    "轻度过敏反应"
  ],
  "careSuggestions": [
    "观察 48 小时内是否继续加重",
    "避免频繁抓挠部位刺激"
  ],
  "nextActions": [
    "若红肿扩大，建议及时问诊",
    "持续 3 天无改善建议就医"
  ],
  "shouldConsultDoctor": true
}
```

### AI 诊断关键信息提取建议字段
```json
{
  "recordId": 50001,
  "petId": 101,
  "extractedAt": "2026-03-15T10:35:00",
  "keyInfo": {
    "primarySymptoms": ["食欲下降", "皮肤瘙痒"],
    "duration": "3天",
    "severity": "轻中度",
    "suspectedIssues": ["轻度过敏反应"],
    "affectedParts": ["耳后", "腹部"],
    "followUpFocus": ["是否持续加重", "抓挠频率变化"]
  },
  "discardedNoise": [
    "安抚性描述",
    "重复表述",
    "无诊断价值的泛化建议"
  ]
}
```

### 健康档案摘要卡建议字段
```json
{
  "petId": 101,
  "petName": "团子",
  "recentDiagnosisCount": 2,
  "timeWindowDays": 7,
  "recentSymptoms": ["食欲下降", "皮肤瘙痒"],
  "suggestedFocus": [
    "持续时间",
    "是否加重",
    "是否出现新症状"
  ],
  "lastDiagnosisTime": "2026-03-15T10:30:00"
}
```

### 诊断病历本建议记录字段
- `recordId`
- `petId`
- `diagnosisTime`
- `symptomTags`
- `symptomDescription`
- `imageUrls`
- `summary`
- `riskLevel`
- `possibleCauses`
- `careSuggestions`
- `nextActions`
- `shouldConsultDoctor`
- `extractedKeyInfo`
- `medicalRecordVersion`

### 关键信息提取规则
- 目标不是保存整段大模型原始回答，而是沉淀可用于连续诊断的核心病历信息
- 仅建议提取：
  - 主要症状
  - 持续时间
  - 严重程度
  - 疑似问题/风险点
  - 受影响部位
  - 后续复诊重点
- 不建议入库：
  - 安慰性话术
  - 重复解释
  - 过长自然语言分析
  - 与病情无关的泛化护理建议
- 建议后端保存“两份结果”：
  - 面向前端展示的完整结构化诊断结果
  - 面向病历本沉淀的精简关键信息
- 建议对同一条病历保留提取版本号，避免后续提取规则调整后无法追踪

### 内容安全审核规则
- 适用范围：
  - AI 诊断图片上传
  - 社区图片/视频发布
  - 其他任何用户可上传图片或视频的功能
- 审核时机：
  - 媒体上传成功后、业务提交前必须先过审核
  - 审核不通过时，后端直接拒绝提交，不允许前端绕过
- 重点拦截内容：
  - 血腥暴力
  - 明显伤残或强刺激性画面
  - 色情低俗
  - 违法违规内容
  - 其他平台规则明确禁止的高风险内容
- 审核结论建议返回：
  - `pass`
  - `review`
  - `reject`
- 当前产品阶段建议：
  - 命中明显血腥暴力直接 `reject`
  - 边界不清晰内容进入 `review`
  - 只有 `pass` 才允许进入后续发布/诊断流程
- 图片和视频都应走统一的媒体审核抽象能力，不要每个业务模块单独实现一套规则

### 媒体审核建议返回字段
```json
{
  "mediaId": "img_10001",
  "mediaType": "image",
  "result": "reject",
  "riskTags": ["bloody", "violence"],
  "reason": "检测到明显血腥暴力内容",
  "reviewRequired": false
}
```

### 前后端边界
- 后端负责：
  - 保存诊断记录
  - 聚合历史记录
  - 提炼健康档案摘要
  - 返回结构化诊断结果
  - 从诊断结果中提取病历级关键信息并沉淀
  - 对所有上传图片/视频进行内容安全审核
- 前端负责：
  - 展示诊断页各输入模块
  - 展示历史摘要卡
  - 展示结构化诊断结果
  - 根据状态控制 CTA 和交互反馈
  - 根据审核失败原因给出清晰提示，不直接暴露审核策略细节

### 待确认
- 历史摘要是实时生成还是异步聚合
- 诊断结果是否需要人工审核标签
- 上传图片张数上限最终是否固定为 6
- 病历本是否单独做页面，还是先只做轻摘要
- 关键信息提取是同步返回，还是诊断完成后异步补全
- `review` 状态是否需要运营/客服复核后台

## 全局治理补充
### 所有媒体上传能力统一约束
- 凡是涉及图片上传或视频上传的地方，都必须先经过大模型或内容安全模型识别
- 审核未通过的内容，不允许发布、不允许进入诊断、不允许在社区展示
- 建议媒体资源表统一保留：
  - `mediaId`
  - `mediaType`
  - `ownerType`
  - `ownerId`
  - `moderationStatus`
  - `riskTags`
  - `moderationTime`

### 所有 AI 结果沉淀统一约束
- 大模型原始输出不应直接作为病历长期存储主数据
- 需要额外提炼出“少而准”的关键信息，用于：
  - 本次结果摘要
  - 历史连续诊断辅助
  - 后续健康档案/病历本页展示
- 后续如接后端开发，建议优先抽象两个通用能力：
  - `AI结果关键信息提取服务`
  - `统一媒体内容安全审核服务`

## 模块四：消息中心
### 已确认设计点
- 图标和快捷入口统一紫系
- 不再使用多彩分类 tone
- 强调品牌统一感和轻量提醒感
- 页面结构统一为：
  - 未读总览
  - 快捷入口
  - 最近会话
  - 系统通知
  - 事件预留槽位

### 后端所需能力
- 会话列表
- 未读计数
- 通知分类计数
- 页面级聚合 payload，直接返回 quick entries / unread summary / system notifications
- 预留诊断完成、审核驳回、精选内容推送等消息槽位

### 建议接口
- `GET /api/messages/conversations`
- `GET /api/messages/unread-summary`
- `GET /api/messages/notification-counts`
- `GET /api/messages/center`

### 当前已落地约束
- `GET /api/messages/center` 已提供页面级消息中心聚合数据
- `GET /api/conversation/center` 继续保留，返回兼容旧字段，同时新增：
  - `quickEntries`
  - `unreadSummary`
  - `systemNotifications`
  - `eventSlots`
- `GET /api/messages/conversations`
- `GET /api/messages/unread-summary`
- `GET /api/messages/notification-counts`
- 当前快捷入口固定为：
  - AI 助手
  - 我的咨询
  - 在线客服
- 当前事件槽位先以占位返回，键值包括：
  - `diagnosis_complete`
  - `moderation_reject`
  - `featured_content_push`

## 模块五：商城
### 已确认设计点
- 顶部分类导航统一视觉规则：
  - 默认灰色图标
  - 选中紫色图标
- 分类入口不使用五颜六色图标

### 后端所需能力
- 分类列表
- 分类 icon 或 icon key
- 商品列表
- 商品推荐 banner
- 商品详情结构化配置
- 购物车规格快照
- 订单确认/提交透传已选规格

### 建议接口
- `GET /api/shop/categories`
- `GET /api/shop/products`
- `GET /api/shop/promo-banner`

### 当前已落地约束
- `GET /api/product/categories` 当前返回分类视觉元数据，新增：
  - `iconKey`
  - `activeIconKey`
- 商城前端不再自行推断彩色分类语义，默认态使用中性 icon，激活态统一紫色 icon
- 分类入口默认保持稳定宽度与边框切换，不再依赖放大阴影制造激活感
- `GET /api/product/{id}/detail` 当前已支持：
  - `specGroups`
  - `highlights`
  - `storySections`
  - `usageNote`
- 购物车加购当前支持 `specLabel`
- 订单确认与提交当前支持 `specLabels`
- 直购链路必须使用前端所选规格生成订单项快照，不能再固定回退 `defaultSpec`
- `imageUrls` 当前仍定位为商品图库；详情正文图片只认 `storySections[].imageUrl`

## 模块六：我的页面
### 已确认设计点
- 页面结构聚焦：
  - 个人资料头部
  - 会员入口
  - 订单状态
  - 服务入口
  - 宠物卡片
  - 设置入口
- 视觉统一为浅底、白卡、紫色主强调
- 不再使用大量彩色 emoji 卡片表达功能入口

### 后端所需能力
- 用户资料
- 订单状态计数
- 积分与优惠券摘要
- 宠物列表

### 当前已落地约束
- 当前我的页面继续复用已有能力：
  - `AuthAPI.getUserInfo()`
  - `OrderAPI.getCount()`
  - `TaskAPI.getPoints()`
  - `CouponAPI.getMyCoupons(0)`
  - `PetAPI.getList()`
- 现阶段未新增 profile 聚合接口，优先复用已稳定能力完成页面清理

## 后续更新规则
每当确认一个画布或模块后，按下面步骤更新本文件：

1. 补充该模块的“已确认设计点”
2. 补充“后端所需能力”
3. 补充“建议接口”和关键字段
4. 记录“待确认”问题

## 当前下一步建议
- 若继续确认 AI 健康诊断页，优先补充：
  - 病历本独立页面结构
  - AI 诊断结果页结构
- 若继续确认社区页，优先补充：
  - 宠物身份模型字段定义
  - 社区普通帖 / 专家帖字段差异
