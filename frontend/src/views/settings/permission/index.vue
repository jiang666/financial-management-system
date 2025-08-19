<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">用户权限</h2>
      <p class="description">管理系统用户、角色和权限分配</p>
    </div>
    
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 用户管理 -->
      <el-tab-pane label="用户管理" name="user">
        <div class="search-form">
          <el-form :model="userSearchForm" inline>
            <el-form-item label="用户名">
              <el-input v-model="userSearchForm.username" placeholder="请输入用户名" clearable />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input v-model="userSearchForm.realName" placeholder="请输入姓名" clearable />
            </el-form-item>
            <el-form-item label="角色">
              <el-select v-model="userSearchForm.role" placeholder="请选择角色" clearable style="width: 140px">
                <el-option
                  v-for="role in roleList"
                  :key="role.code"
                  :label="role.name"
                  :value="role.code"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="searchUsers">搜索</el-button>
              <el-button icon="Refresh" @click="resetUserSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <div class="table-wrapper">
          <div class="table-header">
            <el-button type="primary" icon="Plus" @click="handleAddUser">新增用户</el-button>
            <el-button icon="Download" @click="exportUsers">导出用户</el-button>
          </div>
          
          <el-table :data="userData" v-loading="userLoading">
            <el-table-column prop="username" label="用户名" width="110" />
            <el-table-column prop="realName" label="姓名" width="100" />
            <el-table-column prop="role" label="角色" width="120">
              <template #default="{ row }">
                <el-tag :type="getRoleTagType(row.role)">{{ row.role }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="department" label="部门" width="100" />
            <el-table-column prop="email" label="邮箱" min-width="160" />
            <el-table-column prop="phone" label="电话" width="130" />
            <el-table-column prop="lastLogin" label="最后登录" width="150" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-switch
                  v-model="row.status"
                  active-value="启用"
                  inactive-value="停用"
                  @change="handleUserStatusChange(row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="220">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleEditUser(row)">编辑</el-button>
                <el-button link type="warning" @click="resetPassword(row)">重置密码</el-button>
                <el-button link type="danger" @click="handleDeleteUser(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
      
      <!-- 角色管理 -->
      <el-tab-pane label="角色管理" name="role">
        <div class="table-wrapper">
          <div class="table-header">
            <el-button type="primary" icon="Plus" @click="handleAddRole">新增角色</el-button>
          </div>
          
          <el-table :data="roleData" v-loading="roleLoading">
            <el-table-column prop="name" label="角色名称" width="140" />
            <el-table-column prop="code" label="角色代码" width="140" />
            <el-table-column prop="description" label="描述" min-width="200" />
            <el-table-column prop="userCount" label="用户数量" width="100" align="center" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-switch
                  v-model="row.status"
                  active-value="启用"
                  inactive-value="停用"
                  @change="handleRoleStatusChange(row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" fixed="right" width="250">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleEditRole(row)">编辑</el-button>
                <el-button link type="success" @click="configPermission(row)">权限配置</el-button>
                <el-button link type="danger" @click="handleDeleteRole(row)" :disabled="row.userCount > 0">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
      
      <!-- 权限配置 -->
      <el-tab-pane label="权限配置" name="permission">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card header="角色列表">
              <el-tree
                :data="roleTreeData"
                node-key="id"
                :highlight-current="true"
                @node-click="selectRole"
                :props="{ label: 'name' }"
              />
            </el-card>
          </el-col>
          
          <el-col :span="18">
            <el-card header="权限设置" v-if="selectedRole">
              <div class="permission-header">
                <h4>{{ selectedRole.name }} - 权限配置</h4>
                <el-button type="primary" @click="savePermissions">保存权限</el-button>
              </div>
              
              <el-tree
                ref="permissionTreeRef"
                :data="permissionTreeData"
                node-key="id"
                show-checkbox
                :default-checked-keys="checkedPermissions"
                :props="{ label: 'name', children: 'children' }"
                @check="handlePermissionCheck"
              />
            </el-card>
            
            <el-empty v-else description="请选择一个角色" />
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 新增/编辑用户对话框 -->
    <el-dialog
      v-model="userDialog"
      :title="userDialogTitle"
      width="600px"
      @close="resetUserForm"
    >
      <el-form ref="userFormRef" :model="userForm" :rules="userRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="userForm.username" :disabled="!!userForm.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="userForm.realName" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="角色" prop="role">
              <el-select v-model="userForm.role" style="width: 100%">
                <el-option
                  v-for="role in roleList"
                  :key="role.code"
                  :label="role.name"
                  :value="role.code"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门" prop="department">
              <el-input v-model="userForm.department" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="userForm.email" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="userForm.phone" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" v-if="!userForm.id">
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="userForm.password" type="password" show-password />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="userForm.confirmPassword" type="password" show-password />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <el-button @click="userDialog = false">取消</el-button>
        <el-button type="primary" @click="saveUser">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 新增/编辑角色对话框 -->
    <el-dialog
      v-model="roleDialog"
      :title="roleDialogTitle"
      width="500px"
      @close="resetRoleForm"
    >
      <el-form ref="roleFormRef" :model="roleForm" :rules="roleRules" label-width="100px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="roleForm.name" />
        </el-form-item>
        <el-form-item label="角色代码" prop="code">
          <el-input v-model="roleForm.code" :disabled="!!roleForm.id" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="roleForm.description" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="roleDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRole">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { mockUsers, mockRoles } from '@/api/mockData'

const activeTab = ref('user')
const userLoading = ref(false)
const roleLoading = ref(false)
const userDialog = ref(false)
const roleDialog = ref(false)
const selectedRole = ref(null)
const permissionTreeRef = ref()

const userData = ref([])
const roleData = ref([])
const roleList = ref([])
const checkedPermissions = ref([])

const userSearchForm = ref({
  username: '',
  realName: '',
  role: ''
})

const userForm = ref({
  id: '',
  username: '',
  realName: '',
  role: '',
  department: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

const roleForm = ref({
  id: '',
  name: '',
  code: '',
  description: ''
})

const roleTreeData = ref([])
const permissionTreeData = ref([
  {
    id: 1,
    name: '基础设置',
    children: [
      { id: 11, name: '科目管理' },
      { id: 12, name: '辅助核算' },
      { id: 13, name: '期初余额' },
      { id: 14, name: '币种汇率' },
      { id: 15, name: '系统参数' },
      { id: 16, name: '组织架构' },
      { id: 17, name: '用户权限' }
    ]
  },
  {
    id: 2,
    name: '日常核算',
    children: [
      { id: 21, name: '凭证录入' },
      { id: 22, name: '凭证审核' },
      { id: 23, name: '凭证记账' },
      { id: 24, name: '凭证查询' },
      { id: 25, name: '账簿查询' },
      { id: 26, name: '期末结账' }
    ]
  },
  {
    id: 3,
    name: '报表管理',
    children: [
      { id: 31, name: '资产负债表' },
      { id: 32, name: '利润表' },
      { id: 33, name: '现金流量表' },
      { id: 34, name: '财务指标' }
    ]
  }
])

const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== userForm.value.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const roleRules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入角色代码', trigger: 'blur' }
  ]
}

const userDialogTitle = computed(() => {
  return userForm.value.id ? '编辑用户' : '新增用户'
})

const roleDialogTitle = computed(() => {
  return roleForm.value.id ? '编辑角色' : '新增角色'
})

onMounted(() => {
  loadUserData()
  loadRoleData()
})

const loadUserData = () => {
  userLoading.value = true
  setTimeout(() => {
    userData.value = mockUsers
    userLoading.value = false
  }, 500)
}

const loadRoleData = () => {
  roleLoading.value = true
  setTimeout(() => {
    roleData.value = mockRoles
    roleList.value = mockRoles.map(item => ({
      name: item.name,
      code: item.code
    }))
    roleTreeData.value = mockRoles.map(item => ({
      id: item.id,
      name: item.name,
      code: item.code
    }))
    roleLoading.value = false
  }, 500)
}

const getRoleTagType = (role) => {
  const typeMap = {
    '超级管理员': 'danger',
    '财务主管': 'warning',
    '会计': 'success',
    '出纳': 'info',
    '审计员': ''
  }
  return typeMap[role] || ''
}

const searchUsers = () => {
  loadUserData()
  ElMessage.success('搜索完成')
}

const resetUserSearch = () => {
  userSearchForm.value = {
    username: '',
    realName: '',
    role: ''
  }
  loadUserData()
}

const handleAddUser = () => {
  userForm.value = {
    id: '',
    username: '',
    realName: '',
    role: '',
    department: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: ''
  }
  userDialog.value = true
}

const handleEditUser = (row) => {
  userForm.value = { ...row, password: '', confirmPassword: '' }
  userDialog.value = true
}

const handleDeleteUser = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户"${row.realName}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    ElMessage.success('删除成功')
    loadUserData()
  } catch {
    // 取消删除
  }
}

