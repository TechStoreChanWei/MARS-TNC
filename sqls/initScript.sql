CREATE TABLE tnc_type_device_map (
    device_id BIGINT NOT NULL PRIMARY KEY,
    tnc_type_id BIGINT NOT NULL,
    FOREIGN KEY (tnc_type_id) REFERENCES tnc_type(id) ON DELETE CASCADE
);

CREATE TABLE tnc_type_device_type_map (
    tnc_type_id BIGINT NOT NULL,
    device_type_id BIGINT NOT NULL,
    PRIMARY KEY (tnc_type_id, device_type_id),
    FOREIGN KEY (tnc_type_id) REFERENCES tnc_type(id) ON DELETE CASCADE,
    FOREIGN KEY (device_type_id) REFERENCES adm_device_type(id) ON DELETE CASCADE
);

CREATE TABLE tnc_type (
    id BIGINT NOT NULL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL DEFAULT '',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_by BIGINT,
    deleted_at DATETIME
);

CREATE TABLE tnc_request (
    id BIGINT NOT NULL PRIMARY KEY,
    tnc_type_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL,
    remarks VARCHAR(255) NOT NULL DEFAULT '',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_by BIGINT,
    deleted_at DATETIME,
    FOREIGN KEY (tnc_type_id) REFERENCES tnc_type(id),
    FOREIGN KEY (device_id) REFERENCES adm_device(id)
);

CREATE TABLE tnc_workflow (
    id BIGINT NOT NULL PRIMARY KEY,
    tnc_type_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    sequence INT NOT NULL DEFAULT 0,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_by BIGINT,
    deleted_at DATETIME,
    FOREIGN KEY (tnc_type_id) REFERENCES tnc_type(id)
);

CREATE TABLE tnc_workflow_checklist (
    id BIGINT NOT NULL PRIMARY KEY,
    tnc_workflow_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    details TEXT NOT NULL DEFAULT '',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_by BIGINT,
    deleted_at DATETIME,
    FOREIGN KEY (tnc_workflow_id) REFERENCES tnc_workflow(id)
);

CREATE TABLE tnc_workflow_step (
    id BIGINT NOT NULL PRIMARY KEY,
    tnc_workflow_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    sequence INT NOT NULL DEFAULT 0,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_by BIGINT,
    deleted_at DATETIME,
    FOREIGN KEY (tnc_workflow_id) REFERENCES tnc_workflow(id)
);

CREATE TABLE tnc_workflow_rule (
    id BIGINT NOT NULL PRIMARY KEY,
    tnc_workflow_step_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    action INT NOT NULL,
    request_parameter TEXT NOT NULL DEFAULT '',
    request_payload TEXT NOT NULL DEFAULT '',
    expected_result TEXT NOT NULL DEFAULT '',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_by BIGINT,
    deleted_at DATETIME,
    FOREIGN KEY (tnc_workflow_step_id) REFERENCES tnc_workflow_step(id)
);

CREATE TABLE tnc_workflow_result (
    id BIGINT NOT NULL PRIMARY KEY,
    tnc_request_id BIGINT NOT NULL,
    tnc_workflow_id BIGINT NOT NULL,
    tnc_workflow_step_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL,
    status INT NOT NULL,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_by BIGINT,
    deleted_at DATETIME,
    FOREIGN KEY (tnc_request_id) REFERENCES tnc_request(id),
    FOREIGN KEY (tnc_workflow_id) REFERENCES tnc_workflow(id),
    FOREIGN KEY (tnc_workflow_step_id) REFERENCES tnc_workflow_step(id),
    FOREIGN KEY (device_id) REFERENCES adm_device(id)
);

CREATE TABLE tnc_process_tracking (
    id BIGINT NOT NULL PRIMARY KEY,
    tnc_request_id BIGINT NOT NULL,
    action INT NOT NULL,
    entity_name VARCHAR(255) NOT NULL,
    status INT NOT NULL,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE (tnc_request_id, action, entity_name),
    FOREIGN KEY (tnc_request_id) REFERENCES tnc_request(id)
);

