<template>
  <div class="login-container">
    <el-row class="login-box" justify="center" align="middle">
      <el-col :span="12" class="login-left">
        <div class="login-left">
          <div v-if="showWelcome" class="welcome-text">
            <h2>欢迎回来</h2>
            <p>视频分享社区，分享快乐</p>
          </div>
          <img v-if="!showWelcome" :src="currentGif" class="anime-gif" alt="" />
        </div>
      </el-col>
      <el-col :span="12" class="login-right">
        <el-card class="login-card" shadow="never">
          <template #header>
            <div class="card-header">
              <h3>账号登录</h3>
              <p>还没有账号？<router-link to="/register" class="register-link">立即注册</router-link></p>
            </div>
          </template>
          
          <el-form 
            ref="formRef" 
            :model="loginForm" 
            :rules="loginRules" 
            class="login-form"
            size="large"
            @keyup.enter="handleLogin">
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                :prefix-icon="User"
                clearable
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                show-password
                :prefix-icon="Lock"
              />
            </el-form-item>

            <el-form-item>
              <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
              <el-link type="primary" class="forget-pwd">忘记密码？</el-link>
            </el-form-item>

            <el-form-item>
              <el-button 
                type="primary" 
                :loading="loading" 
                class="login-button"
                @click="handleLogin">
                登录
              </el-button>
            </el-form-item>

            <div class="other-login">
              <div class="divider">
                <span>其他登录方式</span>
              </div>
              <div class="social-login">
                <el-button :icon="Platform">QQ</el-button>
                <el-button :icon="ChatDotRound">微信</el-button>
                <el-button :icon="PhoneFilled">手机号</el-button>
              </div>
            </div>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Platform, ChatDotRound, PhoneFilled } from '@element-plus/icons-vue'
import { login } from '@/api/auth'
import { setToken, setRefreshToken, getToken } from '@/utils/auth'
import { useStore } from 'vuex'

export default {
  name: 'Login',
  setup() {
    const loading = ref(false)
    const loginForm = reactive({
      username: '',
      password: '',
      remember: false
    })

    const loginRules = reactive({
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' }
      ]
    })

    const formRef = ref(null)
    const router = useRouter()
    const store = useStore()

    const gifs = [
      '/assets/gif/naruto.gif',
      // '/assets/gif/superman.gif',
      // '/assets/gif/zoro.gif',
      // '/assets/gif/ichigo.gif'
    ]
    const currentIndex = ref(0)
    const currentGif = ref(gifs[0])
    const showWelcome = ref(true)
    let timer = null
    let gifTimer = null
    let firstShow = true

    // 页面加载时立即检查token并自动登录
    const token = getToken()
    if (token) {
      store.dispatch('user/getInfo')
        .then(() => {
          router.push('/')
        })
        .catch(() => {
          // token无效，留在登录页
        })
    }

    onMounted(() => {
      timer = setTimeout(() => {
        showWelcome.value = false
        // 1秒后开始轮播
        gifTimer = setInterval(() => {
          if (firstShow) {
            // 先保留已有gif一轮
            firstShow = false
            return
          }
          currentIndex.value = (currentIndex.value + 1) % gifs.length
          currentGif.value = gifs[currentIndex.value]
        }, 4000)
      }, 1000)
    })

    onBeforeUnmount(() => {
      clearTimeout(timer)
      clearInterval(gifTimer)
    })

    const handleLogin = () => {
      if (!formRef.value) return
      
      formRef.value.validate((valid) => {
        if (valid) {
          loading.value = true
          const { username, password, remember } = loginForm
          
          login({ username, password })
            .then((res) => {
              if (res.code === 200 && res.data) {
                const { accessToken, refreshToken, userInfo } = res.data
                // 存储 token
                setToken(accessToken)
                if (refreshToken) {
                  setRefreshToken(refreshToken, remember)
                }
                // 存储用户信息到 vuex
                store.commit('user/SET_USER_INFO', userInfo)
                store.commit('user/SET_TOKEN', accessToken)
                
                ElMessage({
                  message: '登录成功',
                  type: 'success'
                })
                router.push('/')
              } else {
                throw new Error(res.message || '登录失败')
              }
            })
            .catch((error) => {
              console.error('Login failed:', error)
              
              // 处理错误信息
              let errorMessage = '登录失败，请检查用户名和密码'
              
              if (error.response && error.response.data) {
                // 后端返回的错误信息
                const responseData = error.response.data
                errorMessage = responseData.message || errorMessage
                
                // 如果是运行时错误，提取具体的错误信息
                if (errorMessage.includes('运行时错误:')) {
                  errorMessage = errorMessage.replace('运行时错误:', '').trim()
                }
              } else if (error.message) {
                errorMessage = error.message
              }
              
              ElMessage.error(errorMessage)
            })
            .finally(() => {
              loading.value = false
            })
        }
      })
    }

    return {
      loading,
      loginForm,
      loginRules,
      formRef,
      handleLogin,
      User,
      Lock,
      Platform,
      ChatDotRound,
      PhoneFilled,
      showWelcome,
      currentGif
    }
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;

  .login-box {
    width: 1000px;
    height: 600px;
    background: white;
    border-radius: 20px;
    box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
    overflow: hidden;

    .login-left {
      width: 100%;
      height: 100%;
      min-height: 600px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 24px 0 0 24px;
      position: relative;
      overflow: hidden;

      .welcome-text {
        text-align: center;
        font-size: 2rem;
        margin-bottom: 24px;
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
      }

      .anime-gif {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: 24px 0 0 24px;
        display: block;
      }
    }

    .login-right {
      height: 600px;
      padding: 40px;
      display: flex;
      align-items: center;

      .login-card {
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
            .register-link {
              color: #667eea;
              text-decoration: none;
              &:hover {
                text-decoration: underline;
              }
            }
          }
        }

        .login-form {
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

          .login-options {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;

            .forget-link {
              color: #666;
              text-decoration: none;
              font-size: 14px;
              &:hover {
                color: #667eea;
              }
            }
          }

          .login-button {
            width: 100%;
            height: 44px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border: none;
            font-size: 16px;
            &:hover {
              opacity: 0.9;
            }
          }

          .other-login {
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

            .social-login {
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