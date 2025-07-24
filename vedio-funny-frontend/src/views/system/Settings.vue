<template>
  <div class="system-settings">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统设置</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" tab-position="left">
        <!-- 基本设置 -->
        <el-tab-pane label="基本设置" name="basic">
          <el-form :model="basicSettings" label-width="120px">
            <el-form-item label="系统名称">
              <el-input v-model="basicSettings.systemName" placeholder="请输入系统名称" />
            </el-form-item>
            
            <el-form-item label="系统版本">
              <el-input v-model="basicSettings.version" disabled />
            </el-form-item>
            
            <el-form-item label="系统Logo">
              <el-upload
                class="avatar-uploader"
                action="#"
                :show-file-list="false"
                :on-success="handleLogoSuccess"
                :before-upload="beforeLogoUpload"
              >
                <img v-if="basicSettings.logo" :src="basicSettings.logo" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>
            
            <el-form-item label="系统描述">
              <el-input
                v-model="basicSettings.description"
                type="textarea"
                :rows="3"
                placeholder="请输入系统描述"
              />
            </el-form-item>
            
            <el-form-item label="维护模式">
              <el-switch v-model="basicSettings.maintenanceMode" />
              <span class="setting-tip">开启后系统将进入维护状态</span>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveBasicSettings">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 安全设置 -->
        <el-tab-pane label="安全设置" name="security">
          <el-form :model="securitySettings" label-width="120px">
            <el-form-item label="密码最小长度">
              <el-input-number
                v-model="securitySettings.minPasswordLength"
                :min="6"
                :max="20"
              />
            </el-form-item>
            
            <el-form-item label="密码复杂度">
              <el-checkbox-group v-model="securitySettings.passwordComplexity">
                <el-checkbox value="uppercase">必须包含大写字母</el-checkbox>
                <el-checkbox value="lowercase">必须包含小写字母</el-checkbox>
                <el-checkbox value="number">必须包含数字</el-checkbox>
                <el-checkbox value="special">必须包含特殊字符</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            
            <el-form-item label="登录失败锁定">
              <el-input-number
                v-model="securitySettings.loginFailLock"
                :min="3"
                :max="10"
              />
              <span class="setting-tip">次后锁定账号</span>
            </el-form-item>
            
            <el-form-item label="会话超时时间">
              <el-input-number
                v-model="securitySettings.sessionTimeout"
                :min="10"
                :max="480"
              />
              <span class="setting-tip">分钟</span>
            </el-form-item>
            
            <el-form-item label="启用验证码">
              <el-switch v-model="securitySettings.enableCaptcha" />
            </el-form-item>
            
            <el-form-item label="启用双因素认证">
              <el-switch v-model="securitySettings.enable2FA" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveSecuritySettings">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 邮件设置 -->
        <el-tab-pane label="邮件设置" name="email">
          <el-form :model="emailSettings" label-width="120px">
            <el-form-item label="SMTP服务器">
              <el-input v-model="emailSettings.smtpServer" placeholder="例如：smtp.gmail.com" />
            </el-form-item>
            
            <el-form-item label="SMTP端口">
              <el-input-number v-model="emailSettings.smtpPort" :min="1" :max="65535" />
            </el-form-item>
            
            <el-form-item label="发件人邮箱">
              <el-input v-model="emailSettings.fromEmail" placeholder="请输入发件人邮箱" />
            </el-form-item>
            
            <el-form-item label="发件人名称">
              <el-input v-model="emailSettings.fromName" placeholder="请输入发件人名称" />
            </el-form-item>
            
            <el-form-item label="邮箱密码">
              <el-input
                v-model="emailSettings.password"
                type="password"
                show-password
                placeholder="请输入邮箱密码"
              />
            </el-form-item>
            
            <el-form-item label="启用SSL">
              <el-switch v-model="emailSettings.enableSSL" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveEmailSettings">保存设置</el-button>
              <el-button @click="testEmailSettings">测试连接</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 通知设置 -->
        <el-tab-pane label="通知设置" name="notification">
          <el-form :model="notificationSettings" label-width="120px">
            <el-form-item label="系统通知">
              <el-switch v-model="notificationSettings.systemNotification" />
            </el-form-item>
            
            <el-form-item label="邮件通知">
              <el-switch v-model="notificationSettings.emailNotification" />
            </el-form-item>
            
            <el-form-item label="短信通知">
              <el-switch v-model="notificationSettings.smsNotification" />
            </el-form-item>
            
            <el-form-item label="通知时间">
              <el-time-picker
                v-model="notificationSettings.notificationTime"
                format="HH:mm"
                placeholder="选择时间"
              />
            </el-form-item>
            
            <el-form-item label="通知内容">
              <el-checkbox-group v-model="notificationSettings.notificationTypes">
                <el-checkbox value="login">登录通知</el-checkbox>
                <el-checkbox value="error">错误通知</el-checkbox>
                <el-checkbox value="warning">警告通知</el-checkbox>
                <el-checkbox value="info">信息通知</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveNotificationSettings">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 备份设置 -->
        <el-tab-pane label="备份设置" name="backup">
          <el-form :model="backupSettings" label-width="120px">
            <el-form-item label="自动备份">
              <el-switch v-model="backupSettings.autoBackup" />
            </el-form-item>
            
            <el-form-item label="备份频率">
              <el-select v-model="backupSettings.backupFrequency" :disabled="!backupSettings.autoBackup">
                <el-option label="每天" value="daily" />
                <el-option label="每周" value="weekly" />
                <el-option label="每月" value="monthly" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="备份时间">
              <el-time-picker
                v-model="backupSettings.backupTime"
                format="HH:mm"
                placeholder="选择时间"
                :disabled="!backupSettings.autoBackup"
              />
            </el-form-item>
            
            <el-form-item label="保留备份数量">
              <el-input-number
                v-model="backupSettings.keepBackups"
                :min="1"
                :max="100"
                :disabled="!backupSettings.autoBackup"
              />
            </el-form-item>
            
            <el-form-item label="备份路径">
              <el-input
                v-model="backupSettings.backupPath"
                placeholder="请输入备份路径"
                :disabled="!backupSettings.autoBackup"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveBackupSettings">保存设置</el-button>
              <el-button @click="manualBackup">立即备份</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'SystemSettings',
  setup() {
    const activeTab = ref('basic')

    const basicSettings = reactive({
      systemName: 'Vue Element Admin',
      version: '1.0.0',
      logo: '',
      description: '基于Vue3和Element Plus的管理系统',
      maintenanceMode: false
    })

    const securitySettings = reactive({
      minPasswordLength: 8,
      passwordComplexity: ['uppercase', 'lowercase', 'number'],
      loginFailLock: 5,
      sessionTimeout: 30,
      enableCaptcha: true,
      enable2FA: false
    })

    const emailSettings = reactive({
      smtpServer: 'smtp.gmail.com',
      smtpPort: 587,
      fromEmail: 'admin@example.com',
      fromName: '系统管理员',
      password: '',
      enableSSL: true
    })

    const notificationSettings = reactive({
      systemNotification: true,
      emailNotification: true,
      smsNotification: false,
      notificationTime: new Date(2024, 0, 1, 9, 0),
      notificationTypes: ['login', 'error', 'warning']
    })

    const backupSettings = reactive({
      autoBackup: true,
      backupFrequency: 'daily',
      backupTime: new Date(2024, 0, 1, 2, 0),
      keepBackups: 30,
      backupPath: '/backup'
    })

    const handleLogoSuccess = (response, file) => {
      basicSettings.logo = URL.createObjectURL(file.raw)
    }

    const beforeLogoUpload = (file) => {
      const isJPG = file.type === 'image/jpeg'
      const isPNG = file.type === 'image/png'
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isJPG && !isPNG) {
        ElMessage.error('上传Logo只能是 JPG/PNG 格式!')
        return false
      }
      if (!isLt2M) {
        ElMessage.error('上传Logo大小不能超过 2MB!')
        return false
      }
      return true
    }

    const saveBasicSettings = () => {
      ElMessage.success('基本设置保存成功')
    }

    const saveSecuritySettings = () => {
      ElMessage.success('安全设置保存成功')
    }

    const saveEmailSettings = () => {
      ElMessage.success('邮件设置保存成功')
    }

    const testEmailSettings = () => {
      ElMessage.info('正在测试邮件连接...')
      setTimeout(() => {
        ElMessage.success('邮件连接测试成功')
      }, 2000)
    }

    const saveNotificationSettings = () => {
      ElMessage.success('通知设置保存成功')
    }

    const saveBackupSettings = () => {
      ElMessage.success('备份设置保存成功')
    }

    const manualBackup = () => {
      ElMessage.info('正在执行手动备份...')
      setTimeout(() => {
        ElMessage.success('备份完成')
      }, 3000)
    }

    return {
      activeTab,
      basicSettings,
      securitySettings,
      emailSettings,
      notificationSettings,
      backupSettings,
      handleLogoSuccess,
      beforeLogoUpload,
      saveBasicSettings,
      saveSecuritySettings,
      saveEmailSettings,
      testEmailSettings,
      saveNotificationSettings,
      saveBackupSettings,
      manualBackup
    }
  }
}
</script>

<style scoped>
.system-settings {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.setting-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 14px;
}

.avatar-uploader {
  text-align: center;
}

.avatar-uploader .avatar {
  width: 100px;
  height: 100px;
  display: block;
}

.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
}

:deep(.el-tabs--left .el-tabs__header.is-left) {
  margin-right: 20px;
}

:deep(.el-tabs--left .el-tabs__nav-wrap.is-left) {
  margin-right: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}
</style> 