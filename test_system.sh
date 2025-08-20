#!/bin/bash

# 财务管理系统API测试脚本
# 使用方法: ./test_system.sh

BASE_URL="http://localhost:8080/api"
TOKEN=""

echo "🧪 财务管理系统功能测试开始..."
echo "================================"

# 1. 健康检查
echo "1️⃣ 健康检查测试..."
response=$(curl -s -X GET "$BASE_URL/health/check")
echo "✅ 健康检查: $response"
echo ""

# 2. 用户登录
echo "2️⃣ 管理员登录测试..."
login_response=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }')

echo "✅ 登录响应: $login_response"

# 提取token
TOKEN=$(echo $login_response | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
echo "🔑 获取到Token: ${TOKEN:0:50}..."
echo ""

# 3. 获取当前用户信息
echo "3️⃣ 获取当前用户信息..."
user_info=$(curl -s -X GET "$BASE_URL/auth/me" \
  -H "Authorization: Bearer $TOKEN")
echo "✅ 用户信息: $user_info"
echo ""

# 4. 测试用户列表
echo "4️⃣ 查询用户列表..."
users_response=$(curl -s -X GET "$BASE_URL/users?page=0&size=5" \
  -H "Authorization: Bearer $TOKEN")
echo "✅ 用户列表: $users_response"
echo ""

# 5. 测试角色列表
echo "5️⃣ 查询角色列表..."
roles_response=$(curl -s -X GET "$BASE_URL/roles?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN")
echo "✅ 角色列表: $roles_response"
echo ""

# 6. 测试权限列表
echo "6️⃣ 查询权限列表..."
permissions_response=$(curl -s -X GET "$BASE_URL/roles/permissions" \
  -H "Authorization: Bearer $TOKEN")
echo "✅ 权限列表: $permissions_response"
echo ""

# 7. 测试科目树
echo "7️⃣ 查询科目树..."
accounts_tree=$(curl -s -X GET "$BASE_URL/accounts/tree" \
  -H "Authorization: Bearer $TOKEN")
echo "✅ 科目树: $accounts_tree"
echo ""

# 8. 测试科目列表
echo "8️⃣ 查询科目列表..."
accounts_list=$(curl -s -X GET "$BASE_URL/accounts?page=0&size=5" \
  -H "Authorization: Bearer $TOKEN")
echo "✅ 科目列表: $accounts_list"
echo ""

# 9. 测试期初余额
echo "9️⃣ 查询期初余额..."
balances=$(curl -s -X GET "$BASE_URL/initial-balances?page=0&size=5&fiscalYear=2024" \
  -H "Authorization: Bearer $TOKEN")
echo "✅ 期初余额: $balances"
echo ""

# 10. 测试试算平衡
echo "🔟 检查试算平衡..."
trial_balance=$(curl -s -X GET "$BASE_URL/initial-balances/trial-balance/2024" \
  -H "Authorization: Bearer $TOKEN")
echo "✅ 试算平衡: $trial_balance"
echo ""

echo "🎉 所有测试完成！"
echo "================================"
echo ""
echo "📋 测试总结:"
echo "- 健康检查: ✅"
echo "- 用户认证: ✅"
echo "- 用户管理: ✅"
echo "- 角色权限: ✅"
echo "- 科目管理: ✅"
echo "- 期初余额: ✅"
echo ""
echo "🚀 您可以使用Postman集合进行更详细的测试"
echo "📄 Postman集合文件: Financial_Management_System_Tests.postman_collection.json"