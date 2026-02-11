# 宠物云咨询后端接口测试

本目录包含宠物云咨询后端服务的接口测试脚本。

## 📁 文件结构

```
test/
├── config.sh                      # 配置文件 (服务地址、测试数据)
├── common.sh                      # 公共函数库
├── run_all_tests.sh              # 运行所有测试
├── README.md                      # 本文件
│
├── auth/                          # 认证模块测试
│   └── test_auth.sh
├── user/                          # 用户相关测试
│   ├── test_pet.sh               # 宠物管理测试
│   └── test_address.sh           # 地址管理测试
├── consult/                       # 咨询相关测试
│   ├── test_consultation.sh      # 咨询模块测试
│   └── test_message.sh           # 消息模块测试
├── shop/                          # 商城相关测试
│   ├── test_shop.sh              # 商品模块测试
│   ├── test_cart.sh              # 购物车测试
│   ├── test_coupon.sh            # 优惠券测试
│   └── test_order.sh             # 订单测试
├── content/                       # 内容相关测试
│   ├── test_article_course_beauty.sh  # 文章/课程/美容服务测试
│   └── test_article_create.sh         # 新增文章接口测试
└── ai/                            # AI服务测试
    └── test_ai.sh
```

## 🔧 环境准备

### 1. 安装依赖

测试脚本使用 `curl` 发送 HTTP 请求，大多数系统已预装。

如未安装，请执行：
```bash
# macOS
brew install curl

# Ubuntu/Debian
apt-get install curl

# CentOS/RHEL
yum install curl
```

### 2. 配置服务地址

编辑 `config.sh` 文件，修改后端服务地址：

```bash
USER_SERVICE_BASE_URL="http://localhost:8117"
SHOP_SERVICE_BASE_URL="http://localhost:8118"
```

如果后端部署在其他地址：

```bash
USER_SERVICE_BASE_URL="http://10.0.12.147:8117"
SHOP_SERVICE_BASE_URL="http://10.0.12.147:8118"
```

## 🚀 使用方法

### 运行所有测试

```bash
cd test
chmod +x *.sh
./run_all_tests.sh
```

### 运行单个测试模块

```bash
# 运行所有测试
./run_all_tests.sh

# 认证测试 (必须先运行，其他测试依赖登录token)
./auth/test_auth.sh

# 宠物管理测试
./user/test_pet.sh

# 其他模块...
```

## 📋 测试模块说明

| 目录 | 测试文件 | 测试内容 | 依赖 |
|------|---------|---------|------|
| auth/ | test_auth.sh | 登录、刷新Token、获取/更新用户信息、登出 | 无 |
| user/ | test_pet.sh | 宠物列表、创建、详情、更新、删除 | test_auth.sh |
| user/ | test_address.sh | 地址列表、创建、详情、更新、删除、默认地址 | test_auth.sh |
| consult/ | test_consultation.sh | 咨询创建、聊天记录、发送消息、完成/取消 | test_auth.sh |
| consult/ | test_message.sh | 消息列表、未读数、标记已读 | test_auth.sh |
| shop/ | test_shop.sh | 商品分类、商品列表、商品详情、评价、搜索 | 无 (公开接口) |
| shop/ | test_cart.sh | 购物车列表、添加、更新、删除、清空 | test_auth.sh |
| shop/ | test_coupon.sh | 优惠券列表、领取、我的优惠券 | test_auth.sh |
| shop/ | test_order.sh | 订单确认、提交、详情、取消、支付、确认收货 | test_auth.sh |
| ai/ | test_ai.sh | AI诊断、聊天、文生图、图像编辑 | test_auth.sh |
| content/ | test_article_course_beauty.sh | 文章、课程、美容服务 | test_auth.sh |
| content/ | test_article_create.sh | 新增文章接口 | test_auth.sh |

## 📝 测试缓存

测试脚本会自动保存一些数据供其他测试使用：

- `.auth_cache` - 登录后的Token和UserId
- `.pet_cache` - 创建的宠物ID
- `.address_cache` - 创建的地址ID
- `.product_cache` - 商品ID

如需重新测试，可以删除这些缓存文件：

```bash
rm -f .auth_cache .pet_cache .address_cache .product_cache
```

## ⚠️ 注意事项

1. **登录测试**: 微信登录需要真实的 `code`，测试时可能返回失败
2. **删除操作**: 部分删除操作需要手动确认 (输入 `y`)
3. **测试顺序**: 建议按依赖顺序运行，先运行 `test_auth.sh`
4. **服务状态**: 确保后端服务已启动，否则会报连接错误
5. **端口清理**: 自测完成后必须检查并清理端口占用
   - 每次自测完成后，需检查 8117、8118 端口是否被占用
   - 如果自测完端口仍被占用，必须 kill 掉占用进程
   - 清理命令：
     ```bash
     # 查看端口占用
     lsof -i :8117 -i :8118

     # 杀掉占用进程
     lsof -ti :8117 :8118 | xargs -r kill -9
     ```

## 🔍 输出示例

```
======================================================================
[1] 测试微信登录接口
----------------------------------------
   [POST] http://localhost:8117/api/auth/login
   Body: {"code": "test_wx_code_123", "nickname": "测试用户", ...}
   Status: 200
   Response: {
     "success": true,
     "code": 200,
     "message": "操作成功",
     "data": {
       "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
       "userId": 123456
     }
   }
   ✅ 微信登录 - 成功
   📋 Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   📋 UserId: 123456
```

## 🛠️ 故障排除

### 连接失败

```
[ERROR] 无法连接到 用户服务: http://localhost:8117
请检查:
  1. 后端服务是否已启动
  2. 服务地址是否正确 (config.sh)
```

**解决方法**: 检查后端服务是否已启动，端口是否正确

### 401 未授权

```
Response: {"success": false, "code": 401, "message": "未登录或登录已过期"}
```

**解决方法**: 先运行 `test_auth.sh` 登录获取Token

### 权限问题

```
bash: ./test_auth.sh: Permission denied
```

**解决方法**: 添加执行权限
```bash
chmod +x *.sh
```

### 404 接口不存在

**解决方法**: 检查后端接口路径是否与前端调用的路径一致
