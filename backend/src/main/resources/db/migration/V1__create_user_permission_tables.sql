-- 用户权限模块数据库表结构

-- 用户表
CREATE TABLE IF NOT EXISTS tb_user (
    id VARCHAR(36) NOT NULL COMMENT '用户唯一标识，UUID',
    create_time BIGINT NOT NULL COMMENT '创建时间，毫秒时间戳',
    update_time BIGINT NOT NULL COMMENT '更新时间，毫秒时间戳',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人，登录账号',
    update_by VARCHAR(50) NOT NULL COMMENT '更新人，登录账号',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    department VARCHAR(100) COMMENT '部门名称',
    department_id VARCHAR(36) COMMENT '部门ID，关联tb_department.id',
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-启用 INACTIVE-停用',
    last_login_at BIGINT COMMENT '最后登录时间，毫秒时间戳',
    last_login_ip VARCHAR(45) COMMENT '最后登录IP',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- 索引
CREATE INDEX idx_user_phone ON tb_user(phone);
CREATE INDEX idx_user_department_id ON tb_user(department_id);
CREATE INDEX idx_user_status ON tb_user(status);
CREATE INDEX idx_user_deleted ON tb_user(deleted);

-- 角色表
CREATE TABLE IF NOT EXISTS tb_role (
    id VARCHAR(36) NOT NULL COMMENT '角色唯一标识，UUID',
    create_time BIGINT NOT NULL COMMENT '创建时间，毫秒时间戳',
    update_time BIGINT NOT NULL COMMENT '更新时间，毫秒时间戳',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人，登录账号',
    update_by VARCHAR(50) NOT NULL COMMENT '更新人，登录账号',
    role_code VARCHAR(100) NOT NULL COMMENT '角色编码',
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(200) COMMENT '角色描述',
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE-启用 INACTIVE-停用',
    can_delete TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否可以删除: 0-否 1-是',
    can_modify TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否可以编辑: 0-否 1-是',
    user_count INT NOT NULL DEFAULT 0 COMMENT '使用该角色的用户数量',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code),
    UNIQUE KEY uk_role_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色信息表';

-- 索引
CREATE INDEX idx_role_status ON tb_role(status);
CREATE INDEX idx_role_deleted ON tb_role(deleted);

-- 资源表
CREATE TABLE IF NOT EXISTS tb_resource (
    id VARCHAR(36) NOT NULL COMMENT '资源唯一标识，UUID',
    create_time BIGINT NOT NULL COMMENT '创建时间，毫秒时间戳',
    update_time BIGINT NOT NULL COMMENT '更新时间，毫秒时间戳',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人，登录账号',
    update_by VARCHAR(50) NOT NULL COMMENT '更新人，登录账号',
    resource_code VARCHAR(100) COMMENT '资源编码',
    resource_name VARCHAR(100) NOT NULL COMMENT '资源名称',
    resource_type VARCHAR(50) NOT NULL COMMENT '资源类型: TOP_MENU-顶级菜单 SUB_MENU-子菜单 BUTTON-按钮 API-接口',
    parent_id VARCHAR(36) COMMENT '父资源ID',
    url VARCHAR(200) COMMENT '访问路径',
    icon VARCHAR(50) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '显示排序',
    path VARCHAR(500) COMMENT '层级路径，如: /1/2/3',
    level INT NOT NULL DEFAULT 1 COMMENT '层级深度',
    description VARCHAR(200) COMMENT '资源描述',
    deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除: 0-否 1-是',
    PRIMARY KEY (id),
    UNIQUE KEY uk_resource_code (resource_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资源权限表';

-- 索引
CREATE INDEX idx_resource_parent_id ON tb_resource(parent_id);
CREATE INDEX idx_resource_type ON tb_resource(resource_type);
CREATE INDEX idx_resource_url ON tb_resource(url);
CREATE INDEX idx_resource_deleted ON tb_resource(deleted);

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS tb_user_role (
    id VARCHAR(36) NOT NULL COMMENT '关联唯一标识，UUID',
    create_time BIGINT NOT NULL COMMENT '创建时间，毫秒时间戳',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人，登录账号',
    user_id VARCHAR(36) NOT NULL COMMENT '用户ID，关联tb_user.id',
    role_id VARCHAR(36) NOT NULL COMMENT '角色ID，关联tb_role.id',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 索引
CREATE INDEX idx_user_role_user_id ON tb_user_role(user_id);
CREATE INDEX idx_user_role_role_id ON tb_user_role(role_id);

-- 角色资源关联表
CREATE TABLE IF NOT EXISTS tb_role_resource (
    id VARCHAR(36) NOT NULL COMMENT '关联唯一标识，UUID',
    create_time BIGINT NOT NULL COMMENT '创建时间，毫秒时间戳',
    create_by VARCHAR(50) NOT NULL COMMENT '创建人，登录账号',
    role_id VARCHAR(36) NOT NULL COMMENT '角色ID，关联tb_role.id',
    resource_id VARCHAR(36) NOT NULL COMMENT '资源ID，关联tb_resource.id',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_resource (role_id, resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色资源关联表';

-- 索引
CREATE INDEX idx_role_resource_role_id ON tb_role_resource(role_id);
CREATE INDEX idx_role_resource_resource_id ON tb_role_resource(resource_id);