const handleUserStatusChange = (row) => {
  ElMessage.success(`用户"${row.realName}"状态已更新`)
}

const resetPassword = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置用户"${row.realName}"的密码吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    ElMessage.success('密码重置成功，新密码为：123456')
  } catch {
    // 取消重置
  }
}

const saveUser = async () => {
  const valid = await userFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  userDialog.value = false
  ElMessage.success(userForm.value.id ? '编辑成功' : '新增成功')
  loadUserData()
}

const resetUserForm = () => {
  userFormRef.value?.resetFields()
}

const exportUsers = () => {
  ElMessage.success('用户导出功能开发中')
}

const handleAddRole = () => {
  roleForm.value = {
    id: '',
    name: '',
    code: '',
    description: ''
  }
  roleDialog.value = true
}

const handleEditRole = (row) => {
  roleForm.value = { ...row }
  roleDialog.value = true
}

const handleDeleteRole = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除角色"${row.name}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    ElMessage.success('删除成功')
    loadRoleData()
  } catch {
    // 取消删除
  }
}

const handleRoleStatusChange = (row) => {
  ElMessage.success(`角色"${row.name}"状态已更新`)
}

const saveRole = async () => {
  const valid = await roleFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  roleDialog.value = false
  ElMessage.success(roleForm.value.id ? '编辑成功' : '新增成功')
  loadRoleData()
}

const resetRoleForm = () => {
  roleFormRef.value?.resetFields()
}

const configPermission = (row) => {
  activeTab.value = 'permission'
  selectedRole.value = row
  loadRolePermissions(row.id)
}

const selectRole = (data) => {
  selectedRole.value = data
  loadRolePermissions(data.id)
}

const loadRolePermissions = (roleId) => {
  // 模拟加载角色权限
  if (roleId === 1) { // 超级管理员
    checkedPermissions.value = [11, 12, 13, 14, 15, 16, 17, 21, 22, 23, 24, 25, 26, 31, 32, 33, 34]
  } else if (roleId === 2) { // 财务主管
    checkedPermissions.value = [11, 12, 13, 14, 21, 22, 23, 24, 25, 26, 31, 32, 33, 34]
  } else {
    checkedPermissions.value = [21, 24, 25]
  }
}

const handlePermissionCheck = () => {
  // 权限选择变化处理
}

const savePermissions = () => {
  const checkedKeys = permissionTreeRef.value.getCheckedKeys()
  ElMessage.success(`权限保存成功，共选择了${checkedKeys.length}个权限`)
}
</script>

<style scoped lang="scss">
.permission-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  h4 {
    margin: 0;
    color: #303133;
  }
}
</style>