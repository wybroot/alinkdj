export const NOTICE_CHANNEL_META = {
  IN_APP: { name: '系统站内信', icon: 'Bell', hint: '通知保存到系统数据库，接收人登录后可在通知中心查看。', fields: [] },
  WECHAT_WORK: {
    name: '企业微信应用消息', icon: 'ChatDotRound',
    hint: '使用企业自建应用，配置应用可见范围与服务器可信 IP；收件地址为企业微信成员 UserID。',
    doc: 'https://developer.work.weixin.qq.com/document/path/90236',
    fields: [
      { key: 'corpId', label: '企业 ID（CorpID）' },
      { key: 'agentId', label: '应用 ID（AgentID）' },
      { key: 'secretEnv', label: '应用密钥变量名', default: 'PARTY_NOTICE_WECOM_SECRET' }
    ]
  },
  DINGTALK: {
    name: '钉钉工作通知', icon: 'Promotion',
    hint: '使用企业内部应用的工作通知接口。请配置组织、应用和可见范围；收件地址为钉钉成员 UserID。',
    doc: 'https://open.dingtalk.com/document/orgapp/asynchronous-sending-of-enterprise-session-messages',
    fields: [
      { key: 'corpId', label: '组织 ID（CorpID）' },
      { key: 'clientId', label: '应用 ID（ClientID）' },
      { key: 'agentId', label: '微应用 ID（AgentID）' },
      { key: 'clientSecretEnv', label: '应用密钥变量名', default: 'PARTY_NOTICE_DINGTALK_SECRET' }
    ]
  },
  SMS: {
    name: '阿里云短信', icon: 'Message',
    hint: '须开通阿里云短信服务并取得审核通过的签名、通知模板。测试会产生短信费用，使用 REGULAR 模板。',
    doc: 'https://help.aliyun.com/zh/sms/developer-reference/api-dysmsapi-2017-05-25-sendsms',
    fields: [
      { key: 'provider', label: '短信服务商', default: 'ALIYUN', options: [{ label: '阿里云短信', value: 'ALIYUN' }] },
      { key: 'signName', label: '已审核短信签名' },
      { key: 'accessKeyIdEnv', label: '访问密钥 ID 变量名', default: 'PARTY_NOTICE_SMS_KEY_ID' },
      { key: 'accessKeySecretEnv', label: '访问密钥变量名', default: 'PARTY_NOTICE_SMS_KEY_SECRET' }
    ]
  },
  EMAIL: {
    name: '邮件通知（SMTP）', icon: 'Message',
    hint: '开启邮箱 SMTP 服务，使用授权码或专用密码。支持 SSL（通常465端口）和 STARTTLS（通常587端口）。',
    fields: [
      { key: 'host', label: 'SMTP 服务器' },
      { key: 'port', label: '端口', default: 465 },
      { key: 'security', label: '传输加密', default: 'SSL', options: [{ label: 'SSL / TLS', value: 'SSL' }, { label: 'STARTTLS', value: 'STARTTLS' }] },
      { key: 'username', label: 'SMTP 登录账号' },
      { key: 'from', label: '发件邮箱' },
      { key: 'passwordEnv', label: '邮箱授权码变量名', default: 'PARTY_NOTICE_SMTP_PASSWORD' }
    ]
  }
}

export const NOTICE_TYPES = {
  REGULAR: '党建业务通知', DEADLINE_WARNING: '合规时限预警', TRANS_PROBATION: '转正到期催办',
  DISCIPLINE_AUDIT: '纪检把关通知', MEETING_NOTICE: '三会一课通知'
}

export function noticeStatus(row) {
  if (row.sendStatus === 1) return row.channelCode === 'IN_APP' ? '已存入站内信' : '服务商已受理'
  return { 0: '待发送 / 待核对', 2: '发送失败', 3: '结果待确认', 4: '部分受理' }[row.sendStatus] || '尚未确认'
}
