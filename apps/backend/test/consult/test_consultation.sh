#!/bin/bash

# 宠物云咨询后端接口测试
# 咨询模块测试

source "$(dirname "$0")/../common.sh"

print_title "咨询模块测试 (/api/consultation, /api/doctor)"

if ! load_auth; then
    print_info "未找到认证信息，请先运行 ./test_auth.sh"
    exit 1
fi

[ -f "$(dirname "$0")/.pet_cache" ] && PET_ID=$(cat "$(dirname "$0")/.pet_cache") || PET_ID=""

check_connection "${USER_SERVICE_BASE_URL}/api/doctor/departments" "用户服务" || exit 1

print_step "1" "测试获取科室列表接口"
api_get "获取科室列表" "${USER_SERVICE_BASE_URL}/api/doctor/departments"

print_step "2" "测试获取医生列表接口"
response=$(http_get "${USER_SERVICE_BASE_URL}/api/doctor/list" "$AUTH_TOKEN")
http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')
print_request "GET" "${USER_SERVICE_BASE_URL}/api/doctor/list"
print_response "$http_code" "$body"
is_success "$body" "$http_code" && DOCTOR_ID=$(echo "$body" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*') || DOCTOR_ID="1"

[ -n "$DOCTOR_ID" ] && { print_step "3" "测试获取医生详情接口"; api_get "获取医生详情" "${USER_SERVICE_BASE_URL}/api/doctor/${DOCTOR_ID}"; } || print_skip "获取医生详情"

print_step "4" "测试获取咨询列表接口"
api_get "获取咨询列表" "${USER_SERVICE_BASE_URL}/api/consultation/list"

if [ -n "$PET_ID" ]; then
    print_step "5" "测试创建咨询接口"
    consult_data='{"petId":'$PET_ID',"doctorId":'$DOCTOR_ID',"type":1,"description":"狗狗最近食欲不振"}'
    response=$(http_post "${USER_SERVICE_BASE_URL}/api/consultation/create" "$consult_data" "$AUTH_TOKEN")
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    print_request "POST" "${USER_SERVICE_BASE_URL}/api/consultation/create" "$consult_data"
    print_response "$http_code" "$body"
    is_success "$body" "$http_code" && CONSULTATION_ID=$(echo "$body" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*') || CONSULTATION_ID=""
else
    print_info "请先运行 ./test_pet.sh 创建宠物"
fi

print_summary
