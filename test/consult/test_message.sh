#!/bin/bash

# 宠物云咨询后端接口测试
# 消息模块测试

source "$(dirname "$0")/../common.sh"

print_title "消息模块测试 (/api/message)"

if ! load_auth; then
    print_info "未找到认证信息，请先运行 ./test_auth.sh"
    exit 1
fi

check_connection "${USER_SERVICE_BASE_URL}/api/message/list" "用户服务" || exit 1

print_step "1" "测试获取消息列表接口"
api_get "获取消息列表" "${USER_SERVICE_BASE_URL}/api/message/list"

print_step "2" "测试获取未读消息数量接口"
api_get "获取未读消息数量" "${USER_SERVICE_BASE_URL}/api/message/unread-count"

print_step "3" "测试全部标记已读接口"
api_put "全部标记已读" "${USER_SERVICE_BASE_URL}/api/message/read-all" "{}"

print_summary
