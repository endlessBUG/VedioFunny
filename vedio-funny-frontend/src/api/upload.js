import axios from 'axios'

/**
 * 创建支持进度监控的上传请求
 * @param {File} file - 要上传的文件
 * @param {Function} onProgress - 进度回调函数
 * @returns {Promise} 上传响应
 */
export const uploadFile = (file, onProgress) => {
  // 创建FormData对象
  const formData = new FormData()
  formData.append('file', file)

  // 创建自定义的axios实例，用于上传
  return axios.post('/api/upload', formData, {
    // 上传进度处理
    onUploadProgress: (progressEvent) => {
      if (progressEvent.total) {
        // 计算上传进度百分比
        const percentage = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        // 调用进度回调
        onProgress({ percent: percentage })
      }
    },
    // 设置请求头
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    // 支持大文件上传
    maxContentLength: Infinity,
    maxBodyLength: Infinity,
    // 超时设置（对于大文件要设置较长的超时时间）
    timeout: 24 * 60 * 60 * 1000 // 24小时
  })
} 