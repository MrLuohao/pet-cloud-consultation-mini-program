# 宠物云咨询项目 - 编码规范

## 代码注释规范

**注释必须写在代码上方，禁止写在代码同一行的后面。**

### 正确示例
```java
// 默认昵称，用户后续可通过接口修改
wxUser.setNickname("宠物主人");

// 默认空头像
wxUser.setAvatarUrl("");

// 默认未知
wxUser.setGender(0);
```

### 错误示例
```java
wxUser.setNickname("宠物主人");  // 默认昵称，用户后续可通过接口修改
wxUser.setAvatarUrl("");  // 默认空头像
wxUser.setGender(0);  // 默认未知
```

---

## 数据库建表规范

所有继承 `BaseEntity` 的实体类，建表时**必须**包含以下基础字段：

```sql
-- BaseEntity 基础字段（必须）
`id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
`creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
`creator_name` VARCHAR(50) DEFAULT NULL COMMENT '创建人姓名',
`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
`modifier_name` VARCHAR(50) DEFAULT NULL COMMENT '修改人姓名',
`modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
`is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除: 0否 1是',  -- 视业务需要添加
```

### 字段说明

| 字段 | 必须 | 说明 |
|------|------|------|
| id | ✓ | 主键，自增 |
| creator_id | ✓ | 创建人ID，自动填充 |
| creator_name | ✓ | 创建人姓名，自动填充 |
| create_time | ✓ | 创建时间，自动填充 |
| modifier_id | ✓ | 修改人ID，自动填充 |
| modifier_name | ✓ | 修改人姓名，自动填充 |
| modify_time | ✓ | 修改时间，自动更新 |
| is_deleted | 视情况 | 软删除标识，需结合业务场景判断 |

### is_deleted 字段使用规范（重要）

**必须结合业界经典案例判断是否需要 is_deleted 字段，不要盲目添加！**

#### 需要 is_deleted 的表（主表/业务实体表）

| 表类型 | 示例 | 原因 |
|--------|------|------|
| 用户相关表 | `wx_user`, `user_address` | 用户可能删除后重建 |
| 宠物信息表 | `user_pet` | 宠物信息可删除 |
| 商品相关表 | `product`, `product_category` | 商品可下架/删除 |
| 订单相关表 | `order_info` | 订单可取消 |
| 配置表 | `task_definition`, `system_config` | 配置可禁用/删除 |

#### 不需要 is_deleted 的表（流水/日志/事实记录表）

| 表类型 | 示例 | 原因 | 处理方式 |
|--------|------|------|----------|
| 流水表 | `points_history`, `payment_record` | 历史记录不可篡改 | Entity 中使用 `@TableField(exist = false)` |
| 日志表 | `operation_log`, `login_log` | 审计需要，不能删除 | Entity 中使用 `@TableField(exist = false)` |
| 事实记录表 | `user_task`, `health_record` | 记录已完成的事实 | Entity 中使用 `@TableField(exist = false)` |
| 关联关系表 | `article_like`, `product_collection` | 通常用唯一约束 | 视业务场景 |

#### 判断标准

```
问自己三个问题：
1. 这条记录是否代表一个"历史事实"？
2. 删除这条记录是否会导致数据不完整或审计问题？
3. 这条记录是否需要长期保留用于追溯？

如果任一答案为"是"，则不需要 is_deleted 字段。
```

#### Entity 层处理方式

当表不需要 `is_deleted` 字段时，由于 `BaseEntity` 包含该字段，需要在 Entity 中显式排除：

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("points_history")
public class PointsHistory extends BaseEntity {

    // ... 业务字段 ...

    /**
     * 此表为流水记录表，不需要软删除功能
     * 显式设置为 false，MyBatis-Plus 将不会查询此字段
     */
    @TableField(exist = false)
    private Integer isDeleted;
}
```

### 建表示例

#### 示例1：主表（需要 is_deleted）

```sql
CREATE TABLE `task_definition` (
  -- 业务字段
  `task_code` VARCHAR(50) NOT NULL COMMENT '任务编码',
  `task_name` VARCHAR(100) NOT NULL COMMENT '任务名称',
  `points` INT NOT NULL DEFAULT 0 COMMENT '奖励积分',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',

  -- BaseEntity 字段（含 is_deleted）
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(50) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(50) DEFAULT NULL COMMENT '修改人姓名',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除',

  UNIQUE KEY `uk_task_code` (`task_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务定义表';
