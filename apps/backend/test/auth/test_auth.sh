#!/bin/bash

# 宠物云咨询后端接口测试
# 认证模块测试

# 加载公共函数
source "$(dirname "$0")/../common.sh"

print_title "认证模块测试 (/api/auth)"

# 检查服务连接
check_connection "${USER_SERVICE_BASE_URL}/api/auth/login" "用户服务" || exit 1

# ========== 1. 微信登录（开发环境模拟登录） ==========
print_step "1" "测试微信登录接口"

# 登录接口只传code，其他信息通过 updateUserInfo 接口更新
login_data='{"code": "'$TEST_CODE'"}'

response=$(http_post "${USER_SERVICE_BASE_URL}/api/auth/login" "$login_data" "")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

print_request "POST" "${USER_SERVICE_BASE_URL}/api/auth/login" "$login_data"
print_response "$http_code" "$body"

if is_success "$body" "$http_code"; then
    print_success "微信登录"

    # 提取 token 和 userId
    AUTH_TOKEN=$(echo "$body" | grep -o '"token":"[^"]*"' | sed 's/"token":"//;s/"$//')
    USER_ID=$(echo "$body" | grep -o '"userId":[0-9]*' | grep -o '[0-9]*')

    if [ -n "$AUTH_TOKEN" ]; then
        print_data "Token: ${AUTH_TOKEN:0:50}..."
        print_data "UserId: $USER_ID"
        save_auth "$AUTH_TOKEN" "$USER_ID"
    fi
else
    print_fail "微信登录"
fi

# ========== 2. 刷新Token ==========
print_step "2" "测试刷新Token接口"

if [ -n "$AUTH_TOKEN" ]; then
    api_post "刷新Token" "${USER_SERVICE_BASE_URL}/api/auth/refresh" "{}"
else
    print_skip "刷新Token (未登录)"
fi

# ========== 3. 获取用户信息 ==========
print_step "3" "测试获取用户信息接口"

if [ -n "$AUTH_TOKEN" ]; then
    api_get "获取用户信息" "${USER_SERVICE_BASE_URL}/api/auth/userinfo"
else
    print_skip "获取用户信息 (未登录)"
fi

# ========== 4. 更新用户信息 ==========
print_step "4" "测试更新用户信息接口"

if [ -n "$AUTH_TOKEN" ]; then
    update_data='{"nickname": "测试用户-更新", "gender": 1}'
    api_put "更新用户信息" "${USER_SERVICE_BASE_URL}/api/auth/userinfo" "$update_data"
else
    print_skip "更新用户信息 (未登录)"
fi

# 测试总结
print_summary
