#!/bin/bash

# 宠物云咨询后端接口测试
# 优惠券模块测试

source "$(dirname "$0")/../common.sh"

print_title "优惠券模块测试 (/api/coupon)"

if ! load_auth; then
    print_info "未找到认证信息，请先运行 ./test_auth.sh"
    exit 1
fi

check_connection "${SHOP_SERVICE_BASE_URL}/api/coupon/list" "商城服务" || exit 1

print_step "1" "测试获取可领取优惠券列表接口"
response=$(http_get "${SHOP_SERVICE_BASE_URL}/api/coupon/list" "$AUTH_TOKEN")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')
print_request "GET" "${SHOP_SERVICE_BASE_URL}/api/coupon/list"
print_response "$http_code" "$body"
is_success "$body" "$http_code" && COUPON_ID=$(echo "$body" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*') || COUPON_ID=""

print_step "2" "测试获取我的优惠券接口"
api_get "获取我的优惠券" "${SHOP_SERVICE_BASE_URL}/api/coupon/my"

print_step "3" "测试获取订单可用优惠券接口"
api_get "获取订单可用优惠券" "${SHOP_SERVICE_BASE_URL}/api/coupon/available?totalAmount=100.00"

print_summary
