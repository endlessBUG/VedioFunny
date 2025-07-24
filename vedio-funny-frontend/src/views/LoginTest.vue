<template>
  <div style="padding: 50px;">
    <h2>登录测试页面</h2>
    <div style="margin: 20px 0;">
      <strong>测试账号：</strong>
      <ul>
        <li>管理员：admin / 123456</li>
        <li>普通用户：test / 123456</li>
      </ul>
    </div>
    
    <el-form style="max-width: 400px;">
      <el-form-item label="用户名">
        <el-input v-model="username" placeholder="输入用户名" />
      </el-form-item>
      
      <el-form-item label="密码">
        <el-input v-model="password" type="password" placeholder="输入密码" />
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="testLogin" :loading="loading">
          测试登录
        </el-button>
        <el-button @click="testAPI">测试API连接</el-button>
      </el-form-item>
    </el-form>
    
    <div v-if="result" style="margin-top: 20px;">
      <h3>测试结果：</h3>
      <pre>{{ result }}</pre>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { login } from '@/api/auth'
import { ElMessage } from 'element-plus'
import axios from 'axios'

export default {
  name: 'LoginTest',
  setup() {
    const username = ref('admin')
    const password = ref('123456')
    const loading = ref(false)
    const result = ref('')

    const testLogin = async () => {
      loading.value = true
      result.value = ''
      
      try {
        console.log('开始测试登录...')
        console.log('用户名:', username.value)
        console.log('密码:', password.value)
        
        const response = await login({
          username: username.value,
          password: password.value,
          remember: false
        })
        
        console.log('登录成功响应:', response)
        result.value = JSON.stringify(response, null, 2)
        ElMessage.success('登录测试成功')
        
      } catch (error) {
        console.error('登录测试失败:', error)
        result.value = `登录失败: ${error.message || error}`
        ElMessage.error('登录测试失败: ' + (error.message || error))
      } finally {
        loading.value = false
      }
    }

    const testAPI = async () => {
      try {
        console.log('测试API连接...')
        const response = await axios.get('http://localhost:8082/api/auth/test', {
          timeout: 5000
        })
        console.log('API连接成功:', response)
        result.value = `API连接成功: ${JSON.stringify(response.data, null, 2)}`
        ElMessage.success('API连接成功')
      } catch (error) {
        console.error('API连接失败:', error)
        if (error.code === 'ECONNREFUSED') {
          result.value = '网关服务(8082)未启动，请先启动后端服务'
        } else {
          result.value = `API连接失败: ${error.message}`
        }
        ElMessage.error('API连接失败')
      }
    }

    return {
      username,
      password,
      loading,
      result,
      testLogin,
      testAPI
    }
  }
}
</script> 