import request from '@/utils/request'

export function listPermissions() {
  return request({ url: '/api/auth/permission/list', method: 'get' })
}
export function addPermission(data) {
  return request({ url: '/api/auth/permission/add', method: 'post', data })
}
export function updatePermission(data) {
  return request({ url: '/api/auth/permission/update', method: 'post', data })
}
export function deletePermission(id) {
  return request({ url: `/api/auth/permission/${id}`, method: 'delete' })
} 