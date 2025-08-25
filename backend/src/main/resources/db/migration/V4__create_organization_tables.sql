-- 创建部门表
CREATE TABLE IF NOT EXISTS departments (
    id VARCHAR(36) PRIMARY KEY COMMENT '部门ID',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '部门编码',
    name VARCHAR(100) NOT NULL COMMENT '部门名称',
    parent_id VARCHAR(36) COMMENT '上级部门ID',
    level INT NOT NULL DEFAULT 1 COMMENT '层级',
    path VARCHAR(500) COMMENT '层级路径',
    manager_id VARCHAR(36) COMMENT '部门负责人ID',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    description TEXT COMMENT '部门描述',
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time BIGINT NOT NULL COMMENT '创建时间',
    update_time BIGINT NOT NULL COMMENT '更新时间',
    create_by VARCHAR(36) COMMENT '创建人',
    update_by VARCHAR(36) COMMENT '更新人',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
    INDEX idx_departments_code (code),
    INDEX idx_departments_parent_id (parent_id),
    INDEX idx_departments_level (level),
    INDEX idx_departments_status (status),
    INDEX idx_departments_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- 创建岗位表
CREATE TABLE IF NOT EXISTS positions (
    id VARCHAR(36) PRIMARY KEY COMMENT '岗位ID',
    department_id VARCHAR(36) NOT NULL COMMENT '部门ID',
    name VARCHAR(100) NOT NULL COMMENT '岗位名称',
    level VARCHAR(20) NOT NULL COMMENT '岗位等级',
    count INT NOT NULL DEFAULT 1 COMMENT '编制人数',
    actual_count INT NOT NULL DEFAULT 0 COMMENT '实际人数',
    salary DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT '基础薪资',
    status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态: 1-启用 0-禁用',
    description TEXT COMMENT '岗位描述',
    create_time BIGINT NOT NULL COMMENT '创建时间',
    update_time BIGINT NOT NULL COMMENT '更新时间',
    create_by VARCHAR(36) COMMENT '创建人',
    update_by VARCHAR(36) COMMENT '更新人',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除',
    INDEX idx_positions_department_id (department_id),
    INDEX idx_positions_status (status),
    INDEX idx_positions_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='岗位表';

-- 插入初始数据
INSERT INTO departments (id, code, name, parent_id, level, path, manager_id, contact_phone, description, status, sort_order, create_time, update_time, create_by, update_by, deleted) VALUES
('dept-001', 'DEPT001', '总公司', NULL, 1, '/dept-001', NULL, '021-12345678', '公司总部', 1, 1, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 0),
('dept-002', 'DEPT002', '财务部', 'dept-001', 2, '/dept-001/dept-002', NULL, '021-12345679', '负责公司财务管理', 1, 1, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 0),
('dept-003', 'DEPT003', '人力资源部', 'dept-001', 2, '/dept-001/dept-003', NULL, '021-12345680', '负责人力资源管理', 1, 2, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 0),
('dept-004', 'DEPT004', '技术部', 'dept-001', 2, '/dept-001/dept-004', NULL, '021-12345681', '负责技术研发', 1, 3, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 0),
('dept-005', 'DEPT005', '销售部', 'dept-001', 2, '/dept-001/dept-005', NULL, '021-12345682', '负责销售业务', 1, 4, UNIX_TIMESTAMP(NOW())*1000, UNIX_TIMESTAMP(NOW())*1000, 'system', 'system', 0);

-- 更新用户表中的部门ID（如果有相应的用户）
UPDATE tb_user SET department_id = 'dept-004' WHERE department = '信息技术部';
UPDATE tb_user SET department_id = 'dept-002' WHERE department = '财务部';
UPDATE tb_user SET department_id = 'dept-003' WHERE department = '人力资源部';
UPDATE tb_user SET department_id = 'dept-005' WHERE department = '销售部';