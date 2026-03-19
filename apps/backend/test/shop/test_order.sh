#!/bin/bash

# 宠物云咨询后端接口测试
# 订单模块测试

source "$(dirname "$0")/../common.sh"

print_title "订单模块测试 (/api/order)"

if ! load_auth; then
    print_info "未找到认证信息，请先运行 ./test_auth.sh"
    exit 1
fi

[ -f "$(dirname "$0")/.product_cache" ] && PRODUCT_ID=$(cat "$(dirname "$0")/.product_cache") || PRODUCT_ID="1"
[ -f "$(dirname "$0")/.address_cache" ] && ADDRESS_ID=$(cat "$(dirname "$0")/.address_cache") || ADDRESS_ID=""

check_connection "${SHOP_SERVICE_BASE_URL}/api/order/count" "商城服务" || exit 1

print_step "1" "测试获取订单数量统计接口"
api_get "获取订单数量统计" "${SHOP_SERVICE_BASE_URL}/api/order/count"

print_step "2" "测试获取订单列表接口"
response=$(http_get "${SHOP_SERVICE_BASE_URL}/api/order/list" "$AUTH_TOKEN")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')
print_request "GET" "${SHOP_SERVICE_BASE_URL}/api/order/list"
print_response "$http_code" "$body"
is_success "$body" "$http_code" && ORDER_ID=$(echo "$body" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*') || ORDER_ID=""

[ -n "$ORDER_ID" ] && { print_step "3" "测试获取订单详情接口"; api_get "获取订单详情" "${SHOP_SERVICE_BASE_URL}/api/order/${ORDER_ID}"; } || print_skip "获取订单详情"

print_step "4" "测试订单确认页接口"
api_post "获取订单确认信息" "${SHOP_SERVICE_BASE_URL}/api/order/confirm" '{"productIds":['$PRODUCT_ID'],"quantities":[1]}'

if [ -n "$ADDRESS_ID" ]; then
    print_step "5" "测试提交订单接口"
    confirm_action "是否创建测试订单" && api_post "提交订单" "${SHOP_SERVICE_BASE_URL}/api/order/submit" '{"productIds":['$PRODUCT_ID'],"quantities":[1],"addressId":'$ADDRESS_ID'}' || print_skip "提交订单"
else
    print_info "请先运行 ./test_address.sh 创建地址"
fi

print_summary
