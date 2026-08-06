-- 测试用 H2 表结构（简化版）
CREATE TABLE IF NOT EXISTS device
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_code     VARCHAR(50)  NOT NULL UNIQUE,
    device_name     VARCHAR(100) NOT NULL,
    area            VARCHAR(100),
    mac             VARCHAR(50),
    ip              VARCHAR(50),
    latitude        DECIMAL(10, 7),
    longitude       DECIMAL(10, 7),
    camera_type     VARCHAR(50),
    operation_type  VARCHAR(50),
    project_group   VARCHAR(50)  NOT NULL,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS personnel
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    wx_open_id    VARCHAR(100) UNIQUE,
    account       VARCHAR(50)  NOT NULL UNIQUE,
    name          VARCHAR(50)  NOT NULL,
    phone         VARCHAR(20)  NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    project_group VARCHAR(50)  NOT NULL,
    latitude      DECIMAL(10, 7),
    longitude     DECIMAL(10, 7),
    status        TINYINT  DEFAULT 1,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS team
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_name     VARCHAR(100) NOT NULL,
    project_group VARCHAR(50)  NOT NULL,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS work_order
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_order_code      VARCHAR(50)  NOT NULL UNIQUE,
    device_id            BIGINT,
    fault_type           VARCHAR(100),
    emergency_level      VARCHAR(20)  NOT NULL,
    work_order_type      VARCHAR(50),
    reference_photo      VARCHAR(500),
    publisher_id         BIGINT,
    publish_time         DATETIME,
    status               VARCHAR(20)  NOT NULL,
    claimer_id           BIGINT,
    claim_time           DATETIME,
    start_time           DATETIME,
    in_progress_time     DATETIME,
    complete_time        DATETIME,
    confirm_time         DATETIME,
    response_duration    INT,
    repair_duration      INT,
    checkin_lat          DECIMAL(10, 7),
    checkin_lng          DECIMAL(10, 7),
    checkin_time         DATETIME,
    checkin_photos       VARCHAR(2000),
    process_desc         TEXT,
    process_photos       VARCHAR(2000),
    end_photos           VARCHAR(2000),
    repair_result        VARCHAR(20),
    fault_description    TEXT,
    special_requirements TEXT,
    replaced_parts       TEXT,
    repairer_info        TEXT,
    is_priority          TINYINT DEFAULT 0,
    forced_close_reason  TEXT,
    forced_close_by      BIGINT,
    forced_close_time    DATETIME,
    forced_confirm_by    BIGINT,
    forced_confirm_time  DATETIME,
    project_group        VARCHAR(50)  NOT NULL,
    created_at           DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME DEFAULT CURRENT_TIMESTAMP
);
