#!/bin/bash

# 宠物云咨询后端接口测试
# 购物车模块测试

source "$(dirname "$0")/../common.sh"

print_title "购物车模块测试 (/api/cart)"

if ! load_auth; then
    print_info "未找到认证信息，请先运行 ./test_auth.sh"
    exit 1
fi

[ -f "$(dirname "$0")/.product_cache" ] && PRODUCT_ID=$(cat "$(dirname "$0")/.product_cache") || PRODUCT_ID="1"

check_connection "${SHOP_SERVICE_BASE_URL}/api/cart/list" "商城服务" || exit 1

CART_ID=""

# 1. 获取购物车列表
print_step "1" "测试获取购物车列表接口"
api_get "获取购物车列表" "${SHOP_SERVICE_BASE_URL}/api/cart/list"

# 2. 获取购物车商品数量
print_step "2" "测试获取购物车商品数量接口"
api_get "获取购物车商品数量" "${SHOP_SERVICE_BASE_URL}/api/cart/count"

# 3. 添加商品到购物车
print_step "3" "测试添加商品到购物车接口"
response=$(curl -s -w "\n%{http_code}" -X POST \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -H "Authorization: Bearer $AUTH_TOKEN" \
    -H "X-User-Id: $USER_ID" \
    -d "productId=${PRODUCT_ID}&quantity=1" \
    "${SHOP_SERVICE_BASE_URL}/api/cart/add")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

print_request "POST" "${SHOP_SERVICE_BASE_URL}/api/cart/add" "productId=${PRODUCT_ID}&quantity=1"
print_response "$http_code" "$body"

if is_success "$body" "$http_code"; then
    print_success "添加商品到购物车"
    CART_ID=$(echo "$body" | grep -o '"data":[0-9]*' | cut -d':' -f2)
    [ -n "$CART_ID" ] && print_data "购物车项ID: $CART_ID"
else
    print_fail "添加商品到购物车"
fi

# 4. 更新购物车数量
if [ -n "$CART_ID" ]; then
    print_step "4" "测试更新购物车数量接口"
    response=$(curl -s -w "\n%{http_code}" -X PUT \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -H "Authorization: Bearer $AUTH_TOKEN" \
        -H "X-User-Id: $USER_ID" \
        -d "cartId=${CART_ID}&quantity=2" \
        "${SHOP_SERVICE_BASE_URL}/api/cart/update")
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    print_request "PUT" "${SHOP_SERVICE_BASE_URL}/api/cart/update" "cartId=${CART_ID}&quantity=2"
    print_response "$http_code" "$body"
    is_success "$body" "$http_code" && print_success "更新购物车数量" || print_fail "更新购物车数量"
else
    print_skip "更新购物车数量 (无购物车ID)"
fi

# 5. 删除购物车商品
if [ -n "$CART_ID" ]; then
    print_step "5" "测试删除购物车商品接口"
    if confirm_action "是否删除购物车商品"; then
        response=$(curl -s -w "\n%{http_code}" -X DELETE \
            -H "Authorization: Bearer $AUTH_TOKEN" \
            -H "X-User-Id: $USER_ID" \
            "${SHOP_SERVICE_BASE_URL}/api/cart/delete?cartId=${CART_ID}")
        http_code=$(echo "$response" | tail -n1)
        body=$(echo "$response" | sed '$d')
        print_request "DELETE" "${SHOP_SERVICE_BASE_URL}/api/cart/delete?cartid=${CART_ID}"
        print_response "$http_code" "$body"
        is_success "$body" "$http_code" && print_success "删除购物车商品" || print_fail "删除购物车商品"
    else
        print_skip "删除购物车商品"
    fi
else
    print_skip "删除购物车商品 (无购物车ID)"
fi

# 6. 清空购物车
print_step "6" "测试清空购物车接口"
if confirm_action "是否清空购物车"; then
    response=$(curl -s -w "\n%{http_code}" -X DELETE \
        -H "Authorization: Bearer $AUTH_TOKEN" \
        -H "X-User-Id: $USER_ID" \
        "${SHOP_SERVICE_BASE_URL}/api/cart/clear")
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    print_request "DELETE" "${SHOP_SERVICE_BASE_URL}/api/cart/clear"
    print_response "$http_code" "$body"
    is_success "$body" "$http_code" && print_success "清空购物车" || print_fail "清空购物车"
else
    print_skip "清空购物车"
fi

print_summary
