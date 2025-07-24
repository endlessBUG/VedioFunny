import request from '@/utils/request'

// 部署服务
export function deployServer(data) {
  return request({
    url: '/server/deploy',
    method: 'post',
    data
  })
}

// ... 其他已有的API方法 ... 