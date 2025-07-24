<template>
  <div class="register-container">
    <el-row class="register-box" justify="center" align="middle">
      <el-col :span="12" class="register-left">
        <div class="welcome-text">
          <h2>欢迎加入</h2>
          <p>加入我们的视频分享社区</p>
        </div>
      </el-col>
      <el-col :span="12" class="register-right">
        <el-card class="register-card" shadow="never">
      <template #header>
            <div class="card-header">
              <h3>创建账号</h3>
              <p>已有账号？<router-link to="/login" class="login-link">立即登录</router-link></p>
            </div>
      </template>
      
          <el-form 
            ref="formRef" 
            :model="registerForm" 
            :rules="registerRules" 
            class="register-form"
            size="large"
            @keyup.enter="handleRegister">
            <el-form-item prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名"
                :prefix-icon="User"
                clearable
              />
        </el-form-item>

            <el-form-item prop="email">
              <el-input
                v-model="registerForm.email"
                placeholder="请输入邮箱"
                :prefix-icon="Message"
                clearable
              />
        </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码"
                show-password
                :prefix-icon="Lock"
              />
        </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请确认密码"
                show-password
                :prefix-icon="Lock"
              />
        </el-form-item>

            <el-form-item prop="agreement">
              <el-checkbox v-model="registerForm.agreement">
                我已阅读并同意
                <el-link type="primary">服务条款</el-link>
                和
                <el-link type="primary">隐私政策</el-link>
              </el-checkbox>
        </el-form-item>

        <el-form-item>
              <el-button 
                type="primary" 
                :loading="loading" 
                class="register-button"
                @click="handleRegister">
                注册
              </el-button>
            </el-form-item>

            <div class="other-register">
              <div class="divider">
                <span>其他注册方式</span>
              </div>
              <div class="social-register">
                <el-button :icon="Platform" circle />
                <el-button :icon="ChatDotRound" circle />
                <el-button :icon="PhoneFilled" circle />
              </div>
            </div>

            <el-form-item class="login-link">
              <span>已有账号？</span>
              <router-link to="/login">立即登录</router-link>
        </el-form-item>
      </el-form>
    </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Platform, ChatDotRound, PhoneFilled } from '@element-plus/icons-vue'
import { register } from '@/api/auth'

export default {
  name: 'Register',
  setup() {
const loading = ref(false)
    const registerForm = reactive({
  username: '',
      email: '',
  password: '',
  confirmPassword: '',
      agreement: false
})

    const registerRules = reactive({
  username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' }
      ],
      agreement: [
        { required: true, message: '请同意服务条款', trigger: 'change' }
  ]
    })

    const formRef = ref(null)
    const router = useRouter()

    const handleRegister = () => {
      if (!formRef.value) return
  
      formRef.value.validate((valid) => {
        if (valid) {
    loading.value = true
          const { username, password, email } = registerForm
          
          register({ username, password, email })
            .then(() => {
              ElMessage({
                message: '注册成功',
                type: 'success'
              })
    router.push('/login')
            })
            .catch((error) => {
              console.error('Registration failed:', error)
              let msg = '注册失败，请稍后重试'
              if (error.response && error.response.data && error.response.data.message) {
                msg = error.response.data.message
              } else if (error.message) {
                msg = error.message
              }
              ElMessage.error(msg)
            })
            .finally(() => {
    loading.value = false
            })
        }
      })
    }

    return {
      loading,
      registerForm,
      registerRules,
      formRef,
      handleRegister,
      User,
      Lock,
      Message,
      Platform,
      ChatDotRound,
      PhoneFilled
    }
  }
}
</script>

<style lang="scss" scoped>
.register-container {
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;

  .register-box {
    width: 1000px;
    height: 700px;
    background: white;
    border-radius: 20px;
    box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
    overflow: hidden;

    .register-left {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      height: 700px;
      display: flex;
  align-items: center;
      justify-content: center;
      color: white;
      padding: 40px;

      .welcome-text {
        text-align: center;
        h2 {
          font-size: 36px;
          margin-bottom: 20px;
        }
        p {
          font-size: 16px;
          opacity: 0.9;
        }
      }
    }

    .register-right {
      height: 700px;
      padding: 40px;
      display: flex;
      align-items: center;

.register-card {
        width: 100%;
        border: none;

        .card-header {
          text-align: center;
          padding-bottom: 20px;
          h3 {
            font-size: 24px;
            color: #333;
            margin-bottom: 10px;
          }
          p {
            color: #666;
            font-size: 14px;
            .login-link {
              color: #667eea;
              text-decoration: none;
              &:hover {
                text-decoration: underline;
              }
            }
          }
        }

        .register-form {
          .el-input {
            --el-input-border-color: #dcdfe6;
            --el-input-hover-border-color: #c0c4cc;
            --el-input-focus-border-color: #409eff;
            
            :deep(.el-input__wrapper) {
              box-shadow: 0 0 0 1px var(--el-input-border-color) inset;
              
              &:hover {
                box-shadow: 0 0 0 1px var(--el-input-hover-border-color) inset;
              }
              
              &.is-focus {
                box-shadow: 0 0 0 1px var(--el-input-focus-border-color) inset;
              }
            }
          }

          .agreement {
            margin: 20px 0;
            .agreement-link {
              color: #667eea;
              text-decoration: none;
              &:hover {
                text-decoration: underline;
              }
            }
          }

          .register-button {
            width: 100%;
            height: 44px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border: none;
            font-size: 16px;
            &:hover {
              opacity: 0.9;
            }
}

          .other-register {
            margin-top: 30px;
            .divider {
              display: flex;
              align-items: center;
              margin: 20px 0;
              
              &::before,
              &::after {
                content: '';
                flex: 1;
                height: 1px;
                background: #eee;
              }
              
              span {
                padding: 0 15px;
                color: #666;
                font-size: 14px;
              }
}

            .social-register {
              display: flex;
              justify-content: center;
              gap: 20px;

.el-button {
                background: #f5f7fa;
                border: none;
                color: #666;
                &:hover {
                  background: #e4e7ed;
                }
              }
            }
          }
        }
      }
    }
  }
}
</style> 