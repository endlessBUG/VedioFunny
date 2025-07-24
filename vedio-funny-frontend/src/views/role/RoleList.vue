<template>
  <div>
    <el-card>
      <div style="margin-bottom: 16px;">
        <el-button type="primary" @click="showAddRole">新增角色</el-button>
      </div>
      <el-table :data="roles" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleCode" label="角色代码" width="120" />
        <el-table-column prop="roleName" label="角色名称" width="120" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="createdTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="updatedTime" label="更新时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.updatedTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="editRole(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteRole(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <el-dialog v-model="showAdd" :title="form.id ? '编辑角色' : '新增角色'" width="800px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="角色代码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色代码，如：ROLE_ADMIN" />
          <div style="font-size: 12px; color: #999; margin-top: 4px;">
            角色代码必须以ROLE_开头，如：ROLE_ADMIN、ROLE_USER、ROLE_GUEST
          </div>
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入角色描述，如：该角色的权限说明" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" placeholder="数字越小排序越靠前" />
          <div style="font-size: 12px; color: #999; margin-top: 4px;">
            数字越小排序越靠前，建议使用10、20、30等间隔数字
          </div>
        </el-form-item>
        
        <el-form-item label="权限配置" prop="permissions">
          <el-popover
            placement="bottom-start"
            width="600"
            trigger="click"
            popper-class="permission-popover"
            :loading="permissionsLoading"
          >
            <template #reference>
              <div style="min-height: 32px; border: 1px solid #dcdfe6; border-radius: 4px; padding: 6px 12px; background: #fff; cursor: pointer; display: flex; align-items: center; justify-content: space-between;">
                <div style="flex: 1; display: flex; flex-wrap: wrap; gap: 4px;">
                  <el-tag 
                    v-for="permissionId in selectedPermissions.slice(0, 3)" 
                    :key="permissionId"
                    size="small"
                    closable
                    @close="removePermission(permissionId)"
                  >
                    {{ getPermissionName(permissionId) }}
                  </el-tag>
                  <el-tag 
                    v-if="selectedPermissions.length > 3"
                    size="small"
                    type="info"
                  >
                    +{{ selectedPermissions.length - 3 }}
                  </el-tag>
                  <span v-if="selectedPermissions.length === 0" style="color: #c0c4cc;">请选择权限</span>
                </div>
                <el-icon style="color: #c0c4cc; margin-left: 8px;">
                  <ArrowDown />
                </el-icon>
              </div>
            </template>
            
            <div style="max-height: 400px; overflow-y: auto;">
              <div v-loading="permissionsLoading">
                <div style="margin-bottom: 10px; display: flex; justify-content: space-between; align-items: center;">
                  <span style="font-weight: 500;">权限配置</span>
                  <div>
                    <el-button size="small" text @click="selectAllPermissions">全选</el-button>
                    <el-button size="small" text @click="clearAllPermissions">清空</el-button>
                  </div>
                </div>
                
                <div v-for="group in groupedPermissions" :key="group.label" style="margin-bottom: 15px;">
                  <div style="font-weight: 500; color: #303133; margin-bottom: 8px; padding-bottom: 5px; border-bottom: 1px solid #ebeef5;">
                    {{ group.label }}
                  </div>
                  <el-checkbox-group v-model="selectedPermissions">
                    <div v-for="permission in group.options" :key="permission.id" style="margin-bottom: 8px; width: 100%;">
                      <el-checkbox :value="permission.id" style="width: 100%;">
                        <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
                          <div style="display: flex; flex-direction: column; align-items: flex-start;">
                            <span style="font-weight: 500;">{{ permission.permissionName }}</span>
                            <span style="font-size: 12px; color: #909399;">{{ permission.permissionCode }}</span>
                          </div>
                          <el-tag size="small" :type="getPermissionTypeColor(permission.resourceType)">
                            {{ permission.resourceType }}
                          </el-tag>
                        </div>
                      </el-checkbox>
                    </div>
                  </el-checkbox-group>
                </div>
              </div>
            </div>
          </el-popover>
          <div style="font-size: 12px; color: #999; margin-top: 4px;">
            点击选择该角色拥有的权限，支持复选框批量操作
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">取消</el-button>
        <el-button type="primary" @click="saveRole">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { 
  listRoles, 
  addRole, 
  updateRole, 
  deleteRole as delRole,
  listPermissions,
  getRolePermissions,
  updateRolePermissions
} from '@/api/role'

const roles = ref([])
const showAdd = ref(false)
const formRef = ref()
const form = ref({
  id: null,
  roleCode: 'ROLE_',
  roleName: '',
  description: '',
  status: 1,
  sortOrder: 0
})

