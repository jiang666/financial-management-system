#!/bin/bash

# 科目管理模块测试脚本
BASE_URL="http://localhost:8080"
TOKEN=""

echo "=== 科目管理模块测试脚本 ==="

# 1. 登录获取Token
echo "1. 登录获取Token..."
LOGIN_RESPONSE=$(curl -s -X POST ${BASE_URL}/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }')

echo "登录响应: $LOGIN_RESPONSE"

# 提取Token（需要根据实际响应格式调整）
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | sed 's/"token":"//')

if [ -z "$TOKEN" ]; then
    echo "❌ 登录失败，请检查用户名密码"
    exit 1
fi

echo "✅ 登录成功，Token: ${TOKEN:0:20}..."

# 2. 测试科目树查询
echo ""
echo "2. 测试科目树查询..."
curl -s -X GET ${BASE_URL}/api/accounts/tree \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq '.'

# 3. 测试科目分页查询
echo ""
echo "3. 测试科目分页查询..."
curl -s -X GET "${BASE_URL}/api/accounts?page=0&size=5" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq '.'

# 4. 测试科目搜索
echo ""
echo "4. 测试科目搜索（关键词：现金）..."
curl -s -X GET "${BASE_URL}/api/accounts/search?keyword=现金" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq '.'

# 5. 测试获取资产类科目
echo ""
echo "5. 测试获取资产类科目..."
curl -s -X GET ${BASE_URL}/api/accounts/tree/ASSET \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq '.'

# 6. 测试生成下一个科目编码
echo ""
echo "6. 测试生成下一个科目编码..."
curl -s -X GET "${BASE_URL}/api/accounts/next-code" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq '.'

# 7. 测试获取期初余额
echo ""
echo "7. 测试获取2025年期初余额..."
curl -s -X GET ${BASE_URL}/api/initial-balances/year/2025 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq '.'

# 8. 测试试算平衡
echo ""
echo "8. 测试试算平衡..."
curl -s -X GET ${BASE_URL}/api/initial-balances/trial-balance/2025 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq '.'

echo ""
echo "=== 测试完成 ==="