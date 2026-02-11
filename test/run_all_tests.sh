#!/bin/bash

# 宠物云咨询后端接口测试
# 运行所有测试

SCRIPT_DIR="$(dirname "$0")"
source "${SCRIPT_DIR}/config.sh"
source "${SCRIPT_DIR}/common.sh"

# 测试结果统计文件
TEST_RESULT_FILE="${SCRIPT_DIR}/.test_result"

# 初始化结果文件
echo "0" > "$TEST_RESULT_FILE.passed"
echo "0" > "$TEST_RESULT_FILE.failed"

echo ""
i
for ((i=1; i<=70; i++)); do printf '#'; done
echo ""
echo "#                                                              #"
echo "#              宠物云咨询后端接口测试套件                          #"
echo "#                                                              #"
for ((i=1; i<=70; i++)); do printf '#'; done
echo ""

echo ""
echo "请先确保:"
echo "  1. 后端服务已启动 (user-service: 8117, shop-service: 8118)"
echo "  2. 如需修改服务地址，请编辑 config.sh"
echo ""
echo "按 Enter 开始测试，或 Ctrl+C 退出..."
read -r

TOTAL_PASSED=0
TOTAL_FAILED=0

run_test() {
    local test_file="$1"
    local description="$2"

    print_separator
    echo "运行: $description"
    print_separator

    if [ -f "$test_file" ]; then
        # 运行测试脚本
        bash "$test_file"

        # 读取测试结果
        if [ -f "$TEST_RESULT_FILE.passed" ]; then
            TESTS_PASSED=$(cat "$TEST_RESULT_FILE.passed")
        else
            TESTS_PASSED=0
        fi
        if [ -f "$TEST_RESULT_FILE.failed" ]; then
            TESTS_FAILED=$(cat "$TEST_RESULT_FILE.failed")
        else
            TESTS_FAILED=0
        fi

        TOTAL_PASSED=$((TOTAL_PASSED + TESTS_PASSED))
        TOTAL_FAILED=$((TOTAL_FAILED + TESTS_FAILED))

        echo ""
        echo "  本轮: 通过=$TESTS_PASSED, 失败=$TESTS_FAILED"
    else
        echo -e "${RED}[ERROR] 测试文件不存在: $test_file${NC}"
        TOTAL_FAILED=$((TOTAL_FAILED + 1))
    fi
}

# 按依赖顺序运行测试
run_test "${SCRIPT_DIR}/auth/test_auth.sh" "认证模块测试"
echo ""

run_test "${SCRIPT_DIR}/user/test_pet.sh" "宠物管理测试"
echo ""

run_test "${SCRIPT_DIR}/user/test_address.sh" "地址管理测试"
echo ""

run_test "${SCRIPT_DIR}/consult/test_consultation.sh" "咨询模块测试"
echo ""

run_test "${SCRIPT_DIR}/consult/test_message.sh" "消息模块测试"
echo ""

run_test "${SCRIPT_DIR}/shop/test_shop.sh" "商城模块测试"
echo ""

run_test "${SCRIPT_DIR}/shop/test_cart.sh" "购物车测试"
echo ""

run_test "${SCRIPT_DIR}/shop/test_coupon.sh" "优惠券测试"
echo ""

run_test "${SCRIPT_DIR}/shop/test_order.sh" "订单测试"
echo ""

run_test "${SCRIPT_DIR}/ai/test_ai.sh" "AI服务测试"
echo ""

run_test "${SCRIPT_DIR}/content/test_article_course_beauty.sh" "文章/课程/美容服务测试"
echo ""

# 清理临时文件
rm -f "$TEST_RESULT_FILE.passed" "$TEST_RESULT_FILE.failed"

# 打印最终总结
print_separator
echo "  最终测试总结"
print_separator
echo "  总计: $((TOTAL_PASSED + TOTAL_FAILED)) 个接口测试"
echo -e "  ${GREEN}通过: ${TOTAL_PASSED}${NC}"
echo -e "  ${RED}失败: ${TOTAL_FAILED}${NC}"
print_separator
