CREATE TABLE IF NOT EXISTS members (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    login_id VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS schedules (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    plan_date DATE NOT NULL,
    content VARCHAR(100) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_schedules_member FOREIGN KEY (member_id) REFERENCES members (id) ON DELETE CASCADE
);

SET @display_order_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'schedules'
      AND COLUMN_NAME = 'display_order'
);

SET @display_order_ddl := IF(
    @display_order_exists = 0,
    'ALTER TABLE schedules ADD COLUMN display_order INT NOT NULL DEFAULT 0',
    'SELECT 1'
);

PREPARE stmt FROM @display_order_ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE schedules
SET display_order = id
WHERE display_order = 0;
