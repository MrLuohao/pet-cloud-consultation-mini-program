#!/bin/bash

# 宠物云咨询后端接口测试
# 公共函数库

# 获取脚本所在目录
COMMON_DIR="$(cd "$(dirname "$0")" && pwd)"

# 加载配置
source "${COMMON_DIR}/config.sh"

# 测试结果统计文件
TEST_RESULT_FILE="${COMMON_DIR}/.test_result"

# 初始化当前脚本的计数器（每个测试脚本开始时重置）
SCRIPT_PASSED=0
SCRIPT_FAILED=0

# 重置统计文件
echo "0" > "$TEST_RESULT_FILE.passed"
echo "0" > "$TEST_RESULT_FILE.failed"

# ========== 日志函数 ==========

print_separator() {
    echo ""
    local i
    for ((i=1; i<=70; i++)); do printf '='; done
    echo ""
}

print_title() {
    print_separator
    echo "  $1"
    print_separator
    echo ""
}

print_step() {
    echo ""
    echo "[$1] $2"
    for i in {1..40}; do printf '-'; done
    echo ""
}

print_success() {
    echo -e "${GREEN}   ✅ $1 - 成功${NC}"
    ((SCRIPT_PASSED++))
    ((TOTAL_PASSED++))

    # 写入统计文件
    echo "$SCRIPT_PASSED" > "$TEST_RESULT_FILE.passed"
    echo "$SCRIPT_FAILED" > "$TEST_RESULT_FILE.failed"
}

print_fail() {
    echo -e "${RED}   ❌ $1 - 失败${NC}"
    ((SCRIPT_FAILED++))
    ((TOTAL_FAILED++))

    # 写入统计文件
    echo "$SCRIPT_PASSED" > "$TEST_RESULT_FILE.passed"
    echo "$SCRIPT_FAILED" > "$TEST_RESULT_FILE.failed"
}

print_skip() {
    echo -e "${YELLOW}   ⏭️  跳过 $1${NC}"
}

print_data() {
    echo -e "${BLUE}   📋 $1${NC}"
}

print_info() {
    echo -e "${YELLOW}[INFO] $1${NC}"
}

print_request() {
    echo "   [$1] $2"
    if [ -n "$3" ]; then
        echo "   Body: $3"
    fi
}

print_response() {
    local status="$1"
    local body="$2"

    # 检查状态码
    if [ -n "$status" ]; then
        echo "   Status: $status"
    fi

    # 格式化输出响应体
    if [ -n "$body" ]; then
        if command -v jq &> /dev/null; then
            echo "   Response:"
            echo "$body" | jq '.' 2>/dev/null | sed 's/^/   /' || echo "   $body"
        else
            # 简单格式化JSON
            local formatted=$(echo "$body" | grep -o '"status":[^,}]*' | head -1)
            if [ -n "$formatted" ]; then
                echo "   Response: { ... status:$formatted ... }"
            else
                echo "   Response: $body"
            fi
        fi
    fi
}

# ========== HTTP请求函数 ==========

http_get() {
    local url="$1"
    local token="$2"

    local headers=(-H "Content-Type: application/json" -H "Accept: application/json")
    if [ -n "$token" ]; then
        headers+=(-H "Authorization: Bearer $token")
    fi
    if [ -n "$USER_ID" ]; then
        headers+=(-H "X-User-Id: $USER_ID")
    fi

    curl -s -w "\n%{http_code}" -X GET "${headers[@]}" "$url"
}

http_post() {
    local url="$1"
    local data="$2"
    local token="$3"

    local headers=(-H "Content-Type: application/json" -H "Accept: application/json")
    if [ -n "$token" ]; then
        headers+=(-H "Authorization: Bearer $token")
    fi
    if [ -n "$USER_ID" ]; then
        headers+=(-H "X-User-Id: $USER_ID")
    fi

    curl -s -w "\n%{http_code}" -X POST "${headers[@]}" -d "$data" "$url"
}

http_put() {
    local url="$1"
    local data="$2"
    local token="$3"

    local headers=(-H "Content-Type: application/json" -H "Accept: application/json")
    if [ -n "$token" ]; then
        headers+=(-H "Authorization: Bearer $token")
    fi
    if [ -n "$USER_ID" ]; then
        headers+=(-H "X-User-Id: $USER_ID")
    fi

    curl -s -w "\n%{http_code}" -X PUT "${headers[@]}" -d "$data" "$url"
}

