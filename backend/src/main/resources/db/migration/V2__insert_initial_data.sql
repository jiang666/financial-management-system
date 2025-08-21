-- 初始化基础数据

-- 插入基础资源（根据前端页面结构）
-- 顶级菜单
INSERT INTO tb_resource (id, create_time, update_time, create_by, update_by, resource_code, resource_name, resource_type, parent_id, url, icon, sort_order, path, level, description, deleted) VALUES
('res-001', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'BASIC_SETTINGS', '基础设置', 'TOP_MENU', NULL, NULL, 'Setting', 1, '/res-001', 1, '基础设置模块', 0),
('res-002', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'DAILY_ACCOUNTING', '日常核算', 'TOP_MENU', NULL, NULL, 'Document', 2, '/res-002', 1, '日常核算模块', 0),
('res-003', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'REPORT_MANAGEMENT', '报表管理', 'TOP_MENU', NULL, NULL, 'DataAnalysis', 3, '/res-003', 1, '报表管理模块', 0);

-- 基础设置子菜单
INSERT INTO tb_resource (id, create_time, update_time, create_by, update_by, resource_code, resource_name, resource_type, parent_id, url, icon, sort_order, path, level, description, deleted) VALUES
('res-011', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'CHART_OF_ACCOUNTS', '科目管理', 'SUB_MENU', 'res-001', '/settings/account', NULL, 1, '/res-001/res-011', 2, '会计科目管理', 0),
('res-012', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'AUXILIARY_ACCOUNTING', '辅助核算', 'SUB_MENU', 'res-001', '/settings/auxiliary', NULL, 2, '/res-001/res-012', 2, '辅助核算设置', 0),
('res-013', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'INITIAL_BALANCE', '期初余额', 'SUB_MENU', 'res-001', '/settings/balance', NULL, 3, '/res-001/res-013', 2, '期初余额设置', 0),
('res-014', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'CURRENCY_RATE', '币种汇率', 'SUB_MENU', 'res-001', '/settings/currency', NULL, 4, '/res-001/res-014', 2, '币种汇率管理', 0),
('res-015', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'SYSTEM_PARAMS', '系统参数', 'SUB_MENU', 'res-001', '/settings/system', NULL, 5, '/res-001/res-015', 2, '系统参数设置', 0),
('res-016', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'ORGANIZATION', '组织架构', 'SUB_MENU', 'res-001', '/settings/organization', NULL, 6, '/res-001/res-016', 2, '组织架构管理', 0),
('res-017', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'USER_PERMISSION', '用户权限', 'SUB_MENU', 'res-001', '/settings/permission', NULL, 7, '/res-001/res-017', 2, '用户权限管理', 0);

-- 用户权限按钮级权限
INSERT INTO tb_resource (id, create_time, update_time, create_by, update_by, resource_code, resource_name, resource_type, parent_id, url, icon, sort_order, path, level, description, deleted) VALUES
-- 用户管理按钮
('res-0171', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'USER_ADD', '新增用户', 'BUTTON', 'res-017', NULL, NULL, 1, '/res-001/res-017/res-0171', 3, '新增用户按钮', 0),
('res-0172', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'USER_EDIT', '编辑', 'BUTTON', 'res-017', NULL, NULL, 2, '/res-001/res-017/res-0172', 3, '编辑用户按钮', 0),
('res-0173', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'USER_DELETE', '删除', 'BUTTON', 'res-017', NULL, NULL, 3, '/res-001/res-017/res-0173', 3, '删除用户按钮', 0),
('res-0174', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'USER_RESET_PWD', '重置密码', 'BUTTON', 'res-017', NULL, NULL, 4, '/res-001/res-017/res-0174', 3, '重置密码按钮', 0),
('res-0175', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'USER_EXPORT', '导出用户', 'BUTTON', 'res-017', NULL, NULL, 5, '/res-001/res-017/res-0175', 3, '导出用户按钮', 0),
-- 角色管理按钮
('res-0176', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'ROLE_ADD', '新增角色', 'BUTTON', 'res-017', NULL, NULL, 6, '/res-001/res-017/res-0176', 3, '新增角色按钮', 0),
('res-0177', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'ROLE_EDIT', '编辑', 'BUTTON', 'res-017', NULL, NULL, 7, '/res-001/res-017/res-0177', 3, '编辑角色按钮', 0),
('res-0178', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'ROLE_DELETE', '删除', 'BUTTON', 'res-017', NULL, NULL, 8, '/res-001/res-017/res-0178', 3, '删除角色按钮', 0),
('res-0179', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'ROLE_CONFIG', '权限配置', 'BUTTON', 'res-017', NULL, NULL, 9, '/res-001/res-017/res-0179', 3, '角色权限配置按钮', 0);

