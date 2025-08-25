# 前端集成测试指南

## 测试准备

### 1. 确保后端服务运行
```bash
cd backend
mvn spring-boot:run
```
确保服务在 http://localhost:8086 正常运行

### 2. 启动前端开发服务器
```bash
cd frontend
npm run dev
```
前端将运行在 http://localhost:5173

## 测试步骤

### 方式1：使用测试页面
1. 在浏览器中打开 `frontend/test-system-params.html`
2. 点击"登录"按钮进行身份验证
3. 依次测试各个功能按钮

### 方式2：使用Vue应用
1. 访问 http://localhost:5173
2. 使用 admin/admin123 登录
3. 导航到：基础设置 -> 系统参数
4. 测试各个功能

## API接口说明

### 已实现的接口
- `GET /api/v1/system/parameters` - 获取参数列表
- `GET /api/v1/system/parameters/{key}` - 获取单个参数
- `PUT /api/v1/system/parameters/{key}` - 更新参数
- `POST /api/v1/system/parameters/batch` - 批量更新
- `GET /api/v1/system/parameters/categories` - 获取分类
- `POST /api/v1/system/parameters/validate` - 验证参数
- `POST /api/v1/system/parameters/refresh` - 刷新缓存
- `GET /api/v1/system/parameters/export` - 导出配置
- `POST /api/v1/system/parameters/import` - 导入配置
- `GET /api/v1/system/parameters/{key}/history` - 参数历史

### 测试数据
系统已初始化以下参数：
- 企业信息（company.*）
- 会计制度（accounting.*）
- 凭证设置（voucher.*）
- 安全设置（password.*, login.*, session.*）
- 备份设置（backup.*）

## 功能测试清单

### 基础功能
- [ ] 参数列表展示
- [ ] 按分类筛选
- [ ] 搜索功能
- [ ] 参数值修改
- [ ] 批量保存

### 高级功能
- [ ] 刷新缓存
- [ ] 导出配置
- [ ] 导入配置
- [ ] 查看历史记录

### 界面交互
- [ ] 表单验证
- [ ] 错误提示
- [ ] 成功提示
- [ ] 加载状态

## 常见问题

### 1. 404错误
如果所有系统参数接口都返回404：
- 检查控制器是否被正确注册
- 确认Spring Boot应用已重新编译和启动
- 检查应用日志中的RequestMapping信息

### 2. 401错误
- 确保已登录并获取token
- 检查token是否正确传递在Authorization header中

### 3. 403错误
- 检查用户是否有相应权限
- 确认权限数据已初始化

## 开发建议

1. **前端优化**
   - 添加参数值的实时验证
   - 实现参数修改的撤销功能
   - 添加参数搜索高亮

2. **功能扩展**
   - 参数模板管理
   - 参数变更审批流程
   - 参数生效通知

3. **性能优化**
   - 前端参数缓存
   - 分页加载优化
   - 批量操作优化