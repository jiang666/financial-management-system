-- 创建币种汇率模块相关表

-- 币种表
CREATE TABLE currencies (
    id VARCHAR(36) NOT NULL COMMENT '主键ID',
    code VARCHAR(3) NOT NULL COMMENT '币种代码',
    name VARCHAR(50) NOT NULL COMMENT '币种名称',
    name_en VARCHAR(50) COMMENT '英文名称',
    symbol VARCHAR(10) NOT NULL COMMENT '币种符号',
    is_base TINYINT NOT NULL DEFAULT 0 COMMENT '是否为基准币种(0-否,1-是)',
    decimal_places INT NOT NULL DEFAULT 2 COMMENT '小数位数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0-禁用,1-启用)',
    sort_order INT COMMENT '排序',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除,1-已删除)',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NOT NULL COMMENT '更新时间',
    created_by VARCHAR(100) NOT NULL COMMENT '创建人',
    updated_by VARCHAR(100) NOT NULL COMMENT '更新人',
    PRIMARY KEY (id),
    UNIQUE KEY uk_currencies_code (code, deleted),
    INDEX idx_currencies_status (status),
    INDEX idx_currencies_base (is_base),
    INDEX idx_currencies_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='币种表';

-- 汇率表
CREATE TABLE exchange_rates (
    id VARCHAR(36) NOT NULL COMMENT '主键ID',
    from_currency_id VARCHAR(36) NOT NULL COMMENT '源币种ID',
    to_currency_id VARCHAR(36) NOT NULL COMMENT '目标币种ID',
    rate DECIMAL(20,8) NOT NULL COMMENT '汇率值',
    effective_date BIGINT NOT NULL COMMENT '生效时间',
    expiry_date BIGINT COMMENT '失效时间',
    source VARCHAR(20) NOT NULL DEFAULT 'MANUAL' COMMENT '汇率来源(MANUAL-手工,AUTO-自动,FRANKFURTER-API)',
    remark VARCHAR(200) COMMENT '备注',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0-禁用,1-启用)',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除,1-已删除)',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NOT NULL COMMENT '更新时间',
    created_by VARCHAR(100) NOT NULL COMMENT '创建人',
    updated_by VARCHAR(100) NOT NULL COMMENT '更新人',
    PRIMARY KEY (id),
    INDEX idx_exchange_rates_from_to (from_currency_id, to_currency_id),
    INDEX idx_exchange_rates_effective (effective_date),
    INDEX idx_exchange_rates_source (source),
    INDEX idx_exchange_rates_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='汇率表';

-- 插入默认币种数据
INSERT INTO currencies (id, code, name, name_en, symbol, is_base, decimal_places, status, sort_order, deleted, created_at, updated_at, created_by, updated_by) VALUES
('currency-cny-001', 'CNY', '人民币', 'Chinese Yuan', '¥', 1, 2, 1, 1, 0, UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 'system', 'system'),
('currency-usd-001', 'USD', '美元', 'US Dollar', '$', 0, 2, 1, 2, 0, UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 'system', 'system'),
('currency-eur-001', 'EUR', '欧元', 'Euro', '€', 0, 2, 1, 3, 0, UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 'system', 'system'),
('currency-gbp-001', 'GBP', '英镑', 'British Pound', '£', 0, 2, 1, 4, 0, UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 'system', 'system'),
('currency-jpy-001', 'JPY', '日元', 'Japanese Yen', '¥', 0, 0, 1, 5, 0, UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 'system', 'system'),
('currency-hkd-001', 'HKD', '港币', 'Hong Kong Dollar', 'HK$', 0, 2, 1, 6, 0, UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 'system', 'system');