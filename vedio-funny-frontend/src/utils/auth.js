import Cookies from 'js-cookie'

const TokenKey = 'Admin-Token'
const RefreshTokenKey = 'vedio_funny_refresh_token'

// Token相关操作
export function getToken() {
  return localStorage.getItem(TokenKey)
}

export function setToken(token) {
  // 如果 token 已经包含 Bearer 前缀，直接存储
  // 否则添加 Bearer 前缀
  const tokenValue = token.startsWith('Bearer ') ? token : `Bearer ${token}`
  return localStorage.setItem(TokenKey, tokenValue)
}

export function removeToken() {
  return localStorage.removeItem(TokenKey)
}

// 刷新Token相关操作
export function getRefreshToken() {
  return Cookies.get(RefreshTokenKey)
}

export function setRefreshToken(refreshToken, remember = false) {
  const options = remember ? { expires: 30 } : { expires: 7 } // 刷新token默认7天，记住我30天
  return Cookies.set(RefreshTokenKey, refreshToken, options)
}

export function removeRefreshToken() {
  return Cookies.remove(RefreshTokenKey)
}

// 清除所有认证信息
export function clearAuth() {
  removeToken()
  removeRefreshToken()
}

// 检查是否已登录
export function isLoggedIn() {
  return !!getToken()
} 