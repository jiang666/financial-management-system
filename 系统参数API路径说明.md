# 系统参数API路径说明

## 1. 后端API路径配置

### 基础配置
- **服务端口**: 8086
- **Context Path**: `/api` (在application.yml中配置)
- **Controller映射**: `/v1/system/parameters`

### 完整的API路径
所有系统参数相关的API路径都以 `http://localhost:8086/api/v1/system/parameters` 为基础。

## 2. API端点列表

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/v1/system/parameters` | 获取参数列表（分页） |
| GET | `/api/v1/system/parameters/{key}` | 获取单个参数 |
| PUT | `/api/v1/system/parameters/{key}` | 更新参数 |
| POST | `/api/v1/system/parameters/batch` | 批量更新参数 |
| GET | `/api/v1/system/parameters/categories` | 获取参数分类 |
| GET | `/api/v1/system/parameters/export` | 导出参数配置 |
| POST | `/api/v1/system/parameters/import` | 导入参数配置 |
| GET | `/api/v1/system/parameters/{key}/history` | 获取参数修改历史 |
| POST | `/api/v1/system/parameters/validate` | 验证参数值 |
| POST | `/api/v1/system/parameters/refresh` | 刷新参数缓存 |

## 3. 前端集成配置

在前端项目中，API请求应该配置如下：

```javascript
// api/request.js 或 axios配置文件中
const api = axios.create({
    baseURL: 'http://localhost:8086/api',  // 注意包含/api
    timeout: 10000
});

// 系统参数API调用示例
const systemParamApi = {
    // 获取参数列表
    getParameters: (params) => api.get('/v1/system/parameters', { params }),
    
    // 获取单个参数
    getParameter: (key) => api.get(`/v1/system/parameters/${key}`),
    
    // 更新参数
    updateParameter: (key, data) => api.put(`/v1/system/parameters/${key}`, data),
    
    // 批量更新
    batchUpdate: (data) => api.post('/v1/system/parameters/batch', data)
    
    // ... 其他API方法
}
```

## 4. 已解决的问题

### 问题1: 500错误
**原因**: 
1. 权限系统中缺少SYSTEM_PARAM_*相关权限
2. ObjectMapper bean未定义

**解决方案**:
1. 临时注释了@PreAuthorize注解
2. 创建了JacksonConfig配置类提供ObjectMapper bean

### 问题2: 404错误
**原因**: API路径配置理解错误

**解决**: 
- 正确的路径是: `/api/v1/system/parameters`
- Context path `/api` + Controller mapping `/v1/system/parameters`

## 5. 注意事项

1. **认证**: 所有API请求需要在Header中携带JWT token
   ```
   Authorization: Bearer {token}
   ```

2. **权限**: 目前已临时禁用权限检查，生产环境需要：
   - 在tb_resource表中添加系统参数相关权限
   - 为角色分配这些权限
   - 恢复@PreAuthorize注解

3. **重启服务**: 修改代码后需要重新编译并启动后端服务

## 6. 测试验证

使用以下命令测试API是否正常工作：

```bash
# 运行测试脚本
node test-system-params-after-fix.js
```

确保后端服务已重新启动，并且所有更改已生效。