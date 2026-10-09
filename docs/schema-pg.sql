-- ==============================================================================
-- 红河数据产业集团有限公司党总支 · 智慧党建管理系统
-- 数据库初始化脚本 (PostgreSQL 14+ 专用 DDL 及基础种子数据)
-- 包含：党员花名册、25步发展流转、文档模板管理(默认+导入兼具)、大屏指标统计
-- ==============================================================================

DROP TABLE IF EXISTS sys_notice_log CASCADE;
DROP TABLE IF EXISTS sys_notice_channel CASCADE;
DROP TABLE IF EXISTS sys_user_role CASCADE;
DROP TABLE IF EXISTS sys_role CASCADE;
DROP TABLE IF EXISTS sys_user CASCADE;
DROP TABLE IF EXISTS party_material_file CASCADE;
DROP TABLE IF EXISTS party_cultivator_relation CASCADE;
DROP TABLE IF EXISTS party_step_record CASCADE;
DROP TABLE IF EXISTS party_doc_template CASCADE;
DROP TABLE IF EXISTS party_member CASCADE;
DROP TABLE IF EXISTS party_annual_quota CASCADE;
DROP TABLE IF EXISTS sys_party_org CASCADE;

-- 1. 创建党组织架构表
CREATE TABLE sys_party_org (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    org_name VARCHAR(150) NOT NULL,
    org_code VARCHAR(50) NOT NULL UNIQUE,
    org_type SMALLINT NOT NULL, -- 1: 集团党总支, 2: 子公司党支部, 3: 直属党小组
    has_approval_right BOOLEAN DEFAULT FALSE, -- 是否有审批权
    leader_name VARCHAR(50), -- 书记姓名
    business_scope VARCHAR(255), -- 业务职责
    sort_order INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_party_org IS '企业党组织架构层级表';

-- 2. 党员/发展成员全生命周期档案表 (支持【所有党员花名册】与【发展党员流转】)
CREATE TABLE party_member (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    org_id BIGINT NOT NULL REFERENCES sys_party_org(id),
    dept_name VARCHAR(100),
    work_no VARCHAR(50) NOT NULL UNIQUE,
    real_name VARCHAR(50) NOT NULL,
    id_card VARCHAR(18) NOT NULL UNIQUE,
    gender SMALLINT DEFAULT 1,                 -- 1: 男, 2: 女
    birth_date DATE,
    education VARCHAR(50),
    job_title VARCHAR(100),                    -- 岗位职务
    
    -- 【党员花名册核心字段】
    party_status SMALLINT NOT NULL DEFAULT 1,  -- 1: 正式党员, 2: 预备党员, 3: 发展对象, 4: 入党积极分子, 5: 入党申请人
    party_post VARCHAR(100) DEFAULT '普通党员', -- 党内职务 (党总支书记、支部书记、副书记、组织委员、纪检委员、宣传委员、党小组长、党员)
    party_standing_years INT DEFAULT 0,        -- 党龄 (年)
    join_party_date DATE,                      -- 入党日期 (接收预备党员日期)
    official_party_date DATE,                  -- 正式转正日期
    dues_status SMALLINT DEFAULT 1,            -- 党费缴纳状态: 1正常 2本月待缴 3异常
    national_party_code VARCHAR(50),           -- 全国党员管理信息系统唯一编码
    annual_study_hours INT DEFAULT 0,          -- 年度集中培训时长 (学时)
    study_target_hours INT DEFAULT 40,         -- 目标集中培训时长 (标准40学时达标)
    
    -- 组织关系转接时间及去向
    origin_branch VARCHAR(150),                -- 原所在党支部 (转入前所在支部/原发展支部)
    transfer_in_date DATE,                     -- 转入本支部时间
    transfer_out_date DATE,                    -- 转出本支部时间
    transfer_out_branch VARCHAR(150),          -- 转出支部名称
    
    -- 国企骨干特色标签
    is_frontline BOOLEAN DEFAULT FALSE,        -- 生产/业务一线
    is_technical_talent BOOLEAN DEFAULT FALSE,  -- 大数据/技术研发骨干
    is_dual_cultivate BOOLEAN DEFAULT FALSE,    -- 双培养骨干
    
    -- 流程状态跟踪 (针对发展中成员)
    current_stage SMALLINT NOT NULL DEFAULT 1, -- 1申请人 2积极分子 3发展对象 4预备党员 5正式党员
    current_step INT NOT NULL DEFAULT 1,       -- 1~25
    step_status SMALLINT NOT NULL DEFAULT 1,   -- 1进行中 2待审核 3完成 4临期预警 5强制阻断
    
    -- 关键核心时间节点 (合规防错引擎校验字段)
    apply_date DATE,                           -- 递交入党申请书日期
    first_talk_date DATE,                      -- 首次谈话日期(须在apply_date+30天内)
    activist_date DATE,                        -- 积极分子备案日期
    target_date DATE,                          -- 确定发展对象日期(须距离activist_date满1年)
    exam_pass_date DATE,                       -- 集中培训考核合格日期(不少于24学时)
    discipline_check_pass SMALLINT DEFAULT 0,  -- 纪检廉政审核(1通过 0未审 -1否决)
    probationary_date DATE,                    -- 支部大会接收预备党员日期
    committee_pass_date DATE,                  -- 党总支/党委集体审批日期(须在大会+90天内)
    official_apply_date DATE,                  -- 提出转正申请日期
    official_date DATE,                        -- 党总支/党委转正审批日期
    
    status SMALLINT DEFAULT 1,                 -- 1正常在册 2延期考察 3取消资格 4调离转出
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_party_member_org ON party_member(org_id);
CREATE INDEX idx_party_member_status ON party_member(party_status);
CREATE INDEX idx_party_member_workno ON party_member(work_no);

-- 3. 25步全流程文档模板管理表 (默认模板 + 管理员导入自定义兼具)
CREATE TABLE party_doc_template (
    id BIGSERIAL PRIMARY KEY,
    step_code INT NOT NULL,                     -- 对应步骤 1~25
    template_code VARCHAR(50) NOT NULL UNIQUE,  -- 编码如 TPL_STEP01_APPLY, TPL_STEP13_DISCIPLINE
    template_name VARCHAR(150) NOT NULL,        -- 模板名称
    is_customized BOOLEAN DEFAULT FALSE,        -- false: 使用系统内置默认模板, true: 使用管理员自定义导入模板
    default_file_name VARCHAR(255) NOT NULL,    -- 默认模板文件名
    default_file_path VARCHAR(255) NOT NULL,    -- 默认模板存储路径
    custom_file_name VARCHAR(255),              -- 自定义模板文件名
    custom_file_path VARCHAR(255),              -- 自定义模板存储路径
    file_version VARCHAR(20) DEFAULT 'v1.0',    -- 模板版本
    placeholders JSONB,                         -- 占位符字段描述
    update_user_name VARCHAR(50),               -- 最后修改/上传人
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 4. 25步全流程办理明细与审计日志表
CREATE TABLE party_step_record (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES party_member(id) ON DELETE CASCADE,
    step_code INT NOT NULL,
    step_name VARCHAR(100) NOT NULL,
    handler_user_id BIGINT,
    handler_name VARCHAR(50),
    start_time TIMESTAMPTZ,
    finish_time TIMESTAMPTZ,
    due_time TIMESTAMPTZ,
    audit_status SMALLINT DEFAULT 0,
    meeting_info VARCHAR(255),
    audit_opinion TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 5. 发展材料文书与本地存储附件表
CREATE TABLE party_material_file (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES party_member(id) ON DELETE CASCADE,
    step_code INT NOT NULL,
    material_code VARCHAR(50) NOT NULL,
    material_name VARCHAR(150) NOT NULL,
    file_path VARCHAR(255),
    file_size BIGINT,
    is_required BOOLEAN DEFAULT TRUE,
    review_status SMALLINT DEFAULT 0,
    reject_reason VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 6. 培养联系人与介绍人关系表
CREATE TABLE party_cultivator_relation (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES party_member(id) ON DELETE CASCADE,
    cultivator_name VARCHAR(50) NOT NULL,
    cultivator_job VARCHAR(100),
    relation_type SMALLINT DEFAULT 1,
    is_primary BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 7. 国企年度发展名额与结构调控表
CREATE TABLE party_annual_quota (
    id BIGSERIAL PRIMARY KEY,
    year_val INT NOT NULL,
    org_id BIGINT NOT NULL REFERENCES sys_party_org(id),
    total_quota INT NOT NULL DEFAULT 0,
    used_quota INT NOT NULL DEFAULT 0,
    frontline_target_ratio NUMERIC(5,2) DEFAULT 40.00,
    youth_target_ratio NUMERIC(5,2) DEFAULT 50.00,
    talent_target_ratio NUMERIC(5,2) DEFAULT 25.00,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 8. 支部“三会一课”与每月主题党日会议台账表 (国企组织生活标准化核心)
CREATE TABLE party_meeting_record (
    id BIGSERIAL PRIMARY KEY,
    org_id BIGINT NOT NULL REFERENCES sys_party_org(id),
    meeting_type SMALLINT NOT NULL,          -- 1:支委会, 2:党员大会, 3:党课, 4:主题党日
    meeting_title VARCHAR(200) NOT NULL,
    meeting_date DATE NOT NULL,
    meeting_place VARCHAR(150),
    moderator_name VARCHAR(50) NOT NULL,
    speaker_name VARCHAR(50),
    expected_count INT NOT NULL,
    actual_count INT NOT NULL,
    attendee_names TEXT,
    meeting_content TEXT,
    doc_file_path VARCHAR(255),
    photo_urls JSONB,
    related_member_id BIGINT,                -- 关联发展党员ID (如表决接收/转正)
    related_step_code INT,                   -- 关联25步步骤号
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 9. 支部“三会一课”会议附件表 (会议通知、会议纪要、决议书、签到表、现场照片)
CREATE TABLE party_meeting_attachment (
    id BIGSERIAL PRIMARY KEY,
    meeting_id BIGINT NOT NULL REFERENCES party_meeting_record(id) ON DELETE CASCADE,
    attach_type VARCHAR(50) NOT NULL,        -- NOTICE:会议通知, MINUTES:会议纪要, RESOLUTION:会议决议, SIGNIN:签到表, PHOTO:现场照片
    attach_type_name VARCHAR(50) NOT NULL,   -- 类型中文名
    file_name VARCHAR(255) NOT NULL,         -- 原始文件名
    file_path VARCHAR(255) NOT NULL,         -- 本地存储相对路径
    file_size BIGINT,                        -- 文件大小 (字节)
    uploader_name VARCHAR(50),               -- 上传人
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_meeting_attach_mid ON party_meeting_attachment(meeting_id);

-- 10. 党员个人学习与学时台账表 (一人一学时档案)
CREATE TABLE party_study_record (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES party_member(id) ON DELETE CASCADE,
    course_name VARCHAR(150) NOT NULL,
    course_type SMALLINT DEFAULT 1,          -- 1:党校集中培训(24学时), 2:专题党课, 3:网络党校, 4:自学研讨
    study_hours INT NOT NULL,                -- 获得学时
    study_date DATE NOT NULL,
    exam_score NUMERIC(5,2),                 -- 考试成绩
    cert_file_path VARCHAR(255),             -- 结业证书凭证
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 11. 党组织与党员奖惩/荣誉台账表 (区分组织荣誉/个人荣誉及纪律惩戒)
CREATE TABLE party_honor_punishment (
    id BIGSERIAL PRIMARY KEY,
    category SMALLINT NOT NULL,              -- 1: 个人奖惩/荣誉, 2: 组织奖惩/荣誉
    record_type SMALLINT NOT NULL,           -- 1: 荣誉表彰, 2: 纪律处分/负面惩戒
    member_id BIGINT REFERENCES party_member(id) ON DELETE SET NULL,
    target_name VARCHAR(100) NOT NULL,       -- 个人姓名或党支部名称
    org_id BIGINT REFERENCES sys_party_org(id),
    org_name VARCHAR(150),
    title VARCHAR(150) NOT NULL,             -- 奖惩/表彰名称
    level VARCHAR(50),                       -- 级别 (集团级、市州级、省部级、国家级)
    grant_org VARCHAR(150),                  -- 授予/决定单位
    doc_no VARCHAR(100),                     -- 表彰/处分文号
    record_date DATE NOT NULL,               -- 决定日期
    reason_content TEXT,                     -- 事迹或处分原因
    attachment_path VARCHAR(255),            -- 证书/红头文件路径
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_hp_cat_type ON party_honor_punishment(category, record_type);

-- 12. 系统党务用户表 (支持密码/免密、企业工号绑定与在册组织)
CREATE TABLE sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    work_no VARCHAR(50) NOT NULL UNIQUE,
    phone VARCHAR(20),
    email VARCHAR(100),
    org_id BIGINT REFERENCES sys_party_org(id),
    org_name VARCHAR(150),
    member_id BIGINT REFERENCES party_member(id) ON DELETE SET NULL,
    status SMALLINT DEFAULT 1,                -- 1: 正常, 0: 禁用
    avatar VARCHAR(255),
    last_login_time TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 13. 系统 RBAC 党务角色定义表
CREATE TABLE sys_role (
    id BIGSERIAL PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL UNIQUE,    -- COMMITTEE_ORGANIZER, BRANCH_SECRETARY, DISCIPLINE_INSPECTOR, SYS_ADMIN, PARTY_MEMBER
    role_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    sort_order INT DEFAULT 0,
    status SMALLINT DEFAULT 1,
    permissions JSONB,                       -- 权限标识列表
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 14. 用户-角色关联表 (支持多角色)
CREATE TABLE sys_user_role (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES sys_user(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES sys_role(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_role UNIQUE (user_id, role_id)
);

-- 15. 多渠道通知服务配置表 (企业微信/钉钉/106短信/邮箱/站内信)
CREATE TABLE sys_notice_channel (
    id BIGSERIAL PRIMARY KEY,
    channel_code VARCHAR(50) NOT NULL UNIQUE, -- IN_APP, WECHAT_WORK, DINGTALK, SMS, EMAIL
    channel_name VARCHAR(100) NOT NULL,
    channel_type SMALLINT NOT NULL,          -- 1: 站内信, 2: 手机短信, 3: 邮件, 4: 企业微信, 5: 钉钉
    enabled SMALLINT DEFAULT 1,              -- 1: 启用, 0: 停用
    config_json JSONB,                       -- 接口认证凭据与参数配置
    template_json JSONB,                     -- 模板映射配置
    remark VARCHAR(255),
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- 16. 党建通知与合规催办调度审计日志表
CREATE TABLE sys_notice_log (
    id BIGSERIAL PRIMARY KEY,
    notice_type VARCHAR(50) NOT NULL,        -- DEADLINE_WARNING, TRANS_PROBATION, DISCIPLINE_AUDIT, MEETING_NOTICE, REGULAR
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    receiver_type VARCHAR(20) DEFAULT 'USER',-- USER, ROLE, ORG, ALL
    receiver_id BIGINT,
    receiver_name VARCHAR(100),
    receiver_target VARCHAR(150),            -- 手机号/邮箱/企微ID
    channel_code VARCHAR(50) NOT NULL,
    send_status SMALLINT DEFAULT 1,          -- 1: 成功, 2: 失败, 0: 待发送
    error_msg TEXT,
    is_read SMALLINT DEFAULT 0,              -- 0: 未读, 1: 已读
    related_member_id BIGINT,
    related_step_code INT,
    send_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    read_time TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notice_rec_read ON sys_notice_log(receiver_id, is_read);

-- ==============================================================================
-- 种子数据初始化（红河数据产业集团有限公司党总支及下设三家子公司支部）
-- ==============================================================================

-- 1. 组织架构 (集团党总支及下设三家子公司党支部，无机关党小组)
INSERT INTO sys_party_org (id, parent_id, org_name, org_code, org_type, has_approval_right, leader_name, business_scope, sort_order) VALUES
(1, 0, '中共红河数据产业集团有限公司总支部委员会', 'ORG_HH_DATA_GROUP', 1, TRUE, '朱文华', '集团全局统筹与党务审查把关，设5名党总支委员', 1),
(2, 1, '中共红河红数信息技术服务有限公司支部委员会', 'ORG_HH_HONGSHU', 2, FALSE, '李卫民', '云服务、运维服务、网络及安全服务', 2),
(3, 1, '中共云南幂次科技有限公司支部委员会', 'ORG_HH_MICI', 2, FALSE, '刘建华', '软件开发、平台运营、企业数字化转型支撑', 3),
(4, 1, '中共红河链达科技有限公司支部委员会', 'ORG_HH_LIANDA', 2, FALSE, '陈明', '城市综合体运营等', 4);

ALTER SEQUENCE sys_party_org_id_seq RESTART WITH 10;

-- 2. 所有党员花名册种子数据 (涵盖党总支 5 名委员及三家子公司党员队伍)
INSERT INTO party_member (id, org_id, dept_name, work_no, real_name, id_card, gender, birth_date, education, job_title, party_status, party_post, party_standing_years, join_party_date, official_party_date, dues_status, is_frontline, is_technical_talent, is_dual_cultivate, current_stage, current_step, step_status, apply_date, first_talk_date, activist_date, target_date, probationary_date, official_date) VALUES
-- 集团党总支 5 名委员
(201, 1, '红河数据产业集团 · 领导班子', 'HH-JT-001', '朱文华', '532501197805121111', 1, '1978-05-12', '硕士研究生', '集团董事长 / 总支书记', 1, '党总支书记', 20, '2005-06-20', '2006-06-20', 1, FALSE, FALSE, FALSE, 5, 25, 3, '2004-03-01', '2004-03-15', '2004-05-10', '2005-05-15', '2005-06-20', '2006-06-20'),
(200, 1, '红河数据产业集团 · 领导班子', 'HH-JT-002', '李建忠', '532501198105180000', 1, '1981-05-18', '大学本科', '集团总经理 / 总支副书记', 1, '党总支副书记', 17, '2008-05-18', '2009-05-18', 1, FALSE, FALSE, FALSE, 5, 25, 3, '2007-02-10', '2007-02-28', '2007-04-12', '2008-04-18', '2008-05-18', '2009-05-18'),
(202, 1, '红河数据产业集团 · 纪检风控部', 'HH-JT-003', '周国平', '532501198203182222', 1, '1982-03-18', '大学本科', '纪检风控部部长 / 纪检委员', 1, '党总支纪检委员', 15, '2010-07-01', '2011-07-01', 1, FALSE, FALSE, FALSE, 5, 25, 3, '2009-04-10', '2009-04-25', '2009-06-12', '2010-06-15', '2010-07-01', '2011-07-01'),
(203, 1, '红河数据产业集团 · 综合管理部', 'HH-JT-005', '杨海', '532501198611093333', 1, '1986-11-09', '大学本科', '综合管理部部长 / 组织委员', 1, '党总支组织委员', 12, '2013-10-15', '2014-10-15', 1, FALSE, FALSE, FALSE, 5, 25, 3, '2012-08-01', '2012-08-15', '2012-09-20', '2013-09-25', '2013-10-15', '2014-10-15'),
(209, 1, '红河数据产业集团 · 企划党群部', 'HH-JT-004', '赵丽萍', '532501198706280909', 2, '1987-06-28', '大学本科', '企划党群部部长 / 宣传委员', 1, '党总支宣传委员', 11, '2014-06-28', '2015-06-28', 1, FALSE, FALSE, FALSE, 5, 25, 3, '2013-04-10', '2013-04-25', '2013-06-15', '2014-06-15', '2014-06-28', '2015-06-28'),

-- 红数信息技术党支部
(204, 2, '红河红数信息技术服务有限公司 · 管理层', 'HH-HS-001', '李卫民', '532501198009214444', 1, '1980-09-21', '大学本科', '总经理 / 支部书记', 1, '党支部书记', 18, '2007-04-18', '2008-04-18', 1, TRUE, TRUE, TRUE, 5, 25, 3, '2006-02-10', '2006-02-28', '2006-04-10', '2007-04-05', '2007-04-18', '2008-04-18'),
(205, 2, '红河红数信息技术服务有限公司 · 技术部', 'HH-HS-003', '王晓东', '532501198812155555', 1, '1988-12-15', '硕士研究生', '技术总监 / 组织委员', 1, '支部组织委员', 10, '2015-05-04', '2016-05-04', 1, TRUE, TRUE, TRUE, 5, 25, 3, '2014-03-01', '2014-03-20', '2014-04-25', '2015-04-20', '2015-05-04', '2016-05-04'),
(101, 2, '红河红数信息技术服务有限公司 · 云计算运维部', 'HH-HS-012', '张强', '532501199704151234', 1, '1997-04-15', '本科', '政务云平台主任运维工程师', 4, '积极分子', 0, NULL, NULL, 1, TRUE, TRUE, TRUE, 2, 7, 4, '2024-04-10', '2024-04-22', '2024-06-15', NULL, NULL, NULL),

-- 云南幂次科技党支部
(206, 3, '云南幂次科技有限公司 · 管理层', 'HH-MC-001', '刘建华', '532501198402286666', 1, '1984-02-28', '硕士研究生', '执行董事 / 支部书记', 1, '党支部书记', 14, '2011-12-10', '2012-12-10', 1, TRUE, TRUE, TRUE, 5, 25, 3, '2010-09-01', '2010-09-18', '2010-11-20', '2011-11-25', '2011-12-10', '2012-12-10'),
(207, 3, '云南幂次科技有限公司 · 算法中心', 'HH-MC-004', '赵丽', '532501199106037777', 2, '1991-06-03', '博士研究生', 'AI 首席科学家 / 宣传委员', 1, '支部宣传委员', 6, '2019-06-28', '2020-06-28', 1, TRUE, TRUE, TRUE, 5, 25, 3, '2018-04-10', '2018-04-25', '2018-06-15', '2019-06-15', '2019-06-28', '2020-06-28'),
(102, 3, '云南幂次科技有限公司 · 算法研发中心', 'HH-MC-035', '林雨涵', '532501199608222345', 2, '1996-08-22', '硕士研究生', '大数据清洗与 AI 算法专家', 3, '发展对象', 0, NULL, NULL, 1, TRUE, TRUE, TRUE, 3, 13, 1, '2023-09-01', '2023-09-15', '2023-10-20', '2024-11-05', NULL, NULL),
(104, 3, '云南幂次科技有限公司 · 基础算力保障部', 'HH-MC-019', '王建国', '532501198705034567', 1, '1987-05-03', '本科', '算力集群总架构师 / 云南省数字工匠', 2, '预备党员', 0, '2025-02-10', NULL, 1, TRUE, TRUE, TRUE, 4, 20, 1, '2023-01-15', '2023-01-28', '2023-03-05', '2024-04-12', '2025-02-10', NULL),

-- 红河链达科技党支部
(208, 4, '红河链达科技有限公司 · 管理层', 'HH-LD-001', '陈明', '532501198307148888', 1, '1983-07-14', '大学本科', '总经理 / 支部书记', 1, '党支部书记', 16, '2009-11-20', '2010-11-20', 1, TRUE, TRUE, TRUE, 5, 25, 3, '2008-08-10', '2008-08-25', '2008-10-15', '2009-10-20', '2009-11-20', '2010-11-20'),
(103, 4, '红河链达科技有限公司 · 数据资产运营部', 'HH-LD-008', '李晓辉', '532501199302113456', 1, '1993-02-11', '本科', '区块链存证平台产品负责人', 2, '预备党员', 0, '2024-03-25', NULL, 1, TRUE, TRUE, FALSE, 5, 23, 4, '2022-08-01', '2022-08-20', '2022-09-25', '2023-10-15', '2024-03-25', NULL),
(106, 4, '红河链达科技有限公司 · 技术开发部', 'HH-LD-002', '刘振华', '532501199003256789', 1, '1990-03-25', '硕士研究生', '技术总监 / 数据安全合规官', 1, '党员', 1, '2023-12-18', '2024-12-28', 1, TRUE, TRUE, TRUE, 5, 25, 3, '2022-03-10', '2022-03-25', '2022-05-15', '2023-06-20', '2023-12-18', '2024-12-28'),
(105, 11, '红河数据产业集团 · 战略综合部', 'HH-JT-006', '陈思佳', '532501200109125678', 2, '2001-09-12', '本科', '企划党群专员', 5, '申请人', 0, NULL, NULL, 1, FALSE, FALSE, FALSE, 1, 2, 4, '2025-03-15', NULL, NULL, NULL, NULL, NULL);

ALTER SEQUENCE party_member_id_seq RESTART WITH 300;

-- 3. 25步全流程标准文档模板数据 (内置默认模板 + 支持管理员导入)
INSERT INTO party_doc_template (step_code, template_code, template_name, is_customized, default_file_name, default_file_path, file_version, placeholders) VALUES
(1, 'TPL_STEP01', '入党申请书(标准范本)', FALSE, '01_入党申请书_官方标准版.docx', '/templates/default/01_apply.docx', 'v1.0', '["realName", "idCard", "workNo", "deptName", "jobTitle", "applyDate"]'),
(2, 'TPL_STEP02', '同入党申请人谈话记录表', FALSE, '02_同入党申请人谈话记录表.docx', '/templates/default/02_talk.docx', 'v1.0', '["realName", "talkerName", "talkDate", "talkPlace", "talkContent"]'),
(3, 'TPL_STEP03', '入党申请人名册备案表', FALSE, '03_入党申请人名册备案表.docx', '/templates/default/03_roster.docx', 'v1.0', '["branchName", "memberList", "recordDate"]'),
(4, 'TPL_STEP04', '入党积极分子推荐与推优表', FALSE, '04_共青团工会推优推荐表.docx', '/templates/default/04_recommend.docx', 'v1.0', '["realName", "recommendOrg", "reason", "meetingDate"]'),
(5, 'TPL_STEP05', '积极分子备案报告及审查意见', FALSE, '05_积极分子备案审查意见表.docx', '/templates/default/05_record_audit.docx', 'v1.0', '["branchName", "generalBranchOpinion", "recordDate"]'),
(6, 'TPL_STEP06', '培养联系人指定登记表', FALSE, '06_培养联系人指定记录表.docx', '/templates/default/06_cultivators.docx', 'v1.0', '["realName", "cultivator1", "cultivator2", "appointDate"]'),
(7, 'TPL_STEP07', '入党积极分子培养考察表(季度写实)', FALSE, '07_积极分子培养考察写实表.docx', '/templates/default/07_cultivate_eval.docx', 'v1.0', '["realName", "quarter1Eval", "quarter2Eval", "quarter3Eval", "quarter4Eval"]'),
(8, 'TPL_STEP08', '季度思想汇报标准格式', FALSE, '08_积极分子思想汇报模板.docx', '/templates/default/08_thought_report.docx', 'v1.0', '["realName", "quarterPeriod", "reportContent", "submitDate"]'),
(9, 'TPL_STEP09', '征求党内外群众意见座谈会纪要', FALSE, '09_群众座谈会纪要.docx', '/templates/default/09_mass_symposium.docx', 'v1.0', '["realName", "moderator", "massList", "opinionSummary"]'),
(10, 'TPL_STEP10', '支委会确定发展对象会议纪要', FALSE, '10_支委会确定发展对象决议.docx', '/templates/default/10_branch_target.docx', 'v1.0', '["branchName", "committeeMembers", "resolutionText"]'),
(11, 'TPL_STEP11', '发展对象备案审查批复函', FALSE, '11_党总支发展对象备案批复.docx', '/templates/default/11_general_target_approval.docx', 'v1.0', '["realName", "targetCode", "approvalDate", "generalBranchName"]'),
(12, 'TPL_STEP12', '入党介绍人指定及意见表', FALSE, '12_入党介绍人意见书.docx', '/templates/default/12_introducer.docx', 'v1.0', '["realName", "introducer1", "introducer2", "introducerOpinion"]'),
(13, 'TPL_STEP13', '政治审查综合分析报告与纪检意见', FALSE, '13_政审综合报告及廉政意见书.docx', '/templates/default/13_political_discipline.docx', 'v1.0', '["realName", "politicalReviewReport", "disciplineConclusion", "inspectorName"]'),
(14, 'TPL_STEP14', '党校集中短期培训考核合格证', FALSE, '14_集中短期培训结业登记表.docx', '/templates/default/14_training_cert.docx', 'v1.0', '["realName", "trainingHours", "examScore", "certNo"]'),
(15, 'TPL_STEP15', '发展对象拟接收预备党员公示公告', FALSE, '15_拟接收预备党员公示公告.docx', '/templates/default/15_publicity.docx', 'v1.0', '["realName", "publicityStart", "publicityEnd", "supervisePhone"]'),
(16, 'TPL_STEP16', '发展党员预审情况报告表', FALSE, '16_党总支预审合格通知书.docx', '/templates/default/16_pre_audit.docx', 'v1.0', '["realName", "volunteerBookCode", "auditPerson"]'),
(17, 'TPL_STEP17', '中国共产党入党志愿书(填报规范指导)', FALSE, '17_入党志愿书规范填写指南.docx', '/templates/default/17_volunteer_guide.docx', 'v1.0', '["realName", "idCard", "resume", "familyMembers"]'),
(18, 'TPL_STEP18', '支部大会接收预备党员决议与票决单', FALSE, '18_支部大会接收决议及无记名票决表.docx', '/templates/default/18_congress_receive.docx', 'v1.0', '["validVoters", "actualVoters", "agreeVotes", "rejectVotes"]'),
(19, 'TPL_STEP19', '党总支指派专人谈话记录表', FALSE, '19_总支指派专人谈话记录.docx', '/templates/default/19_talk_assigned.docx', 'v1.0', '["realName", "assignedTalker", "talkDate", "talkOpinion"]'),
(20, 'TPL_STEP20', '党总支委员会接收预备党员审批决议', FALSE, '20_党总支接收预备党员批复红头文.docx', '/templates/default/20_committee_approval.docx', 'v1.0', '["meetingSession", "docNo", "approvalResult", "startDate"]'),
(21, 'TPL_STEP21', '预备党员入党宣誓仪式程序单', FALSE, '21_入党宣誓仪式流程规范.docx', '/templates/default/21_oath_ceremony.docx', 'v1.0', '["oathDate", "leaderName", "branchName"]'),
(22, 'TPL_STEP22', '预备党员考察写实表与思想汇报', FALSE, '22_预备党员考察写实表.docx', '/templates/default/22_probationary_eval.docx', 'v1.0', '["realName", "halfYearEval1", "halfYearEval2"]'),
(23, 'TPL_STEP23', '转正申请书(标准范本)', FALSE, '23_转正申请书标准模板.docx', '/templates/default/23_official_apply.docx', 'v1.0', '["realName", "probationStart", "probationEnd", "applyDate"]'),
(24, 'TPL_STEP24', '支部大会转正决议及票决汇总表', FALSE, '24_支部大会转正决议表.docx', '/templates/default/24_congress_official.docx', 'v1.0', '["validVoters", "actualVoters", "agreeVotes", "resolutionText"]'),
(25, 'TPL_STEP25', '转正批复与党员人事档案移交清单', FALSE, '25_转正批复及档案移交回执.docx', '/templates/default/25_archive_transfer.docx', 'v1.0', '["docNo", "handoverPerson", "receiverPerson", "archiveList"]');

-- 4. 系统用户与四级角色权限初始化种子数据 (超级管理员、党总支管理员、支部管理员、普通党员)
INSERT INTO sys_role (id, role_code, role_name, description, sort_order, status, permissions) VALUES
(1, 'SYS_ADMIN', '超级管理员', '全平台系统与安全超级管理员，唯一独享用户管理与多渠道通知配置，拥有全平台所有模块增删改查权限', 1, 1, '["user:manage", "role:manage", "notice:channel_manage", "notice:send", "workbench:view", "workbench:create_applicant", "workbench:advance", "workbench:audit", "workbench:transfer", "workbench:export", "workbench:block_override", "roster:view", "roster:create", "roster:edit", "roster:import", "roster:export", "meeting:view", "meeting:create", "meeting:edit", "meeting:delete", "meeting:tags_manage", "meeting:export", "honor:view", "honor:create", "honor:edit", "honor:delete", "honor:export", "template:view", "template:upload", "template:reset", "cockpit:view", "notice:view"]'),
(2, 'GENERAL_BRANCH_ADMIN', '党总支管理员', '集团党总支党务中枢与组织员，拥有党总支本级及直管三家支部的全部党务业务权限，独享第20/25步审批批复权及指标调控、一人一档归档、模板导入', 2, 1, '["workbench:view", "workbench:create_applicant", "workbench:advance", "workbench:audit", "workbench:transfer", "workbench:export", "workbench:block_override", "roster:view", "roster:create", "roster:edit", "roster:import", "roster:export", "meeting:view", "meeting:create", "meeting:edit", "meeting:delete", "meeting:tags_manage", "meeting:export", "honor:view", "honor:create", "honor:edit", "honor:delete", "honor:export", "template:view", "template:upload", "template:reset", "cockpit:view", "notice:view", "notice:send"]'),
(3, 'BRANCH_ADMIN', '支部管理员', '子公司党支部书记及支委，严格锁定本支部业务数据，负责本支部流程发起推进、名册维护、组织生活记录与删除', 3, 1, '["workbench:view", "workbench:create_applicant", "workbench:advance", "workbench:export", "roster:view", "roster:create", "roster:edit", "roster:import", "roster:export", "meeting:view", "meeting:create", "meeting:edit", "meeting:delete", "meeting:export", "honor:view", "honor:create", "honor:edit", "honor:delete", "honor:export", "template:view", "cockpit:view", "notice:view"]'),
(4, 'PARTY_MEMBER', '普通在册党员 / 发展成员本人', '普通在册党员及发展成员，仅限查看个人成长全景档案与个人待办通知', 4, 1, '["workbench:view", "member:self_view", "notice:view", "cockpit:view"]');

ALTER SEQUENCE sys_role_id_seq RESTART WITH 10;

INSERT INTO sys_user (id, username, password, real_name, work_no, phone, email, org_id, org_name, status, last_login_time) VALUES
(1, 'admin', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF1234567890', '系统管理员', 'SYS-ADMIN-01', '13888880001', 'admin@honghe-data.com', 1, '中共红河数据产业集团有限公司总支部委员会', 1, CURRENT_TIMESTAMP),
(2, 'yanghai', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF1234567890', '杨海', 'HH-JT-005', '13987301005', 'yanghai@honghe-data.com', 1, '中共红河数据产业集团有限公司总支部委员会', 1, CURRENT_TIMESTAMP),
(3, 'liweimin', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF1234567890', '李卫民', 'HH-HS-001', '13987302001', 'liweimin@hongshu-info.com', 2, '中共红河红数信息技术服务有限公司支部委员会', 1, CURRENT_TIMESTAMP),
(4, 'liujianhua', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF1234567890', '刘建华', 'HH-MC-001', '13987303001', 'liujianhua@mici-tech.com', 3, '中共云南幂次科技有限公司支部委员会', 1, CURRENT_TIMESTAMP),
(5, 'chenming', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF1234567890', '陈明', 'HH-LD-001', '13987304001', 'chenming@lianda-tech.com', 4, '中共红河链达科技有限公司支部委员会', 1, CURRENT_TIMESTAMP),
(6, 'zhangqiang', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF1234567890', '张强', 'HH-HS-012', '13987302012', 'zhangqiang@hongshu-info.com', 2, '中共红河红数信息技术服务有限公司支部委员会', 1, CURRENT_TIMESTAMP),
(7, 'linyuhan', '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF1234567890', '林雨涵', 'HH-MC-035', '13987303035', 'linyuhan@mici-tech.com', 3, '中共云南幂次科技有限公司支部委员会', 1, CURRENT_TIMESTAMP);

ALTER SEQUENCE sys_user_id_seq RESTART WITH 100;

INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1), -- admin -> SYS_ADMIN (超级管理员)
(2, 2), -- 杨海 -> GENERAL_BRANCH_ADMIN (党总支管理员)
(3, 3), -- 李卫民 -> BRANCH_ADMIN (红数信息支部管理员)
(4, 3), -- 刘建华 -> BRANCH_ADMIN (幂次科技支部管理员)
(5, 3), -- 陈明 -> BRANCH_ADMIN (链达科技支部管理员)
(6, 4), -- 张强 -> PARTY_MEMBER (普通在册党员)
(7, 4); -- 林雨涵 -> PARTY_MEMBER (普通在册党员)

-- 5. 通知渠道服务配置种子数据
INSERT INTO sys_notice_channel (id, channel_code, channel_name, channel_type, enabled, config_json, remark) VALUES
(1, 'IN_APP', '系统站内信 / 实时红点', 1, 1, '{"popup": true, "sound": true, "badge": true}', '平台默认内置通道，提供桌面弹窗与右上角未读数提醒'),
(2, 'WECHAT_WORK', '企业微信应用消息', 4, 1, '{"corpId": "ww987f6543210abcd", "agentId": 100008, "secret": "******", "apiBase": "https://qyapi.weixin.qq.com"}', '推送至国企干部与员工企业微信工作台【红河智慧党建】专栏'),
(3, 'DINGTALK', '钉钉工作通知', 5, 1, '{"appKey": "ding7890abcdef1234", "appSecret": "******", "agentId": 29876543}', '同步推送至钉钉待办任务与群机器人通知'),
(4, 'SMS', '106党务政务短信专网', 2, 1, '{"signName": "红河数据集团党总支", "tplDeadline": "SMS_001928", "tplTrans": "SMS_001929", "apiKey": "******"}', '用于紧急合规阻断预警、转正临期催办关键红线强触达'),
(5, 'EMAIL', '国企内网邮箱服务 (SMTP)', 3, 0, '{"host": "mail.honghe-data.com", "port": 465, "ssl": true, "user": "party-center@honghe-data.com"}', '用于定期发送支部三会一课月度通报与纪检政审函调电子版');

ALTER SEQUENCE sys_notice_channel_id_seq RESTART WITH 10;

-- 6. 通知中心审计与台账种子数据
INSERT INTO sys_notice_log (notice_type, title, content, receiver_type, receiver_name, receiver_target, channel_code, send_status, is_read, related_member_id, related_step_code) VALUES
('DEADLINE_WARNING', '【合规阻断】入党积极分子考察期不满 365 天强制锁定提醒', '【张强】同志积极分子备案时间为 2024-06-15，截至今日考察仅 290 天，未满法定 1 年硬性考察周期，系统合规防错引擎已强制阻断进入第 9 步！', 'ROLE', '李卫民 (红数信息支部书记)', 'liweimin@hongshu-info.com', 'WECHAT_WORK', 1, 0, 101, 7),
('DEADLINE_WARNING', '【时限红线】入党申请谈话 30 天红线临期预警', '【陈思佳】同志于 2025-03-15 递交入党申请书，已满 22 天，距离中组部细则“1个月内必须指派专人谈话”红线仅剩 8 天，请支部抓紧开展谈话并归档谈话记录表。', 'USER', '杨海 (总支组织委员)', '13987301005', 'SMS', 1, 0, 105, 2),
('TRANS_PROBATION', '【转正催办】预备党员预备期届满提醒及转正申请催办', '预备党员【李晓辉】同志预备期（2024-03-25 ~ 2025-03-25）即将满期，已自动下达转正催办通知，请本人于满期前1-2周主动向链达科技党支部递交书面《转正申请书》。', 'USER', '李晓辉 (预备党员)', 'HH-LD-008', 'WECHAT_WORK', 1, 1, 103, 23),
('DISCIPLINE_AUDIT', '【纪检会签】发展对象廉洁从业审查意见书待出具', '发展对象【林雨涵】同志已完成直系亲属政审函调，当前流转至集团纪委出具《廉洁从业意见书》（一票否决权），请纪检风控部周国平部长在线复核会签。', 'ROLE', '周国平 (总支纪检委员)', 'zhouguoping@honghe-data.com', 'DINGTALK', 1, 1, 102, 13),
('MEETING_NOTICE', '【组织生活】2026年第十期“牢记嘱托勇担使命”主题党日活动召开通知', '定于 2026-10-15 下午 14:30 在集团二楼党建实训室召开 10 月主题党日，请各支部全体党员及发展对象佩戴党徽按时签到参会。', 'ALL', '全集团在册党员及发展对象', 'all_members', 'IN_APP', 1, 0, NULL, NULL);
