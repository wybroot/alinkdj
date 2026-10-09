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
            <el-icon :size="46"><Flag /></el-icon>
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
              <p>企业微信 / 钉钉 / 106政务短信 / 邮件 / 站内信秒级触达</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="f-icon"><el-icon><DataBoard /></el-icon></div>
            <div class="f-text">
              <strong>国资党建指标全景调度大屏</strong>
              <p>三家子公司党支部“三会一课”规范台账与先锋示范岗</p>
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
          <el-tab-pane label="党务角色一键体验通道" name="quick">
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
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MOCK_SYS_USERS } from '../data/mockData.js'

const emit = defineEmits(['login-success'])

const loginTab = ref('quick')
const loggingIn = ref(false)
const rememberMe = ref(true)

const loginForm = ref({
  username: 'yanghai',
  password: ''
})

// 预设体验角色列表
const QUICK_ROLES = [
  {
    username: 'yanghai',
    realName: '杨海',
    workNo: 'HH-JT-005',
    roleCode: 'committee_organizer',
    roleName: '党总支组织员',
    duty: '总支规程审查把关 / 25步备案批复 / 指标调控 / 模板管理',
    tagType: 'danger'
  },
  {
    username: 'liweimin',
    realName: '李卫民',
    workNo: 'HH-HS-001',
    roleCode: 'branch_secretary',
    roleName: '支部书记 (红数信息)',
    duty: '发起本支部流程 / 召开三会一课 / 推优写实 / 维护名册',
    tagType: 'warning'
  },
  {
    username: 'zhouguoping',
    realName: '周国平',
    workNo: 'HH-JT-003',
    roleCode: 'discipline_inspector',
    roleName: '党总支纪检委员',
    duty: '政治审查廉洁把关 (一票否决权) / 违纪处分台账监管',
    tagType: 'primary'
  },
  {
    username: 'admin',
    realName: '系统管理员',
    workNo: 'SYS-ADMIN-01',
    roleCode: 'sys_admin',
    roleName: '系统超级管理员',
    duty: '党务用户开通 / 角色权限指派 / 多渠道通知服务配置',
    tagType: 'info'
  },
  {
    username: 'zhangqiang',
    realName: '张强',
    workNo: 'HH-HS-012',
    roleCode: 'member_self',
    roleName: '发展成员本人',
    duty: '在册积极分子 (第7步) / 个人档案查阅 / 思想汇报填报',
    tagType: ''
  }
]

function handleAccountLogin() {
  if (!loginForm.value.username.trim()) {
    ElMessage.warning('请输入用户名或工号！')
    return
  }

  loggingIn.value = true
  setTimeout(() => {
    loggingIn.value = false
    const kw = loginForm.value.username.trim().toLowerCase()
    const found = MOCK_SYS_USERS.find(u => 
      u.username.toLowerCase() === kw || 
      u.workNo.toLowerCase() === kw || 
      u.realName === kw
    )

    if (found) {
      if (found.status === 0) {
        ElMessage.error('该党务账号当前已被系统停用禁用，请联系党总支组织员！')
        return
      }

      // 匹配系统对应的角色 code
      const roleMap = {
        COMMITTEE_ORGANIZER: 'committee_organizer',
        BRANCH_SECRETARY: 'branch_secretary',
        DISCIPLINE_INSPECTOR: 'discipline_inspector',
        SYS_ADMIN: 'sys_admin',
        PARTY_MEMBER: 'member_self'
      }

      const currentRole = roleMap[found.roleCode] || 'committee_organizer'
      ElMessage.success(`欢迎进入系统，${found.realName} 同志！`)
      emit('login-success', { user: found, role: currentRole })
    } else {
      // 允许模拟登录
      const fallbackUser = {
        id: 999,
        username: loginForm.value.username,
        realName: loginForm.value.username,
        workNo: 'HH-TEMP-001',
        orgName: '中共红河数据产业集团有限公司总支部委员会',
        roleCode: 'COMMITTEE_ORGANIZER',
        roleName: '党总支组织员 (集团组织科)',
        status: 1
      }
      ElMessage.success(`欢迎进入智慧党建平台，${fallbackUser.realName}！`)
      emit('login-success', { user: fallbackUser, role: 'committee_organizer' })
    }
  }, 500)
}

function handleQuickLogin(roleItem) {
  const found = MOCK_SYS_USERS.find(u => u.username === roleItem.username) || {
    id: 100,
    username: roleItem.username,
    realName: roleItem.realName,
    workNo: roleItem.workNo,
    roleCode: roleItem.roleCode.toUpperCase(),
    roleName: roleItem.roleName,
    orgName: '中共红河数据产业集团有限公司总支部委员会',
    status: 1
  }

  ElMessage.success(`已成功登录：${roleItem.realName}（身份：${roleItem.roleName}）`)
  emit('login-success', { user: found, role: roleItem.roleCode })
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
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
  border: 2px solid #f4d03f;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #f4d03f;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
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
</style>