```

#### 示例2：流水表（不需要 is_deleted）

```sql
-- 流水记录表，不支持软删除
CREATE TABLE `points_history` (
  -- 业务字段
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `points` INT NOT NULL COMMENT '变动积分',
  `balance` INT NOT NULL COMMENT '变动后余额',
  `type` TINYINT NOT NULL COMMENT '类型',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',

  -- BaseEntity 字段（不含 is_deleted）
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  `creator_name` VARCHAR(50) DEFAULT NULL COMMENT '创建人姓名',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modifier_id` BIGINT DEFAULT NULL COMMENT '修改人ID',
  `modifier_name` VARCHAR(50) DEFAULT NULL COMMENT '修改人姓名',
  `modify_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分流水表';
```

---

## 功能鉴权决策规范（重要）

**新增功能必须结合业界经典案例，以高级产品总监视角判断是否需要登录鉴权。**

### 鉴权决策流程

```
新增功能
    ↓
参考业界经典案例（小红书、美团、淘宝等）
    ↓
以高级产品总监视角评估
    ↓
决定：公开 / 需登录 / 混合模式
    ↓
在 api.js 中使用对应方法：
  - publicRequest() - 公开接口
  - authRequest() - 需登录接口
```

### 业界经典案例参考

| 产品 | 不需登录 | 需登录 |
|------|---------|--------|
| 小红书 | 浏览内容 | 点赞/收藏/评论/发布 |
| 美团 | 浏览商家/商品 | 下单/收藏/地址 |
| 淘宝 | 浏览商品/搜索 | 购买/购物车/收藏 |
| 微信读书 | 浏览书籍推荐 | 阅读/笔记/书架 |
| ChatGPT | 基础对话(有限) | 高级功能/历史记录 |

### 鉴权分类标准

#### 公开功能（不需要登录）

| 类型 | 判断依据 | 示例 |
|------|----------|------|
| 内容浏览 | 内容引流，降低门槛 | 文章列表、商品列表 |
| 信息展示 | 展示平台能力 | 医生列表、门店列表 |
| 核心体验功能 | 让用户先体验再转化 | AI诊断（可设次数限制） |
| 搜索功能 | 内容发现入口 | 商品搜索、热门搜索 |

#### 需登录功能

| 类型 | 判断依据 | 示例 |
|------|----------|------|
| 用户行为数据 | 需要关联用户 | 点赞、收藏、评论 |
| 个人数据 | 隐私相关 | 宠物管理、地址管理 |
| 交易相关 | 涉及金钱 | 订单、购物车、优惠券 |
| 服务预约 | 需要联系用户 | 咨询、美容预约 |
| 积分/奖励 | 用户激励体系 | 任务、积分 |

#### 混合模式（部分公开，部分需登录）

| 功能 | 公开部分 | 需登录部分 |
|------|----------|------------|
| 文章 | 浏览列表/详情 | 点赞/收藏/评论 |
| 商品 | 浏览/搜索 | 收藏/购买/评价 |
| AI诊断 | 有限次数体验 | 无限使用 |

### 决策检查清单

新增功能时，回答以下问题：

```
1. 这个功能是否需要关联用户身份？
   □ 是 → 需登录
   □ 否 → 继续评估

2. 这个功能是否涉及用户隐私数据？
   □ 是 → 需登录
   □ 否 → 继续评估

3. 这个功能是否需要跨设备同步？
   □ 是 → 需登录
   □ 否 → 继续评估

4. 这个功能是否是付费/收费功能？
   □ 是 → 考虑需登录 + 访客体验限制
   □ 否 → 可考虑公开

5. 业界同类产品是如何处理的？
   参考案例：__________

6. 以产品总监视角，这个功能的鉴权策略是否有助于：
   - 用户增长？ □ 是 □ 否
   - 转化率提升？ □ 是 □ 否
   - 用户体验？ □ 是 □ 否
```

### 访客限制功能设计规范

对于"先体验后登录"的功能，需要设计访客限制：

| 要素 | 规范 |
|------|------|
| 限制维度 | 设备ID（存储在本地） |
| 存储方式 | Redis（持久化，不过期） |
| 限制次数 | 根据业务成本决定（如AI诊断：3次） |
| 达到限制 | 引导登录，而非强制阻断 |
| 登录后 | 无限制使用 |

### 代码实现规范

在 `api.js` 中使用正确的请求方法：

```javascript
// ✅ 正确：公开接口
getArticleList(tag) {
  return publicRequest('/api/article/list', 'GET')
}

// ✅ 正确：需登录接口
addPet(data) {
  return authRequest('/api/pet/create', 'POST', data)
}

// ✅ 正确：混合模式 - 公开但有访客限制
diagnose(data, deviceId) {
  const url = deviceId ? `/api/ai/diagnosis?deviceId=${deviceId}` : '/api/ai/diagnosis'
  return publicRequest(url, 'POST', data)
}

// ❌ 错误：应该使用 authRequest 而不是 publicRequest
collect(id) {
  return publicRequest(`/api/article/${id}/collect`, 'POST')  // 收藏需要登录！
}
```

### 本项目鉴权分类表

| 功能模块 | 鉴权策略 | 说明 |
|----------|----------|------|
| 文章浏览 | 公开 | 内容引流 |
| 文章点赞/收藏 | 需登录 | 用户行为 |
| 医生列表 | 公开 | 展示平台能力 |
| AI诊断 | 公开+限制 | 3次访客限制 |
| 宠物管理 | 需登录 | 个人数据 |
| 购物车 | 需登录 | 购买意图 |
| 订单 | 需登录 | 交易数据 |

---

## 其他规范

（待补充...）
