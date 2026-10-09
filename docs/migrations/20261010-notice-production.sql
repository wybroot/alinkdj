-- 已有数据库升级脚本；先备份，再在维护窗口执行。新库使用 schema-pg.sql。
BEGIN;
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS wecom_user_id VARCHAR(100);
ALTER TABLE sys_user ADD COLUMN IF NOT EXISTS dingtalk_user_id VARCHAR(100);
ALTER TABLE sys_notice_log ADD COLUMN IF NOT EXISTS provider_message_id VARCHAR(200);
ALTER TABLE sys_notice_log ADD COLUMN IF NOT EXISTS dedupe_key VARCHAR(200);
CREATE UNIQUE INDEX IF NOT EXISTS uk_notice_dedupe ON sys_notice_log(dedupe_key);
ALTER TABLE sys_notice_log ALTER COLUMN send_status SET DEFAULT 0;

-- 停用旧的模拟/明文凭据配置；保留可用非敏感标识，重新录入后启用。
UPDATE sys_notice_channel SET enabled = 0,
  config_json = jsonb_build_object('corpId', config_json->>'corpId', 'agentId', config_json->>'agentId', 'secretEnv', 'PARTY_NOTICE_WECOM_SECRET')
WHERE channel_code = 'WECHAT_WORK' AND (config_json ? 'secret' OR config_json ? 'apiBase');
UPDATE sys_notice_channel SET enabled = 0,
  config_json = jsonb_build_object('corpId', '', 'clientId', config_json->>'appKey', 'agentId', config_json->>'agentId', 'clientSecretEnv', 'PARTY_NOTICE_DINGTALK_SECRET')
WHERE channel_code = 'DINGTALK' AND (config_json ? 'appKey' OR config_json ? 'appSecret');
UPDATE sys_notice_channel SET enabled = 0, channel_name = '阿里云短信',
  config_json = '{"provider":"ALIYUN","signName":"","accessKeyIdEnv":"PARTY_NOTICE_SMS_KEY_ID","accessKeySecretEnv":"PARTY_NOTICE_SMS_KEY_SECRET"}', template_json = '{}'
WHERE channel_code = 'SMS' AND config_json ? 'apiKey';
UPDATE sys_notice_channel SET enabled = 0, channel_name = '邮件通知（SMTP）',
  config_json = jsonb_build_object('host', config_json->>'host', 'port', config_json->'port', 'security', 'SSL',
    'username', config_json->>'user', 'from', config_json->>'user', 'passwordEnv', 'PARTY_NOTICE_SMTP_PASSWORD')
WHERE channel_code = 'EMAIL' AND (config_json ? 'user' OR config_json ? 'password');
UPDATE sys_notice_channel SET config_json = '{}' WHERE channel_code = 'IN_APP';

-- 历史模拟记录保留审计痕迹，不能当作真实受理或送达凭证。
UPDATE sys_notice_log SET send_status = 3, error_msg = '旧版记录没有服务商消息编号，无法确认实际发送情况'
WHERE send_status = 1 AND provider_message_id IS NULL AND channel_code <> 'IN_APP';

-- 仅清除旧版固定演示密码关联的联系方式，不覆盖已设置正式密码的账号。
UPDATE sys_user SET password = '!UNINITIALIZED!', phone = NULL, email = NULL
WHERE password = '$2a$10$abcdefghijklmnopqrstuvwxyzABCDEF1234567890';
COMMIT;
