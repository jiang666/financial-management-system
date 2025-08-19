<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="title">组织架构</h2>
      <p class="description">管理公司组织架构，设置部门层级和人员分配</p>
    </div>
    
    <el-row :gutter="20">
      <el-col :span="8">
        <div class="tree-panel">
          <div class="panel-header">
            <h3>组织架构树</h3>
            <el-button type="primary" size="small" icon="Plus" @click="handleAddRoot">
              添加根部门
            </el-button>
          </div>
          <el-tree
            :data="treeData"
            :props="treeProps"
            node-key="id"
            default-expand-all
            :expand-on-click-node="false"
            @node-click="handleNodeClick"
            class="organization-tree"
          >
            <template #default="{ node, data }">
              <div class="tree-node">
                <div class="node-content">
                  <el-icon v-if="data.type === '公司'"><OfficeBuilding /></el-icon>
                  <el-icon v-else-if="data.type === '部门'"><Postcard /></el-icon>
                  <el-icon v-else><UserFilled /></el-icon>
                  <span class="node-label">{{ data.name }}</span>
                  <span class="node-count">({{ data.employeeCount }}人)</span>
                </div>
                <div class="node-actions">
                  <el-button link type="primary" size="small" @click.stop="handleAddChild(data)">
                    <el-icon><Plus /></el-icon>
                  </el-button>
                  <el-button link type="primary" size="small" @click.stop="handleEdit(data)">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                  <el-button link type="danger" size="small" @click.stop="handleDelete(data)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </template>
          </el-tree>
        </div>
      </el-col>
      
      <el-col :span="16">
        <div class="detail-panel">
          <el-tabs v-model="activeTab" type="border-card">
            <el-tab-pane label="部门信息" name="department">
              <el-form
                v-if="selectedNode"
                ref="deptFormRef"
                :model="deptForm"
                :rules="deptRules"
                label-width="100px"
              >
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="部门名称" prop="name">
                      <el-input v-model="deptForm.name" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="部门类型" prop="type">
                      <el-select v-model="deptForm.type" style="width: 100%">
                        <el-option label="公司" value="公司" />
                        <el-option label="部门" value="部门" />
                        <el-option label="小组" value="小组" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="上级部门">
                      <el-tree-select
                        v-model="deptForm.parentId"
                        :data="treeData"
                        :props="treeProps"
                        node-key="id"
                        check-strictly
                        placeholder="选择上级部门"
                        style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="负责人" prop="manager">
                      <el-input v-model="deptForm.manager" />
                    </el-form-item>
                  </el-col>
                </el-row>
                
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="联系电话">
                      <el-input v-model="deptForm.phone" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="状态">
                      <el-switch
                        v-model="deptForm.status"
                        active-value="启用"
                        inactive-value="停用"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
                
                <el-form-item label="部门职责">
                  <el-input v-model="deptForm.description" type="textarea" rows="3" />
                </el-form-item>
                
                <el-form-item>
                  <el-button type="primary" @click="saveDepartment">保存</el-button>
                  <el-button @click="resetDeptForm">重置</el-button>
                </el-form-item>
              </el-form>
              
              <el-empty v-else description="请选择一个部门" />
            </el-tab-pane>
            
            <el-tab-pane label="岗位设置" name="position">
              <div v-if="selectedNode">
                <div class="position-header">
                  <el-button type="primary" icon="Plus" @click="handleAddPosition">
                    新增岗位
                  </el-button>
                </div>
                
                <el-table :data="positionData" border>
                  <el-table-column prop="name" label="岗位名称" width="150" />
                  <el-table-column prop="level" label="岗位等级" width="120">
                    <template #default="{ row }">
                      <el-tag :type="getLevelTagType(row.level)">{{ row.level }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="count" label="编制人数" width="100" />
                  <el-table-column prop="actualCount" label="实际人数" width="100" />
                  <el-table-column prop="salary" label="基础薪资" width="120" align="right">
                    <template #default="{ row }">
                      {{ formatMoney(row.salary) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="{ row }">
                      <el-switch
                        v-model="row.status"
                        active-value="启用"
                        inactive-value="停用"
                      />
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="150">
                    <template #default="{ row }">
                      <el-button link type="primary" @click="editPosition(row)">编辑</el-button>
                      <el-button link type="danger" @click="deletePosition(row)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
              
              <el-empty v-else description="请选择一个部门" />
            </el-tab-pane>
            
            <el-tab-pane label="人员分配" name="employee">
              <div v-if="selectedNode">
                <div class="employee-header">
                  <el-button type="primary" icon="Plus" @click="handleAddEmployee">
                    分配员工
                  </el-button>
                </div>
                
                <el-table :data="employeeData" border>
                  <el-table-column prop="name" label="姓名" width="100" />
                  <el-table-column prop="position" label="岗位" width="120" />
                  <el-table-column prop="employeeId" label="工号" width="120" />
                  <el-table-column prop="phone" label="联系电话" width="150" />
                  <el-table-column prop="email" label="邮箱" width="180" />
                  <el-table-column prop="hireDate" label="入职日期" width="120" />
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="{ row }">
                      <el-tag :type="row.status === '在职' ? 'success' : 'info'">
                        {{ row.status }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="150">
                    <template #default="{ row }">
                      <el-button link type="primary" @click="editEmployee(row)">编辑</el-button>
                      <el-button link type="warning" @click="transferEmployee(row)">调动</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
              
              <el-empty v-else description="请选择一个部门" />
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-col>
    </el-row>
    
    <!-- 新增/编辑部门对话框 -->
    <el-dialog
      v-model="deptDialog"
      :title="deptDialogTitle"
      width="500px"
      @close="resetDeptForm"
    >
      <el-form ref="dialogDeptFormRef" :model="dialogDeptForm" :rules="deptRules" label-width="100px">
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="dialogDeptForm.name" />
        </el-form-item>
        <el-form-item label="部门类型" prop="type">
          <el-select v-model="dialogDeptForm.type" style="width: 100%">
            <el-option label="公司" value="公司" />
            <el-option label="部门" value="部门" />
            <el-option label="小组" value="小组" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人" prop="manager">
          <el-input v-model="dialogDeptForm.manager" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="dialogDeptForm.phone" />
        </el-form-item>
        <el-form-item label="部门职责">
          <el-input v-model="dialogDeptForm.description" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deptDialog = false">取消</el-button>
        <el-button type="primary" @click="saveDeptDialog">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 岗位设置对话框 -->
    <el-dialog v-model="positionDialog" title="岗位设置" width="500px">
      <el-form ref="positionFormRef" :model="positionForm" label-width="100px">
        <el-form-item label="岗位名称" prop="name">
          <el-input v-model="positionForm.name" />
        </el-form-item>
        <el-form-item label="岗位等级" prop="level">
          <el-select v-model="positionForm.level" style="width: 100%">
            <el-option label="高级" value="高级" />
            <el-option label="中级" value="中级" />
            <el-option label="初级" value="初级" />
          </el-select>
        </el-form-item>
        <el-form-item label="编制人数" prop="count">
          <el-input-number v-model="positionForm.count" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="基础薪资" prop="salary">
          <el-input-number v-model="positionForm.salary" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="岗位职责">
          <el-input v-model="positionForm.description" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="positionDialog = false">取消</el-button>
        <el-button type="primary" @click="savePosition">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { mockOrganizations } from '@/api/mockData'

const activeTab = ref('department')
const selectedNode = ref(null)
const deptDialog = ref(false)
const positionDialog = ref(false)
const deptFormRef = ref()
const dialogDeptFormRef = ref()
const positionFormRef = ref()

const treeData = ref([])
const positionData = ref([])
const employeeData = ref([])

const treeProps = {
  children: 'children',
  label: 'name'
}

const deptForm = ref({
  id: '',
  name: '',
  type: '部门',
  parentId: null,
  manager: '',
  phone: '',
  status: '启用',
  description: ''
})

const dialogDeptForm = ref({
  id: '',
  name: '',
  type: '部门',
  parentId: null,
  manager: '',
  phone: '',
  description: ''
})

const positionForm = ref({
  id: '',
  name: '',
  level: '中级',
  count: 1,
  salary: 0,
  description: ''
})

const deptRules = {
  name: [
    { required: true, message: '请输入部门名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择部门类型', trigger: 'change' }
  ],
  manager: [
    { required: true, message: '请输入负责人', trigger: 'blur' }
  ]
}

const deptDialogTitle = computed(() => {
  return dialogDeptForm.value.id ? '编辑部门' : '新增部门'
})

onMounted(() => {
  loadTreeData()
})

const loadTreeData = () => {
  // 构建树形数据
  const tree = buildTree(mockOrganizations)
  treeData.value = tree
}

const buildTree = (data) => {
  const tree = []
  const map = {}
  
  data.forEach(item => {
    map[item.id] = { ...item, children: [] }
  })
  
  data.forEach(item => {
    if (item.parentId && map[item.parentId]) {
      map[item.parentId].children.push(map[item.id])
    } else if (!item.parentId) {
      tree.push(map[item.id])
    }
  })
  
  return tree
}

const handleNodeClick = (data) => {
  selectedNode.value = data
  deptForm.value = { ...data, status: data.status || '启用' }
  loadPositionData(data.id)
  loadEmployeeData(data.id)
}

const loadPositionData = (deptId) => {
  // 模拟岗位数据
  positionData.value = [
    { id: 1, name: '部门经理', level: '高级', count: 1, actualCount: 1, salary: 15000, status: '启用' },
    { id: 2, name: '主管', level: '中级', count: 2, actualCount: 1, salary: 10000, status: '启用' },
    { id: 3, name: '专员', level: '初级', count: 5, actualCount: 3, salary: 6000, status: '启用' }
  ]
}

const loadEmployeeData = (deptId) => {
  // 模拟员工数据
  employeeData.value = [
    { id: 1, name: '张三', position: '部门经理', employeeId: 'EMP001', phone: '13800138001', email: 'zhang@example.com', hireDate: '2023-01-15', status: '在职' },
    { id: 2, name: '李四', position: '专员', employeeId: 'EMP002', phone: '13800138002', email: 'li@example.com', hireDate: '2023-03-20', status: '在职' },
    { id: 3, name: '王五', position: '专员', employeeId: 'EMP003', phone: '13800138003', email: 'wang@example.com', hireDate: '2023-06-10', status: '在职' }
  ]
}

const getLevelTagType = (level) => {
  const typeMap = {
    '高级': 'danger',
    '中级': 'warning',
    '初级': 'success'
  }
  return typeMap[level] || ''
}

const formatMoney = (amount) => {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY'
  }).format(amount)
}

const handleAddRoot = () => {
  dialogDeptForm.value = {
    id: '',
    name: '',
    type: '公司',
    parentId: null,
    manager: '',
    phone: '',
    description: ''
  }
  deptDialog.value = true
}

const handleAddChild = (data) => {
  dialogDeptForm.value = {
    id: '',
    name: '',
    type: '部门',
    parentId: data.id,
    manager: '',
    phone: '',
    description: ''
  }
  deptDialog.value = true
}

const handleEdit = (data) => {
  dialogDeptForm.value = { ...data }
  deptDialog.value = true
}

const handleDelete = async (data) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除"${data.name}"吗？删除后子部门也将被删除。`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    ElMessage.success('删除成功')
    loadTreeData()
  } catch {
    // 取消删除
  }
}

const saveDepartment = async () => {
  const valid = await deptFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  ElMessage.success('部门信息保存成功')
  loadTreeData()
}

const resetDeptForm = () => {
  deptFormRef.value?.resetFields()
}

const saveDeptDialog = async () => {
  const valid = await dialogDeptFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  deptDialog.value = false
  ElMessage.success(dialogDeptForm.value.id ? '编辑成功' : '新增成功')
  loadTreeData()
}

const handleAddPosition = () => {
  positionForm.value = {
    id: '',
    name: '',
    level: '中级',
    count: 1,
    salary: 0,
    description: ''
  }
  positionDialog.value = true
}

const editPosition = (row) => {
  positionForm.value = { ...row }
  positionDialog.value = true
}

const deletePosition = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除岗位"${row.name}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    ElMessage.success('删除成功')
    loadPositionData(selectedNode.value.id)
  } catch {
    // 取消删除
  }
}

const savePosition = () => {
  positionDialog.value = false
  ElMessage.success(positionForm.value.id ? '编辑成功' : '新增成功')
  loadPositionData(selectedNode.value.id)
}

const handleAddEmployee = () => {
  ElMessage.info('员工分配功能开发中')
}

const editEmployee = (row) => {
  ElMessage.info(`编辑员工：${row.name}`)
}

const transferEmployee = (row) => {
  ElMessage.info(`调动员工：${row.name}`)
}
</script>

<style scoped lang="scss">
.tree-panel {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  height: 600px;
  
  .panel-header {
    padding: 15px;
    border-bottom: 1px solid #e4e7ed;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    h3 {
      margin: 0;
      font-size: 16px;
      color: #303133;
    }
  }
  
  .organization-tree {
    padding: 15px;
    height: calc(100% - 70px);
    overflow-y: auto;
    
    .tree-node {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;
      
      .node-content {
        display: flex;
        align-items: center;
        flex: 1;
        
        .el-icon {
          margin-right: 5px;
          color: #409EFF;
        }
        
        .node-label {
          margin-right: 8px;
        }
        
        .node-count {
          font-size: 12px;
          color: #909399;
        }
      }
      
      .node-actions {
        opacity: 0;
        transition: opacity 0.3s;
        
        .el-button {
          padding: 2px;
        }
      }
      
      &:hover .node-actions {
        opacity: 1;
      }
    }
  }
}

.detail-panel {
  .position-header,
  .employee-header {
    margin-bottom: 15px;
  }
}

.el-tabs {
  height: 600px;
  
  .el-tab-pane {
    height: calc(100% - 40px);
    overflow-y: auto;
  }
}
</style>