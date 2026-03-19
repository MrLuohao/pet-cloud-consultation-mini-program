#!/bin/bash

# 宠物云咨询后端接口测试
# AI服务测试

source "$(dirname "$0")/../common.sh"

print_title "AI服务测试 (/api/ai, /api/chat)"

if ! load_auth; then
    print_info "未找到认证信息，请先运行 ./test_auth.sh"
    exit 1
fi

check_connection "${USER_SERVICE_BASE_URL}/api/ai/diagnosis" "用户服务" || exit 1

print_step "1" "测试AI健康诊断接口"
diagnosis_data='{"petType":1,"petAge":12,"symptoms":"食欲不振，精神萎靡"}'
response=$(http_post "${USER_SERVICE_BASE_URL}/api/ai/diagnosis" "$diagnosis_data" "$AUTH_TOKEN")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')
print_request "POST" "${USER_SERVICE_BASE_URL}/api/ai/diagnosis" "$diagnosis_data"
print_response "$http_code" "$body"
is_success "$body" "$http_code" && print_success "AI健康诊断" || print_fail "AI健康诊断"

print_step "2" "测试Qwen聊天接口"
confirm_action "是否测试Qwen聊天" && {
    qwen_data='{"userMessage":"你好"}'
    response=$(http_post "${USER_SERVICE_BASE_URL}/api/chat/qwen3Max" "$qwen_data" "$AUTH_TOKEN")
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    print_request "POST" "${USER_SERVICE_BASE_URL}/api/chat/qwen3Max" "$qwen_data"
    print_response "$http_code" "$body"
    is_success "$body" "$http_code" && print_success "Qwen聊天" || print_fail "Qwen聊天"
} || print_skip "Qwen聊天"

print_summary
