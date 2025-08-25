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
            <el-button icon="Download" @click="handleExportUsers">导出用户</el-button>
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
                <el-button link type="success" @click="configurePermissions(row)">权限配置</el-button>
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
            <el-form-item label="角色" prop="roleId">
              <el-select v-model="userForm.roleId" style="width: 100%">
                <el-option
                  v-for="role in roleTreeData"
                  :key="role.id"
                  :label="role.name"
                  :value="role.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门" prop="departmentId">
              <el-tree-select
                v-model="userForm.departmentId"
                :data="departmentTree"
                :props="{ label: 'name', value: 'id' }"
                placeholder="请选择部门"
                style="width: 100%"
                @change="handleDepartmentChange"
              />
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
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="userForm.status" style="width: 100%">
                <el-option label="启用" value="启用" />
                <el-option label="停用" value="停用" />
              </el-select>
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
        <el-form-item label="状态" prop="status">
          <el-select v-model="roleForm.status" style="width: 100%">
            <el-option label="启用" value="启用" />
            <el-option label="停用" value="停用" />
          </el-select>
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
import { ref, onMounted, computed, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getUserList, 
  getUserDetail, 
  createUser, 
  updateUser, 
  deleteUser, 
  resetUserPassword, 
  updateUserStatus,
  assignUserRoles,
  exportUsers
} from '@/api/users'
import { 
  getRoleList, 
  getAllRoles, 
  createRole, 
  updateRole, 
  deleteRole, 
  updateRoleStatus,
  configRolePermissions,
  getRolePermissions
} from '@/api/roles'
import { getResourceTree } from '@/api/resources'
import { departmentApi } from '@/api/organization'

const activeTab = ref('user')
const userLoading = ref(false)
const roleLoading = ref(false)
const userDialog = ref(false)
const roleDialog = ref(false)
const selectedRole = ref(null)
const permissionTreeRef = ref()
const userFormRef = ref()
const roleFormRef = ref()

const userData = ref([])
const roleData = ref([])
const roleList = ref([])
const checkedPermissions = ref([])
const departmentTree = ref([])
const departmentOptions = ref([])

const userSearchForm = ref({
  username: '',
  realName: '',
  role: ''
})

const userForm = ref({
  id: '',
  username: '',
  realName: '',
  roleId: '',
  department: '',
  departmentId: '',
  email: '',
  phone: '',
  status: '启用',
  password: '',
  confirmPassword: ''
})

const roleForm = ref({
  id: '',
  name: '',
  code: '',
  description: '',
  status: '启用'
})

const roleTreeData = ref([])
const permissionTreeData = ref([])

const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  roleId: [
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
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
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

const roleRules = computed(() => ({
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ],
  code: roleForm.value.id ? [] : [
    { required: true, message: '请输入角色代码', trigger: 'blur' }
  ]
}))

const userDialogTitle = computed(() => {
  return userForm.value.id ? '编辑用户' : '新增用户'
})

const roleDialogTitle = computed(() => {
  return roleForm.value.id ? '编辑角色' : '新增角色'
})

onMounted(async () => {
  // 只加载默认激活标签页的数据
  if (activeTab.value === 'user') {
    loadUserData()
    // 用户管理页面需要角色选项和部门选项
    await loadRoleOptions()
    await loadDepartmentTree()
  } else if (activeTab.value === 'role') {
    loadRoleData()
  } else if (activeTab.value === 'permission') {
    loadResourceTree()
  }
})

// 监听标签页切换，每次切换都加载对应数据
watch(activeTab, async (newTab, oldTab) => {
  if (newTab === 'user') {
    loadUserData()
    // 用户管理页面需要角色选项和部门选项
    if (!roleTreeData.value || roleTreeData.value.length === 0) {
      await loadRoleOptions()
    }
    if (!departmentTree.value || departmentTree.value.length === 0) {
      await loadDepartmentTree()
    }
  } else if (newTab === 'role') {
    loadRoleData()
  } else if (newTab === 'permission') {
    loadResourceTree()
  }
})

const loadDepartmentTree = async () => {
  try {
    const res = await departmentApi.getDepartmentTree()
    if (res.code === 200) {
      departmentTree.value = res.data || []
    }
  } catch (error) {
    console.error('获取部门树失败:', error)
    ElMessage.error('获取部门列表失败')
  }
}

const handleDepartmentChange = (departmentId) => {
  // 找到选中的部门
  const findDepartment = (nodes, id) => {
    for (const node of nodes) {
      if (node.id === id) {
        return node
      }
      if (node.children) {
        const found = findDepartment(node.children, id)
        if (found) return found
      }
    }
    return null
  }
  
  const dept = findDepartment(departmentTree.value, departmentId)
  if (dept) {
    userForm.value.department = dept.name
  }
}