http_delete() {
    local url="$1"
    local token="$2"

    local headers=(-H "Content-Type: application/json" -H "Accept: application/json")
    if [ -n "$token" ]; then
        headers+=(-H "Authorization: Bearer $token")
    fi
    if [ -n "$USER_ID" ]; then
        headers+=(-H "X-User-Id: $USER_ID")
    fi

    curl -s -w "\n%{http_code}" -X DELETE "${headers[@]}" "$url"
}

# ========== 响应处理函数 ==========

# 检查响应是否成功
is_success() {
    local body="$1"
    local http_code="$2"

    # HTTP 200-299 且 status=true 或 code=SUCCESS/00000000
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        if echo "$body" | grep -q '"status"[[:space:]]*:[[:space:]]*true'; then
            return 0
        fi
        if echo "$body" | grep -q '"code"[[:space:]]*:[[:space:]]*"SUCCESS\|"code"[[:space:]]*:[[:space:]]*"00000000'; then
            return 0
        fi
    fi
    return 1
}

do_request() {
    local method="$1"
    local name="$2"
    local url="$3"
    local data="$4"

    print_request "$method" "$url" "$data"

    local response=""
    local http_code=""

    case "$method" in
        GET)
            response=$(http_get "$url" "$AUTH_TOKEN")
            ;;
        POST)
            response=$(http_post "$url" "$data" "$AUTH_TOKEN")
            ;;
        PUT)
            response=$(http_put "$url" "$data" "$AUTH_TOKEN")
            ;;
        DELETE)
            response=$(http_delete "$url" "$AUTH_TOKEN")
            ;;
    esac

    # 分离响应体和状态码
    http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')

    print_response "$http_code" "$body"

    if is_success "$body" "$http_code"; then
        print_success "$name"
        return 0
    else
        print_fail "$name"
        return 1
    fi
}

api_get() {
    local name="$1"
    local url="$2"
    do_request "GET" "$name" "$url" ""
}

api_post() {
    local name="$1"
    local url="$2"
    local data="$3"
    do_request "POST" "$name" "$url" "$data"
}

api_put() {
    local name="$1"
    local url="$2"
    local data="$3"
    do_request "PUT" "$name" "$url" "$data"
}

api_delete() {
    local name="$1"
    local url="$2"
    do_request "DELETE" "$name" "$url" ""
}

# ========== 缓存函数 ==========

save_auth() {
    local token="$1"
    local user_id="$2"
    echo "$token" > "$AUTH_CACHE_FILE"
    echo "$user_id" >> "$AUTH_CACHE_FILE"
    AUTH_TOKEN="$token"
    USER_ID="$user_id"
}

load_auth() {
    if [ -f "$AUTH_CACHE_FILE" ]; then
        AUTH_TOKEN=$(head -n1 "$AUTH_CACHE_FILE")
        USER_ID=$(sed -n '2p' "$AUTH_CACHE_FILE")
        return 0
    fi
    return 1
}

clear_auth() {
    rm -f "$AUTH_CACHE_FILE"
    AUTH_TOKEN=""
    USER_ID=""
}

# ========== 用户确认函数 ==========

confirm_action() {
    local prompt="$1"
    echo -n "   ${prompt} (y/N): "
    read -r response
    if [ "$response" = "y" ] || [ "$response" = "Y" ]; then
        return 0
    fi
    return 1
}

# ========== 测试总结函数 ==========

print_summary() {
    print_separator
    echo "  测试总结"
    print_separator
    echo "  总计: $((SCRIPT_PASSED + SCRIPT_FAILED)) 个接口测试"
    echo -e "  ${GREEN}通过: ${SCRIPT_PASSED}${NC}"
    echo -e "  ${RED}失败: ${SCRIPT_FAILED}${NC}"
    print_separator
}

# ========== 错误处理函数 ==========

check_connection() {
    local url="$1"
    local service_name="$2"

    local status=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null)
    if [ "$status" != "000" ]; then
        return 0
    fi
    echo -e "${RED}[ERROR] 无法连接到 $service_name: $url${NC}"
    echo "请检查后端服务是否已启动"
    return 1
}
