#!/bin/bash

# 宠物云咨询后端接口测试
# 配置文件

# 获取脚本所在目录
CONFIG_DIR="$(cd "$(dirname "$0")" && pwd)"

# 后端服务地址
USER_SERVICE_BASE_URL="http://localhost:8117"
SHOP_SERVICE_BASE_URL="http://localhost:8118"

# 测试用手机号和验证码（开发环境模拟登录）
TEST_PHONE="13800138000"
TEST_CODE="TEST_CODE_123"

# 保存认证信息的文件
AUTH_CACHE_FILE="${CONFIG_DIR}/.auth_cache"

# 测试结果统计（使用全局变量，在各个测试脚本间累积）
TOTAL_PASSED=0
TOTAL_FAILED=0

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m'
