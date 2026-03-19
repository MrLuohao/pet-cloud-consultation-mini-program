#!/bin/bash

# 宠物云咨询后端接口测试
# 地址管理模块测试

source "$(dirname "$0")/../common.sh"

print_title "地址管理模块测试 (/api/address)"

if ! load_auth; then
    print_info "未找到认证信息，请先运行 ./test_auth.sh"
    exit 1
fi

check_connection "${USER_SERVICE_BASE_URL}/api/address/list" "用户服务" || exit 1

ADDRESS_ID=""

# 1. 获取地址列表
print_step "1" "测试获取地址列表接口"
response=$(http_get "${USER_SERVICE_BASE_URL}/api/address/list" "$AUTH_TOKEN")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')
print_request "GET" "${USER_SERVICE_BASE_URL}/api/address/list"
print_response "$http_code" "$body"
if is_success "$body" "$http_code"; then
    print_success "获取地址列表"
    ADDRESS_ID=$(echo "$body" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*' | head -1)
    [ -n "$ADDRESS_ID" ] && print_data "找到地址ID: $ADDRESS_ID"
else
    print_fail "获取地址列表"
fi

# 2. 创建地址
print_step "2" "测试创建地址接口"
address_data='{"contactName":"张三","contactPhone":"13800138000","province":"广东省","city":"深圳市","district":"南山区","detailAddress":"科技园xxx号","isDefault":1}'
response=$(http_post "${USER_SERVICE_BASE_URL}/api/address/create" "$address_data" "$AUTH_TOKEN")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')
print_request "POST" "${USER_SERVICE_BASE_URL}/api/address/create" "$address_data"
print_response "$http_code" "$body"
if is_success "$body" "$http_code"; then
    print_success "创建地址"
    ADDRESS_ID=$(echo "$body" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*' | head -1)
    [ -n "$ADDRESS_ID" ] && echo "$ADDRESS_ID" > "$(dirname "$0")/.address_cache"
else
    print_fail "创建地址"
fi

# 3-7. 其他接口
[ -n "$ADDRESS_ID" ] && { print_step "3" "测试获取地址详情接口"; api_get "获取地址详情" "${USER_SERVICE_BASE_URL}/api/address/${ADDRESS_ID}"; } || print_skip "获取地址详情"
[ -n "$ADDRESS_ID" ] && { print_step "4" "测试更新地址接口"; api_put "更新地址" "${USER_SERVICE_BASE_URL}/api/address/update" '{"id":'$ADDRESS_ID',"contactName":"李四"}'; } || print_skip "更新地址"
[ -n "$ADDRESS_ID" ] && { print_step "5" "测试设置默认地址接口"; api_put "设置默认地址" "${USER_SERVICE_BASE_URL}/api/address/default?addressId=${ADDRESS_ID}" "{}"; } || print_skip "设置默认地址"
[ -n "$ADDRESS_ID" ] && { print_step "6" "测试删除地址接口"; confirm_action "是否删除测试地址" && api_delete "删除地址" "${USER_SERVICE_BASE_URL}/api/address/delete?addressId=${ADDRESS_ID}" || print_skip "删除地址"; } || print_skip "删除地址"

print_summary
