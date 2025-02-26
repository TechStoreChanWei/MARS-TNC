CREATE TABLE adm_device_arm_schedule (
    id BIGINT NOT NULL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    arm_time DATETIME NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_by BIGINT,
    deleted_at DATETIME
);
CREATE INDEX idx_device_arm_schedule_device_id ON adm_device_arm_schedule(device_id);