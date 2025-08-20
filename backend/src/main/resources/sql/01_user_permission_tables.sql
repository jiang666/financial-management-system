-- 用户权限模块数据库表创建脚本
-- 创建时间: 2025-01-19

-- 使用数据库
USE financial_db;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    department_id VARCHAR(36) COMMENT '部门ID',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    last_login_at TIMESTAMP NULL COMMENT '最后登录时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(36) COMMENT '创建人',
    updated_by VARCHAR(36) COMMENT '更新人',
    deleted BOOLEAN DEFAULT FALSE COMMENT '逻辑删除标记',
    version INT DEFAULT 0 COMMENT '版本号(乐观锁)',
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_department_id (department_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 角色表
CREATE TABLE IF NOT EXISTS roles (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    name VARCHAR(100) NOT NULL COMMENT '角色名称',
    description TEXT COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    deleted BOOLEAN DEFAULT FALSE,
    version INT DEFAULT 0,
    
    INDEX idx_code (code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 3. 权限表
CREATE TABLE IF NOT EXISTS permissions (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    name VARCHAR(100) NOT NULL COMMENT '权限名称',
    resource VARCHAR(200) COMMENT '资源标识',
    action VARCHAR(50) COMMENT '操作类型',
    description TEXT COMMENT '权限描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    
    INDEX idx_code (code),
    INDEX idx_resource (resource)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 4. 用户角色关联表
CREATE TABLE IF NOT EXISTS user_roles (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id VARCHAR(36) NOT NULL COMMENT '用户ID',
    role_id VARCHAR(36) NOT NULL COMMENT '角色ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    
    UNIQUE KEY uk_user_role (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 5. 角色权限关联表
CREATE TABLE IF NOT EXISTS role_permissions (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    role_id VARCHAR(36) NOT NULL COMMENT '角色ID',
    permission_id VARCHAR(36) NOT NULL COMMENT '权限ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 插入默认数据
-- 1. 默认角色
INSERT INTO roles (id, code, name, description) VALUES 
('550e8400-e29b-41d4-a716-446655440001', 'SUPER_ADMIN', '超级管理员', '系统超级管理员，拥有所有权限'),
('550e8400-e29b-41d4-a716-446655440002', 'ADMIN', '系统管理员', '系统管理员，拥有大部分管理权限'),
('550e8400-e29b-41d4-a716-446655440003', 'ACCOUNTANT', '会计', '财务会计人员，负责凭证录入和账务处理'),
('550e8400-e29b-41d4-a716-446655440004', 'CASHIER', '出纳', '出纳人员，负责现金和银行账户管理'),
('550e8400-e29b-41d4-a716-446655440005', 'AUDITOR', '审计员', '审计人员，负责凭证审核'),
('550e8400-e29b-41d4-a716-446655440006', 'VIEWER', '查看者', '只读用户，仅有查看权限');

-- 2. 默认权限
INSERT INTO permissions (id, code, name, resource, action, description) VALUES 
-- 用户管理权限
('660e8400-e29b-41d4-a716-446655440001', 'USER_CREATE', '创建用户', 'USER', 'CREATE', '创建新用户'),
('660e8400-e29b-41d4-a716-446655440002', 'USER_READ', '查看用户', 'USER', 'READ', '查看用户信息'),
('660e8400-e29b-41d4-a716-446655440003', 'USER_UPDATE', '修改用户', 'USER', 'UPDATE', '修改用户信息'),
('660e8400-e29b-41d4-a716-446655440004', 'USER_DELETE', '删除用户', 'USER', 'DELETE', '删除用户'),

-- 凭证管理权限
('660e8400-e29b-41d4-a716-446655440005', 'VOUCHER_CREATE', '创建凭证', 'VOUCHER', 'CREATE', '创建会计凭证'),
('660e8400-e29b-41d4-a716-446655440006', 'VOUCHER_READ', '查看凭证', 'VOUCHER', 'READ', '查看会计凭证'),
('660e8400-e29b-41d4-a716-446655440007', 'VOUCHER_UPDATE', '修改凭证', 'VOUCHER', 'UPDATE', '修改会计凭证'),
('660e8400-e29b-41d4-a716-446655440008', 'VOUCHER_DELETE', '删除凭证', 'VOUCHER', 'DELETE', '删除会计凭证'),
('660e8400-e29b-41d4-a716-446655440009', 'VOUCHER_REVIEW', '审核凭证', 'VOUCHER', 'REVIEW', '审核会计凭证'),
('660e8400-e29b-41d4-a716-446655440010', 'VOUCHER_POST', '记账', 'VOUCHER', 'POST', '凭证记账'),

-- 报表权限
('660e8400-e29b-41d4-a716-446655440011', 'REPORT_VIEW', '查看报表', 'REPORT', 'VIEW', '查看财务报表'),
('660e8400-e29b-41d4-a716-446655440012', 'REPORT_EXPORT', '导出报表', 'REPORT', 'EXPORT', '导出财务报表');

-- 3. 超级管理员拥有所有权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT '550e8400-e29b-41d4-a716-446655440001', id FROM permissions;

-- 4. 创建默认超级管理员账号 (密码: admin123，实际使用时需要修改)
-- 注意：密码已使用BCrypt加密
INSERT INTO users (id, username, password, email, real_name, status) VALUES 
('770e8400-e29b-41d4-a716-446655440001', 'admin', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'admin@company.com', '系统管理员', 1);

-- 5. 给默认管理员分配超级管理员角色
INSERT INTO user_roles (user_id, role_id) VALUES 
('770e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001');