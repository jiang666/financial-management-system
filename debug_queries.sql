-- 调试汇率历史查询问题的SQL语句
-- 请在数据库中执行这些查询并提供结果

-- 1. 查看JPY币种的基本信息
SELECT id, code, name, is_base, status, deleted FROM currencies WHERE code = 'JPY';

-- 2. 查看exchange_rates表结构
DESCRIBE exchange_rates;

-- 3. 查看所有JPY相关的汇率记录
SELECT 
    er.*,
    c1.code as from_currency_code,
    c2.code as to_currency_code
FROM exchange_rates er
LEFT JOIN currencies c1 ON er.from_currency_id = c1.id
LEFT JOIN currencies c2 ON er.to_currency_id = c2.id
WHERE er.from_currency_id = (SELECT id FROM currencies WHERE code = 'JPY' LIMIT 1)
   OR er.to_currency_id = (SELECT id FROM currencies WHERE code = 'JPY' LIMIT 1)
ORDER BY er.effective_date DESC;

-- 4. 检查是否有deleted=0的记录
SELECT COUNT(*) as total_count FROM exchange_rates WHERE deleted = 0;

-- 5. 检查JPY相关的有效记录
SELECT COUNT(*) as jpy_active_count 
FROM exchange_rates er
WHERE er.deleted = 0 
  AND er.status = 1
  AND (er.from_currency_id = (SELECT id FROM currencies WHERE code = 'JPY' LIMIT 1)
       OR er.to_currency_id = (SELECT id FROM currencies WHERE code = 'JPY' LIMIT 1));