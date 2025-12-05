CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  email VARCHAR(100) UNIQUE,
  role VARCHAR(20) DEFAULT 'USER',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_username (username),
  INDEX idx_email (email)
);

CREATE TABLE IF NOT EXISTS service_configs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  service_type VARCHAR(20) NOT NULL,
  service_name VARCHAR(100) NOT NULL,
  base_url VARCHAR(500) NOT NULL,
  username VARCHAR(100),
  password VARCHAR(255),
  api_token VARCHAR(500),
  connection_status VARCHAR(20) DEFAULT 'UNKNOWN',
  last_test_time TIMESTAMP NULL,
  created_by BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_service_type (service_type),
  INDEX idx_created_by (created_by)
);

CREATE TABLE IF NOT EXISTS sync_tasks (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_name VARCHAR(100) NOT NULL,
  description TEXT,
  jira_config_id BIGINT NOT NULL,
  radicate_config_id BIGINT NOT NULL,
  jql_expression TEXT NOT NULL,
  cron_expression VARCHAR(100) NOT NULL,
  is_enabled BOOLEAN DEFAULT TRUE,
  sync_status VARCHAR(20) DEFAULT 'IDLE',
  last_sync_time TIMESTAMP NULL,
  next_sync_time TIMESTAMP NULL,
  created_by BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_task_status (is_enabled, sync_status),
  INDEX idx_next_sync (next_sync_time)
);

CREATE TABLE IF NOT EXISTS sync_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  start_time TIMESTAMP NOT NULL,
  end_time TIMESTAMP NULL,
  status VARCHAR(20) NOT NULL,
  total_items INTEGER DEFAULT 0,
  processed_items INTEGER DEFAULT 0,
  failed_items INTEGER DEFAULT 0,
  error_message TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_task_time (task_id, start_time DESC),
  INDEX idx_status (status)
);

CREATE TABLE IF NOT EXISTS sync_details (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  record_id BIGINT NOT NULL,
  item_id VARCHAR(100) NOT NULL,
  action VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL,
  error_message TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_record_item (record_id, item_id),
  INDEX idx_action_status (action, status)
);

CREATE TABLE IF NOT EXISTS conflict_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id BIGINT NOT NULL,
  item_id VARCHAR(255) NOT NULL,
  item_type VARCHAR(50) NOT NULL,
  source_data JSON NOT NULL,
  target_data JSON NOT NULL,
  conflict_type VARCHAR(100) NOT NULL,
  resolution VARCHAR(50),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  resolved_at TIMESTAMP NULL,
  INDEX idx_conflict_task (task_id),
  INDEX idx_conflict_item (item_id, item_type)
);

INSERT INTO users (username, password, email, role) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBaUKk7h.T0mUO', 'admin@jirasync.com', 'ADMIN')
ON DUPLICATE KEY UPDATE username=username;
