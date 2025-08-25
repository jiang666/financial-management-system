-- 补充缺失的系统参数
-- 该脚本补充前端页面所需但V7中未包含的参数

-- 1. 基本设置 - 补充电子邮箱参数
INSERT INTO system_parameters (id, param_key, param_value, param_type, category, description, is_system, is_encrypted, required, visible, editable, effective_immediately, restart_required, sort_order, created_at, updated_at, created_by, deleted)
VALUES 
(UUID(), 'company.email', '', 'STRING', 'BASIC', '电子邮箱', 0, 0, 0, 1, 1, 1, 0, 7, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0);

-- 2. 会计期间设置参数
INSERT INTO system_parameters (id, param_key, param_value, param_type, category, description, is_system, is_encrypted, required, visible, editable, effective_immediately, restart_required, sort_order, created_at, updated_at, created_by, deleted)
VALUES 
-- 会计年度
(UUID(), 'period.fiscal_year', '2025', 'STRING', 'FINANCIAL', '会计年度', 0, 0, 1, 1, 1, 1, 0, 100, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
-- 启用期间
(UUID(), 'period.start_period', '2025-01', 'STRING', 'FINANCIAL', '启用期间', 0, 0, 1, 1, 1, 0, 1, 101, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
-- 当前期间
(UUID(), 'period.current_period', '2025-08', 'STRING', 'FINANCIAL', '当前会计期间', 0, 0, 1, 1, 1, 1, 0, 102, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
-- 是否按期结账
(UUID(), 'period.is_period_close', 'true', 'BOOLEAN', 'FINANCIAL', '是否按期结账', 0, 0, 1, 1, 1, 1, 0, 103, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
-- 自动结转损益
(UUID(), 'period.auto_carry_forward', 'true', 'BOOLEAN', 'FINANCIAL', '自动结转损益', 0, 0, 1, 1, 1, 1, 0, 104, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0);

-- 3. 凭证设置补充参数
INSERT INTO system_parameters (id, param_key, param_value, param_type, category, description, is_system, is_encrypted, required, visible, editable, effective_immediately, restart_required, sort_order, min_value, max_value, default_value, created_at, updated_at, created_by, deleted)
VALUES 
-- 凭证编号位数
(UUID(), 'voucher.number_length', '4', 'INTEGER', 'FINANCIAL', '凭证编号位数', 0, 0, 1, 1, 1, 1, 0, 24, '3', '8', '4', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
-- 审核后允许修改
(UUID(), 'voucher.allow_edit_after_audit', 'false', 'BOOLEAN', 'FINANCIAL', '审核后允许修改', 0, 0, 1, 1, 1, 1, 0, 25, NULL, NULL, 'false', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0);

-- 4. 凭证字类型数据（作为JSON存储）
INSERT INTO system_parameters (id, param_key, param_value, param_type, category, description, is_system, is_encrypted, required, visible, editable, effective_immediately, restart_required, sort_order, created_at, updated_at, created_by, deleted)
VALUES 
(UUID(), 'voucher.types', '[{"code":"记","name":"记账凭证","enabled":true},{"code":"收","name":"收款凭证","enabled":true},{"code":"付","name":"付款凭证","enabled":true},{"code":"转","name":"转账凭证","enabled":false}]', 'JSON', 'FINANCIAL', '凭证字设置', 0, 0, 1, 1, 1, 1, 0, 26, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0);

-- 5. 权限控制补充参数
INSERT INTO system_parameters (id, param_key, param_value, param_type, category, description, is_system, is_encrypted, required, visible, editable, effective_immediately, restart_required, sort_order, default_value, created_at, updated_at, created_by, deleted)
VALUES 
-- 制单人可修改
(UUID(), 'permission.creator_can_edit', 'true', 'BOOLEAN', 'SYSTEM', '制单人可修改', 1, 0, 1, 1, 1, 1, 0, 80, 'true', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
-- 审核人可修改
(UUID(), 'permission.auditor_can_edit', 'false', 'BOOLEAN', 'SYSTEM', '审核人可修改', 1, 0, 1, 1, 1, 1, 0, 81, 'false', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
-- 记账人可修改
(UUID(), 'permission.bookkeeper_can_edit', 'false', 'BOOLEAN', 'SYSTEM', '记账人可修改', 1, 0, 1, 1, 1, 1, 0, 82, 'false', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
-- 跨期间查询
(UUID(), 'permission.cross_period_query', 'false', 'BOOLEAN', 'SYSTEM', '跨期间查询', 1, 0, 1, 1, 1, 1, 0, 83, 'false', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
-- 数据导出权限（JSON数组）
(UUID(), 'permission.export_roles', '["admin","finance"]', 'JSON', 'SYSTEM', '数据导出权限', 1, 0, 1, 1, 1, 1, 0, 84, '["admin","finance"]', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0),
-- 登录失败锁定（已存在login.max_attempts，这里补充启用开关）
(UUID(), 'login.lock_enabled', 'true', 'BOOLEAN', 'SYSTEM', '启用登录失败锁定', 1, 0, 1, 1, 1, 1, 0, 46, 'true', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0);

-- 6. 备份设置补充参数
INSERT INTO system_parameters (id, param_key, param_value, param_type, category, description, is_system, is_encrypted, required, visible, editable, effective_immediately, restart_required, sort_order, options, default_value, created_at, updated_at, created_by, deleted)
VALUES 
-- 备份频率
(UUID(), 'backup.frequency', 'daily', 'ENUM', 'SYSTEM', '备份频率', 1, 0, 1, 1, 1, 1, 0, 63, '["daily","weekly","monthly"]', 'daily', UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 0);

-- 7. 备份历史表（如果需要记录备份历史）
CREATE TABLE IF NOT EXISTS backup_history (
    id VARCHAR(36) PRIMARY KEY COMMENT '备份ID',
    filename VARCHAR(255) NOT NULL COMMENT '文件名',
    file_path VARCHAR(500) COMMENT '文件路径',
    file_size VARCHAR(50) COMMENT '文件大小',
    backup_type VARCHAR(20) COMMENT '备份类型(AUTO/MANUAL)',
    status VARCHAR(20) COMMENT '状态(SUCCESS/FAILED)',
    error_message TEXT COMMENT '错误信息',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    created_by VARCHAR(36) COMMENT '创建人',
    deleted INT NOT NULL DEFAULT 0 COMMENT '删除标记',
    INDEX idx_created_at (created_at),
    INDEX idx_backup_type (backup_type),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='备份历史记录表';