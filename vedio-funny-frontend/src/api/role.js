import request from '@/utils/request'

// 获取角色列表
export function listRoles() {
  return request({ url: '/api/auth/role/list', method: 'get' })
}

// 添加角色
export function addRole(data) {
  return request({ url: '/api/auth/role/add', method: 'post', data })
}

// 更新角色
export function updateRole(data) {
  return request({ url: '/api/auth/role/update', method: 'post', data })
}

// 删除角色
export function deleteRole(id) {
  return request({ url: `/api/auth/role/${id}`, method: 'delete' })
}

// 获取角色详情
export function getRoleById(id) {
  return request({ url: `/api/auth/role/${id}`, method: 'get' })
}

// 获取所有权限列表
export function listPermissions() {
  return request({ url: '/api/auth/permission/list', method: 'get' })
}

// 获取角色的权限列表
export function getRolePermissions(roleId) {
  return request({ 
    url: '/api/auth/role/permissions', 
    method: 'get',
    params: { roleId }
  })
}

// 更新角色权限
export function updateRolePermissions(roleId, permissionIds) {
  return request({ 
    url: '/api/auth/role/permissions', 
    method: 'post', 
    params: { roleId },
    data: permissionIds 
  })
} 
 