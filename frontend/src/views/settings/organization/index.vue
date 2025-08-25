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
                  <el-table-column prop="realName" label="姓名" width="100" />
                  <el-table-column prop="positionName" label="岗位" width="120" />
                  <el-table-column prop="employeeId" label="工号" width="120" />
                  <el-table-column prop="phone" label="联系电话" width="150" />
                  <el-table-column prop="email" label="邮箱" width="180" />
                  <el-table-column prop="hireDate" label="入职日期" width="120">
                    <template #default="{ row }">
                      {{ formatDate(row.hireDate) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="{ row }">
                      <el-tag :type="getStatusType(row.status)">
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
    
    <!-- 分配员工对话框 -->
    <el-dialog v-model="assignEmployeeDialog" title="分配员工" width="600px">
      <el-form ref="assignFormRef" :model="assignForm" label-width="100px">
        <el-form-item label="选择员工" prop="userId">
          <el-select 
            v-model="assignForm.userId" 
            filterable 
            placeholder="请选择员工"
            style="width: 100%"
            @change="handleEmployeeSelect"
          >
            <el-option
              v-for="emp in availableEmployees"
              :key="emp.id"
              :label="`${emp.realName} (${emp.username})`"
              :value="emp.id"
            >
              <div style="display: flex; justify-content: space-between;">
                <span>{{ emp.realName }}</span>
                <span style="color: #8492a6; font-size: 13px">{{ emp.username }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="选择岗位" prop="positionId">
          <el-select 
            v-model="assignForm.positionId" 
            placeholder="请选择岗位（可选）"
            style="width: 100%"
          >
            <el-option
              v-for="pos in departmentPositions"
              :key="pos.id"
              :label="pos.name"
              :value="pos.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="员工信息" v-if="selectedEmployeeInfo">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="姓名">{{ selectedEmployeeInfo.realName }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ selectedEmployeeInfo.username }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ selectedEmployeeInfo.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="电话">{{ selectedEmployeeInfo.phone || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignEmployeeDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAssignEmployee">确定分配</el-button>
      </template>
    </el-dialog>
    
    <!-- 编辑员工对话框 -->
    <el-dialog v-model="editEmployeeDialog" title="编辑员工信息" width="500px">
      <el-form ref="editEmployeeFormRef" :model="editEmployeeForm" label-width="100px">
        <el-form-item label="员工姓名">
          <el-input v-model="editEmployeeForm.realName" disabled />
        </el-form-item>
        <el-form-item label="当前部门">
          <el-input v-model="editEmployeeForm.departmentName" disabled />
        </el-form-item>
        <el-form-item label="岗位" prop="positionId">
          <el-select 
            v-model="editEmployeeForm.positionId" 
            placeholder="请选择岗位"
            style="width: 100%"
          >
            <el-option
              v-for="pos in departmentPositions"
              :key="pos.id"
              :label="pos.name"
              :value="pos.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editEmployeeDialog = false">取消</el-button>
        <el-button type="primary" @click="saveEditEmployee">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 调动员工对话框 -->
    <el-dialog v-model="transferEmployeeDialog" title="员工调动" width="600px">
      <el-form ref="transferFormRef" :model="transferForm" label-width="100px">
        <el-form-item label="员工信息">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="姓名">{{ transferForm.employeeName }}</el-descriptions-item>
            <el-descriptions-item label="当前部门">{{ transferForm.fromDepartmentName }}</el-descriptions-item>
          </el-descriptions>
        </el-form-item>
        <el-form-item label="目标部门" prop="toDepartmentId">
          <el-tree-select
            v-model="transferForm.toDepartmentId"
            :data="filteredDepartments"
            :props="treeProps"
            node-key="id"
            placeholder="请选择目标部门"
            style="width: 100%"
            @change="handleTargetDepartmentChange"
          />
        </el-form-item>
        <el-form-item label="目标岗位" prop="positionId">
          <el-select 
            v-model="transferForm.positionId" 
            placeholder="请选择岗位（可选）"
            style="width: 100%"
          >
            <el-option
              v-for="pos in targetDepartmentPositions"
              :key="pos.id"
              :label="pos.name"
              :value="pos.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="调动原因" prop="reason">
          <el-input 
            v-model="transferForm.reason" 
            type="textarea" 
            rows="3"
            placeholder="请输入调动原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferEmployeeDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmTransferEmployee">确定调动</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { departmentApi, positionApi, employeeApi } from '@/api/organization'

const activeTab = ref('department')
const selectedNode = ref(null)
const deptDialog = ref(false)
const positionDialog = ref(false)
const assignEmployeeDialog = ref(false)
const editEmployeeDialog = ref(false)
const transferEmployeeDialog = ref(false)
const deptFormRef = ref()
const dialogDeptFormRef = ref()
const positionFormRef = ref()
const assignFormRef = ref()
const editEmployeeFormRef = ref()
const transferFormRef = ref()

const treeData = ref([])
const positionData = ref([])
const employeeData = ref([])
const availableEmployees = ref([])
const departmentPositions = ref([])
const targetDepartmentPositions = ref([])
const selectedEmployeeInfo = ref(null)
const filteredDepartments = ref([])

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

const assignForm = ref({
  userId: '',
  positionId: ''
})

const editEmployeeForm = ref({
  id: '',
  realName: '',
  departmentName: '',
  positionId: ''
})

const transferForm = ref({
  userId: '',
  employeeName: '',
  fromDepartmentId: '',
  fromDepartmentName: '',
  toDepartmentId: '',
  positionId: '',
  reason: ''
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

// 监听标签页切换
watch(activeTab, async (newTab) => {
  if (!selectedNode.value) return
  
  try {
    switch (newTab) {
      case 'department':
        // 部门信息已经在选中节点时加载，不需要重新加载
        break
      case 'position':
        await loadPositionData(selectedNode.value.id)
        break
      case 'employee':
        await loadEmployeeData(selectedNode.value.id)
        break
    }
  } catch (error) {
    console.error('切换标签页加载数据失败:', error)
  }
})

const loadTreeData = async () => {
  try {
    const res = await departmentApi.getDepartmentTree()
    if (res.code === 200) {
      treeData.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取部门树失败')
    }
  } catch (error) {
    console.error('获取部门树失败:', error)
    ElMessage.error('获取部门树失败')
  }
}


const handleNodeClick = async (data) => {
  selectedNode.value = data
  deptForm.value = { ...data }
  await loadPositionData(data.id)
  await loadEmployeeData(data.id)
}

const loadPositionData = async (deptId) => {
  try {
    const res = await departmentApi.getDepartmentPositions(deptId)
    if (res.code === 200) {
      positionData.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取岗位列表失败')
    }
  } catch (error) {
    console.error('获取岗位列表失败:', error)
    ElMessage.error('获取岗位列表失败')
  }
}

const loadEmployeeData = async (deptId) => {
  try {
    const res = await employeeApi.getDepartmentEmployees(deptId)
    if (res.code === 200) {
      employeeData.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取员工列表失败')
    }
  } catch (error) {
    console.error('获取员工列表失败:', error)
    ElMessage.error('获取员工列表失败')
  }
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

const formatDate = (timestamp) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const getStatusType = (status) => {
  const statusMap = {
    'ACTIVE': 'success',
    'INACTIVE': 'danger',
    'PENDING': 'warning',
    '在职': 'success',
    '离职': 'info',
    '休假': 'warning'
  }
  return statusMap[status] || 'info'
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
    
    const res = await departmentApi.deleteDepartment(data.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadTreeData()
      selectedNode.value = null
      deptForm.value = {
        id: '',
        name: '',
        type: '部门',
        parentId: null,
        manager: '',
        phone: '',
        status: '启用',
        description: ''
      }
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除部门失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const saveDepartment = async () => {
  const valid = await deptFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  try {
    const data = {
      name: deptForm.value.name,
      type: deptForm.value.type,
      parentId: deptForm.value.parentId,
      managerId: deptForm.value.managerId,
      phone: deptForm.value.phone,
      status: deptForm.value.status,
      description: deptForm.value.description
    }
    
    const res = await departmentApi.updateDepartment(deptForm.value.id, data)
    if (res.code === 200) {
      ElMessage.success('部门信息保存成功')
      await loadTreeData()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存部门信息失败:', error)
    ElMessage.error('保存失败')
  }
}

const resetDeptForm = () => {
  deptFormRef.value?.resetFields()
}

const saveDeptDialog = async () => {
  const valid = await dialogDeptFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  try {
    const data = {
      name: dialogDeptForm.value.name,
      type: dialogDeptForm.value.type,
      parentId: dialogDeptForm.value.parentId,
      managerId: dialogDeptForm.value.managerId,
      phone: dialogDeptForm.value.phone,
      description: dialogDeptForm.value.description
    }
    
    let res
    if (dialogDeptForm.value.id) {
      res = await departmentApi.updateDepartment(dialogDeptForm.value.id, data)
    } else {
      res = await departmentApi.createDepartment(data)
    }
    
    if (res.code === 200) {
      deptDialog.value = false
      ElMessage.success(dialogDeptForm.value.id ? '编辑成功' : '新增成功')
      await loadTreeData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('保存部门失败:', error)
    ElMessage.error('操作失败')
  }
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
    
    const res = await positionApi.deletePosition(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadPositionData(selectedNode.value.id)
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除岗位失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const savePosition = async () => {
  try {
    const data = {
      departmentId: selectedNode.value.id,
      name: positionForm.value.name,
      level: positionForm.value.level,
      count: positionForm.value.count,
      salary: positionForm.value.salary,
      description: positionForm.value.description
    }
    
    let res
    if (positionForm.value.id) {
      // 更新时不需要departmentId
      delete data.departmentId
      data.status = positionForm.value.status || '启用'
      res = await positionApi.updatePosition(positionForm.value.id, data)
    } else {
      res = await positionApi.createPosition(data)
    }
    
    if (res.code === 200) {
      positionDialog.value = false
      ElMessage.success(positionForm.value.id ? '编辑成功' : '新增成功')
      await loadPositionData(selectedNode.value.id)
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('保存岗位失败:', error)
    ElMessage.error('操作失败')
  }
}

const handleAddEmployee = async () => {
  try {
    const res = await employeeApi.getAvailableEmployees()
    if (res.code === 200) {
      availableEmployees.value = res.data || []
      
      if (availableEmployees.value.length === 0) {
        ElMessage.warning('暂无可分配的员工')
        return
      }
      
      // 加载当前部门的岗位列表
      const posRes = await departmentApi.getDepartmentPositions(selectedNode.value.id)
      if (posRes.code === 200) {
        departmentPositions.value = posRes.data || []
      }
      
      // 重置表单
      assignForm.value = {
        userId: '',
        positionId: ''
      }
      selectedEmployeeInfo.value = null
      
      assignEmployeeDialog.value = true
    } else {
      ElMessage.error(res.message || '获取可用员工失败')
    }
  } catch (error) {
    console.error('打开分配员工对话框失败:', error)
    ElMessage.error('打开分配员工对话框失败')
  }
}

const handleEmployeeSelect = (userId) => {
  selectedEmployeeInfo.value = availableEmployees.value.find(emp => emp.id === userId)
}

const confirmAssignEmployee = async () => {
  try {
    if (!assignForm.value.userId) {
      ElMessage.warning('请选择要分配的员工')
      return
    }
    
    const assignRes = await employeeApi.assignEmployee(selectedNode.value.id, {
      userId: assignForm.value.userId,
      departmentId: selectedNode.value.id,
      positionId: assignForm.value.positionId
    })
    
    if (assignRes.code === 200) {
      ElMessage.success('分配成功')
      assignEmployeeDialog.value = false
      await loadEmployeeData(selectedNode.value.id)
      await loadTreeData() // 刷新树结构更新员工数量
    } else {
      ElMessage.error(assignRes.message || '分配失败')
    }
  } catch (error) {
    console.error('分配员工失败:', error)
    ElMessage.error('分配失败')
  }
}

const editEmployee = async (row) => {
  try {
    // 加载当前部门的岗位列表
    const posRes = await departmentApi.getDepartmentPositions(selectedNode.value.id)
    if (posRes.code === 200) {
      departmentPositions.value = posRes.data || []
    }
    
    // 设置编辑表单数据
    editEmployeeForm.value = {
      id: row.id,
      realName: row.realName,
      departmentName: selectedNode.value.name,
      positionId: row.positionId || ''
    }
    
    editEmployeeDialog.value = true
  } catch (error) {
    console.error('打开编辑员工对话框失败:', error)
    ElMessage.error('打开编辑员工对话框失败')
  }
}

const saveEditEmployee = async () => {
  try {
    // 更新员工的岗位信息
    const updateRes = await employeeApi.assignEmployee(selectedNode.value.id, {
      userId: editEmployeeForm.value.id,
      departmentId: selectedNode.value.id,
      positionId: editEmployeeForm.value.positionId
    })
    
    if (updateRes.code === 200) {
      ElMessage.success('更新成功')
      editEmployeeDialog.value = false
      await loadEmployeeData(selectedNode.value.id)
    } else {
      ElMessage.error(updateRes.message || '更新失败')
    }
  } catch (error) {
    console.error('更新员工信息失败:', error)
    ElMessage.error('更新失败')
  }
}

const transferEmployee = async (row) => {
  try {
    // 获取部门树用于选择目标部门
    const treeRes = await departmentApi.getDepartmentTree()
    if (treeRes.code !== 200 || !treeRes.data || treeRes.data.length === 0) {
      ElMessage.error('获取部门列表失败')
      return
    }
    
    // 过滤掉当前部门
    const filterCurrentDepartment = (nodes) => {
      return nodes.filter(node => node.id !== selectedNode.value.id).map(node => {
        if (node.children && node.children.length > 0) {
          return {
            ...node,
            children: filterCurrentDepartment(node.children)
          }
        }
        return node
      })
    }
    
    filteredDepartments.value = filterCurrentDepartment(treeRes.data)
    
    if (filteredDepartments.value.length === 0) {
      ElMessage.warning('没有其他部门可供调动')
      return
    }
    
    // 设置调动表单数据
    transferForm.value = {
      userId: row.id,
      employeeName: row.realName,
      fromDepartmentId: selectedNode.value.id,
      fromDepartmentName: selectedNode.value.name,
      toDepartmentId: '',
      positionId: '',
      reason: ''
    }
    
    targetDepartmentPositions.value = []
    transferEmployeeDialog.value = true
  } catch (error) {
    console.error('打开调动员工对话框失败:', error)
    ElMessage.error('打开调动员工对话框失败')
  }
}

const handleTargetDepartmentChange = async (departmentId) => {
  try {
    // 加载目标部门的岗位列表
    const posRes = await departmentApi.getDepartmentPositions(departmentId)
    if (posRes.code === 200) {
      targetDepartmentPositions.value = posRes.data || []
    }
    // 清空已选岗位
    transferForm.value.positionId = ''
  } catch (error) {
    console.error('获取目标部门岗位失败:', error)
    targetDepartmentPositions.value = []
  }
}

const confirmTransferEmployee = async () => {
  try {
    if (!transferForm.value.toDepartmentId) {
      ElMessage.warning('请选择目标部门')
      return
    }
    
    if (!transferForm.value.reason) {
      ElMessage.warning('请输入调动原因')
      return
    }
    
    const transferRes = await employeeApi.transferEmployee({
      userId: transferForm.value.userId,
      fromDepartmentId: transferForm.value.fromDepartmentId,
      toDepartmentId: transferForm.value.toDepartmentId,
      positionId: transferForm.value.positionId,
      reason: transferForm.value.reason
    })
    
    if (transferRes.code === 200) {
      ElMessage.success('调动成功')
      transferEmployeeDialog.value = false
      await loadEmployeeData(selectedNode.value.id)
      await loadTreeData() // 刷新树结构更新员工数量
    } else {
      ElMessage.error(transferRes.message || '调动失败')
    }
  } catch (error) {
    console.error('调动员工失败:', error)
    ElMessage.error('调动失败')
  }
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