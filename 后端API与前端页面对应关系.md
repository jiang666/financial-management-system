# 后端API与前端页面对应关系

## 📊 API - 页面对应总览

### ✅ **已实现的后端API对应的前端页面**

| 后端模块 | API端点 | 对应前端页面 | 页面路径 | 实现状态 |
|---------|---------|------------|---------|----------|
| **认证模块** | `/api/auth/*` | 登录页面 | `/views/auth/Login.vue` | ✅ 已对接 |
| **用户管理** | `/api/users/*` | 用户权限页面 | `/views/settings/permission/index.vue` | ✅ 可对接 |
| **角色权限** | `/api/roles/*` | 用户权限页面 | `/views/settings/permission/index.vue` | ✅ 可对接 |
| **科目管理** | `/api/accounts/*` | 科目管理页面 | `/views/settings/account/index.vue` | ✅ 可对接 |
| **期初余额** | `/api/initial-balances/*` | 期初余额页面 | `/views/settings/balance/index.vue` | ✅ 可对接 |

### 🔍 **详细API与页面功能对应**

#### 1. **认证模块** (`AuthController`)
```
后端API:
- POST /api/auth/login          → 登录功能
- POST /api/auth/register       → 注册功能
- GET  /api/auth/me            → 获取当前用户信息
- POST /api/auth/logout        → 退出登录
- POST /api/auth/change-password → 修改密码

前端页面: /views/auth/Login.vue
- ✅ 登录表单（账号密码登录）
- ✅ 手机验证码登录（界面已有，后端待实现）
- ✅ 第三方登录（界面已有，后端待实现）
```

#### 2. **用户管理模块** (`UserController`)
```
后端API:
- GET    /api/users              → 用户列表查询
- POST   /api/users              → 创建用户
- PUT    /api/users/{id}         → 更新用户信息
- DELETE /api/users/{id}         → 删除用户
- GET    /api/users/{id}         → 获取用户详情
- POST   /api/users/{id}/reset-password → 重置密码

前端页面: /views/settings/permission/index.vue (用户管理Tab)
- ✅ 用户列表展示
- ✅ 用户搜索功能
- ✅ 新增用户对话框
- ✅ 编辑用户对话框
- ✅ 重置密码功能
- ✅ 删除用户功能
```

#### 3. **角色权限模块** (`RoleController`)
```
后端API:
- GET  /api/roles               → 角色列表查询
- GET  /api/roles/permissions   → 获取所有权限

前端页面: /views/settings/permission/index.vue (角色管理/权限配置Tab)
- ✅ 角色列表展示
- ✅ 角色权限配置
- ✅ 权限树形结构展示
- ✅ 角色权限分配
```

#### 4. **科目管理模块** (`ChartOfAccountController`)
```
后端API:
- GET    /api/accounts          → 科目列表查询
- POST   /api/accounts          → 创建科目
- PUT    /api/accounts/{id}     → 更新科目
- DELETE /api/accounts/{id}     → 删除科目
- GET    /api/accounts/{id}     → 获取科目详情
- GET    /api/accounts/tree     → 获取科目树
- GET    /api/accounts/search   → 搜索科目

前端页面: /views/settings/account/index.vue
- ✅ 科目树形列表展示
- ✅ 科目搜索功能
- ✅ 新增科目对话框
- ✅ 编辑科目对话框
- ✅ 添加子科目功能
- ✅ 科目状态管理
- ✅ 编码规则设置
```

#### 5. **期初余额模块** (`InitialBalanceController`)
```
后端API:
- GET    /api/initial-balances                     → 期初余额列表
- POST   /api/initial-balances                     → 创建期初余额
- PUT    /api/initial-balances/{id}                → 更新期初余额
- DELETE /api/initial-balances/{id}                → 删除期初余额
- GET    /api/initial-balances/trial-balance/{year} → 试算平衡检查
- POST   /api/initial-balances/confirm/{year}       → 确认期初余额

前端页面: /views/settings/balance/index.vue
- ✅ 期初余额列表
- ✅ 借贷余额录入
- ✅ 试算平衡检查
- ✅ 批量导入导出
- ✅ 汇总统计功能
```

### 📌 **前端页面但后端未实现的功能**

| 前端页面 | 页面功能 | 需要的后端API | 优先级 |
|---------|---------|--------------|--------|
| **凭证录入** | `/views/voucher/entry/index.vue` | 凭证增删改查API | 高 |
| **凭证审核** | `/views/voucher/review/index.vue` | 凭证审核API | 高 |
| **凭证记账** | `/views/voucher/posting/index.vue` | 凭证记账API | 高 |
| **账簿查询** | `/views/voucher/books/index.vue` | 各类账簿查询API | 高 |
| **财务报表** | `/views/reports/financial/index.vue` | 报表生成API | 高 |
| **辅助核算** | `/views/settings/auxiliary/index.vue` | 辅助核算项API | 中 |
| **币种汇率** | `/views/settings/currency/index.vue` | 币种管理API | 中 |
| **组织架构** | `/views/settings/organization/index.vue` | 组织管理API | 中 |
| **现金银行** | `/views/cash/bank/index.vue` | 现金管理API | 中 |
| **应收应付** | `/views/accounts/receivable/index.vue` | 往来管理API | 低 |

### 🚀 **如何对接前后端**

#### 1. **替换Mock数据为真实API**

当前前端使用的是mock数据：
```javascript
// 前端代码示例 - 需要替换
import { mockUsers, mockRoles } from '@/api/mockData'

// 替换为真实API调用
import { usersAPI, rolesAPI } from '@/api'
```

#### 2. **更新数据加载方法**

```javascript
// 当前代码（使用mock）
const loadUserData = () => {
  userLoading.value = true
  setTimeout(() => {
    userData.value = mockUsers
    userLoading.value = false
  }, 500)
}

// 改为真实API调用
const loadUserData = async () => {
  userLoading.value = true
  try {
    const response = await usersAPI.getUsers({
      page: 0,
      size: 10
    })
    userData.value = response.content
  } catch (error) {
    ElMessage.error('加载用户数据失败')
  } finally {
    userLoading.value = false
  }
}
```

#### 3. **更新操作方法**

```javascript
// 保存用户 - 对接真实API
const saveUser = async () => {
  const valid = await userFormRef.value.validate()
  if (!valid) return
  
  try {
    if (userForm.value.id) {
      await usersAPI.updateUser(userForm.value.id, userForm.value)
    } else {
      await usersAPI.createUser(userForm.value)
    }
    ElMessage.success('保存成功')
    userDialog.value = false
    loadUserData()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}
```

### 📝 **数据格式对齐**

后端返回格式：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [...],
    "totalElements": 100,
    "totalPages": 10
  }
}
```

前端需要调整：
- 使用 `response.content` 获取列表数据
- 使用 `response.totalElements` 获取总数
- 处理分页参数

### 🎯 **推荐对接顺序**

1. **用户权限模块** - 最基础的功能
2. **科目管理模块** - 财务系统核心
3. **期初余额模块** - 财务数据基础
4. **凭证管理模块** - 日常业务核心（需后端实现）
5. **报表模块** - 数据展示（需后端实现）

---

**更新时间**: 2025-08-20 13:00