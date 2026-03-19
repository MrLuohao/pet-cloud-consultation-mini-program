#!/bin/bash

# 商品评价功能测试
# 包含：评价列表、写评价、点赞、编辑、追评

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
TEST_DIR="$(dirname "$SCRIPT_DIR")"

# 加载公共函数
source "${TEST_DIR}/common.sh"

print_title "商品评价功能测试"

# 加载认证信息
if ! load_auth; then
    echo "[ERROR] 请先运行 auth/test_auth.sh 进行登录"
    exit 1
fi

# 检查商城服务连接
if ! check_connection "${SHOP_SERVICE_BASE_URL}/v1/health/" "商城服务"; then
    exit 1
fi

# 加载商品缓存
PRODUCT_CACHE_FILE="$(dirname "$0")/../.product_cache"
if [ -f "$PRODUCT_CACHE_FILE" ]; then
    PRODUCT_ID=$(head -n1 "$PRODUCT_CACHE_FILE")
else
    PRODUCT_ID=1
fi

echo "[INFO] 使用商品ID: $PRODUCT_ID"
echo "[INFO] 使用用户ID: $USER_ID"

# ==================== 测试评价列表 ====================

print_step "1" "测试评价列表接口"

# 1.1 获取评价列表（全部）
api_get "获取评价列表(全部)" "${SHOP_SERVICE_BASE_URL}/api/product/${PRODUCT_ID}/reviews?filter=all&page=1&size=10"

# 1.2 获取评价列表（好评）
api_get "获取评价列表(好评)" "${SHOP_SERVICE_BASE_URL}/api/product/${PRODUCT_ID}/reviews?filter=good&page=1&size=10"

# 1.3 获取评价列表（差评）
api_get "获取评价列表(差评)" "${SHOP_SERVICE_BASE_URL}/api/product/${PRODUCT_ID}/reviews?filter=bad&page=1&size=10"

# 1.4 获取评价列表（有图）
api_get "获取评价列表(有图)" "${SHOP_SERVICE_BASE_URL}/api/product/${PRODUCT_ID}/reviews?filter=withImages&page=1&size=10"

# ==================== 测试可评价订单项查询 ====================

print_step "2" "测试可评价订单项查询"

api_get "获取可评价订单项" "${SHOP_SERVICE_BASE_URL}/api/product/${PRODUCT_ID}/reviewable-order-item"

# ==================== 测试点赞功能 ====================

print_step "3" "测试点赞功能"

# 先获取评价列表，取第一个评价的ID
echo ""
echo "[INFO] 获取评价ID用于点赞测试..."
RESPONSE=$(http_get "${SHOP_SERVICE_BASE_URL}/api/product/${PRODUCT_ID}/reviews?filter=all&page=1&size=1" "$AUTH_TOKEN")
BODY=$(echo "$RESPONSE" | sed '$d')

# 提取评价ID (简单方式)
REVIEW_ID=$(echo "$BODY" | grep -o '"id"[[:space:]]*:[[:space:]]*[0-9]*' | head -1 | grep -o '[0-9]*$')

if [ -n "$REVIEW_ID" ] && [ "$REVIEW_ID" -gt 0 ]; then
    echo "[INFO] 找到评价ID: $REVIEW_ID"

    # 3.1 点赞
    api_post "点赞评价" "${SHOP_SERVICE_BASE_URL}/api/product/review/${REVIEW_ID}/like" "{}"

    # 3.2 取消点赞
    api_post "取消点赞" "${SHOP_SERVICE_BASE_URL}/api/product/review/${REVIEW_ID}/like" "{}"
else
    print_skip "点赞测试（没有可用的评价）"
fi

# ==================== 测试编辑评价 ====================

print_step "4" "测试编辑评价（需要自己的评价）"

# 查找自己的评价
echo ""
echo "[INFO] 查找自己的评价..."
ALL_RESPONSE=$(http_get "${SHOP_SERVICE_BASE_URL}/api/product/${PRODUCT_ID}/reviews?filter=all&page=1&size=20" "$AUTH_TOKEN")
ALL_BODY=$(echo "$ALL_RESPONSE" | sed '$d')

# 检查是否有自己的评价 (userId 匹配)
MY_REVIEW_ID=$(echo "$ALL_BODY" | grep -o "\"userId\"[[:space:]]*:[[:space:]]*${USER_ID}" -A 20 | grep -o '"id"[[:space:]]*:[[:space:]]*[0-9]*' | head -1 | grep -o '[0-9]*$')

if [ -n "$MY_REVIEW_ID" ] && [ "$MY_REVIEW_ID" -gt 0 ]; then
    echo "[INFO] 找到自己的评价ID: $MY_REVIEW_ID"

    # 4.1 编辑评价
    api_put "编辑评价" "${SHOP_SERVICE_BASE_URL}/api/product/review/${MY_REVIEW_ID}" '{
        "rating": 5,
        "content": "这是一条编辑后的评价内容 - 测试编辑功能",
        "images": "[]"
    }'
else
    print_skip "编辑评价测试（没有找到自己的评价）"
    echo "[INFO] 提示：需要先创建订单并完成评价后才能测试编辑功能"
fi

# ==================== 测试追评 ====================

print_step "5" "测试追评功能"

if [ -n "$MY_REVIEW_ID" ] && [ "$MY_REVIEW_ID" -gt 0 ]; then
    # 检查是否已追评
    REVIEW_DETAIL=$(echo "$ALL_BODY" | grep -o "\"id\"[[:space:]]*:[[:space:]]*${MY_REVIEW_ID}" -A 50 | head -60)
    HAS_FOLLOW_UP=$(echo "$REVIEW_DETAIL" | grep -o '"followUpContent"[[:space:]]*:[[:space:]]*"[^"]*"' | grep -v '""' | grep -v "null")

    if [ -z "$HAS_FOLLOW_UP" ]; then
        # 5.1 添加追评
        api_post "添加追评" "${SHOP_SERVICE_BASE_URL}/api/product/review/${MY_REVIEW_ID}/follow-up" '{
            "content": "使用一段时间后的追评 - 测试追评功能"
        }'
    else
        print_skip "追评测试（该评价已有追评）"
    fi
else
    print_skip "追评测试（没有找到自己的评价）"
fi

# ==================== 测试商品详情（含评价） ====================

print_step "6" "测试商品详情（含评价）"

api_get "获取商品详情" "${SHOP_SERVICE_BASE_URL}/api/product/${PRODUCT_ID}/detail"

# ==================== 测试总结 ====================

print_summary
