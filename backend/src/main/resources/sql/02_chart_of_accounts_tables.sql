-- 02_chart_of_accounts_tables.sql
-- 科目管理模块数据库表创建脚本

-- 使用数据库
USE financial_db;

-- 1. 会计科目表
CREATE TABLE IF NOT EXISTS chart_of_accounts (
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
    path VARCHAR(500) COMMENT '科目路径',
    full_name VARCHAR(500) COMMENT '完整科目名称',
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
    INDEX idx_status (status),
    INDEX idx_path (path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会计科目表';

-- 2. 期初余额表
CREATE TABLE IF NOT EXISTS initial_balances (
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
    confirmed_at TIMESTAMP NULL,
    confirmed_by VARCHAR(36),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    
    UNIQUE KEY uk_account_year (account_id, fiscal_year),
    FOREIGN KEY (account_id) REFERENCES chart_of_accounts(id),
    INDEX idx_fiscal_year (fiscal_year),
    INDEX idx_is_confirmed (is_confirmed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='期初余额表';

-- 3. 会计期间表
CREATE TABLE IF NOT EXISTS accounting_periods (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    fiscal_year INT NOT NULL COMMENT '会计年度',
    period INT NOT NULL COMMENT '会计期间(1-12月)',
    period_name VARCHAR(50) NOT NULL COMMENT '期间名称',
    start_date DATE NOT NULL COMMENT '期间开始日期',
    end_date DATE NOT NULL COMMENT '期间结束日期',
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' COMMENT '期间状态: OPEN,CLOSED,LOCKED',
    is_current BOOLEAN DEFAULT FALSE COMMENT '是否当前期间',
    is_adjustment BOOLEAN DEFAULT FALSE COMMENT '是否调整期',
    closed_at TIMESTAMP NULL COMMENT '结账时间',
    closed_by VARCHAR(36) COMMENT '结账人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    
    UNIQUE KEY uk_year_period (fiscal_year, period),
    INDEX idx_status (status),
    INDEX idx_is_current (is_current),
    INDEX idx_start_date (start_date),
    INDEX idx_end_date (end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会计期间表';

-- 4. 插入默认会计科目
INSERT INTO chart_of_accounts (code, name, level, account_type, balance_direction, is_leaf, path, full_name) VALUES
-- 资产类
('1001', '库存现金', 1, 'ASSET', 'DEBIT', TRUE, '1001', '库存现金'),
('1002', '银行存款', 1, 'ASSET', 'DEBIT', TRUE, '1002', '银行存款'),
('1012', '其他货币资金', 1, 'ASSET', 'DEBIT', TRUE, '1012', '其他货币资金'),
('1101', '交易性金融资产', 1, 'ASSET', 'DEBIT', TRUE, '1101', '交易性金融资产'),
('1122', '应收账款', 1, 'ASSET', 'DEBIT', TRUE, '1122', '应收账款'),
('1123', '预付账款', 1, 'ASSET', 'DEBIT', TRUE, '1123', '预付账款'),
('1221', '其他应收款', 1, 'ASSET', 'DEBIT', TRUE, '1221', '其他应收款'),
('1401', '材料采购', 1, 'ASSET', 'DEBIT', TRUE, '1401', '材料采购'),
('1403', '原材料', 1, 'ASSET', 'DEBIT', TRUE, '1403', '原材料'),
('1405', '库存商品', 1, 'ASSET', 'DEBIT', TRUE, '1405', '库存商品'),
('1601', '固定资产', 1, 'ASSET', 'DEBIT', TRUE, '1601', '固定资产'),
('1602', '累计折旧', 1, 'ASSET', 'CREDIT', TRUE, '1602', '累计折旧'),
('1701', '无形资产', 1, 'ASSET', 'DEBIT', TRUE, '1701', '无形资产'),
('1702', '累计摊销', 1, 'ASSET', 'CREDIT', TRUE, '1702', '累计摊销'),
('1801', '长期待摊费用', 1, 'ASSET', 'DEBIT', TRUE, '1801', '长期待摊费用'),

-- 负债类
('2001', '短期借款', 1, 'LIABILITY', 'CREDIT', TRUE, '2001', '短期借款'),
('2201', '应付票据', 1, 'LIABILITY', 'CREDIT', TRUE, '2201', '应付票据'),
('2202', '应付账款', 1, 'LIABILITY', 'CREDIT', TRUE, '2202', '应付账款'),
('2203', '预收账款', 1, 'LIABILITY', 'CREDIT', TRUE, '2203', '预收账款'),
('2211', '应付职工薪酬', 1, 'LIABILITY', 'CREDIT', TRUE, '2211', '应付职工薪酬'),
('2221', '应交税费', 1, 'LIABILITY', 'CREDIT', TRUE, '2221', '应交税费'),
('2241', '其他应付款', 1, 'LIABILITY', 'CREDIT', TRUE, '2241', '其他应付款'),
('2501', '长期借款', 1, 'LIABILITY', 'CREDIT', TRUE, '2501', '长期借款'),

-- 权益类
('4001', '实收资本', 1, 'EQUITY', 'CREDIT', TRUE, '4001', '实收资本'),
('4002', '资本公积', 1, 'EQUITY', 'CREDIT', TRUE, '4002', '资本公积'),
('4101', '盈余公积', 1, 'EQUITY', 'CREDIT', TRUE, '4101', '盈余公积'),
('4103', '本年利润', 1, 'EQUITY', 'CREDIT', TRUE, '4103', '本年利润'),
('4104', '利润分配', 1, 'EQUITY', 'CREDIT', TRUE, '4104', '利润分配'),

-- 成本类
('5001', '生产成本', 1, 'COST', 'DEBIT', TRUE, '5001', '生产成本'),
('5101', '制造费用', 1, 'COST', 'DEBIT', TRUE, '5101', '制造费用'),
('5201', '劳务成本', 1, 'COST', 'DEBIT', TRUE, '5201', '劳务成本'),

-- 损益类 - 收入
('6001', '主营业务收入', 1, 'REVENUE', 'CREDIT', TRUE, '6001', '主营业务收入'),
('6051', '其他业务收入', 1, 'REVENUE', 'CREDIT', TRUE, '6051', '其他业务收入'),
('6101', '公允价值变动损益', 1, 'REVENUE', 'CREDIT', TRUE, '6101', '公允价值变动损益'),
('6111', '投资收益', 1, 'REVENUE', 'CREDIT', TRUE, '6111', '投资收益'),
('6301', '营业外收入', 1, 'REVENUE', 'CREDIT', TRUE, '6301', '营业外收入'),

-- 损益类 - 费用
('6401', '主营业务成本', 1, 'COST', 'DEBIT', TRUE, '6401', '主营业务成本'),
('6402', '其他业务成本', 1, 'COST', 'DEBIT', TRUE, '6402', '其他业务成本'),
('6403', '营业税金及附加', 1, 'COST', 'DEBIT', TRUE, '6403', '营业税金及附加'),
('6601', '销售费用', 1, 'COST', 'DEBIT', TRUE, '6601', '销售费用'),
('6602', '管理费用', 1, 'COST', 'DEBIT', TRUE, '6602', '管理费用'),
('6603', '财务费用', 1, 'COST', 'DEBIT', TRUE, '6603', '财务费用'),
('6701', '资产减值损失', 1, 'COST', 'DEBIT', TRUE, '6701', '资产减值损失'),
('6711', '营业外支出', 1, 'COST', 'DEBIT', TRUE, '6711', '营业外支出'),
('6801', '所得税费用', 1, 'COST', 'DEBIT', TRUE, '6801', '所得税费用');

-- 5. 插入2025年的会计期间
INSERT INTO accounting_periods (fiscal_year, period, period_name, start_date, end_date, is_current) VALUES
(2025, 1, '2025年1月', '2025-01-01', '2025-01-31', TRUE),
(2025, 2, '2025年2月', '2025-02-01', '2025-02-28', FALSE),
(2025, 3, '2025年3月', '2025-03-01', '2025-03-31', FALSE),
(2025, 4, '2025年4月', '2025-04-01', '2025-04-30', FALSE),
(2025, 5, '2025年5月', '2025-05-01', '2025-05-31', FALSE),
(2025, 6, '2025年6月', '2025-06-01', '2025-06-30', FALSE),
(2025, 7, '2025年7月', '2025-07-01', '2025-07-31', FALSE),
(2025, 8, '2025年8月', '2025-08-01', '2025-08-31', FALSE),
(2025, 9, '2025年9月', '2025-09-01', '2025-09-30', FALSE),
(2025, 10, '2025年10月', '2025-10-01', '2025-10-31', FALSE),
(2025, 11, '2025年11月', '2025-11-01', '2025-11-30', FALSE),
(2025, 12, '2025年12月', '2025-12-01', '2025-12-31', FALSE);