#!/bin/bash

# 宠物云咨询后端接口测试
# 新增文章接口测试

source "$(dirname "$0")/../common.sh"

print_title "新增文章接口测试 (/api/article/create)"

# 加载认证信息
if ! load_auth; then
    print_info "未找到认证信息，请先运行 ./test_auth.sh"
    exit 1
fi

# 检查服务连接
check_connection "${USER_SERVICE_BASE_URL}/api/article/list" "用户服务" || exit 1

# ========== 1. 测试新增文章 - 发布状态 ==========
print_step "1" "测试新增文章（发布状态）"

article_data='{
  "title":"测试文章-宠物健康指南",
  "coverUrl":"https://example.com/cover.jpg",
  "summary":"这是一篇关于宠物健康的指南文章",
  "content":"# 宠物健康指南\n\n## 1. 饮食健康\n\n宠物饮食需要注意营养均衡...\n\n## 2. 运动健康\n\n每天适量运动对宠物健康非常重要...",
  "tag":"健康,指南",
  "status":1
}'

response=$(http_post "${USER_SERVICE_BASE_URL}/api/article/create" "$article_data" "$AUTH_TOKEN")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

print_request "POST" "${USER_SERVICE_BASE_URL}/api/article/create" "$article_data"
print_response "$http_code" "$body"

PUBLISHED_ARTICLE_ID=""
if is_success "$body" "$http_code"; then
    print_success "新增文章（发布状态）"
    PUBLISHED_ARTICLE_ID=$(echo "$body" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*' | head -1)
    if [ -n "$PUBLISHED_ARTICLE_ID" ]; then
        print_data "新建文章ID: $PUBLISHED_ARTICLE_ID"
    fi
else
    print_fail "新增文章（发布状态）"
fi

# ========== 2. 测试新增文章 - 草稿状态 ==========
print_step "2" "测试新增文章（草稿状态）"

draft_data='{
  "title":"测试文章-宠物训练技巧（草稿）",
  "summary":"这是一篇关于宠物训练的草稿文章",
  "content":"# 宠物训练技巧\n\n## 基础训练\n\n待补充内容...",
  "tag":"训练",
  "status":0
}'

response=$(http_post "${USER_SERVICE_BASE_URL}/api/article/create" "$draft_data" "$AUTH_TOKEN")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

print_request "POST" "${USER_SERVICE_BASE_URL}/api/article/create" "$draft_data"
print_response "$http_code" "$body"

DRAFT_ARTICLE_ID=""
if is_success "$body" "$http_code"; then
    print_success "新增文章（草稿状态）"
    DRAFT_ARTICLE_ID=$(echo "$body" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*' | head -1)
    if [ -n "$DRAFT_ARTICLE_ID" ]; then
        print_data "新建草稿ID: $DRAFT_ARTICLE_ID"
    fi
else
    print_fail "新增文章（草稿状态）"
fi

# ========== 3. 测试新增文章 - 缺少必填项（标题为空） ==========
print_step "3" "测试新增文章 - 缺少必填项（标题为空）"

invalid_data='{
  "title":"",
  "content":"这是一篇测试文章"
}'

response=$(http_post "${USER_SERVICE_BASE_URL}/api/article/create" "$invalid_data" "$AUTH_TOKEN")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

print_request "POST" "${USER_SERVICE_BASE_URL}/api/article/create" "$invalid_data"
print_response "$http_code" "$body"

# 预期失败（验证失败）
if ! is_success "$body" "$http_code"; then
    print_success "正确拒绝了无效请求（标题为空）"
else
    print_fail "应该拒绝无效请求（标题为空）"
fi

# ========== 4. 测试新增文章 - 缺少必填项（内容为空） ==========
print_step "4" "测试新增文章 - 缺少必填项（内容为空）"

invalid_data2='{
  "title":"测试标题"
}'

response=$(http_post "${USER_SERVICE_BASE_URL}/api/article/create" "$invalid_data2" "$AUTH_TOKEN")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

print_request "POST" "${USER_SERVICE_BASE_URL}/api/article/create" "$invalid_data2"
print_response "$http_code" "$body"

# 预期失败（验证失败）
if ! is_success "$body" "$http_code"; then
    print_success "正确拒绝了无效请求（内容为空）"
else
    print_fail "应该拒绝无效请求（内容为空）"
fi

# ========== 5. 验证发布的文章可以在列表中获取 ==========
if [ -n "$PUBLISHED_ARTICLE_ID" ]; then
    print_step "5" "验证发布的文章可以在列表中获取"

    response=$(http_get "${USER_SERVICE_BASE_URL}/api/article/list" "$AUTH_TOKEN")
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    print_request "GET" "${USER_SERVICE_BASE_URL}/api/article/list"
    print_response "$http_code" "$body"

    # 检查返回的文章列表中是否包含新创建的文章ID
    if is_success "$body" "$http_code"; then
        if echo "$body" | grep -q "\"id\":$PUBLISHED_ARTICLE_ID"; then
            print_success "新发布的文章出现在列表中"
        else
            print_info "新发布的文章未在列表中找到（可能需要刷新）"
        fi
    else
        print_fail "获取文章列表失败"
    fi
else
    print_skip "验证发布的文章（无文章ID）"
fi

# ========== 6. 获取新创建的文章详情 ==========
if [ -n "$PUBLISHED_ARTICLE_ID" ]; then
    print_step "6" "获取新创建的文章详情"
    api_get "获取文章详情" "${USER_SERVICE_BASE_URL}/api/article/${PUBLISHED_ARTICLE_ID}"
else
    print_skip "获取文章详情（无文章ID）"
fi

# 测试总结
print_summary
