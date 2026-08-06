-- ========================================================
-- 工单管理系统（GZGD）数据库初始化脚本
-- 数据库：MySQL 8.0
-- 字符集：utf8mb4
-- ========================================================

CREATE DATABASE IF NOT EXISTS gzgd
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE gzgd;

-- --------------------------------------------------------
-- 1. 设备台账
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS device
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    device_code     VARCHAR(50)  NOT NULL UNIQUE COMMENT '设备编号',
    device_name     VARCHAR(100) NOT NULL COMMENT '设备名称',
    area            VARCHAR(100) COMMENT '所属区域',
    mac             VARCHAR(50) COMMENT 'MAC 地址',
    ip              VARCHAR(50) COMMENT 'IP 地址',
    latitude        DECIMAL(10, 7) COMMENT '纬度',
    longitude       DECIMAL(10, 7) COMMENT '经度',
    camera_type     VARCHAR(50) COMMENT '摄像机类型',
    operation_type  VARCHAR(50) COMMENT '运营类型 → 工单类型',
    project_group   VARCHAR(50)  NOT NULL COMMENT '项目组名（数据隔离）',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_device_project_group (project_group),
    INDEX idx_device_device_code (device_code),
    INDEX idx_device_ip (ip)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='设备台账';

-- --------------------------------------------------------
-- 2. 人员管理
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS personnel
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    wx_open_id    VARCHAR(100) UNIQUE COMMENT '微信 OpenID',
    account       VARCHAR(50)  NOT NULL UNIQUE COMMENT '登录账号',
    name          VARCHAR(50)  NOT NULL COMMENT '姓名',
    phone         VARCHAR(20)  NOT NULL COMMENT '手机号',
    role          VARCHAR(20)  NOT NULL COMMENT '角色：内场/外场/项目管理/公司管理',
    project_group VARCHAR(50)  NOT NULL COMMENT '项目组名（数据隔离）',
    latitude      DECIMAL(10, 7) COMMENT '当前纬度',
    longitude     DECIMAL(10, 7) COMMENT '当前经度',
    status        TINYINT  DEFAULT 1 COMMENT '状态：1=启用，0=禁用',
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_personnel_project_group (project_group),
    INDEX idx_personnel_role (role),
    UNIQUE INDEX idx_personnel_account (account),
    UNIQUE INDEX idx_personnel_wx_open_id (wx_open_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='人员管理';

-- --------------------------------------------------------
-- 3. 码表
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS code_table
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    code_type   VARCHAR(50)  NOT NULL COMMENT '码表类型',
    code_value  VARCHAR(50)  NOT NULL COMMENT '码值',
    code_label  VARCHAR(100) NOT NULL COMMENT '显示名称',
    sort_order  INT DEFAULT 0 COMMENT '排序号',
    status      TINYINT DEFAULT 1 COMMENT '状态：1=启用，0=禁用',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_code_type (code_type),
    UNIQUE INDEX idx_code_type_value (code_type, code_value)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='码表';

-- --------------------------------------------------------
-- 4. 工单（核心表）
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS work_order
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    work_order_code      VARCHAR(50)  NOT NULL UNIQUE COMMENT '工单编号：YYYYMMDD/工单类型-0001',
    device_id            BIGINT COMMENT '关联设备 ID',
    fault_type           VARCHAR(100) COMMENT '故障类型',
    emergency_level      VARCHAR(20)  NOT NULL COMMENT '紧急程度：一级/二级/三级',
    work_order_type      VARCHAR(50) COMMENT '工单类型',
    reference_photo      VARCHAR(500) COMMENT '参照物照片 URL',
    publisher_id         BIGINT COMMENT '发布人 ID',
    publish_time         DATETIME COMMENT '发布时间',
    status               VARCHAR(20)  NOT NULL COMMENT '状态',
    claimer_id           BIGINT COMMENT '认领人 ID',
    claim_time           DATETIME COMMENT '认领时间',
    start_time           DATETIME COMMENT '开始作业时间',
    in_progress_time     DATETIME COMMENT '作业中时间',
    complete_time        DATETIME COMMENT '完成时间',
    confirm_time         DATETIME COMMENT '确认时间',
    response_duration    INT COMMENT '响应时长（分钟）',
    repair_duration      INT COMMENT '修复时长（分钟）',
    checkin_lat          DECIMAL(10, 7) COMMENT '签到纬度',
    checkin_lng          DECIMAL(10, 7) COMMENT '签到经度',
    checkin_time         DATETIME COMMENT '签到时间',
    checkin_photos       JSON COMMENT '签到照片 URL 数组',
    process_desc         TEXT COMMENT '排查过程文字',
    process_photos       JSON COMMENT '排查照片 URL 数组',
    end_photos           JSON COMMENT '结束照片 URL 数组',
    repair_result        VARCHAR(20) COMMENT '维修结果：fixed/not_fixed',
    fault_description    TEXT COMMENT '故障现象描述',
    special_requirements TEXT COMMENT '特殊要求',
    replaced_parts       TEXT COMMENT '更换部件',
    repairer_info        TEXT COMMENT '维修人信息（JSON）',
    is_priority          TINYINT DEFAULT 0 COMMENT '是否置顶：1=是，0=否',
    forced_close_reason  TEXT COMMENT '强制关闭原因',
    forced_close_by      BIGINT COMMENT '强制关闭发起人 ID',
    forced_close_time    DATETIME COMMENT '强制关闭发起时间',
    forced_confirm_by    BIGINT COMMENT '强制关闭确认人 ID',
    forced_confirm_time  DATETIME COMMENT '强制关闭确认时间',
    project_group        VARCHAR(50)  NOT NULL COMMENT '项目组（数据隔离）',
    created_at           DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at           DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_wo_status (status),
    INDEX idx_wo_project_group (project_group),
    UNIQUE INDEX idx_work_order_code (work_order_code),
    INDEX idx_wo_emergency (emergency_level),
    INDEX idx_wo_publisher (publisher_id),
    INDEX idx_wo_claimer (claimer_id),
    INDEX idx_wo_publish_time (publish_time),
    INDEX idx_wo_device (device_id),
    INDEX idx_wo_list (project_group, status, emergency_level, is_priority, publish_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='工单';

-- --------------------------------------------------------
-- 5. 工单操作日志
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS work_order_log
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    work_order_id BIGINT       NOT NULL COMMENT '工单 ID',
    operator_id   BIGINT COMMENT '操作人 ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    action        VARCHAR(50)  NOT NULL COMMENT '操作类型',
    action_time   DATETIME     NOT NULL COMMENT '操作时间',
    remark        VARCHAR(500) COMMENT '备注说明',
    INDEX idx_wol_work_order (work_order_id),
    INDEX idx_wol_action_time (action_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='工单操作日志';

-- --------------------------------------------------------
-- 6. SLA 配置
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS sla_config
(
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    emergency_level         VARCHAR(20) NOT NULL UNIQUE COMMENT '紧急程度',
    target_response_minutes INT         NOT NULL COMMENT '目标响应时限（分钟）',
    target_repair_minutes   INT         NOT NULL COMMENT '目标修复时限（分钟）',
    created_at              DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at              DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='SLA 配置';

-- --------------------------------------------------------
-- 7. 班组
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS team
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    team_name     VARCHAR(100) NOT NULL COMMENT '班组名称',
    project_group VARCHAR(50)  NOT NULL COMMENT '所属项目组',
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_team_project_group (project_group)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='班组';

-- --------------------------------------------------------
-- 8. 班组成员
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS team_member
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    team_id      BIGINT  NOT NULL COMMENT '班组 ID',
    personnel_id BIGINT  NOT NULL COMMENT '人员 ID',
    is_driver    TINYINT DEFAULT 0 COMMENT '是否司机：1=是，0=否',
    date         DATE    NOT NULL COMMENT '排班起始日期',
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_tm_team (team_id),
    INDEX idx_tm_date (date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='班组成员';

-- --------------------------------------------------------
-- 9. 班组车辆
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS team_vehicle
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    team_id      BIGINT       NOT NULL COMMENT '班组 ID',
    vehicle_name VARCHAR(100) NOT NULL COMMENT '车辆名称',
    date         DATE         NOT NULL COMMENT '排班起始日期',
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_tv_team (team_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='班组车辆';

-- --------------------------------------------------------
-- 10. 工单编号序列（用于行锁自增）
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS work_order_seq
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    date_key    VARCHAR(50) NOT NULL UNIQUE COMMENT '日期+工单类型键：YYYYMMDD/类型',
    current_seq INT         NOT NULL DEFAULT 0 COMMENT '当前序号',
    updated_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='工单编号序列';

-- ========================================================
-- 初始数据
-- ========================================================

-- SLA 默认配置
INSERT INTO sla_config (emergency_level, target_response_minutes, target_repair_minutes) VALUES
('一级', 60, 240),
('二级', 30, 120),
('三级', 15, 60)
ON DUPLICATE KEY UPDATE target_response_minutes = VALUES(target_response_minutes),
                        target_repair_minutes   = VALUES(target_repair_minutes);

-- 紧急程度码表
INSERT INTO code_table (code_type, code_value, code_label, sort_order) VALUES
('emergency_level', '一级', '一级', 1),
('emergency_level', '二级', '二级', 2),
('emergency_level', '三级', '三级', 3)
ON DUPLICATE KEY UPDATE code_label = VALUES(code_label);

-- 角色码表
INSERT INTO code_table (code_type, code_value, code_label, sort_order) VALUES
('role', '内场', '内场人员', 1),
('role', '外场', '外场工程师', 2),
('role', '项目管理', '项目管理人员', 3),
('role', '公司管理', '公司管理人员', 4)
ON DUPLICATE KEY UPDATE code_label = VALUES(code_label);

-- 示例项目组（如需更多请在码表管理中维护）
INSERT INTO code_table (code_type, code_value, code_label, sort_order) VALUES
('project_group', '演示项目组', '演示项目组', 1)
ON DUPLICATE KEY UPDATE code_label = VALUES(code_label);

-- 演示设备坐标
INSERT INTO device (device_code, device_name, area, ip, latitude, longitude, operation_type, project_group) VALUES
('CAM-A01', '卡口摄像机 A01', '天河区', '192.168.1.101', 23.1291, 113.2644, '维护', '演示项目组'),
('CAM-A02', '卡口摄像机 A02', '越秀区', '192.168.1.102', 23.1350, 113.2700, '维护', '演示项目组'),
('CAM-A03', '卡口摄像机 A03', '海珠区', '192.168.1.103', 23.1000, 113.2800, '维护', '演示项目组'),
('CAM-A04', '卡口摄像机 A04', '白云区', '192.168.1.104', 23.1800, 113.2500, '维护', '演示项目组')
ON DUPLICATE KEY UPDATE device_name = VALUES(device_name), area = VALUES(area), ip = VALUES(ip), latitude = VALUES(latitude), longitude = VALUES(longitude);

-- 示例测试用户（密码登录暂未启用，默认使用邮箱验证码登录）
-- 演示账号：FrenchFriesWX@outlook.com / 内场
INSERT INTO personnel (account, name, phone, role, project_group, status, latitude, longitude) VALUES
('FrenchFriesWX@outlook.com', '管理员', '13800138000', '内场', '演示项目组', 1, NULL, NULL),
('pm@gzgd.com', '项目经理', '13800138001', '项目管理', '演示项目组', 1, NULL, NULL),
('field@gzgd.com', '外场工程师', '13800138002', '外场', '演示项目组', 1, 23.1300, 113.2700)
ON DUPLICATE KEY UPDATE name = VALUES(name), latitude = VALUES(latitude), longitude = VALUES(longitude);