const loadResourceTree = async () => {
  try {
    const res = await getResourceTree()
    if (res.code === 200) {
      // 过滤出菜单和操作权限，不包括API接口权限
      permissionTreeData.value = filterResourceTree(res.data)
    }
  } catch (error) {
    ElMessage.error('获取资源树失败')
  }
}

const filterResourceTree = (resources) => {
  return resources.map(resource => {
    const node = {
      id: resource.id,
      name: resource.resourceName,
      children: []
    }
    
    if (resource.children && resource.children.length > 0) {
      // 过滤掉interface类型的资源
      const filteredChildren = resource.children.filter(child => child.resourceType !== 'interface')
      if (filteredChildren.length > 0) {
        node.children = filterResourceTree(filteredChildren)
      }
    }
    
    return node
  }).filter(node => node.children.length > 0 || node.id) // 移除没有子节点的空节点
}

const loadUserData = async () => {
  userLoading.value = true
  try {
    const res = await getUserList({
      page: 0,
      size: 10,
      ...userSearchForm.value
    })
    if (res.code === 200) {
      userData.value = res.data.content.map(user => ({
        ...user,
        role: user.roles?.[0]?.name || '',
        roleCode: user.roles?.[0]?.roleCode || '',
        status: user.status === 'ACTIVE' ? '启用' : '停用',
        lastLogin: user.lastLoginAt ? new Date(user.lastLoginAt).toLocaleString() : '从未登录'
      }))
    }
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    userLoading.value = false
  }
}

const loadRoleOptions = async () => {
  try {
    // 加载所有角色用于下拉选择
    const allRolesRes = await getAllRoles()
    if (allRolesRes.code === 200) {
      roleList.value = allRolesRes.data.map(item => ({
        name: item.name,
        code: item.roleCode
      }))
      roleTreeData.value = allRolesRes.data.map(item => ({
        id: item.id,
        name: item.name,
        code: item.roleCode
      }))
    }
  } catch (error) {
    ElMessage.error('获取角色选项失败')
  }
}

const loadRoleData = async () => {
  roleLoading.value = true
  try {
    const res = await getRoleList({
      page: 0,
      size: 10
    })
    if (res.code === 200) {
      roleData.value = res.data.content.map(role => ({
        ...role,
        status: role.status === 'ACTIVE' ? '启用' : '停用'
      }))
    }
    
    // 如果角色选项还没有加载，也加载一下
    if (!roleTreeData.value || roleTreeData.value.length === 0) {
      await loadRoleOptions()
    }
  } catch (error) {
    ElMessage.error('获取角色列表失败')
  } finally {
    roleLoading.value = false
  }
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
    roleId: '',
    department: '',
    departmentId: '',
    email: '',
    phone: '',
    status: '启用',
    password: '',
    confirmPassword: ''
  }
  userDialog.value = true
}

const handleEditUser = (row) => {
  userForm.value = { 
    ...row, 
    roleId: row.roles?.[0]?.id || '',
    departmentId: row.departmentId || '',
    status: row.status || '启用',
    password: '', 
    confirmPassword: '' 
  }
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
    const res = await deleteUser(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadUserData()
    }
  } catch {
    // 取消删除
  }
}

const handleUserStatusChange = async (row) => {
  try {
    const status = row.status === '启用' ? 'ACTIVE' : 'INACTIVE'
    const res = await updateUserStatus(row.id, status)
    if (res.code === 200) {
      ElMessage.success(`用户"${row.realName}"状态已更新`)
    }
  } catch (error) {
    ElMessage.error('状态更新失败')
    // 恢复原状态
    row.status = row.status === '启用' ? '停用' : '启用'
  }
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
    const res = await resetUserPassword(row.id, '123456')
    if (res.code === 200) {
      ElMessage.success('密码重置成功，新密码为：123456')
    }
  } catch {
    // 取消重置
  }
}

