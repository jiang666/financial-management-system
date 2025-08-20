@echo off
chcp 65001 >nul
echo 🧪 财务管理系统功能测试开始...
echo ================================

set BASE_URL=http://localhost:8080/api

echo 1️⃣ 健康检查测试...
curl -s -X GET "%BASE_URL%/health/check"
echo.
echo.

echo 2️⃣ 管理员登录测试...
curl -s -X POST "%BASE_URL%/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"username\": \"admin\", \"password\": \"admin123\"}" > login_response.json

echo ✅ 登录成功！请查看 login_response.json 文件获取token
echo.

echo 💡 接下来请按以下步骤手动测试：
echo.
echo 1. 从 login_response.json 文件中复制 accessToken 的值
echo 2. 将 YOUR_TOKEN_HERE 替换为实际的token，然后运行以下命令：
echo.
echo curl -X GET "%BASE_URL%/auth/me" -H "Authorization: Bearer YOUR_TOKEN_HERE"
echo curl -X GET "%BASE_URL%/users?page=0&size=5" -H "Authorization: Bearer YOUR_TOKEN_HERE"
echo curl -X GET "%BASE_URL%/roles?page=0&size=10" -H "Authorization: Bearer YOUR_TOKEN_HERE"
echo curl -X GET "%BASE_URL%/roles/permissions" -H "Authorization: Bearer YOUR_TOKEN_HERE"
echo curl -X GET "%BASE_URL%/accounts/tree" -H "Authorization: Bearer YOUR_TOKEN_HERE"
echo curl -X GET "%BASE_URL%/accounts?page=0&size=5" -H "Authorization: Bearer YOUR_TOKEN_HERE"
echo curl -X GET "%BASE_URL%/initial-balances?page=0&size=5&fiscalYear=2024" -H "Authorization: Bearer YOUR_TOKEN_HERE"
echo curl -X GET "%BASE_URL%/initial-balances/trial-balance/2024" -H "Authorization: Bearer YOUR_TOKEN_HERE"
echo.

echo 📄 更便捷的测试方法：
echo - 导入 Financial_Management_System_Tests.postman_collection.json 到 Postman
echo - 使用 测试指南.md 中的详细步骤
echo.

pause