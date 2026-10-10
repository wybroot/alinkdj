-- ==============================================================================
-- 党员组织关系转接与职务调整备案 数据库表结构
-- 1. party_relation_transfer: 党员转接记录（转入/转出全流程凭证与台账）
-- 2. party_position_adjustment: 党员党内职务调整备案（任免决定、批文号与职务联动）
-- ==============================================================================

CREATE TABLE IF NOT EXISTS party_relation_transfer (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT,                                -- 关联党员ID
    member_name VARCHAR(50) NOT NULL,                -- 党员姓名
    id_card VARCHAR(18),                             -- 身份证号
    work_no VARCHAR(50),                             -- 工号
    gender SMALLINT DEFAULT 1,                       -- 1: 男, 2: 女
    phone VARCHAR(30),                               -- 联系电话
    party_status SMALLINT NOT NULL DEFAULT 1,        -- 1: 正式党员, 2: 预备党员
    party_post VARCHAR(100) DEFAULT '普通党员',       -- 党内职务
    transfer_type SMALLINT NOT NULL,                -- 1: 转入, 2: 转出
    from_org_id BIGINT,                              -- 转出党组织ID (若系统内)
    from_org_name VARCHAR(150) NOT NULL,             -- 原所在党组织/转出支部
    to_org_id BIGINT,                                -- 转入党组织ID (若系统内)
    to_org_name VARCHAR(150) NOT NULL,               -- 目标党组织/转入支部
    letter_no VARCHAR(100),                          -- 介绍信编号/凭证号 (如: 红数转字〔2026〕第08号)
    transfer_date DATE NOT NULL,                     -- 转接办理日期
    transfer_reason VARCHAR(255),                    -- 转接原因 (岗位调动、入职、离职调转等)
    dues_paid_to_date DATE,                          -- 党费交至日期/月份
    operator_name VARCHAR(50),                       -- 经办人
    approval_status SMALLINT DEFAULT 2,              -- 1: 审核中, 2: 已办结/已确认, 3: 已驳回
    remark VARCHAR(500),                             -- 备注
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_transfer_type ON party_relation_transfer(transfer_type);
CREATE INDEX IF NOT EXISTS idx_transfer_member ON party_relation_transfer(member_id);
CREATE INDEX IF NOT EXISTS idx_transfer_date ON party_relation_transfer(transfer_date);

COMMENT ON TABLE party_relation_transfer IS '党员组织关系转接详细台账表';


CREATE TABLE IF NOT EXISTS party_position_adjustment (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,                       -- 关联党员ID
    member_name VARCHAR(50) NOT NULL,                -- 党员姓名
    work_no VARCHAR(50),                             -- 工号
    org_id BIGINT NOT NULL,                          -- 任职党组织ID
    org_name VARCHAR(150) NOT NULL,                  -- 任职党组织名称
    old_post VARCHAR(100) NOT NULL,                  -- 调整前党内职务
    new_post VARCHAR(100) NOT NULL,                  -- 调整后党内职务 (新任职务)
    adjust_type VARCHAR(50) DEFAULT '任职任命',       -- 调整类型 (任职任命、换届任命、届中调整、兼任、免去职务)
    document_no VARCHAR(100) NOT NULL,               -- 批准文号 / 任免批复号 (如: 红数党发〔2026〕15号)
    effective_date DATE NOT NULL,                    -- 调整生效日期 / 发文日期
    approval_unit VARCHAR(150) NOT NULL,             -- 批准机关 / 决定单位 (如: 中共红河数据产业集团有限公司总支部委员会)
    duty_description VARCHAR(255),                   -- 职责分工说明
    operator_name VARCHAR(50),                       -- 备案登记人
    remark VARCHAR(500),                             -- 备注
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_adjust_member ON party_position_adjustment(member_id);
CREATE INDEX IF NOT EXISTS idx_adjust_org ON party_position_adjustment(org_id);
CREATE INDEX IF NOT EXISTS idx_adjust_date ON party_position_adjustment(effective_date);

COMMENT ON TABLE party_position_adjustment IS '党员党内职务调整备案记录表';