const saveUser = async () => {
  const valid = await userFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  try {
    const data = {
      username: userForm.value.username,
      realName: userForm.value.realName,
      email: userForm.value.email,
      phone: userForm.value.phone,
      department: userForm.value.department,
      departmentId: userForm.value.departmentId,
      position: userForm.value.department // 使用部门作为职位
    }
    
    let res
    if (userForm.value.id) {
      // 编辑用户时需要添加必填的 status 字段和角色ID
      data.status = userForm.value.status === '启用' ? 'ACTIVE' : 'INACTIVE'
      // 如果选择了角色，包含角色ID数组
      if (userForm.value.roleId) {
        data.roleIds = [userForm.value.roleId]
      }
      res = await updateUser(userForm.value.id, data)
    } else {
      data.password = userForm.value.password
      res = await createUser(data)
    }
    
    if (res.code === 200) {
      // 如果创建成功并且选择了角色，分配角色
      if (!userForm.value.id && userForm.value.roleId && res.data.id) {
        await assignUserRoles(res.data.id, [userForm.value.roleId])
      }
      
      userDialog.value = false
      ElMessage.success(userForm.value.id ? '编辑成功' : '新增成功')
      loadUserData()
    }
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const resetUserForm = () => {
  userFormRef.value?.resetFields()
}

const handleExportUsers = async () => {
  try {
    const res = await exportUsers(userSearchForm.value)
    // 下载文件
    const blob = new Blob([res.data], { type: 'application/vnd.ms-excel' })
    const link = document.createElement('a')
    link.href = window.URL.createObjectURL(blob)
    link.download = `用户列表_${new Date().toLocaleDateString()}.xlsx`
    link.click()
    window.URL.revokeObjectURL(link.href)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const handleAddRole = () => {
  roleForm.value = {
    id: '',
    name: '',
    code: '',
    description: '',
    status: '启用'
  }
  roleDialog.value = true
}

const handleEditRole = (row) => {
  roleForm.value = { 
    ...row,
    status: row.status || '启用'
  }
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
    const res = await deleteRole(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadRoleData()
    }
  } catch {
    // 取消删除
  }
}

const handleRoleStatusChange = async (row) => {
  try {
    const status = row.status === '启用' ? 'ACTIVE' : 'INACTIVE'
    const res = await updateRoleStatus(row.id, status)
    if (res.code === 200) {
      ElMessage.success(`角色"${row.name}"状态已更新`)
    }
  } catch (error) {
    ElMessage.error('状态更新失败')
    // 恢复原状态
    row.status = row.status === '启用' ? '停用' : '启用'
  }
}

const saveRole = async () => {
  const valid = await roleFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  try {
    const data = {
      name: roleForm.value.name,
      roleCode: roleForm.value.code,
      description: roleForm.value.description
    }
    
    let res
    if (roleForm.value.id) {
      // 编辑时需要添加状态字段
      data.status = roleForm.value.status === '启用' ? 'ACTIVE' : 'INACTIVE'
      res = await updateRole(roleForm.value.id, data)
    } else {
      res = await createRole(data)
    }
    
    if (res.code === 200) {
      roleDialog.value = false
      ElMessage.success(roleForm.value.id ? '编辑成功' : '新增成功')
      loadRoleData()
    }
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const resetRoleForm = () => {
  roleFormRef.value?.resetFields()
}

const configurePermissions = async (row) => {
  activeTab.value = 'permission'
  selectedRole.value = row
  // 确保权限树数据已加载
  if (permissionTreeData.value.length === 0) {
    await loadResourceTree()
  }
  // 等待下一个tick确保DOM已更新
  await nextTick()
  loadRolePermissions(row.id)
}

const selectRole = async (data) => {
  selectedRole.value = data
  // 确保权限树数据已加载
  if (permissionTreeData.value.length === 0) {
    await loadResourceTree()
  }
  // 等待下一个tick确保DOM已更新
  await nextTick()
  loadRolePermissions(data.id)
}

const loadRolePermissions = async (roleId) => {
  try {
    const res = await getRolePermissions(roleId)
    if (res.code === 200) {
      // 后端返回的是资源ID数组，直接赋值给checkedPermissions
      checkedPermissions.value = res.data || []
      // 等待权限树渲染完成后设置选中状态
      await nextTick()
      setTimeout(() => {
        if (permissionTreeRef.value) {
          permissionTreeRef.value.setCheckedKeys(checkedPermissions.value)
        }
      }, 100)
    }
  } catch (error) {
    ElMessage.error('获取角色权限失败')
  }
}

const handlePermissionCheck = () => {
  // 权限选择变化处理
}

const savePermissions = async () => {
  try {
    const checkedKeys = permissionTreeRef.value.getCheckedKeys()
    const res = await configRolePermissions(selectedRole.value.id, checkedKeys)
    if (res.code === 200) {
      ElMessage.success(`权限保存成功，共选择了${checkedKeys.length}个权限`)
    }
  } catch (error) {
    ElMessage.error('权限保存失败')
  }
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