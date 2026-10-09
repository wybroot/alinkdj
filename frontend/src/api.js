import { ref } from 'vue'

export const demoEnabled = import.meta.env.DEV && import.meta.env.VITE_DEMO_MODE !== 'false'
export const apiSession = ref(null)
const baseUrl = (import.meta.env.VITE_API_BASE_URL || '/api').replace(/\/$/, '')

export function clearApiSession() {
  apiSession.value = null
}

export async function apiRequest(path, options = {}) {
  if (path !== '/auth/login' && !apiSession.value?.token) {
    throw new Error('请退出演示身份，使用正式账号和密码登录后操作通知服务')
  }
  let response
  try {
    response = await fetch(`${baseUrl}${path}`, {
      method: options.method || 'GET',
      headers: { 'Content-Type': 'application/json', ...(apiSession.value?.token ? { Authorization: `Bearer ${apiSession.value.token}` } : {}) },
      ...(options.body !== undefined ? { body: JSON.stringify(options.body) } : {}),
      signal: AbortSignal.timeout(55000)
    })
  } catch {
    throw new Error('无法确认服务器响应，请检查网络并刷新通知记录；请勿直接重复发送')
  }
  let result
  try { result = await response.json() } catch { throw new Error('服务接口不可用，请检查后端服务及代理配置') }
  if (!response.ok || result.code !== 200) {
    if (response.status === 401 || result.code === 401) clearApiSession()
    const error = new Error(result.message || '操作失败')
    error.data = result.data
    throw error
  }
  return result.data
}

export async function loginAccount(username, password) {
  const data = await apiRequest('/auth/login', { method: 'POST', body: { username, password } })
  const codes = data.roles.map(role => role.roleCode)
  let role
  if (codes.includes('SYS_ADMIN')) role = 'sys_admin'
  else if (codes.includes('GENERAL_BRANCH_ADMIN')) role = 'general_branch_admin'
  else if (codes.includes('BRANCH_ADMIN')) role = { 2: 'branch_admin_hs', 3: 'branch_admin_mc', 4: 'branch_admin_ld' }[data.userInfo.orgId]
  else if (codes.includes('PARTY_MEMBER')) role = 'party_member'
  if (!role) throw new Error('账号尚未分配有效角色或所属组织，请联系管理员')
  const user = { ...data.userInfo, roleName: data.roles.map(item => item.roleName).join(' / ') }
  apiSession.value = { token: data.token, user, role }
  return { user, role }
}
