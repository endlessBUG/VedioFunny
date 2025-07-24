import request from '@/utils/request'

export function getModelInstances() {
  return request({
    url: '/api/registry/vedio-funny-model/instances',
    method: 'get'
  })
} 