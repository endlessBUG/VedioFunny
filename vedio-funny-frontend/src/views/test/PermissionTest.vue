<template>
  <div class="permission-test">
    <el-card>
      <template #header>
        <h3>权限测试页面</h3>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <h4>用户信息</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="用户ID">{{ userInfo?.userId }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ userInfo?.username }}</el-descriptions-item>
            <el-descriptions-item label="用户类型">{{ userInfo?.userType }}</el-descriptions-item>
          </el-descriptions>
        </el-col>
        
        <el-col :span="12">
          <h4>权限信息</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="角色">
              <el-tag v-for="role in roles" :key="role" type="success" class="mr-2">
                {{ role }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="权限数量">{{ permissions.length }}</el-descriptions-item>
          </el-descriptions>
        </el-col>
      </el-row>
      
      <el-divider />
      
      <h4>权限列表</h4>
      <el-row :gutter="10">
        <el-col :span="6" v-for="permission in permissions" :key="permission">
          <el-tag class="mb-2">{{ permission }}</el-tag>
        </el-col>
      </el-row>
      
      <el-divider />
      
      <h4>权限测试</h4>
      <el-space wrap>
        <el-button 
          type="primary"
        >
          用户查看权限 (user:read)
        </el-button>
        
        <el-button 
          type="success"
        >
          用户编辑权限 (user:write)
        </el-button>
        
        <el-button 
          type="info"
        >
          模型查看权限 (model:read)
        </el-button>
        
        <el-button 
          type="danger"
        >
          管理员角色 (ROLE_ADMIN)
        </el-button>
        
        <el-button 
          type="warning"
        >
          仪表盘权限 (dashboard:view)
        </el-button>
      </el-space>
      
      <el-divider />
      
      <h4>操作测试</h4>
      <el-space>
        <el-button @click="refreshPermissions" type="primary">
          刷新权限信息
        </el-button>
        
        <el-button @click="testPermissionCheck" type="success">
          测试权限检查
        </el-button>
        
        <el-button @click="clearStorage" type="danger">
          清空localStorage
        </el-button>
      </el-space>
      
      <el-divider />
      
      <h4>测试日志</h4>
      <el-input
        v-model="testLog"
        type="textarea"
        :rows="10"
        readonly
        placeholder="测试日志将在这里显示..."
      />
    </el-card>
  </div>
</template>

<script>
import { computed, ref } from 'vue'
import { useStore } from 'vuex'
import { PermissionUtil } from '@/utils/permission'
import { ElMessage } from 'element-plus'

export default {
  name: 'PermissionTest',
  setup() {
    const store = useStore()
    const testLog = ref('')
    
    const userInfo = computed(() => store.getters['user/userInfo'])
    const roles = computed(() => store.getters['user/roles'])
    const permissions = computed(() => store.getters['user/permissions'])
    
    const addLog = (message) => {
      const timestamp = new Date().toLocaleTimeString()
      testLog.value += `[${timestamp}] ${message}\n`
    }
    
    const refreshPermissions = () => {
      addLog('开始刷新权限信息...')
      store.dispatch('user/initUserFromStorage')
      addLog(`权限刷新完成，当前角色: ${roles.value.join(', ')}`)
      addLog(`权限数量: ${permissions.value.length}`)
      ElMessage.success('权限信息已刷新')
    }
    
    const testPermissionCheck = () => {
      addLog('开始权限检查测试...')
      
      const testPermissions = [
        'user:read',
        'user:write', 
        'model:read',
        'dashboard:view',
        'admin:system'
      ]
      
      testPermissions.forEach(permission => {
        const hasPermission = PermissionUtil.hasPermission(permission)
        addLog(`权限检查 ${permission}: ${hasPermission ? '✅ 有权限' : '❌ 无权限'}`)
      })
      
      const testRoles = ['ROLE_ADMIN', 'ROLE_USER', 'ROLE_GUEST']
      testRoles.forEach(role => {
        const hasRole = PermissionUtil.hasRole(role)
        addLog(`角色检查 ${role}: ${hasRole ? '✅ 有角色' : '❌ 无角色'}`)
      })
      
      ElMessage.success('权限检查测试完成')
    }
    
    const clearStorage = () => {
      localStorage.clear()
      addLog('localStorage已清空')
      ElMessage.warning('localStorage已清空，请刷新页面')
    }
    
    // 页面加载时记录初始状态
    addLog('页面加载完成')
    addLog(`用户信息: ${userInfo.value ? '已加载' : '未加载'}`)
    addLog(`角色数量: ${roles.value.length}`)
    addLog(`权限数量: ${permissions.value.length}`)
    
    return {
      userInfo,
      roles,
      permissions,
      testLog,
      refreshPermissions,
      testPermissionCheck,
      clearStorage
    }
  }
}
</script>

<style scoped>
.permission-test {
  padding: 20px;
}

.mr-2 {
  margin-right: 8px;
}

.mb-2 {
  margin-bottom: 8px;
}
</style> 