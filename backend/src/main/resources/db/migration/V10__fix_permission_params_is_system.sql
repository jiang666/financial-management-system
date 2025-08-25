-- V10: 修复权限参数的is_system标识
-- 将权限相关参数的is_system设为0，允许管理员修改

UPDATE system_parameters 
SET is_system = 0,
    updated_at = UNIX_TIMESTAMP() * 1000,
    updated_by = 'migration'
WHERE param_key IN (
    'permission.creator_can_edit',
    'permission.auditor_can_edit',
    'permission.bookkeeper_can_edit',
    'permission.cross_period_query',
    'permission.export_roles',
    'login.lock_enabled',
    'login.max_attempts'
);

-- 确保所有权限和登录相关参数都可以编辑
UPDATE system_parameters 
SET editable = 1,
    updated_at = UNIX_TIMESTAMP() * 1000,
    updated_by = 'migration'
WHERE (param_key LIKE 'permission.%' OR param_key LIKE 'login.%')
  AND editable = 0;