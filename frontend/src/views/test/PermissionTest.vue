<template>
  <div class="permission-test">
    <el-card header="权限测试页面">
      <div class="user-info">
        <h3>当前用户信息</h3>
        <p><strong>用户名:</strong> {{ user.username }}</p>
        <p><strong>真实姓名:</strong> {{ user.realName }}</p>
        <p><strong>邮箱:</strong> {{ user.email }}</p>
        <p><strong>部门:</strong> {{ user.department }}</p>
        <p><strong>职位:</strong> {{ user.position }}</p>
        <p><strong>状态:</strong> {{ user.status }}</p>
      </div>
      
      <el-divider />
      
      <div class="authorities">
        <h3>用户权限列表</h3>
        <el-tag v-for="authority in authorities" :key="authority" style="margin: 4px;">
          {{ authority }}
        </el-tag>
      </div>
      
      <el-divider />
      
      <div class="resources">
        <h3>用户资源权限</h3>
        <el-tree
          :data="resourcesTree"
          :props="{ label: 'resourceName', children: 'children' }"
          default-expand-all
          node-key="id"
        >
          <template #default="{ node, data }">
            <span>
              {{ data.resourceName }}
              <el-tag size="small" type="info" style="margin-left: 8px;">
                {{ data.resourceType }}
              </el-tag>
              <el-tag size="small" type="success" style="margin-left: 4px;" v-if="data.resourceCode">
                {{ data.resourceCode }}
              </el-tag>
            </span>
          </template>
        </el-tree>
      </div>
      
      <el-divider />
      
      <div class="permission-buttons">
        <h3>权限按钮测试</h3>
        <div style="margin: 10px 0;">
          <PermissionButton permission="ROLE_ADMIN">
            <el-button type="primary">超级管理员按钮</el-button>
          </PermissionButton>
          
          <PermissionButton permission="USER_ADD">
            <el-button type="success">新增用户按钮</el-button>
          </PermissionButton>
          
          <PermissionButton permission="USER_EDIT">
            <el-button type="warning">编辑用户按钮</el-button>
          </PermissionButton>
          
          <PermissionButton permission="USER_DELETE">
            <el-button type="danger">删除用户按钮</el-button>
          </PermissionButton>
          
          <PermissionButton permission="ROLE_ADD">
            <el-button type="info">新增角色按钮</el-button>
          </PermissionButton>
        </div>
        
        <div style="margin: 10px 0;">
          <p>以下按钮测试不存在的权限（应该不显示）：</p>
          <PermissionButton permission="NON_EXISTENT_PERMISSION">
            <el-button type="primary">不存在权限的按钮</el-button>
          </PermissionButton>
        </div>
      </div>
      
      <el-divider />
      
      <div class="actions">
        <el-button @click="refreshUserInfo">刷新用户信息</el-button>
        <el-button @click="refreshResources">刷新权限资源</el-button>
        <el-button type="danger" @click="handleLogout">退出登录</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { PermissionButton } from '@/components/Permission'

const store = useStore()
const router = useRouter()

const user = computed(() => store.getters.user)
const authorities = computed(() => store.getters.authorities)
const resources = computed(() => store.getters.resources)

const resourcesTree = computed(() => {
  if (!resources.value) return []
  // 如果resources是数组，直接返回；否则包装成数组
  return Array.isArray(resources.value) ? resources.value : [resources.value]
})

const refreshUserInfo = async () => {
  try {
    await store.dispatch('getUserInfo')
    ElMessage.success('用户信息刷新成功')
  } catch (error) {
    ElMessage.error('刷新用户信息失败')
  }
}

const refreshResources = async () => {
  try {
    await store.dispatch('getUserResources')
    ElMessage.success('权限资源刷新成功')
  } catch (error) {
    ElMessage.error('刷新权限资源失败')
  }
}

const handleLogout = async () => {
  try {
    await store.dispatch('logout')
    ElMessage.success('退出成功')
    router.push('/login')
  } catch (error) {
    ElMessage.error('退出失败')
  }
}
</script>

<style scoped lang="scss">
.permission-test {
  padding: 20px;
  
  .user-info {
    p {
      margin: 8px 0;
      line-height: 1.6;
    }
  }
  
  .authorities {
    .el-tag {
      margin-right: 8px;
      margin-bottom: 8px;
    }
  }
  
  .resources {
    .el-tree {
      margin-top: 10px;
    }
  }
  
  .permission-buttons {
    .el-button {
      margin-right: 10px;
      margin-bottom: 10px;
    }
  }
  
  .actions {
    .el-button {
      margin-right: 10px;
    }
  }
}
</style>