// 权限相关数据
const permissions = ref([])
const selectedPermissions = ref([])
const permissionsLoading = ref(false)

// 权限分组计算属性（现在只有菜单权限，简化处理）
const groupedPermissions = computed(() => {
  // 现在后端只返回菜单权限，直接返回一个分组
  return [{
    label: '菜单权限',
    options: permissions.value
  }]
})

// 表单验证规则
const rules = {
  roleCode: [
    { required: true, message: '请输入角色代码', trigger: 'blur' },
    { pattern: /^ROLE_[A-Z_]+$/, message: '角色代码格式为ROLE_开头的大写字母和下划线', trigger: 'blur' }
  ],
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

const fetchRoles = async () => {
  try {
    const res = await listRoles()
    roles.value = res.data || []
  } catch (error) {
    ElMessage.error('获取角色列表失败')
  }
}

// 获取权限列表
const fetchPermissions = async () => {
  try {
    permissionsLoading.value = true
    const res = await listPermissions()
    permissions.value = res.data || []
  } catch (error) {
    ElMessage.error('获取权限列表失败')
  } finally {
    permissionsLoading.value = false
  }
}

// 获取角色权限（通过API获取指定角色的权限）
const fetchRolePermissions = async (roleId) => {
  try {
    const res = await getRolePermissions(roleId)
    selectedPermissions.value = res.data || []
    console.log(`获取角色${roleId}的权限:`, res.data)
  } catch (error) {
    console.error('获取角色权限失败:', error)
    ElMessage.error('获取角色权限失败')
    selectedPermissions.value = []
  }
}

// 获取当前用户权限（从localStorage获取）
const getCurrentUserPermissions = () => {
  try {
    const userInfoStr = localStorage.getItem('userInfo')
    if (!userInfoStr) {
      console.warn('localStorage中没有用户信息')
      return []
    }
    
    const userInfo = JSON.parse(userInfoStr)
    const userPermissions = userInfo.permissions || []
    
    console.log('当前用户权限:', userPermissions)
    return userPermissions
  } catch (error) {
    console.error('从localStorage获取用户权限失败:', error)
    return []
  }
}

// 权限类型颜色（现在只有菜单权限）
const getPermissionTypeColor = (type) => {
  return 'primary' // 菜单权限统一使用primary颜色
}

const saveRole = async () => {
  try {
    await formRef.value.validate()
    
    let roleId
    if (form.value.id) {
      await updateRole(form.value)
      roleId = form.value.id
      ElMessage.success('修改成功')
    } else {
      const res = await addRole(form.value)
      roleId = res.data?.id
      ElMessage.success('新增成功')
    }
    
    // 更新角色权限
    if (roleId && selectedPermissions.value.length >= 0) {
      try {
        await updateRolePermissions(roleId, selectedPermissions.value)
        console.log('权限更新成功')
      } catch (error) {
        ElMessage.warning('角色保存成功，但权限更新失败')
      }
    }
    
    showAdd.value = false
    resetForm()
    fetchRoles()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const editRole = async (row) => {
  form.value = { ...row }
  showAdd.value = true
  
  // 加载权限列表（如果还没加载）
  if (permissions.value.length === 0) {
    await fetchPermissions()
  }
  
  // 加载角色权限
  if (row.id) {
    await fetchRolePermissions(row.id)
  }
}

const deleteRole = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个角色吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await delRole(row.id)
    ElMessage.success('删除成功')
    fetchRoles()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const resetForm = () => {
  form.value = {
    id: null,
    roleCode: 'ROLE_',
    roleName: '',
    description: '',
    status: 1,
    sortOrder: 0
  }
  selectedPermissions.value = []
  formRef.value?.resetFields()
}

// 新增角色
const showAddRole = async () => {
  resetForm()
  showAdd.value = true
  
  // 加载权限列表
  if (permissions.value.length === 0) {
    await fetchPermissions()
  }
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 权限选择相关方法
const removePermission = (permissionId) => {
  const index = selectedPermissions.value.indexOf(permissionId)
  if (index > -1) {
    selectedPermissions.value.splice(index, 1)
  }
}

const getPermissionName = (permissionId) => {
  const permission = permissions.value.find(p => p.id === permissionId)
  return permission ? permission.permissionName : `权限${permissionId}`
}

const selectAllPermissions = () => {
  selectedPermissions.value = permissions.value.map(p => p.id)
}

const clearAllPermissions = () => {
  selectedPermissions.value = []
}

onMounted(fetchRoles)
</script>

<style scoped>
.permission-popover {
  --el-popover-padding: 16px;
}

.permission-popover .el-checkbox {
  margin-right: 0;
  margin-bottom: 0;
}

.permission-popover .el-checkbox__label {
  width: 100%;
}
</style> 