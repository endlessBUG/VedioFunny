<template>
  <div class="user-profile">
    <el-row :gutter="20">
      <!-- 左侧用户信息卡片 -->
      <el-col :span="8">
        <el-card class="profile-card">
          <div class="profile-header">
            <el-avatar :size="100" :src="userInfo.avatar" />
            <h3>{{ userInfo.username }}</h3>
            <p>{{ userInfo.role }}</p>
          </div>
          
          <el-divider />
          
          <div class="profile-info">
            <div class="info-item">
              <span class="label">用户名：</span>
              <span class="value">{{ userInfo.username }}</span>
            </div>
            <div class="info-item">
              <span class="label">邮箱：</span>
              <span class="value">{{ userInfo.email }}</span>
            </div>
            <div class="info-item">
              <span class="label">手机号：</span>
              <span class="value">{{ userInfo.phone }}</span>
            </div>
            <div class="info-item">
              <span class="label">注册时间：</span>
              <span class="value">{{ userInfo.createTime }}</span>
            </div>
            <div class="info-item">
              <span class="label">最后登录：</span>
              <span class="value">{{ userInfo.lastLoginTime }}</span>
            </div>
          </div>
          
          <el-divider />
          
          <div class="profile-actions">
            <el-button type="primary" @click="handleEdit">编辑资料</el-button>
            <el-button @click="handleChangePassword">修改密码</el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧详细信息 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>详细信息</span>
            </div>
          </template>
          
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本信息" name="basic">
              <el-form :model="userInfo" label-width="100px">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="真实姓名">
                      <el-input v-model="userInfo.realName" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="性别">
                      <el-select v-model="userInfo.gender" style="width: 100%">
                        <el-option label="男" value="male" />
                        <el-option label="女" value="female" />
                        <el-option label="保密" value="secret" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="生日">
                      <el-date-picker
                        v-model="userInfo.birthday"
                        type="date"
                        placeholder="选择日期"
                        style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="所在地区">
                      <el-cascader
                        v-model="userInfo.region"
                        :options="regionOptions"
                        placeholder="请选择地区"
                        style="width: 100%"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>
                
                <el-form-item label="个人简介">
                  <el-input
                    v-model="userInfo.bio"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入个人简介"
                  />
                </el-form-item>
                
                <el-form-item>
                  <el-button type="primary" @click="handleSave">保存</el-button>
                  <el-button @click="handleReset">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="安全设置" name="security">
              <div class="security-items">
                <div class="security-item">
                  <div class="item-info">
                    <h4>登录密码</h4>
                    <p>建议您定期更换密码，设置安全性高的密码可以使账号更安全</p>
                  </div>
                  <el-button @click="handleChangePassword">修改</el-button>
                </div>
                
                <el-divider />
                
                <div class="security-item">
                  <div class="item-info">
                    <h4>手机绑定</h4>
                    <p>已绑定手机：{{ userInfo.phone }}</p>
                  </div>
                  <el-button @click="handleChangePhone">修改</el-button>
                </div>
                
                <el-divider />
                
                <div class="security-item">
                  <div class="item-info">
                    <h4>邮箱绑定</h4>
                    <p>已绑定邮箱：{{ userInfo.email }}</p>
                  </div>
                  <el-button @click="handleChangeEmail">修改</el-button>
                </div>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="登录记录" name="login-history">
              <el-table :data="loginHistory" style="width: 100%">
                <el-table-column prop="loginTime" label="登录时间" width="180" />
                <el-table-column prop="ip" label="IP地址" width="140" />
                <el-table-column prop="location" label="登录地点" />
                <el-table-column prop="device" label="设备信息" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="scope.row.status === '成功' ? 'success' : 'danger'">
                      {{ scope.row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
            placeholder="请输入原密码"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handlePasswordSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserInfo } from '@/api/user'

export default {
  name: 'UserProfile',
  setup() {
    const activeTab = ref('basic')
    const passwordDialogVisible = ref(false)
    const passwordFormRef = ref(null)

    const userInfo = reactive({
      username: '',
      email: '',
      phone: '',
      role: '',
      avatar: '',
      createTime: '',
      lastLoginTime: '',
      realName: '',
      gender: '',
      birthday: '',
      region: [],
      bio: ''
    })

    const passwordForm = reactive({
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })

    const passwordRules = {
      oldPassword: [
        { required: true, message: '请输入原密码', trigger: 'blur' }
      ],
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请再次输入新密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (value !== passwordForm.newPassword) {
              callback(new Error('两次输入的密码不一致'))
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }
      ]
    }

    const regionOptions = [
      {
        value: '北京市',
        label: '北京市',
        children: [
          {
            value: '北京市',
            label: '北京市',
            children: [
              { value: '朝阳区', label: '朝阳区' },
              { value: '海淀区', label: '海淀区' },
              { value: '东城区', label: '东城区' }
            ]
          }
        ]
      }
    ]

    const loginHistory = ref([
      {
        loginTime: '2024-01-15 10:30:00',
        ip: '192.168.1.100',
        location: '北京市',
        device: 'Chrome 120.0.0.0',
        status: '成功'
      },
      {
        loginTime: '2024-01-14 15:20:00',
        ip: '192.168.1.101',
        location: '上海市',
        device: 'Firefox 121.0',
        status: '成功'
      },
      {
        loginTime: '2024-01-13 09:15:00',
        ip: '192.168.1.102',
        location: '广州市',
        device: 'Safari 17.0',
        status: '失败'
      }
    ])

    const handleEdit = () => {
      ElMessage.info('进入编辑模式')
    }

    const handleSave = () => {
      ElMessage.success('保存成功')
    }

    const handleReset = () => {
      ElMessage.info('已重置')
    }

    const handleChangePassword = () => {
      passwordDialogVisible.value = true
    }

    const handleChangePhone = () => {
      ElMessage.info('修改手机号功能')
    }

    const handleChangeEmail = () => {
      ElMessage.info('修改邮箱功能')
    }

    const handlePasswordSubmit = async () => {
      if (!passwordFormRef.value) return
      
      try {
        await passwordFormRef.value.validate()
        ElMessage.success('密码修改成功')
        passwordDialogVisible.value = false
        // 重置表单
        Object.assign(passwordForm, {
          oldPassword: '',
          newPassword: '',
          confirmPassword: ''
        })
      } catch (error) {
        ElMessage.error('表单验证失败')
      }
    }

    onMounted(() => {
      getUserInfo().then(res => {
        if (res.code === 200 && res.data) {
          const data = res.data
          Object.assign(userInfo, {
            username: data.username,
            email: data.email,
            phone: data.phone,
            role: data.roles ? data.roles[0] : '',
            avatar: data.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
            lastLoginTime: data.lastLoginTime,
            realName: data.nickname || data.username,
            userType: data.userType
          })
        } else {
          ElMessage.error(res.message || '获取用户信息失败')
        }
      }).catch(error => {
        ElMessage.error('获取用户信息失败：' + error.message)
      })
    })

    return {
      activeTab,
      userInfo,
      passwordDialogVisible,
      passwordForm,
      passwordFormRef,
      passwordRules,
      regionOptions,
      loginHistory,
      handleEdit,
      handleSave,
      handleReset,
      handleChangePassword,
      handleChangePhone,
      handleChangeEmail,
      handlePasswordSubmit
    }
  }
}
</script>

<style scoped>
.user-profile {
  padding: 20px;
}

.profile-card {
  text-align: center;
}

.profile-header {
  margin-bottom: 20px;
}

.profile-header h3 {
  margin: 10px 0 5px 0;
  color: #303133;
}

.profile-header p {
  color: #909399;
  margin: 0;
}

.profile-info {
  text-align: left;
}

.info-item {
  display: flex;
  margin-bottom: 10px;
}

.info-item .label {
  color: #909399;
  width: 80px;
  flex-shrink: 0;
}

.info-item .value {
  color: #303133;
  flex: 1;
}

.profile-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.security-items {
  padding: 20px 0;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
}

.item-info h4 {
  margin: 0 0 5px 0;
  color: #303133;
}

.item-info p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

:deep(.el-tabs__content) {
  padding: 20px 0;
}
</style> 