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

- 继承 `BaseEntity` 的实体默认需要：
  - `id`
  - `creator_id`
  - `creator_name`
  - `create_time`
  - `modifier_id`
  - `modifier_name`
  - `modify_time`
- `is_deleted` 是否保留必须按业务表类型判断，不能盲加。

## is_deleted 判断

- 主表、配置表、可撤销业务表通常保留 `is_deleted`。
- 流水表、事实记录表、审计日志表通常不保留 `is_deleted`。
- 如果实体继承了 `BaseEntity` 但实际表不含 `is_deleted`，需在实体层显式排除。

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
