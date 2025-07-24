<template>
  <div style="padding: 50px; max-width: 500px; margin: 0 auto;">
    <h2>简单登录测试</h2>
    
    <div style="margin: 20px 0; padding: 10px; background: #f0f0f0; border-radius: 5px;">
      <strong>测试账号：</strong><br>
      admin / 123456<br>
      test / 123456
    </div>
    
    <div style="margin: 20px 0;">
      <label>用户名：</label><br>
      <input 
        v-model="username" 
        style="width: 200px; padding: 5px; margin: 5px 0;"
        placeholder="输入用户名"
      />
    </div>
    
    <div style="margin: 20px 0;">
      <label>密码：</label><br>
      <input 
        v-model="password" 
        type="password"
        style="width: 200px; padding: 5px; margin: 5px 0;"
        placeholder="输入密码"
      />
    </div>
    
    <div style="margin: 20px 0;">
      <button 
        @click="testLogin" 
        :disabled="loading"
        style="padding: 10px 20px; background: #409eff; color: white; border: none; border-radius: 5px; cursor: pointer;"
      >
        {{ loading ? '登录中...' : '测试登录' }}
      </button>
      
      <button 
        @click="testConnection" 
        style="padding: 10px 20px; background: #67c23a; color: white; border: none; border-radius: 5px; cursor: pointer; margin-left: 10px;"
      >
        测试连接
      </button>
      
      <button 
        @click="clearResult" 
        style="padding: 10px 20px; background: #e6a23c; color: white; border: none; border-radius: 5px; cursor: pointer; margin-left: 10px;"
      >
        清除结果
      </button>
    </div>
    
    <div v-if="result" style="margin: 20px 0; padding: 15px; background: #f9f9f9; border-radius: 5px; border: 1px solid #ddd;">
      <h3>测试结果：</h3>
      <pre style="white-space: pre-wrap; word-wrap: break-word;">{{ result }}</pre>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import axios from 'axios'

export default {
  name: 'LoginSimple',
  setup() {
    const username = ref('admin')
    const password = ref('123456')
    const loading = ref(false)
    const result = ref('')

    const testLogin = async () => {
      loading.value = true
      result.value = ''
      
      const startTime = Date.now()
      
      try {
        console.log('=== 开始登录测试 ===')
        console.log('用户名:', username.value)
        console.log('密码:', password.value)
        console.log('时间:', new Date().toLocaleString())
        
        // 直接调用登录API
        const response = await axios.post('http://localhost:8082/api/auth/login', {
          username: username.value,
          password: password.value,
          remember: false
        }, {
          timeout: 10000,
          headers: {
            'Content-Type': 'application/json'
          }
        })
        
        const endTime = Date.now()
        console.log('登录成功！响应时间:', endTime - startTime, 'ms')
        console.log('响应数据:', response.data)
        
        result.value = `✅ 登录成功！
        
响应时间: ${endTime - startTime}ms
状态码: ${response.status}
响应数据:
${JSON.stringify(response.data, null, 2)}`
        
      } catch (error) {
        const endTime = Date.now()
        console.error('登录失败！响应时间:', endTime - startTime, 'ms')
        console.error('错误详情:', error)
        
        let errorMsg = '❌ 登录失败！\n\n'
        errorMsg += `响应时间: ${endTime - startTime}ms\n`
        
        if (error.response) {
          errorMsg += `状态码: ${error.response.status}\n`
          errorMsg += `错误信息: ${error.response.data?.message || '未知错误'}\n`
          errorMsg += `响应数据: ${JSON.stringify(error.response.data, null, 2)}`
        } else if (error.request) {
          errorMsg += `网络错误: ${error.message}\n`
          errorMsg += '可能原因: 后端服务未启动或网络连接问题'
        } else {
          errorMsg += `其他错误: ${error.message}`
        }
        
        result.value = errorMsg
      } finally {
        loading.value = false
      }
    }

    const testConnection = async () => {
      try {
        result.value = '🔄 测试连接中...'
        
        const response = await axios.get('http://localhost:8082/api/auth/test', {
          timeout: 5000
        })
        
        result.value = `✅ 连接成功！
        
服务状态: 正常
响应数据:
${JSON.stringify(response.data, null, 2)}`
        
      } catch (error) {
        let errorMsg = '❌ 连接失败！\n\n'
        
        if (error.code === 'ECONNREFUSED') {
          errorMsg += '错误类型: 连接被拒绝\n'
          errorMsg += '可能原因: 网关服务(8082)未启动\n'
          errorMsg += '解决方案: 请先启动 vedio-funny-gateway 服务'
        } else if (error.code === 'ENOTFOUND') {
          errorMsg += '错误类型: 域名解析失败\n'
          errorMsg += '可能原因: 网络连接问题'
        } else {
          errorMsg += `错误信息: ${error.message}`
        }
        
        result.value = errorMsg
      }
    }

    const clearResult = () => {
      result.value = ''
    }

    // 页面加载时自动测试连接
    setTimeout(() => {
      testConnection()
    }, 1000)

    return {
      username,
      password,
      loading,
      result,
      testLogin,
      testConnection,
      clearResult
    }
  }
}
</script> 