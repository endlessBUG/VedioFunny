<template>
  <div class="user-list">
    <!-- 搜索和操作栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="searchForm.email" placeholder="请输入邮箱" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="inactive" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getList">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户表格 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>用户列表</span>
          <el-button type="primary" @click="openAdd">新增用户</el-button>
        </div>
      </template>

      <el-table :data="userList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="scope">
            <el-tag :type="getRoleType(scope.row.role)">
              {{ scope.row.role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="text" size="small" @click="openEdit(scope.row)">
              编辑
            </el-button>
            <el-button type="text" size="small" @click="handleView(scope.row)">
              查看
            </el-button>
            <el-button 
              type="text" 
              size="small" 
              style="color: #f56c6c"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          @current-change="getList"
          @size-change="getList"
          layout="total, sizes, prev, pager, next, jumper"
          class="mt-2"
        />
      </div>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog
      v-model="editDialog.visible"
      :title="editDialog.isEdit ? '编辑用户' : '新增用户'"
      width="600px"
    >
      <el-form ref="editUserForm" :model="editDialog.form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="editDialog.form.username" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editDialog.form.email" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="editDialog.form.newPassword" type="password" placeholder="不修改请留空" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewDialog.visible" title="用户详情" width="400px" :show-close="true">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="ID">{{ viewDialog.data.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ viewDialog.data.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ viewDialog.data.nickname }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ viewDialog.data.email }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ viewDialog.data.status === 1 ? '启用' : '禁用' }}</el-descriptions-item>
        <el-descriptions-item label="用户类型">{{ viewDialog.data.userType }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ viewDialog.data.createdTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ viewDialog.data.updatedTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewDialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchUserList, deleteUser, editUser } from '@/api/user'

export default {
  name: 'UserList',
  setup() {
    const loading = ref(false)
    const dialogVisible = ref(false)
    const dialogTitle = ref('新增用户')
    const addUserForm = ref(null)
    const editUserForm = ref(null)

    const searchForm = reactive({
      username: '',
      email: '',
      status: ''
    })

    const userForm = ref(null)

    const userFormData = reactive({
      id: '',
      username: '',
      email: '',
      phone: '',
      role: 'user',
      status: 'active'
    })

    const userRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱地址', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
      ],
      phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
      ],
      role: [
        { required: true, message: '请选择角色', trigger: 'change' }
      ]
    }

    const userList = ref([])
    const pagination = reactive({ page: 1, size: 10, total: 0 })

    const getList = () => {
      loading.value = true
      fetchUserList({ ...searchForm, page: pagination.page, size: pagination.size })
        .then(res => {
          const pageData = res.data
          userList.value = pageData.content || []
          pagination.total = pageData.totalElements || 0
          pagination.size = pageData.size || 10
          pagination.page = (pageData.number || 0) + 1
        })
        .catch(() => ElMessage.error('获取用户列表失败'))
        .finally(() => (loading.value = false))
    }

    const getRoleType = (role) => {
      const types = {
        'admin': 'danger',
        'user': 'primary',
        'guest': 'info'
      }
      return types[role] || 'info'
    }

    const handleAdd = () => {
      dialogTitle.value = '新增用户'
      dialogVisible.value = true
      resetForm()
    }

    const handleEdit = (row) => {
      dialogTitle.value = '编辑用户'
      dialogVisible.value = true
      Object.assign(userFormData, row)
    }

    const handleView = (row) => {
      ElMessage.info(`查看用户: ${row.username}`)
    }

    const handleDelete = (row) => {
      ElMessageBox.confirm(
        `确定要删除用户 "${row.username}" 吗？`,
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
      ).then(() => {
        return deleteUser(row.id)
      }).then(() => {
        getList()
      }).catch(() => {
        ElMessage.info('已取消删除')
      })
    }

    const handleStatusChange = (row) => {
    }

    const handleSubmit = async () => {
      if (!addUserForm.value) return
      
      try {
        await addUserForm.value.validate()
        dialogVisible.value = false
      } catch (error) {
        ElMessage.error('表单验证失败')
      }
    }

    const resetForm = () => {
      if (addUserForm.value) {
        addUserForm.value.resetFields()
      }
      Object.assign(userFormData, {
        id: '',
        username: '',
        email: '',
        phone: '',
        role: 'user',
        status: 'active'
      })
    }

    const resetSearch = () => {
      Object.assign(searchForm, {
        username: '',
        email: '',
        status: ''
      })
    }

    // 编辑弹窗响应式对象
    const editDialog = reactive({
      visible: false,
      isEdit: false,
      form: {
        id: '',
        username: '',
        email: '',
        phone: '',
        role: '',
        newPassword: ''
      }
    })

    function openEdit(row) {
      editDialog.isEdit = true
      Object.assign(editDialog.form, {
        id: '',
        username: '',
        email: '',
        phone: '',
        role: '',
        newPassword: ''
      })
      Object.assign(editDialog.form, row)
      editDialog.form.newPassword = ''
      editDialog.visible = true
    }

    function openAdd() {
      editDialog.isEdit = false
      Object.assign(editDialog.form, {
        id: '',
        username: '',
        email: '',
        phone: '',
        role: '',
        newPassword: ''
      })
      editDialog.visible = true
    }

    function submitEdit() {
      const data = { ...editDialog.form }
      if (data.newPassword) {
        data.password = data.newPassword
      }
      delete data.newPassword
      editUser(data)
        .then(() => {
          editDialog.visible = false
          getList()
        })
        .catch(() => ElMessage.error('保存失败'))
    }

    // 查看详情弹窗
    const viewDialog = reactive({ visible: false, data: {} })

    onMounted(getList)

    return {
      loading,
      searchForm,
      userList,
      pagination,
      dialogVisible,
      dialogTitle,
      addUserForm,
      editUserForm,
      userForm: userFormData,
      userRules,
      getRoleType,
      getList,
      handleEdit,
      handleView,
      handleDelete,
      handleStatusChange,
      resetForm,
      resetSearch,
      editDialog,
      openEdit,
      openAdd,
      submitEdit,
      viewDialog
    }
  }
}
</script>

<style scoped>
.user-list {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 0;
}

.table-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-wrapper {
  margin-top: 20px;
  text-align: right;
}

:deep(.el-table) {
  margin-top: 10px;
}

:deep(.el-form--inline .el-form-item) {
  margin-right: 20px;
}

.el-dialog__body {
  max-height: 70vh;
  overflow-y: auto;
}
</style> 