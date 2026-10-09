<template>
  <div class="login-container">
    <!-- 动态国企红金粒子/装饰背景 -->
    <div class="bg-layer">
      <div class="top-light"></div>
      <div class="bottom-pattern"></div>
    </div>

    <div class="login-box-wrapper">
      <!-- 左侧：国企党建庄严政治门面与使命愿景 -->
      <div class="login-left-brand">
        <div class="brand-top">
          <div class="party-emblem">
            <PartyEmblem style="width: 52px; height: 52px;" />
          </div>
          <div class="org-titles">
            <div class="org-badge">中共红河数据产业集团有限公司总支部委员会</div>
            <h2>智慧党建数字化管理系统</h2>
            <div class="eng-sub">SOE SMART PARTY BUILDING DIGITAL PLATFORM</div>
          </div>
        </div>

        <div class="brand-core-features">
          <div class="feature-item">
            <div class="f-icon"><el-icon><Compass /></el-icon></div>
            <div class="f-text">
              <strong>发展党员 25 步全规程管控</strong>
              <p>严格对标《中国共产党发展党员工作细则》，锁定法定节拍</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="f-icon"><el-icon><Lock /></el-icon></div>
            <div class="f-text">
              <strong>细则强阻断防错合规引擎</strong>
              <p>考察期不满 365 天强制锁定，纪检廉洁会签一票否决</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="f-icon"><el-icon><Connection /></el-icon></div>
            <div class="f-text">
              <strong>多渠道党务消息调度直达</strong>
              <p>企业微信 / 钉钉 / 阿里云短信 / 邮件 / 站内信秒级触达</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="f-icon"><el-icon><DataBoard /></el-icon></div>
            <div class="f-text">
              <strong>国资党建指标全景调度大屏</strong>
              <p>党支部“三会一课”规范台账与先锋示范岗</p>
            </div>
          </div>
        </div>

        <div class="brand-footer-quote">
          “坚持党的领导、加强党的建设，是我国国有企业的光荣传统，是国有企业的‘根’和‘魂’。”
        </div>
      </div>

      <!-- 右侧：党务认证与快捷体验登录卡片 -->
      <div class="login-right-card">
        <div class="card-header">
          <h3>党员干部认证登录</h3>
          <span class="sub">请使用已在册的党务账号或工号完成身份核验</span>
        </div>

        <!-- 登录方式切换 -->
        <el-tabs v-model="loginTab" class="login-tabs">
          <!-- 方式 1: 密码登录 -->
          <el-tab-pane label="党务账号密码登录" name="account">
            <el-form :model="loginForm" class="login-form" @keyup.enter="handleAccountLogin">
              <el-form-item>
                <el-input 
                  v-model="loginForm.username" 
                  size="large"
                  placeholder="请输入用户名 / 党员干部工号" 
                  prefix-icon="User"
                  clearable
                />
              </el-form-item>
              <el-form-item>
                <el-input 
                  v-model="loginForm.password" 
                  type="password" 
                  size="large"
                  placeholder="请输入登录密码（默认初始密码 123456）" 
                  prefix-icon="Lock" 
                  show-password
                  clearable
                />
              </el-form-item>
              <div class="form-options">
                <el-checkbox v-model="rememberMe">记住当前登录状态</el-checkbox>
                <el-button link type="primary" size="small" @click="handleForgetPwd">忘记密码 / 重置咨询</el-button>
              </div>
              <el-button 
                type="primary" 
                size="large" 
                class="submit-btn" 
                :loading="loggingIn"
                @click="handleAccountLogin"
              >
                立即登录智慧党建平台
              </el-button>
            </el-form>
          </el-tab-pane>

          <!-- 方式 2: 国企党务角色一键快捷体验免密通道 -->
          <el-tab-pane v-if="demoEnabled" label="党务角色演示体验" name="quick">
            <div class="quick-roles-panel">
              <div class="panel-tip">
                <el-icon><InfoFilled /></el-icon>
                <span>点击下方任一在册党务角色，即可秒级切换并体验对应 RBAC 细粒度权限：</span>
              </div>
              <div class="quick-role-list">
                <div 
                  v-for="role in QUICK_ROLES" 
                  :key="role.roleCode" 
                  class="quick-role-item"
                  @click="handleQuickLogin(role)"
                >
                  <div class="qr-left">
                    <el-avatar :size="36" class="qr-avatar">{{ role.realName.slice(0, 1) }}</el-avatar>
                    <div class="qr-info">
                      <div class="qr-name-row">
                        <strong>{{ role.realName }}</strong>
                        <el-tag size="small" :type="role.tagType" effect="plain">{{ role.roleName }}</el-tag>
                      </div>
                      <span class="qr-duty">{{ role.duty }}</span>
                    </div>
                  </div>
                  <el-button link type="primary" icon="Right">进入体验</el-button>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>

        <div class="security-declaration">
          <el-icon><CircleCheckFilled /></el-icon>
          <span>国企数据涉密安全体系 · 严禁非本单位授权人员违规访问</span>
        </div>
      </div>
    </div>

    <!-- 首次登录强制修改密码安全弹窗 (不可关闭、必须达到高强度要求) -->
    <el-dialog
      v-model="forceChangePwdVisible"
      title="【首次登录安全合规要求】强制修改初始密码"
      width="540px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      class="force-pwd-dialog"
    >
      <el-alert
        title="密码合规要求：为了防范弱密码安全隐患，首次登录必须修改密码。新密码长度至少8位，且必须同时包含【大写/小写字母】、【数字】及【特殊字符】组合！"
        type="warning"
        :closable="false"
        style="margin-bottom: 18px;"
      />
      <el-form label-width="110px">
        <el-form-item label="当前账号">
          <el-input :value="`${pendingUser?.realName} (${pendingUser?.workNo})`" disabled />
        </el-form-item>
        <el-form-item label="原初始密码*" required>
          <el-input v-model="changePwdForm.oldPwd" type="password" placeholder="请输入初始密码（默认 123456）" show-password />
        </el-form-item>
        <el-form-item label="设置新密码*" required>
          <el-input 
            v-model="changePwdForm.newPwd" 
            type="password" 
            placeholder="至少8位，含字母+数字+特殊符号" 
            show-password 
          />
          <div class="pwd-strength-hint">
            <span :class="{ 'valid-req': pwdCheckLength }">✔ 长度≥8位</span>
            <span :class="{ 'valid-req': pwdCheckLetter }">✔ 包含英文字母</span>
            <span :class="{ 'valid-req': pwdCheckNumber }">✔ 包含数字</span>
            <span :class="{ 'valid-req': pwdCheckSpecial }">✔ 包含特殊字符 (!@#$%^&*等)</span>
          </div>
        </el-form-item>
        <el-form-item label="确认新密码*" required>
          <el-input 
            v-model="changePwdForm.confirmPwd" 
            type="password" 
            placeholder="请再次输入新密码" 
            show-password 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <el-button link type="info" @click="cancelForceChange">放弃并返回登录</el-button>
          <el-button type="primary" :disabled="!isPwdValid" @click="submitForceChangePwd">
            确认修改并安全进入系统
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 登录页页脚居中：国企版权与ICP备案占位 -->
    <footer class="login-footer">
      <div class="footer-links">
        <span>红河数据产业集团有限公司党总支 · 智慧党建数字化管理系统</span>
        <span class="divider">|</span>
        <span>技术支持：红河数产集团/数据业务部</span>
        <span class="divider">|</span>
        <a href="javascript:void(0)" class="footer-link">系统安全等级保护三级认定</a>
      </div>
      <div class="footer-copyright">
        <span>Copyright © 2024-2026 红河数据产业集团有限公司 版权所有</span>
        <span class="divider">|</span>
        <a href="https://beian.miit.gov.cn" target="_blank" rel="noopener noreferrer" class="icp-link">
          滇ICP备2026089123号-1
        </a>
        <span class="divider">|</span>
        <span class="security-badge">
          <el-icon :size="13"><Lock /></el-icon> 滇公网安备 53250102000888号
        </span>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PartyEmblem from './PartyEmblem.vue'
import { MOCK_SYS_USERS } from '../data/mockData.js'
import { demoEnabled, loginAccount, clearApiSession } from '../api.js'

const emit = defineEmits(['login-success'])

const loginTab = ref('account')
const loggingIn = ref(false)
const rememberMe = ref(true)

const loginForm = ref({
  username: 'yanghai',
  password: ''
})

// 预设四级体验角色列表
const QUICK_ROLES = [
  {
    username: 'admin',
    realName: '系统管理员',
    workNo: 'SYS-ADMIN-01',
    roleCode: 'sys_admin',
    roleName: '超级管理员',
    duty: '【最高权限】唯一独享用户权限管理、多渠道通知密钥配置，拥有全平台所有权限',
    tagType: 'info'
  },
  {
    username: 'yanghai',
    realName: '杨海',
    workNo: 'HH-JT-005',
    roleCode: 'general_branch_admin',
    roleName: '党总支管理员',
    duty: '【总支+直管三支部全部业务】第20/25步审批批复、跨支部指标调控、一人一档归档、模板导入',
    tagType: 'danger'
  },
  {
    username: 'liweimin',
    realName: '李卫民',
    workNo: 'HH-HS-001',
    roleCode: 'branch_admin_hs',
    roleName: '红数支部管理员',
    duty: '【严格锁定红数支部】本支部党员发展、本支部名册维护、本支部三会一课记录与删除',
    tagType: 'warning'
  },
  {
    username: 'liujianhua',
    realName: '刘建华',
    workNo: 'HH-MC-001',
    roleCode: 'branch_admin_mc',
    roleName: '幂次支部管理员',
    duty: '【严格锁定幂次科技】本支部党员发展、本支部名册维护、本支部三会一课记录与删除',
    tagType: 'warning'
  },
  {
    username: 'chenming',
    realName: '陈明',
    workNo: 'HH-LD-001',
    roleCode: 'branch_admin_ld',
    roleName: '链达支部管理员',
    duty: '【严格锁定链达科技】本支部党员发展、本支部名册维护、本支部三会一课记录与删除',
    tagType: 'warning'
  },
  {
    username: 'zhangqiang',
    realName: '张强',
    workNo: 'HH-HS-012',
    roleCode: 'party_member',
    roleName: '普通党员 (积极分子)',
    duty: '【严格仅限个人档案】仅查阅个人一人一档成长进度及个人待办通知，无管理与审批权限',
    tagType: ''
  }
]

const forceChangePwdVisible = ref(false)
const pendingUser = ref(null)
const pendingRole = ref('party_member')

const changePwdForm = ref({
  oldPwd: '',
  newPwd: '',
  confirmPwd: ''
})

// 密码强度校验：长度>=8位，字母、数字、特殊字符
const pwdCheckLength = computed(() => (changePwdForm.value.newPwd || '').length >= 8)
const pwdCheckLetter = computed(() => /[a-zA-Z]/.test(changePwdForm.value.newPwd || ''))
const pwdCheckNumber = computed(() => /\d/.test(changePwdForm.value.newPwd || ''))
const pwdCheckSpecial = computed(() => /[^a-zA-Z0-9]/.test(changePwdForm.value.newPwd || ''))

const isPwdValid = computed(() => {
  return pwdCheckLength.value && 
         pwdCheckLetter.value && 
         pwdCheckNumber.value && 
         pwdCheckSpecial.value &&
         changePwdForm.value.newPwd === changePwdForm.value.confirmPwd &&
         changePwdForm.value.oldPwd.trim().length > 0 &&
         changePwdForm.value.newPwd !== changePwdForm.value.oldPwd
})

function checkAndProcessLogin(user, role) {
  // 检查是否为首次登录或需要强制改密 (mustChangePwd === true)
  if (user.mustChangePwd) {
    pendingUser.value = user
    pendingRole.value = role
    changePwdForm.value = {
      oldPwd: '',
      newPwd: '',
      confirmPwd: ''
    }
    forceChangePwdVisible.value = true
    ElMessage.warning('检测到您首次登录或当前为初始弱密码，依据国企网络安全要求，请先完成强密码修改！')
  } else {
    ElMessage.success(`欢迎进入系统，${user.realName} 同志！`)
    emit('login-success', { user, role })
  }
}

function submitForceChangePwd() {
  if (!isPwdValid.value) {
    ElMessage.warning('新密码必须包含字母、数字及特殊字符组合且长度不少于8位，且两次输入须一致！')
    return
  }

  // 1. 更新密码和状态
  pendingUser.value.password = changePwdForm.value.newPwd
  pendingUser.value.mustChangePwd = false

  forceChangePwdVisible.value = false
  ElMessage.success('恭喜！新密码设置成功，符合国企网络安全高强度标准，正在为您登录系统...')

  setTimeout(() => {
    emit('login-success', { user: pendingUser.value, role: pendingRole.value })
  }, 400)
}

function cancelForceChange() {
  forceChangePwdVisible.value = false
  pendingUser.value = null
  ElMessage.info('已取消改密并返回登录页')
}

async function handleAccountLogin() {
  if (!loginForm.value.username.trim()) {
    ElMessage.warning('请输入用户名或工号！')
    return
  }

  if (!loginForm.value.password) return ElMessage.warning('请输入密码')
  loggingIn.value = true
  try {
    const session = await loginAccount(loginForm.value.username.trim(), loginForm.value.password)
    loginForm.value.password = ''
    emit('login-success', session)
    ElMessage.success('登录成功')
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loggingIn.value = false
  }
}

function handleQuickLogin(roleItem) {
  if (!demoEnabled) return
  clearApiSession()
  const found = MOCK_SYS_USERS.find(u => u.username === roleItem.username) || {
    id: 100,
    username: roleItem.username,
    realName: roleItem.realName,
    workNo: roleItem.workNo,
    roleCode: roleItem.roleCode.toUpperCase(),
    roleName: roleItem.roleName,
    orgName: '中共红河数据产业集团有限公司总支部委员会',
    mustChangePwd: roleItem.roleCode === 'party_member', // 张强首次体验触发改密
    status: 1
  }

  checkAndProcessLogin(found, roleItem.roleCode)
}

function handleForgetPwd() {
  ElMessageBox.alert(
    '若忘记密码或需要重置，请联系红河数据产业集团党总支组织员（杨海，综合管理部 / 电话：13987301005）或系统管理员处理。',
    '密码重置咨询',
    { confirmButtonText: '已知晓', type: 'info' }
  )
}
</script>

<style scoped>
.login-container {
  width: 100vw;
  height: 100vh;
  position: relative;
  background: radial-gradient(circle at 50% 20%, #460809 0%, #200304 80%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* 背景光效与古典窗花花纹 */
.bg-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.top-light {
  position: absolute;
  top: -100px;
  left: 50%;
  transform: translateX(-50%);
  width: 800px;
  height: 350px;
  background: radial-gradient(ellipse, rgba(212, 175, 55, 0.25) 0%, rgba(194, 28, 29, 0) 70%);
  filter: blur(40px);
}

.login-box-wrapper {
  position: relative;
  z-index: 10;
  width: 1080px;
  max-width: 95vw;
  height: 620px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.45);
  display: flex;
  overflow: hidden;
  border: 1px solid rgba(212, 175, 55, 0.35);
}

/* 左侧品牌专区 */
.login-left-brand {
  flex: 1.15;
  background: linear-gradient(135deg, #a71819 0%, #830e10 100%);
  color: #fff;
  padding: 44px 42px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
}

.brand-top {
  display: flex;
  align-items: center;
  gap: 16px;
}

.party-emblem {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.org-titles .org-badge {
  font-size: 13px;
  color: #f4d03f;
  letter-spacing: 0.5px;
  margin-bottom: 4px;
  font-weight: 500;
}

.org-titles h2 {
  font-size: 22px;
  font-weight: 800;
  margin: 0 0 4px 0;
  letter-spacing: 1px;
}

.org-titles .eng-sub {
  font-size: 10.5px;
  opacity: 0.7;
  letter-spacing: 0.5px;
}

.brand-core-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin: 30px 0;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.f-icon {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  background: rgba(244, 208, 63, 0.18);
  color: #f4d03f;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.f-text strong {
  font-size: 14px;
  display: block;
  margin-bottom: 2px;
  color: #fff;
}

.f-text p {
  font-size: 12px;
  opacity: 0.75;
  margin: 0;
  line-height: 1.4;
}

.brand-footer-quote {
  font-size: 12px;
  line-height: 1.6;
  color: #f4d03f;
  border-top: 1px dashed rgba(244, 208, 63, 0.35);
  padding-top: 16px;
  font-style: italic;
}

/* 右侧登录表单专区 */
.login-right-card {
  flex: 1.1;
  background: #ffffff;
  padding: 40px 44px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.card-header h3 {
  font-size: 22px;
  color: #1a1a1a;
  font-weight: 700;
  margin: 0 0 6px 0;
}

.card-header .sub {
  font-size: 13px;
  color: #909399;
}

.login-tabs {
  margin-top: 16px;
  flex: 1;
}

.login-form {
  margin-top: 16px;
}

.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, #c21c1d 0%, #a71819 100%);
  border: none;
}

.submit-btn:hover {
  background: linear-gradient(135deg, #d32f2f 0%, #b71c1c 100%);
}

/* 快捷免密体验面板 */
.quick-roles-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.panel-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #e6a23c;
  background: #fdf6ec;
  padding: 8px 10px;
  border-radius: 4px;
}

.quick-role-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 330px;
  overflow-y: auto;
  padding-right: 4px;
}

.quick-role-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fafafa;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-role-item:hover {
  background: #fef0f0;
  border-color: #fbc4c4;
  transform: translateX(4px);
}

.qr-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.qr-avatar {
  background: #c21c1d;
  color: #fff;
  font-weight: bold;
}

.qr-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.qr-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.qr-name-row strong {
  font-size: 14px;
  color: #303133;
}

.qr-duty {
  font-size: 11.5px;
  color: #909399;
}

.security-declaration {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 12px;
  color: #909399;
  border-top: 1px solid #f0f0f0;
  padding-top: 14px;
  margin-top: 10px;
}

.security-declaration .el-icon {
  color: #67c23a;
}

/* 页脚居中版权与备案号 */
.login-footer {
  position: absolute;
  bottom: 18px;
  left: 0;
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  z-index: 10;
  pointer-events: auto;
}

.footer-links, .footer-copyright {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.72);
}

.footer-copyright {
  color: rgba(255, 255, 255, 0.55);
}

.login-footer .divider {
  color: rgba(255, 255, 255, 0.25);
  font-size: 11px;
}

.footer-link, .icp-link {
  color: rgba(244, 208, 63, 0.85);
  text-decoration: none;
  transition: all 0.2s;
}

.footer-link:hover, .icp-link:hover {
  color: #f4d03f;
  text-decoration: underline;
}

.security-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

/* 首次登录强密码检查提示样式 */
.pwd-strength-hint {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 8px;
  font-size: 11.5px;
  color: #f56c6c;
}

.pwd-strength-hint span {
  transition: color 0.2s;
}

.pwd-strength-hint .valid-req {
  color: #67c23a !important;
  font-weight: 600;
}
</style>
