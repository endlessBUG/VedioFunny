import request from '@/utils/request'

// 获取用户列表
export function fetchUserList(params) {
  return request({
    url: '/api/auth/user/list',
    method: 'get',
    params
  })
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/api/auth/user/delete/${id}`,
    method: 'delete'
  })
}

// 编辑用户（预留）
export function editUser(data) {
  return request({
    url: '/api/auth/user/edit',
    method: 'post',
    data
  })
}

// 获取当前用户信息
export function getUserInfo() {
  return request({
    url: '/api/auth/auth/userinfo',
    method: 'get',
    headers: {
      Authorization: localStorage.getItem('token')
    }
  })
} 