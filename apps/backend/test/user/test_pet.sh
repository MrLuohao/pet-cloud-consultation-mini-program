#!/bin/bash

# 宠物云咨询后端接口测试
# 宠物管理模块测试

# 加载公共函数
source "$(dirname "$0")/../common.sh"

print_title "宠物管理模块测试 (/api/pet)"

# 加载认证信息
if ! load_auth; then
    print_info "未找到认证信息，请先运行 ./test_auth.sh"
    exit 1
fi

# 检查服务连接
check_connection "${USER_SERVICE_BASE_URL}/api/pet/list" "用户服务" || exit 1

PET_ID=""

# ========== 1. 获取宠物列表 ==========
print_step "1" "测试获取宠物列表接口"

response=$(http_get "${USER_SERVICE_BASE_URL}/api/pet/list" "$AUTH_TOKEN")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

print_request "GET" "${USER_SERVICE_BASE_URL}/api/pet/list"
print_response "$http_code" "$body"

if is_success "$body" "$http_code"; then
    print_success "获取宠物列表"
    PET_ID=$(echo "$body" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*' | head -1)
    if [ -n "$PET_ID" ]; then
        print_data "找到宠物ID: $PET_ID"
    fi
else
    print_fail "获取宠物列表"
fi

# ========== 2. 创建宠物 ==========
print_step "2" "测试创建宠物接口"

pet_data='{"name":"旺财","type":1,"breed":"金毛","gender":0,"birthday":"2023-01-01","weight":"30.5"}'

response=$(http_post "${USER_SERVICE_BASE_URL}/api/pet/create" "$pet_data" "$AUTH_TOKEN")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

print_request "POST" "${USER_SERVICE_BASE_URL}/api/pet/create" "$pet_data"
print_response "$http_code" "$body"

if is_success "$body" "$http_code"; then
    print_success "创建宠物"
    PET_ID=$(echo "$body" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*' | head -1)
    if [ -n "$PET_ID" ]; then
        print_data "新建宠物ID: $PET_ID"
        echo "$PET_ID" > "$(dirname "$0")/.pet_cache"
    fi
else
    print_fail "创建宠物"
fi

# ========== 3. 获取宠物详情 ==========
if [ -n "$PET_ID" ]; then
    print_step "3" "测试获取宠物详情接口"
    api_get "获取宠物详情" "${USER_SERVICE_BASE_URL}/api/pet/${PET_ID}"
else
    print_skip "获取宠物详情 (无宠物ID)"
fi

# ========== 4. 更新宠物 ==========
if [ -n "$PET_ID" ]; then
    print_step "4" "测试更新宠物接口"
    update_data='{"id":'$PET_ID',"name":"旺财-更新","weight":"32.5"}'
    api_put "更新宠物" "${USER_SERVICE_BASE_URL}/api/pet/update" "$update_data"
else
    print_skip "更新宠物 (无宠物ID)"
fi

# ========== 5. 删除宠物 ==========
if [ -n "$PET_ID" ]; then
    print_step "5" "测试删除宠物接口"
    if confirm_action "是否要删除测试宠物"; then
        response=$(http_delete "${USER_SERVICE_BASE_URL}/api/pet/delete?recordId=${PET_ID}" "$AUTH_TOKEN")
        http_code=$(echo "$response" | tail -n1)
        body=$(echo "$response" | sed '$d')
        print_request "DELETE" "${USER_SERVICE_BASE_URL}/api/pet/delete?recordId=${PET_ID}"
        print_response "$http_code" "$body"
        if is_success "$body" "$http_code"; then
            print_success "删除宠物"
        else
            print_fail "删除宠物"
        fi
    else
        print_skip "删除宠物"
    fi
fi

# 测试总结
print_summary
