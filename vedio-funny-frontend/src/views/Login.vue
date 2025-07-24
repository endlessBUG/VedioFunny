<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>VedioFun 管理系统</h2>
        <p>欢迎登录</p>
      </div>
      
      <el-form 
        ref="loginFormRef" 
        :model="loginForm" 
        :rules="loginRules" 
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            prefix-icon="User"
            size="large"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-tips">
        <p>测试账号: admin / 123456</p>
        <p>普通用户: test / 123456</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

export default {
  name: 'Login',
  setup() {
    const store = useStore()
    const router = useRouter()
    
    // 响应式数据
    const loginFormRef = ref(null)
    const loading = ref(false)
    
    const loginForm = reactive({
      username: 'admin',
      password: '123456',
      remember: false
    })

    // 表单验证规则
    const loginRules = reactive({
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
      ]
    })

    // 登录处理函数
    const handleLogin = async () => {
      console.log('🚀 开始登录流程')
      console.log('表单数据:', loginForm)
      
      if (!loginFormRef.value) {
        console.error('❌ 表单引用不存在')
        ElMessage.error('表单初始化失败')
        return
      }
      
      try {
        // 表单验证
        console.log('📝 开始表单验证...')
        await loginFormRef.value.validate()
        console.log('✅ 表单验证通过')
        
        loading.value = true
        
        // 调用登录API
        console.log('🔐 调用登录API...')
        const loginData = {
          username: loginForm.username.trim(),
          password: loginForm.password,
          remember: loginForm.remember
        }
        console.log('登录参数:', loginData)
        
        // 通过Vuex store调用登录
        await store.dispatch('user/login', loginData)
        console.log('✅ 登录成功')
        
        ElMessage.success('登录成功！')
        
        // 获取用户信息
        console.log('👤 获取用户信息...')
        await store.dispatch('user/getInfo')
        console.log('✅ 用户信息获取成功')
        
        // 跳转到主页
        const redirect = router.currentRoute.value.query.redirect || '/'
        console.log('🔄 准备跳转到:', redirect)
        
        await router.push(redirect)
        console.log('✅ 页面跳转成功')
        
      } catch (error) {
        console.error('❌ 登录失败:', error)
        console.error('错误详情:', error.response || error.message || error)
        
        // 处理错误信息
        let errorMessage = '登录失败'
        
        if (error.response && error.response.data) {
          // 后端返回的错误信息
          const responseData = error.response.data
          errorMessage = responseData.message || '登录失败'
          
          // 如果是运行时错误，提取具体的错误信息
          if (errorMessage.includes('运行时错误:')) {
            errorMessage = errorMessage.replace('运行时错误:', '').trim()
          }
        } else if (error.response) {
          // HTTP错误响应
          const status = error.response.status
          const data = error.response.data
          
          if (status === 401) {
            errorMessage = '用户名或密码错误'
          } else if (status === 403) {
            errorMessage = '账号已被禁用'
          } else if (status === 500) {
            errorMessage = '服务器内部错误'
          } else {
            errorMessage = data?.message || `HTTP ${status} 错误`
          }
        } else if (error.message) {
          // 网络错误或其他错误
          if (error.message.includes('Network Error')) {
            errorMessage = '网络连接失败，请检查网络或服务器状态'
          } else {
            errorMessage = error.message
          }
        }
        
        ElMessage.error(errorMessage)
      } finally {
        loading.value = false
        console.log('🏁 登录流程结束')
      }
    }

    // 组件挂载后的调试信息
    onMounted(() => {
      console.log('🎯 登录组件已挂载')
      console.log('Store:', store)
      console.log('Router:', router)
      console.log('Form Ref:', loginFormRef.value)
      
      // 延迟检查组件状态
      setTimeout(() => {
        console.log('📊 组件状态检查:')
        console.log('- loginFormRef:', loginFormRef.value)
        console.log('- loginForm:', loginForm)
        console.log('- 当前路由:', router.currentRoute.value.path)
        console.log('- 是否已登录:', store.getters.isLoggedIn)
      }, 1000)
    })

    return {
      loginFormRef,
      loginForm,
      loginRules,
      loading,
      handleLogin
    }
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-box {
  width: 400px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  padding: 40px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  color: #303133;
  margin-bottom: 10px;
  font-size: 24px;
}

.login-header p {
  color: #909399;
  font-size: 14px;
}

.login-form {
  margin-top: 20px;
}

.login-btn {
  width: 100%;
  height: 45px;
}

.login-tips {
  margin-top: 20px;
  padding: 15px;
  background: #f0f9ff;
  border-radius: 5px;
  border-left: 4px solid #409eff;
}

.login-tips p {
  margin: 5px 0;
  font-size: 13px;
  color: #606266;
}

/* Element Plus 样式覆盖 */
:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
}

:deep(.el-checkbox__label) {
  color: #606266;
}
</style> 