<template>
  <div>
    <el-card>
      <div style="margin-bottom: 16px; display: flex; align-items: center; gap: 12px;">
        <el-input v-model="searchName" placeholder="请输入权限名称" clearable style="width: 220px;" @keyup.enter="handleSearch" />
        <el-button type="primary" @click="handleSearch">查询</el-button>
      </div>
      <el-table :data="pagedPermissions" style="width: 100%" :default-sort="{prop: 'sortOrder', order: 'ascending'}">
        <el-table-column prop="id" label="ID" width="80" sortable />
        <el-table-column prop="permissionCode" label="权限代码" width="150" sortable />
        <el-table-column prop="permissionName" label="权限名称" width="150" sortable />
        <el-table-column prop="resourceType" label="资源类型" width="100" sortable>
          <template #default="{ row }">
            <el-tag :type="getResourceTypeTag(row.resourceType)">
              {{ getResourceTypeLabel(row.resourceType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="description" label="描述" sortable />
        <el-table-column prop="status" label="状态" width="80" sortable>
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" sortable />
        <el-table-column prop="createdTime" label="创建时间" width="160" sortable>
          <template #default="{ row }">
            {{ formatDateTime(row.createdTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button 
              size="small" 
              :type="row.status === 1 ? 'danger' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin: 16px 0; text-align: right;">
        <el-pagination
          background
          layout="total, prev, pager, next, sizes, jumper"
          :total="filteredPermissions.length"
          :page-size="pageSize"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <el-dialog v-model="showAdd" :title="form.id ? '编辑权限' : '新增权限'" width="700px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="权限代码" prop="permissionCode">
          <el-input v-model="form.permissionCode" placeholder="请输入权限代码，如：user:read" />
          <div style="font-size: 12px; color: #999; margin-top: 4px;">
            权限代码格式：模块:操作，如：user:read、role:write、system:admin
          </div>
        </el-form-item>
        <el-form-item label="权限名称" prop="permissionName">
          <el-input v-model="form.permissionName" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="资源类型" prop="resourceType">
          <el-select v-model="form.resourceType" placeholder="请选择资源类型">
            <el-option label="菜单" value="MENU" />
            <el-option label="按钮" value="BUTTON" />
            <el-option label="接口" value="API" />
          </el-select>
        </el-form-item>
        <el-form-item label="资源路径" prop="resourcePath">
          <el-input v-model="form.resourcePath" placeholder="请输入资源路径，如：/api/user/list" />
        </el-form-item>
        <el-form-item label="HTTP方法" prop="httpMethod">
          <el-select v-model="form.httpMethod" placeholder="请选择HTTP方法" clearable>
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
            <el-option label="PATCH" value="PATCH" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入权限描述" />
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
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">取消</el-button>
        <el-button type="primary" @click="savePermission">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listPermissions, addPermission, updatePermission, deletePermission as delPermission } from '@/api/permission'

const permissions = ref([])
const showAdd = ref(false)
const formRef = ref()
const form = ref({
  id: null,
  permissionCode: '',
  permissionName: '',
  resourceType: 'API',
  resourcePath: '',
  httpMethod: '',
  description: '',
  status: 1,
  sortOrder: 0
})
const searchName = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const filteredPermissions = computed(() => {
  let result = permissions.value
  if (searchName.value) {
    result = result.filter(item =>
      item.permissionName && item.permissionName.includes(searchName.value)
    )
  }
  // 默认按照 sortOrder 排序
  return result.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
})

const pagedPermissions = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredPermissions.value.slice(start, end)
})

// 表单验证规则
const rules = {
  permissionCode: [
    { required: true, message: '请输入权限代码', trigger: 'blur' },
    { pattern: /^[a-z]+:[a-z]+$/, message: '权限代码格式为：模块:操作，如：user:read', trigger: 'blur' }
  ],
  permissionName: [
    { required: true, message: '请输入权限名称', trigger: 'blur' }
  ],
  resourceType: [
    { required: true, message: '请选择资源类型', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

const fetchPermissions = async () => {
  try {
    const res = await listPermissions()
    permissions.value = res.data || []
  } catch (error) {
    ElMessage.error('获取权限列表失败')
  }
}

const savePermission = async () => {
  try {
    await formRef.value.validate()
    
    if (form.value.id) {
      await updatePermission(form.value)
      ElMessage.success('修改成功')
    } else {
      await addPermission(form.value)
      ElMessage.success('新增成功')
    }
    showAdd.value = false
    resetForm()
    fetchPermissions()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const toggleStatus = async (row) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await updatePermission({ ...row, status: newStatus })
    ElMessage.success(newStatus === 1 ? '启用成功' : '禁用成功')
    fetchPermissions()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const resetForm = () => {
  form.value = {
    id: null,
    permissionCode: '',
    permissionName: '',
    resourceType: 'API',
    resourcePath: '',
    httpMethod: '',
    description: '',
    status: 1,
    sortOrder: 0
  }
  formRef.value?.resetFields()
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

const getResourceTypeLabel = (type) => {
  const labels = {
    'MENU': '菜单',
    'BUTTON': '按钮',
    'API': '接口'
  }
  return labels[type] || type
}

const getResourceTypeTag = (type) => {
  const tags = {
    'MENU': 'primary',
    'BUTTON': 'warning',
    'API': 'success'
  }
  return tags[type] || 'info'
}

const getMethodTag = (method) => {
  const tags = {
    'GET': 'success',
    'POST': 'primary',
    'PUT': 'warning',
    'DELETE': 'danger',
    'PATCH': 'info'
  }
  return tags[method] || 'info'
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

onMounted(fetchPermissions)
</script> 