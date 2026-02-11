#!/bin/bash

# 宠物云咨询后端接口测试
# 商城模块测试

# 加载公共函数
source "$(dirname "$0")/../common.sh"

print_title "商城模块测试 (/api/product, /api/search)"

# 加载认证信息
load_auth || print_info "未找到认证信息，部分接口可能需要登录"

# 检查服务连接
check_connection "${SHOP_SERVICE_BASE_URL}/api/product/list" "商城服务" || exit 1

PRODUCT_ID=""

# ========== 1. 获取商品分类 ==========
print_step "1" "测试获取商品分类接口"

response=$(http_get "${SHOP_SERVICE_BASE_URL}/api/product/categories" "$AUTH_TOKEN")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

print_request "GET" "${SHOP_SERVICE_BASE_URL}/api/product/categories"
print_response "$http_code" "$body"

if is_success "$body" "$http_code"; then
    print_success "获取商品分类"
else
    print_fail "获取商品分类"
fi

# ========== 2. 获取商品列表 ==========
print_step "2" "测试获取商品列表接口"

response=$(http_get "${SHOP_SERVICE_BASE_URL}/api/product/list" "$AUTH_TOKEN")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

print_request "GET" "${SHOP_SERVICE_BASE_URL}/api/product/list"
print_response "$http_code" "$body"

if is_success "$body" "$http_code"; then
    print_success "获取商品列表"
    # 提取第一个商品ID
    PRODUCT_ID=$(echo "$body" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*' | head -1)
    if [ -n "$PRODUCT_ID" ]; then
        print_data "找到商品ID: $PRODUCT_ID"
        echo "$PRODUCT_ID" > "$(dirname "$0")/.product_cache"
    fi
else
    print_fail "获取商品列表"
fi

# ========== 3. 按分类获取商品 ==========
print_step "3" "测试按分类获取商品接口（食品分类）"
api_get "按分类获取商品" "${SHOP_SERVICE_BASE_URL}/api/product/list?categoryId=1"

# ========== 4. 获取商品详情 ==========
if [ -n "$PRODUCT_ID" ]; then
    print_step "4" "测试获取商品详情接口"
    api_get "获取商品详情" "${SHOP_SERVICE_BASE_URL}/api/product/${PRODUCT_ID}"
else
    print_skip "获取商品详情 (无商品ID)"
fi

# ========== 5. 获取商品评价 ==========
if [ -n "$PRODUCT_ID" ]; then
    print_step "5" "测试获取商品评价接口"
    api_get "获取商品评价" "${SHOP_SERVICE_BASE_URL}/api/product/${PRODUCT_ID}/reviews?page=1&size=10"
else
    print_skip "获取商品评价 (无商品ID)"
fi

# ========== 7. 搜索商品 ==========
print_step "7" "测试搜索商品接口"
# 使用 URL 编码的搜索关键词
api_get "搜索商品" "${SHOP_SERVICE_BASE_URL}/api/search/products?keyword=%E7%8E%A9%E5%85%B7"

# 测试总结
print_summary
