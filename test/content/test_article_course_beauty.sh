#!/bin/bash

# 宠物云咨询后端接口测试
# 文章/课程/美容服务测试

source "$(dirname "$0")/../common.sh"

print_title "文章/课程/美容服务测试"

if ! load_auth; then
    print_info "未找到认证信息，请先运行 ./test_auth.sh"
    exit 1
fi

check_connection "${USER_SERVICE_BASE_URL}/api/course/list" "用户服务" || exit 1

print_step "1" "测试获取文章列表接口"
response=$(http_get "${USER_SERVICE_BASE_URL}/api/article/list" "$AUTH_TOKEN")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')
print_request "GET" "${USER_SERVICE_BASE_URL}/api/article/list"
print_response "$http_code" "$body"
is_success "$body" "$http_code" && ARTICLE_ID=$(echo "$body" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*') || ARTICLE_ID=""

[ -n "$ARTICLE_ID" ] && { print_step "2" "测试获取文章详情接口"; api_get "获取文章详情" "${USER_SERVICE_BASE_URL}/api/article/${ARTICLE_ID}"; } || print_skip "获取文章详情"

print_step "3" "测试获取课程列表接口"
response=$(http_get "${USER_SERVICE_BASE_URL}/api/course/list" "$AUTH_TOKEN")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')
print_request "GET" "${USER_SERVICE_BASE_URL}/api/course/list"
print_response "$http_code" "$body"
is_success "$body" "$http_code" && COURSE_ID=$(echo "$body" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*') || COURSE_ID=""

[ -n "$COURSE_ID" ] && { print_step "4" "测试获取课程详情接口"; api_get "获取课程详情" "${USER_SERVICE_BASE_URL}/api/course/${COURSE_ID}"; } || print_skip "获取课程详情"

print_step "5" "测试获取门店列表接口"
response=$(http_get "${USER_SERVICE_BASE_URL}/api/beauty/stores" "$AUTH_TOKEN")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')
print_request "GET" "${USER_SERVICE_BASE_URL}/api/beauty/stores"
print_response "$http_code" "$body"
is_success "$body" "$http_code" && STORE_ID=$(echo "$body" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*') || STORE_ID=""

print_summary
