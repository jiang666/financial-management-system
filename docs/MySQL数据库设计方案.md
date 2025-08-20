# 财务系统MySQL数据库设计方案

## 📊 数据库总体设计

### 设计原则
- **单一数据源**: 所有数据存储在MySQL中
- **强一致性**: 保证财务数据的准确性和完整性
- **审计完整**: 所有表包含完整的审计字段
- **性能优化**: 合理的索引设计和查询优化

---

## 🗄️ 数据库结构划分

### 1. 用户权限模块
```sql
-- 用户表
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    department_id VARCHAR(36),
    status TINYINT DEFAULT 1 COMMENT '1-启用 0-禁用',
    last_login_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    deleted BOOLEAN DEFAULT FALSE,
    version INT DEFAULT 0,
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_department_id (department_id),
    INDEX idx_created_at (created_at)
);

-- 角色表
CREATE TABLE roles (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    status TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    deleted BOOLEAN DEFAULT FALSE,
    version INT DEFAULT 0,
    
    INDEX idx_code (code),
    INDEX idx_status (status)
);

-- 权限表
CREATE TABLE permissions (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    resource VARCHAR(200),
    action VARCHAR(50),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    
    INDEX idx_code (code),
    INDEX idx_resource (resource)
);

-- 用户角色关联表
CREATE TABLE user_roles (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id VARCHAR(36) NOT NULL,
    role_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    
    UNIQUE KEY uk_user_role (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- 角色权限关联表
CREATE TABLE role_permissions (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    role_id VARCHAR(36) NOT NULL,
    permission_id VARCHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (permission_id) REFERENCES permissions(id)
);
```

### 2. 基础设置模块
```sql
-- 会计科目表
CREATE TABLE chart_of_accounts (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '科目编码',
    name VARCHAR(200) NOT NULL COMMENT '科目名称',
    english_name VARCHAR(200) COMMENT '英文名称',
    parent_id VARCHAR(36) COMMENT '父级科目ID',
    level TINYINT NOT NULL COMMENT '科目级次',
    account_type VARCHAR(20) NOT NULL COMMENT '科目类型: ASSET,LIABILITY,EQUITY,COST,REVENUE',
    balance_direction VARCHAR(10) NOT NULL COMMENT '余额方向: DEBIT,CREDIT',
    auxiliary_type VARCHAR(50) COMMENT '辅助核算类型',
    is_leaf BOOLEAN DEFAULT TRUE COMMENT '是否叶子节点',
    status TINYINT DEFAULT 1 COMMENT '状态: 1-启用 0-停用',
    memo_code VARCHAR(20) COMMENT '助记码',
    currency_code VARCHAR(10) DEFAULT 'CNY',
    is_quantity BOOLEAN DEFAULT FALSE COMMENT '是否数量核算',
    unit VARCHAR(20) COMMENT '数量单位',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    deleted BOOLEAN DEFAULT FALSE,
    version INT DEFAULT 0,
    
    INDEX idx_code (code),
    INDEX idx_parent_id (parent_id),
    INDEX idx_level (level),
    INDEX idx_account_type (account_type),
    INDEX idx_status (status)
);

-- 币种表
CREATE TABLE currencies (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    code VARCHAR(10) NOT NULL UNIQUE COMMENT '币种代码 USD,EUR,CNY',
    name VARCHAR(50) NOT NULL COMMENT '币种名称',
    symbol VARCHAR(10) COMMENT '币种符号',
    decimal_places TINYINT DEFAULT 2 COMMENT '小数位数',
    is_base_currency BOOLEAN DEFAULT FALSE COMMENT '是否本位币',
    status TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    deleted BOOLEAN DEFAULT FALSE,
    
    INDEX idx_code (code),
    INDEX idx_is_base_currency (is_base_currency)
);

-- 汇率表
CREATE TABLE exchange_rates (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    from_currency VARCHAR(10) NOT NULL,
    to_currency VARCHAR(10) NOT NULL,
    rate DECIMAL(15,6) NOT NULL COMMENT '汇率',
    rate_type VARCHAR(20) DEFAULT 'ACCOUNTING' COMMENT '汇率类型',
    effective_date DATE NOT NULL COMMENT '生效日期',
    end_date DATE COMMENT '失效日期',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    
    INDEX idx_currency_date (from_currency, to_currency, effective_date),
    INDEX idx_effective_date (effective_date)
);

-- 期初余额表
CREATE TABLE initial_balances (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    account_id VARCHAR(36) NOT NULL,
    fiscal_year INT NOT NULL,
    beginning_balance DECIMAL(15,2) DEFAULT 0 COMMENT '年初余额',
    beginning_debit DECIMAL(15,2) DEFAULT 0 COMMENT '年初借方',
    beginning_credit DECIMAL(15,2) DEFAULT 0 COMMENT '年初贷方',
    ytd_debit DECIMAL(15,2) DEFAULT 0 COMMENT '本年借方累计',
    ytd_credit DECIMAL(15,2) DEFAULT 0 COMMENT '本年贷方累计',
    currency_code VARCHAR(10) DEFAULT 'CNY',
    auxiliary_info JSON COMMENT '辅助核算信息',
    is_confirmed BOOLEAN DEFAULT FALSE COMMENT '是否已确认',
    confirmed_at TIMESTAMP,
    confirmed_by VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    
    UNIQUE KEY uk_account_year (account_id, fiscal_year),
    FOREIGN KEY (account_id) REFERENCES chart_of_accounts(id),
    INDEX idx_fiscal_year (fiscal_year),
    INDEX idx_is_confirmed (is_confirmed)
);
```

