-- 创建系统参数历史记录表
CREATE TABLE IF NOT EXISTS system_parameter_history (
    id VARCHAR(36) PRIMARY KEY COMMENT '主键ID',
    param_key VARCHAR(100) NOT NULL COMMENT '参数键',
    old_value TEXT COMMENT '旧值',
    new_value TEXT COMMENT '新值',
    operation VARCHAR(20) NOT NULL COMMENT '操作类型',
    operator VARCHAR(50) COMMENT '操作人',
    operate_time BIGINT NOT NULL COMMENT '操作时间',
    remark VARCHAR(500) COMMENT '备注',
    operate_ip VARCHAR(50) COMMENT '操作IP',
    param_type VARCHAR(20) COMMENT '参数类型',
    category VARCHAR(50) COMMENT '参数分类'
);

-- 创建索引
CREATE INDEX idx_param_history_key ON system_parameter_history(param_key);
CREATE INDEX idx_param_history_time ON system_parameter_history(operate_time);
CREATE INDEX idx_param_history_operator ON system_parameter_history(operator);