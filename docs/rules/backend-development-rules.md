# Backend Development Rules

适用范围：`apps/backend/pet-cloud-consultation/` 下全部 Java、数据库与接口协作。

## Java 与编码规范

- 后端编译、测试统一使用 Java 21。
- 注释必须写在代码上方，禁止行尾注释。
- 变更接口后，必须同步 API 文档与根级协作文档。

## 数据库规则

- `apps/backend/pet-cloud-consultation/db/pet_cloud_db.sql` 是项目唯一保留的建表 SQL 文件。
- 其他 SQL 迁移脚本不再作为长期保留入口。
- 建表应优先以“最终态建表”维护，不依赖多份历史 ALTER 脚本拼接。

## BaseEntity 规则

- `BaseEntity` 默认只承接通用审计字段：
  - `id`
  - `creator_id`
  - `creator_name`
  - `create_time`
  - `modifier_id`
  - `modifier_name`
  - `modify_time`
- `BaseEntity` 默认不再携带 `is_deleted`。
- 是否需要软删除，由具体实体按业务语义自行声明，不能把软删除当默认字段。

## is_deleted 判断

- 主内容表、可撤销业务表、需要误删恢复或审核下架的表，才考虑保留 `is_deleted`。
- 流水表、事实记录表、审计日志表、关系表、中间表通常不保留 `is_deleted`。
- 双边共享数据不得用单个 `is_deleted` 表达“某一方删除”。
- 私信会话、群会话成员状态、收藏夹展示状态这类按参与人可见性变化的数据，应优先使用“按用户维度状态字段”，例如隐藏时间、归档状态、置顶状态。
- 当前项目内，`community_post`、`community_comment` 保留软删除；`private_conversation` 不再使用全局软删除。

## 鉴权规则

- 用户行为、个人数据、交易相关、预约相关接口默认需要登录。
- 内容浏览、公共展示、受控访客体验接口可公开。
- 鉴权策略必须按产品语义判断，不能只图实现方便。

## 当前后端协作口径

- 后端接口必须让位于已确认的页面交互。
- 地址链路按地址管理页 + 地址编辑页理解，不再为独立地址选择页维持旧语义。
- `pet_cloud_db.sql` 必须覆盖后端代码当前实际使用的全部表。

## 当前接口与字段协作规则

- 首页能力继续围绕：
  - `GET /api/home/summary`
  - `GET /api/pets/current-card`
  - `GET /api/pets`
  - `GET /api/health/reminders/summary`
  - `GET /api/care/today-summary`
  - `GET /api/content/featured`
- 社区能力继续围绕：
  - `GET /api/community/feed`
  - `GET /api/community/posts`
  - `GET /api/community/post/{postId}`
  - `POST /api/community/post`
- 商品详情继续以 `specGroups`、`highlights`、`storySections`、`usageNote` 为结构化契约。
- 购物车、订单确认、订单提交、订单快照必须继续保留 `specLabel/specLabels`。
- 地址保存以编辑页最终字段值为准，不得用历史识别结果反向覆盖。
- `imageUrls` 只作为商品图库，不承担正文展示顺序语义。