-- 日常核算子菜单
INSERT INTO tb_resource (id, create_time, update_time, create_by, update_by, resource_code, resource_name, resource_type, parent_id, url, icon, sort_order, path, level, description, deleted) VALUES
('res-021', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'VOUCHER_ENTRY', '凭证录入', 'SUB_MENU', 'res-002', '/voucher/entry', NULL, 1, '/res-002/res-021', 2, '凭证录入', 0),
('res-022', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'VOUCHER_REVIEW', '凭证审核', 'SUB_MENU', 'res-002', '/voucher/review', NULL, 2, '/res-002/res-022', 2, '凭证审核', 0),
('res-023', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'VOUCHER_POSTING', '凭证记账', 'SUB_MENU', 'res-002', '/voucher/posting', NULL, 3, '/res-002/res-023', 2, '凭证记账', 0),
('res-024', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'VOUCHER_QUERY', '凭证查询', 'SUB_MENU', 'res-002', '/voucher/query', NULL, 4, '/res-002/res-024', 2, '凭证查询', 0),
('res-025', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'BOOK_QUERY', '账簿查询', 'SUB_MENU', 'res-002', '/voucher/books', NULL, 5, '/res-002/res-025', 2, '账簿查询', 0),
('res-026', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'PERIOD_CLOSING', '期末结账', 'SUB_MENU', 'res-002', '/voucher/closing', NULL, 6, '/res-002/res-026', 2, '期末结账', 0);

-- 报表管理子菜单
INSERT INTO tb_resource (id, create_time, update_time, create_by, update_by, resource_code, resource_name, resource_type, parent_id, url, icon, sort_order, path, level, description, deleted) VALUES
('res-031', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'FINANCIAL_REPORTS', '财务报表', 'SUB_MENU', 'res-003', '/reports/financial', NULL, 1, '/res-003/res-031', 2, '财务报表', 0),
('res-032', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'ANALYSIS_REPORTS', '分析报表', 'SUB_MENU', 'res-003', '/reports/analysis', NULL, 2, '/res-003/res-032', 2, '分析报表', 0),
('res-033', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'CUSTOM_REPORTS', '自定义报表', 'SUB_MENU', 'res-003', '/reports/custom', NULL, 3, '/res-003/res-033', 2, '自定义报表', 0);

-- 插入基础角色
INSERT INTO tb_role (id, create_time, update_time, create_by, update_by, role_code, name, description, status, can_delete, can_modify, user_count, deleted) VALUES
('role-001', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'SUPER_ADMIN', '超级管理员', '系统超级管理员，拥有所有权限', 'ACTIVE', 0, 0, 1, 0),
('role-002', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'FINANCE_MANAGER', '财务主管', '负责凭证审核、报表查看等', 'ACTIVE', 1, 1, 0, 0),
('role-003', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'ACCOUNTANT', '会计', '负责凭证录入、账簿查询等', 'ACTIVE', 1, 1, 0, 0),
('role-004', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'CASHIER', '出纳', '负责现金日记账、银行对账等', 'ACTIVE', 1, 1, 0, 0),
('role-005', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'AUDITOR', '审计员', '负责查看报表、审计核查等', 'ACTIVE', 1, 1, 0, 0);

-- 插入默认管理员用户（密码: admin123）
INSERT INTO tb_user (id, create_time, update_time, create_by, update_by, username, password, real_name, email, phone, department, department_id, status, last_login_at, last_login_ip, deleted) VALUES
('user-001', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'admin', '$2a$10$EuWPZHzz32dJN7jexM34MOeYirDdFAZm2kuWj7VEOJhhZkDrxfvUu', '系统管理员', 'admin@company.com', '13800000000', '信息技术部', NULL, 'ACTIVE', NULL, NULL, 0);

-- 关联用户角色
INSERT INTO tb_user_role (id, create_time, create_by, user_id, role_id) VALUES
('ur-001', UNIX_TIMESTAMP(NOW())*1000, 'system', 'user-001', 'role-001');

-- 超级管理员拥有所有资源权限
INSERT INTO tb_role_resource (id, create_time, create_by, role_id, resource_id)
SELECT 
    CONCAT('rr-', LPAD((@row_number:=@row_number+1), 3, '0')), 
    UNIX_TIMESTAMP(NOW())*1000, 
    'system', 
    'role-001', 
    id 
FROM tb_resource, (SELECT @row_number:=0) r;