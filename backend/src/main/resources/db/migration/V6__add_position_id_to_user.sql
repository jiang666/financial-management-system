-- 为用户表添加岗位ID字段
ALTER TABLE tb_user ADD COLUMN position_id VARCHAR(36) COMMENT '岗位ID' AFTER position;