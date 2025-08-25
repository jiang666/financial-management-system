import request from './request'

// 部门管理接口
export const departmentApi = {
  // 获取部门树
  getDepartmentTree() {
    return request({
      url: '/v1/departments/tree',
      method: 'get'
    })
  },

  // 获取部门详情
  getDepartmentById(id) {
    return request({
      url: `/v1/departments/${id}`,
      method: 'get'
    })
  },

  // 创建部门
  createDepartment(data) {
    return request({
      url: '/v1/departments',
      method: 'post',
      data
    })
  },

  // 更新部门
  updateDepartment(id, data) {
    return request({
      url: `/v1/departments/${id}`,
      method: 'put',
      data
    })
  },

  // 删除部门
  deleteDepartment(id) {
    return request({
      url: `/v1/departments/${id}`,
      method: 'delete'
    })
  },

  // 获取部门统计信息
  getDepartmentStatistics(id) {
    return request({
      url: `/v1/departments/${id}/statistics`,
      method: 'get'
    })
  },

  // 获取部门岗位列表
  getDepartmentPositions(departmentId) {
    return request({
      url: `/v1/departments/${departmentId}/positions`,
      method: 'get'
    })
  }
}

// 岗位管理接口
export const positionApi = {
  // 获取岗位详情
  getPositionById(id) {
    return request({
      url: `/v1/positions/${id}`,
      method: 'get'
    })
  },

  // 创建岗位
  createPosition(data) {
    return request({
      url: '/v1/positions',
      method: 'post',
      data
    })
  },

  // 更新岗位
  updatePosition(id, data) {
    return request({
      url: `/v1/positions/${id}`,
      method: 'put',
      data
    })
  },

  // 删除岗位
  deletePosition(id) {
    return request({
      url: `/v1/positions/${id}`,
      method: 'delete'
    })
  }
}

// 员工管理接口
export const employeeApi = {
  // 获取部门员工列表
  getDepartmentEmployees(departmentId) {
    return request({
      url: `/v1/departments/${departmentId}/employees`,
      method: 'get'
    })
  },

  // 获取可分配的员工列表
  getAvailableEmployees() {
    return request({
      url: '/v1/departments/employees/available',
      method: 'get'
    })
  },

  // 分配员工到部门
  assignEmployee(departmentId, data) {
    return request({
      url: `/v1/departments/${departmentId}/employees`,
      method: 'post',
      data
    })
  },

  // 从部门移除员工
  removeEmployee(departmentId, userId) {
    return request({
      url: `/v1/departments/${departmentId}/employees/${userId}`,
      method: 'delete'
    })
  },

  // 员工部门调动
  transferEmployee(data) {
    return request({
      url: '/v1/departments/employees/transfer',
      method: 'post',
      data
    })
  }
}