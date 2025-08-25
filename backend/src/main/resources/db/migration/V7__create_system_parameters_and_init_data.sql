-- 创建系统参数表（如果不存在）
CREATE TABLE IF NOT EXISTS system_parameters (
    id VARCHAR(36) PRIMARY KEY COMMENT '参数ID',
    param_key VARCHAR(100) NOT NULL UNIQUE COMMENT '参数键',
    param_value TEXT COMMENT '参数值',
    param_type VARCHAR(20) NOT NULL COMMENT '参数类型',
    category VARCHAR(50) COMMENT '参数分类',
    description VARCHAR(500) COMMENT '参数描述',
    is_system TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否系统参数',
    is_encrypted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否加密',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    validation_rule VARCHAR(500) COMMENT '验证规则',
    min_value VARCHAR(50) COMMENT '最小值',
    max_value VARCHAR(50) COMMENT '最大值',
    options TEXT COMMENT '可选值列表(JSON)',
    default_value TEXT COMMENT '默认值',
    required TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否必填',
    visible TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否可见',
    editable TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否可编辑',
    effective_immediately TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否立即生效',
    restart_required TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否需要重启',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NOT NULL COMMENT '更新时间',
    created_by VARCHAR(36) COMMENT '创建人',
    updated_by VARCHAR(36) COMMENT '更新人',
    deleted INT NOT NULL DEFAULT 0 COMMENT '删除标记'
);

-- 创建索引
CREATE INDEX idx_system_parameters_key ON system_parameters(param_key);
CREATE INDEX idx_system_parameters_category ON system_parameters(category);
CREATE INDEX idx_system_parameters_is_system ON system_parameters(is_system);
CREATE INDEX idx_system_parameters_deleted ON system_parameters(deleted);