### 3. 总账管理模块
```sql
-- 凭证表
CREATE TABLE vouchers (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    voucher_number VARCHAR(50) NOT NULL COMMENT '凭证号',
    voucher_type VARCHAR(10) NOT NULL DEFAULT 'GEN' COMMENT '凭证类型: GEN,REC,PAY,TRF',
    voucher_date DATE NOT NULL COMMENT '凭证日期',
    fiscal_period VARCHAR(7) NOT NULL COMMENT '会计期间 YYYY-MM',
    summary VARCHAR(500) COMMENT '摘要',
    attachment_count INT DEFAULT 0 COMMENT '附件张数',
    total_debit DECIMAL(15,2) NOT NULL DEFAULT 0,
    total_credit DECIMAL(15,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT 'DRAFT,REVIEWED,POSTED,CANCELLED',
    prepared_by VARCHAR(36) COMMENT '制单人',
    prepared_at TIMESTAMP COMMENT '制单时间',
    reviewed_by VARCHAR(36) COMMENT '审核人',
    reviewed_at TIMESTAMP COMMENT '审核时间',
    posted_by VARCHAR(36) COMMENT '记账人',
    posted_at TIMESTAMP COMMENT '记账时间',
    reverse_voucher_id VARCHAR(36) COMMENT '冲销凭证ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    deleted BOOLEAN DEFAULT FALSE,
    version INT DEFAULT 0,
    
    UNIQUE KEY uk_number_period (voucher_number, fiscal_period),
    INDEX idx_voucher_date (voucher_date),
    INDEX idx_fiscal_period (fiscal_period),
    INDEX idx_status (status),
    INDEX idx_prepared_by (prepared_by)
);

-- 凭证分录表
CREATE TABLE voucher_entries (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    voucher_id VARCHAR(36) NOT NULL,
    line_number INT NOT NULL COMMENT '行号',
    account_id VARCHAR(36) NOT NULL,
    summary VARCHAR(500) COMMENT '摘要',
    debit_amount DECIMAL(15,2) DEFAULT 0,
    credit_amount DECIMAL(15,2) DEFAULT 0,
    currency_code VARCHAR(10) DEFAULT 'CNY',
    exchange_rate DECIMAL(10,6) DEFAULT 1,
    original_amount DECIMAL(15,2) COMMENT '原币金额',
    quantity DECIMAL(15,4) COMMENT '数量',
    unit_price DECIMAL(15,6) COMMENT '单价',
    auxiliary_info JSON COMMENT '辅助核算信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    
    FOREIGN KEY (voucher_id) REFERENCES vouchers(id),
    FOREIGN KEY (account_id) REFERENCES chart_of_accounts(id),
    INDEX idx_voucher_id (voucher_id),
    INDEX idx_account_id (account_id),
    INDEX idx_line_number (voucher_id, line_number)
);
```

