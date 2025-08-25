-- 添加部门类型字段
ALTER TABLE departments ADD COLUMN type VARCHAR(20) DEFAULT '部门' COMMENT '部门类型' AFTER name;

-- 更新现有数据的类型
UPDATE departments SET type = '公司' WHERE level = 1;
UPDATE departments SET type = '部门' WHERE level = 2;
UPDATE departments SET type = '小组' WHERE level > 2;