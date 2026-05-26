-- Skill 配置表
CREATE TABLE IF NOT EXISTS skill_config (
    id SERIAL PRIMARY KEY,
    skill_name VARCHAR(255) NOT NULL,
    description TEXT,
    skill_path VARCHAR(1024) NOT NULL,
    is_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_skill_config_name ON skill_config(skill_name);
CREATE INDEX IF NOT EXISTS idx_skill_config_enabled ON skill_config(is_enabled);