### 4. 往来账款模块
```sql
-- 客户表
CREATE TABLE customers (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    short_name VARCHAR(100),
    tax_number VARCHAR(50),
    address TEXT,
    contact_person VARCHAR(50),
    phone VARCHAR(50),
    email VARCHAR(100),
    credit_limit DECIMAL(15,2) DEFAULT 0,
    credit_days INT DEFAULT 30,
    status TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    deleted BOOLEAN DEFAULT FALSE,
    version INT DEFAULT 0,
    
    INDEX idx_code (code),
    INDEX idx_name (name),
    INDEX idx_status (status)
);

-- 供应商表
CREATE TABLE suppliers (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    short_name VARCHAR(100),
    tax_number VARCHAR(50),
    address TEXT,
    contact_person VARCHAR(50),
    phone VARCHAR(50),
    email VARCHAR(100),
    payment_terms VARCHAR(100),
    status TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    deleted BOOLEAN DEFAULT FALSE,
    version INT DEFAULT 0,
    
    INDEX idx_code (code),
    INDEX idx_name (name),
    INDEX idx_status (status)
);

-- 发票表
CREATE TABLE invoices (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    invoice_number VARCHAR(50) NOT NULL,
    invoice_type VARCHAR(20) NOT NULL COMMENT 'SALES,PURCHASE',
    customer_id VARCHAR(36),
    supplier_id VARCHAR(36),
    invoice_date DATE NOT NULL,
    due_date DATE,
    total_amount DECIMAL(15,2) NOT NULL,
    tax_amount DECIMAL(15,2) DEFAULT 0,
    net_amount DECIMAL(15,2) NOT NULL,
    currency_code VARCHAR(10) DEFAULT 'CNY',
    exchange_rate DECIMAL(10,6) DEFAULT 1,
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING,CONFIRMED,PAID,CANCELLED',
    voucher_id VARCHAR(36) COMMENT '关联凭证ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    deleted BOOLEAN DEFAULT FALSE,
    version INT DEFAULT 0,
    
    INDEX idx_invoice_number (invoice_number),
    INDEX idx_customer_id (customer_id),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_invoice_date (invoice_date),
    INDEX idx_status (status),
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    FOREIGN KEY (voucher_id) REFERENCES vouchers(id)
);

-- 应收账款表
CREATE TABLE receivables (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    customer_id VARCHAR(36) NOT NULL,
    invoice_id VARCHAR(36),
    amount DECIMAL(15,2) NOT NULL,
    paid_amount DECIMAL(15,2) DEFAULT 0,
    outstanding_amount DECIMAL(15,2) NOT NULL,
    due_date DATE,
    aging_days INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'OPEN' COMMENT 'OPEN,PARTIAL,CLOSED,WRITTEN_OFF',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (invoice_id) REFERENCES invoices(id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_due_date (due_date),
    INDEX idx_status (status),
    INDEX idx_aging_days (aging_days)
);

-- 应付账款表
CREATE TABLE payables (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    supplier_id VARCHAR(36) NOT NULL,
    invoice_id VARCHAR(36),
    amount DECIMAL(15,2) NOT NULL,
    paid_amount DECIMAL(15,2) DEFAULT 0,
    outstanding_amount DECIMAL(15,2) NOT NULL,
    due_date DATE,
    status VARCHAR(20) DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    FOREIGN KEY (invoice_id) REFERENCES invoices(id),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_due_date (due_date),
    INDEX idx_status (status)
);
```