-- 初始化系统参数数据
-- 企业信息
INSERT INTO system_parameters (id, param_key, param_value, param_type, category, description, is_system, is_encrypted, required, visible, editable, effective_immediately, restart_required, sort_order, created_at, updated_at, created_by, deleted)
VALUES 
(UUID(), 'company.name', '', 'STRING', 'BASIC', '企业名称', 0, 0, 1, 1, 1, 1, 0, 1, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
(UUID(), 'company.code', '', 'STRING', 'BASIC', '企业代码', 0, 0, 0, 1, 1, 1, 0, 2, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
(UUID(), 'company.tax_number', '', 'STRING', 'BASIC', '税号', 0, 0, 0, 1, 1, 1, 0, 3, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
(UUID(), 'company.legal_person', '', 'STRING', 'BASIC', '法人代表', 0, 0, 0, 1, 1, 1, 0, 4, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
(UUID(), 'company.address', '', 'STRING', 'BASIC', '注册地址', 0, 0, 0, 1, 1, 1, 0, 5, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
(UUID(), 'company.phone', '', 'STRING', 'BASIC', '联系电话', 0, 0, 0, 1, 1, 1, 0, 6, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0);

-- 会计制度
INSERT INTO system_parameters (id, param_key, param_value, param_type, category, description, is_system, is_encrypted, required, visible, editable, effective_immediately, restart_required, sort_order, options, created_at, updated_at, created_by, deleted)
VALUES 
(UUID(), 'accounting.standard', 'ENTERPRISE', 'ENUM', 'BASIC', '会计准则', 0, 0, 1, 1, 1, 0, 1, 10, '["ENTERPRISE","SMALL_ENTERPRISE"]', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
(UUID(), 'accounting.currency', 'CNY', 'STRING', 'BASIC', '记账本位币', 0, 0, 1, 1, 1, 0, 1, 11, NULL, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
(UUID(), 'accounting.start_date', '', 'DATE', 'BASIC', '账套启用日期', 0, 0, 1, 1, 1, 0, 1, 12, NULL, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
(UUID(), 'accounting.fiscal_year_start', '1', 'INTEGER', 'BASIC', '会计年度起始月', 0, 0, 1, 1, 1, 0, 1, 13, NULL, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0);

-- 凭证设置
INSERT INTO system_parameters (id, param_key, param_value, param_type, category, description, is_system, is_encrypted, required, visible, editable, effective_immediately, restart_required, sort_order, options, default_value, created_at, updated_at, created_by, deleted)
VALUES 
(UUID(), 'voucher.auto_number', 'true', 'BOOLEAN', 'FINANCIAL', '凭证自动编号', 0, 0, 1, 1, 1, 1, 0, 20, NULL, 'true', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
(UUID(), 'voucher.number_rule', 'YEARLY', 'ENUM', 'FINANCIAL', '凭证编号规则', 0, 0, 1, 1, 1, 0, 1, 21, '["YEARLY","MONTHLY","CONTINUOUS"]', 'YEARLY', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
(UUID(), 'voucher.approval_flow', 'SINGLE', 'ENUM', 'FINANCIAL', '审核流程', 0, 0, 1, 1, 1, 1, 0, 22, '["NONE","SINGLE","MULTI"]', 'SINGLE', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
(UUID(), 'voucher.allow_delete', 'false', 'BOOLEAN', 'FINANCIAL', '允许删除已审核凭证', 0, 0, 1, 1, 1, 1, 0, 23, NULL, 'false', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0);

-- 科目设置
INSERT INTO system_parameters (id, param_key, param_value, param_type, category, description, is_system, is_encrypted, required, visible, editable, effective_immediately, restart_required, sort_order, default_value, created_at, updated_at, created_by, deleted)
VALUES 
(UUID(), 'account.code_rule', '4-2-2-2', 'STRING', 'FINANCIAL', '科目编码规则', 0, 0, 1, 1, 1, 0, 1, 30, '4-2-2-2', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
(UUID(), 'account.max_level', '4', 'INTEGER', 'FINANCIAL', '科目最大级次', 0, 0, 1, 1, 1, 0, 1, 31, '4', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
(UUID(), 'account.allow_auxiliary', 'true', 'BOOLEAN', 'FINANCIAL', '启用辅助核算', 0, 0, 1, 1, 1, 0, 1, 32, 'true', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0);

-- 安全设置
INSERT INTO system_parameters (id, param_key, param_value, param_type, category, description, is_system, is_encrypted, required, visible, editable, effective_immediately, restart_required, sort_order, min_value, max_value, default_value, created_at, updated_at, created_by, deleted)
VALUES 
(UUID(), 'password.min_length', '8', 'INTEGER', 'SYSTEM', '密码最小长度', 1, 0, 1, 1, 1, 1, 0, 40, '6', '20', '8', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
(UUID(), 'password.expire_days', '90', 'INTEGER', 'SYSTEM', '密码有效期(天)', 1, 0, 1, 1, 1, 1, 0, 41, '0', '365', '90', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
(UUID(), 'password.complexity', 'true', 'BOOLEAN', 'SYSTEM', '密码复杂度要求', 1, 0, 1, 1, 1, 1, 0, 42, NULL, NULL, 'true', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
(UUID(), 'login.max_attempts', '5', 'INTEGER', 'SYSTEM', '最大登录尝试次数', 1, 0, 1, 1, 1, 1, 0, 43, '3', '10', '5', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
(UUID(), 'login.lock_duration', '30', 'INTEGER', 'SYSTEM', '账户锁定时长(分钟)', 1, 0, 1, 1, 1, 1, 0, 44, '5', '1440', '30', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
(UUID(), 'session.timeout', '1800', 'INTEGER', 'SYSTEM', '会话超时时间(秒)', 1, 0, 1, 1, 1, 1, 0, 45, '300', '86400', '1800', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0);

-- 日志设置
INSERT INTO system_parameters (id, param_key, param_value, param_type, category, description, is_system, is_encrypted, required, visible, editable, effective_immediately, restart_required, sort_order, options, default_value, created_at, updated_at, created_by, deleted)
VALUES 
(UUID(), 'log.level', 'INFO', 'ENUM', 'SYSTEM', '日志级别', 1, 0, 1, 1, 1, 0, 1, 50, '["DEBUG","INFO","WARN","ERROR"]', 'INFO', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
(UUID(), 'log.retention_days', '90', 'INTEGER', 'SYSTEM', '日志保留天数', 1, 0, 1, 1, 1, 1, 0, 51, '7', '365', '90', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
(UUID(), 'log.archive_enable', 'true', 'BOOLEAN', 'SYSTEM', '启用日志归档', 1, 0, 1, 1, 1, 1, 0, 52, NULL, 'true', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0);

-- 备份设置
INSERT INTO system_parameters (id, param_key, param_value, param_type, category, description, is_system, is_encrypted, required, visible, editable, effective_immediately, restart_required, sort_order, default_value, created_at, updated_at, created_by, deleted)
VALUES 
(UUID(), 'backup.auto_enable', 'true', 'BOOLEAN', 'SYSTEM', '启用自动备份', 1, 0, 1, 1, 1, 1, 0, 60, 'true', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
(UUID(), 'backup.schedule', '02:00', 'TIME', 'SYSTEM', '备份时间', 1, 0, 1, 1, 1, 1, 0, 61, '02:00', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
(UUID(), 'backup.retention_days', '30', 'INTEGER', 'SYSTEM', '备份保留天数', 1, 0, 1, 1, 1, 1, 0, 62, '30', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0);

-- 报表参数
INSERT INTO system_parameters (id, param_key, param_value, param_type, category, description, is_system, is_encrypted, required, visible, editable, effective_immediately, restart_required, sort_order, default_value, created_at, updated_at, created_by, deleted)
VALUES 
(UUID(), 'report.export_format', 'EXCEL', 'ENUM', 'REPORT', '默认导出格式', 0, 0, 1, 1, 1, 1, 0, 70, '["EXCEL","PDF","CSV"]', 'EXCEL', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
(UUID(), 'report.watermark_enable', 'true', 'BOOLEAN', 'REPORT', '启用水印', 0, 0, 1, 1, 1, 1, 0, 71, 'true', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
(UUID(), 'report.watermark_text', '', 'STRING', 'REPORT', '水印文字', 0, 0, 0, 1, 1, 1, 0, 72, '', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0);

-- 系统参数相关权限
INSERT INTO tb_resource (id, create_time, update_time, create_by, update_by, resource_code, resource_name, resource_type, parent_id, url, icon, sort_order, path, level, description, deleted) VALUES
-- 系统参数菜单权限
('res-0151', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'SYSTEM_PARAM_VIEW', '查看系统参数', 'BUTTON', 'res-015', NULL, NULL, 1, '/res-001/res-015/res-0151', 3, '查看系统参数权限', 0),
('res-0152', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'SYSTEM_PARAM_EDIT', '编辑系统参数', 'BUTTON', 'res-015', NULL, NULL, 2, '/res-001/res-015/res-0152', 3, '编辑系统参数权限', 0),
('res-0153', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'SYSTEM_PARAM_EXPORT', '导出参数配置', 'BUTTON', 'res-015', NULL, NULL, 3, '/res-001/res-015/res-0153', 3, '导出参数配置权限', 0),
('res-0154', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'SYSTEM_PARAM_IMPORT', '导入参数配置', 'BUTTON', 'res-015', NULL, NULL, 4, '/res-001/res-015/res-0154', 3, '导入参数配置权限', 0),
('res-0155', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'SYSTEM_PARAM_HISTORY', '查看修改历史', 'BUTTON', 'res-015', NULL, NULL, 5, '/res-001/res-015/res-0155', 3, '查看参数修改历史权限', 0),
('res-0156', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 'SYSTEM_PARAM_ADMIN', '系统参数管理', 'BUTTON', 'res-015', NULL, NULL, 6, '/res-001/res-015/res-0156', 3, '系统参数管理员权限', 0);

-- 给超级管理员分配系统参数权限
INSERT INTO tb_role_resource (id, role_id, resource_id, create_time, update_time, create_by, update_by) VALUES
(UUID(), 'role-001', 'res-015', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system'),
(UUID(), 'role-001', 'res-0151', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system'),
(UUID(), 'role-001', 'res-0152', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system'),
(UUID(), 'role-001', 'res-0153', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system'),
(UUID(), 'role-001', 'res-0154', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system'),
(UUID(), 'role-001', 'res-0155', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system'),
(UUID(), 'role-001', 'res-0156', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system');

-- 给财务主管分配部分系统参数权限
INSERT INTO tb_role_resource (id, role_id, resource_id, create_time, update_time, create_by, update_by) VALUES
(UUID(), 'role-002', 'res-015', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system'),
(UUID(), 'role-002', 'res-0151', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system'),
(UUID(), 'role-002', 'res-0152', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system'),
(UUID(), 'role-002', 'res-0153', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system');