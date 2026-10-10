-- 补齐初始化中断导致缺失的系统表及种子数据
-- 密码统一为临时密码 Admin@123456，登录后请立即修改
-- 可重复执行：先清理再重建

DROP TABLE IF EXISTS sys_notice_log CASCADE;
DROP TABLE IF EXISTS sys_notice_channel CASCADE;
DROP TABLE IF EXISTS sys_user_role CASCADE;
DROP TABLE IF EXISTS sys_role CASCADE;
DROP TABLE IF EXISTS sys_user CASCADE;

CREATE TABLE sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    work_no VARCHAR(50) NOT NULL UNIQUE,
    phone VARCHAR(20),
    email VARCHAR(100),
    wecom_user_id VARCHAR(100),
    dingtalk_user_id VARCHAR(100),
    org_id BIGINT REFERENCES sys_party_org(id),
    org_name VARCHAR(150),
    member_id BIGINT REFERENCES party_member(id) ON DELETE SET NULL,
    status SMALLINT DEFAULT 1,
    avatar VARCHAR(255),
    last_login_time TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_role (
    id BIGSERIAL PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    sort_order INT DEFAULT 0,
    status SMALLINT DEFAULT 1,
    permissions JSONB,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_user_role (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES sys_user(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES sys_role(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_role UNIQUE (user_id, role_id)
);

CREATE TABLE sys_notice_channel (
    id BIGSERIAL PRIMARY KEY,
    channel_code VARCHAR(50) NOT NULL UNIQUE,
    channel_name VARCHAR(100) NOT NULL,
    channel_type SMALLINT NOT NULL,
    enabled SMALLINT DEFAULT 1,
    config_json JSONB,
    template_json JSONB,
    remark VARCHAR(255),
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_notice_log (
    id BIGSERIAL PRIMARY KEY,
    notice_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    receiver_type VARCHAR(20) DEFAULT 'USER',
    receiver_id BIGINT,
    receiver_name VARCHAR(100),
    receiver_target VARCHAR(150),
    channel_code VARCHAR(50) NOT NULL,
    send_status SMALLINT DEFAULT 0,
    provider_message_id VARCHAR(200),
    dedupe_key VARCHAR(200) UNIQUE,
    error_msg TEXT,
    is_read SMALLINT DEFAULT 0,
    related_member_id BIGINT,
    related_step_code INT,
    send_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    read_time TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notice_rec_read ON sys_notice_log(receiver_id, is_read);

INSERT INTO sys_role (id, role_code, role_name, description, sort_order, status, permissions) VALUES
(1, 'SYS_ADMIN', '超级管理员', '全平台系统与安全超级管理员，唯一独享用户管理与多渠道通知配置，拥有全平台所有模块增删改查权限', 1, 1, '["user:manage", "role:manage", "notice:channel_manage", "notice:send", "workbench:view", "workbench:create_applicant", "workbench:advance", "workbench:audit", "workbench:transfer", "workbench:export", "workbench:block_override", "roster:view", "roster:create", "roster:edit", "roster:import", "roster:export", "meeting:view", "meeting:create", "meeting:edit", "meeting:delete", "meeting:tags_manage", "meeting:export", "honor:view", "honor:create", "honor:edit", "honor:delete", "honor:export", "template:view", "template:upload", "template:reset", "cockpit:view", "notice:view"]'),
(2, 'GENERAL_BRANCH_ADMIN', '党总支管理员', '集团党总支党务中枢与组织员，拥有党总支本级及直管三家支部的全部党务业务权限，独享第20/25步审批批复权及指标调控、一人一档归档、模板导入', 2, 1, '["workbench:view", "workbench:create_applicant", "workbench:advance", "workbench:audit", "workbench:transfer", "workbench:export", "workbench:block_override", "roster:view", "roster:create", "roster:edit", "roster:import", "roster:export", "meeting:view", "meeting:create", "meeting:edit", "meeting:delete", "meeting:tags_manage", "meeting:export", "honor:view", "honor:create", "honor:edit", "honor:delete", "honor:export", "template:view", "template:upload", "template:reset", "cockpit:view", "notice:view", "notice:send"]'),
(3, 'BRANCH_ADMIN', '支部管理员', '子公司党支部书记及支委，严格锁定本支部业务数据，负责本支部流程发起推进、名册维护、组织生活记录与删除', 3, 1, '["workbench:view", "workbench:create_applicant", "workbench:advance", "workbench:export", "roster:view", "roster:create", "roster:edit", "roster:import", "roster:export", "meeting:view", "meeting:create", "meeting:edit", "meeting:delete", "meeting:export", "honor:view", "honor:create", "honor:edit", "honor:delete", "honor:export", "template:view", "cockpit:view", "notice:view"]'),
(4, 'PARTY_MEMBER', '普通在册党员 / 发展成员本人', '普通在册党员及发展成员，仅限查看个人成长全景档案与个人待办通知', 4, 1, '["workbench:view", "member:self_view", "notice:view", "cockpit:view"]');

ALTER SEQUENCE sys_role_id_seq RESTART WITH 10;

INSERT INTO sys_user (id, username, password, real_name, work_no, phone, email, org_id, org_name, status, last_login_time) VALUES
(1, 'admin', crypt('Admin@123456', gen_salt('bf', 12)), '系统管理员', 'SYS-ADMIN-01', '13888880001', 'admin@honghe-data.com', 1, '中共红河数据产业集团有限公司总支部委员会', 1, CURRENT_TIMESTAMP),
(2, 'yanghai', crypt('Admin@123456', gen_salt('bf', 12)), '杨海', 'HH-JT-005', '13987301005', 'yanghai@honghe-data.com', 1, '中共红河数据产业集团有限公司总支部委员会', 1, CURRENT_TIMESTAMP),
(3, 'liweimin', crypt('Admin@123456', gen_salt('bf', 12)), '李卫民', 'HH-HS-001', '13987302001', 'liweimin@hongshu-info.com', 2, '中共红河红数信息技术服务有限公司支部委员会', 1, CURRENT_TIMESTAMP),
(4, 'liujianhua', crypt('Admin@123456', gen_salt('bf', 12)), '刘建华', 'HH-MC-001', '13987303001', 'liujianhua@mici-tech.com', 3, '中共云南幂次科技有限公司支部委员会', 1, CURRENT_TIMESTAMP),
(5, 'chenming', crypt('Admin@123456', gen_salt('bf', 12)), '陈明', 'HH-LD-001', '13987304001', 'chenming@lianda-tech.com', 4, '中共红河链达科技有限公司支部委员会', 1, CURRENT_TIMESTAMP),
(6, 'zhangqiang', crypt('Admin@123456', gen_salt('bf', 12)), '张强', 'HH-HS-012', '13987302012', 'zhangqiang@hongshu-info.com', 2, '中共红河红数信息技术服务有限公司支部委员会', 1, CURRENT_TIMESTAMP),
(7, 'linyuhan', crypt('Admin@123456', gen_salt('bf', 12)), '林雨涵', 'HH-MC-035', '13987303035', 'linyuhan@mici-tech.com', 3, '中共云南幂次科技有限公司支部委员会', 1, CURRENT_TIMESTAMP);

ALTER SEQUENCE sys_user_id_seq RESTART WITH 100;

INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 3),
(5, 3),
(6, 4),
(7, 4);

INSERT INTO sys_notice_channel (id, channel_code, channel_name, channel_type, enabled, config_json, remark) VALUES
(1, 'IN_APP', '系统站内信', 1, 1, '{}', '通知存入数据库，接收人登录后查看'),
(2, 'WECHAT_WORK', '企业微信应用消息', 4, 0, '{"corpId":"","agentId":"","secretEnv":"PARTY_NOTICE_WECOM_SECRET"}', '企业自建应用；请配置可见范围与服务器可信IP'),
(3, 'DINGTALK', '钉钉工作通知', 5, 0, '{"corpId":"","clientId":"","agentId":"","clientSecretEnv":"PARTY_NOTICE_DINGTALK_SECRET"}', '企业内部应用工作通知；返回异步任务编号，不等同送达'),
(4, 'SMS', '阿里云短信', 2, 0, '{"provider":"ALIYUN","signName":"","accessKeyIdEnv":"PARTY_NOTICE_SMS_KEY_ID","accessKeySecretEnv":"PARTY_NOTICE_SMS_KEY_SECRET"}', '使用已审核的签名及通知模板，按实际发送计费'),
(5, 'EMAIL', '邮件通知（SMTP）', 3, 0, '{"host":"","port":465,"security":"SSL","username":"","from":"","passwordEnv":"PARTY_NOTICE_SMTP_PASSWORD"}', '需要邮箱SMTP授权码，支持SSL和STARTTLS');

ALTER SEQUENCE sys_notice_channel_id_seq RESTART WITH 10;