### 5. 系统日志模块
```sql
-- 系统操作日志表
CREATE TABLE system_logs (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id VARCHAR(36),
    username VARCHAR(50),
    module VARCHAR(50) NOT NULL COMMENT '模块名称',
    operation VARCHAR(100) NOT NULL COMMENT '操作名称',
    description TEXT COMMENT '操作描述',
    ip_address VARCHAR(45),
    user_agent TEXT,
    request_url VARCHAR(500),
    request_method VARCHAR(10),
    request_params TEXT,
    response_status INT,
    execution_time BIGINT COMMENT '执行时间(毫秒)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_user_id (user_id),
    INDEX idx_module (module),
    INDEX idx_operation (operation),
    INDEX idx_created_at (created_at),
    INDEX idx_ip_address (ip_address)
);

-- 数据审计记录表
CREATE TABLE audit_trails (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    table_name VARCHAR(100) NOT NULL COMMENT '表名',
    record_id VARCHAR(36) NOT NULL COMMENT '记录ID',
    operation VARCHAR(20) NOT NULL COMMENT 'INSERT,UPDATE,DELETE',
    old_values JSON COMMENT '修改前数据',
    new_values JSON COMMENT '修改后数据',
    changed_fields TEXT COMMENT '变更字段列表',
    user_id VARCHAR(36),
    username VARCHAR(50),
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_table_record (table_name, record_id),
    INDEX idx_user_id (user_id),
    INDEX idx_operation (operation),
    INDEX idx_created_at (created_at)
);

-- 登录日志表
CREATE TABLE login_logs (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    username VARCHAR(50) NOT NULL,
    login_type VARCHAR(20) DEFAULT 'WEB' COMMENT 'WEB,MOBILE,API',
    login_status VARCHAR(20) NOT NULL COMMENT 'SUCCESS,FAILED',
    ip_address VARCHAR(45),
    user_agent TEXT,
    failure_reason VARCHAR(200),
    session_id VARCHAR(100),
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    logout_time TIMESTAMP,
    
    INDEX idx_username (username),
    INDEX idx_login_status (login_status),
    INDEX idx_login_time (login_time),
    INDEX idx_ip_address (ip_address)
);
```

---

## 📈 索引优化策略

### 主要索引设计
```sql
-- 1. 主键索引(自动创建)
-- 2. 唯一索引
-- 3. 业务查询索引
-- 4. 外键索引
-- 5. 复合索引

-- 凭证查询优化索引
CREATE INDEX idx_voucher_date_status ON vouchers(voucher_date, status);
CREATE INDEX idx_voucher_period_type ON vouchers(fiscal_period, voucher_type);

-- 账款查询优化索引
CREATE INDEX idx_receivable_customer_status ON receivables(customer_id, status);
CREATE INDEX idx_payable_supplier_status ON payables(supplier_id, status);

-- 日志查询优化索引
CREATE INDEX idx_log_user_time ON system_logs(user_id, created_at);
CREATE INDEX idx_audit_table_time ON audit_trails(table_name, created_at);
```

---

## 🔒 数据安全策略

### 1. 权限控制
- 行级安全控制
- 字段级权限控制
- 数据脱敏处理

### 2. 数据备份
- 定期全量备份
- 增量备份策略
- 事务日志备份

### 3. 审计跟踪
- 所有表包含审计字段
- 关键操作记录审计日志
- 数据变更追踪

---

## 📊 性能优化建议

### 1. 查询优化
- 避免全表扫描
- 合理使用索引
- SQL语句优化

### 2. 存储优化
- 数据类型选择
- 表分区策略
- 历史数据归档

### 3. 连接池配置
```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

---

**文档版本**: V1.0  
**创建日期**: 2025-01-19  
**适用项目**: 内部财务管理系统