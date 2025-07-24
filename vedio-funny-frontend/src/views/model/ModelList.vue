<template>
  <div class="model-list">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="名称">
          <el-input v-model="searchForm.name" placeholder="请输入名称" clearable />
        </el-form-item>
        <el-form-item label="厂商">
          <el-select v-model="searchForm.vendor" placeholder="请选择厂商" clearable>
            <el-option label="OpenAI" value="OpenAI" />
            <el-option label="Anthropic" value="Anthropic" />
            <el-option label="Google" value="Google" />
            <el-option label="Meta" value="Meta" />
            <el-option label="其他" value="Other" />
          </el-select>
        </el-form-item>
        <el-form-item label="模型名称">
          <el-input v-model="searchForm.modelName" placeholder="请输入模型名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 -->
    <div class="operation-bar">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增模型
      </el-button>
    </div>

    <!-- 模型列表 -->
    <el-card>
      <el-table 
        v-loading="loading" 
        :data="modelList" 
        style="width: 100%"
        :default-sort="{ prop: 'createdTime', order: 'descending' }"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="id" label="ID" width="80" sortable />
        <el-table-column prop="name" label="名称" sortable />
        <el-table-column prop="vendor" label="厂商" width="120" sortable>
          <template #default="scope">
            <el-tag>{{ scope.row.vendor }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="modelName" label="模型名称" width="150" sortable />
        <el-table-column prop="version" label="版本" width="100" sortable />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status"
              :active-value="1"
              :inactive-value="0"
              :loading="row.statusLoading"
              @change="() => handleStatusChange(row)"
              :disabled="row.statusLoading"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="180" sortable />
        <el-table-column label="标签" min-width="200">
          <template #default="{ row }">
            <div class="tag-group">
              <el-tag
                v-for="tag in row.tags"
                :key="tag"
                :type="getTagType(tag)"
                size="small"
                class="model-tag"
                :effect="getTagEffect(tag)"
              >
                {{ getTagLabel(tag) }}
            </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button-group>
              <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="success" link @click="handleDownload(scope.row)">下载</el-button>
              <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 模型表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增模型' : '编辑模型'"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="modelFormRef"
        :model="modelForm"
        :rules="rules"
        label-width="100px"
        class="model-form"
      >
        <el-form-item label="模型名称" prop="name">
          <el-input v-model="modelForm.name" placeholder="请输入模型名称" />
        </el-form-item>

        <el-form-item label="厂商" prop="vendor">
          <el-input v-model="modelForm.vendor" placeholder="请输入厂商名称" />
        </el-form-item>

        <el-form-item label="模型标识" prop="modelName">
          <el-input v-model="modelForm.modelName" placeholder="请输入模型标识" />
        </el-form-item>

        <el-form-item label="版本" prop="version">
          <el-input v-model="modelForm.version" placeholder="请输入版本号" />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="modelForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入模型描述"
          />
        </el-form-item>

        <!-- 表单中的开关 -->
        <el-form-item label="状态">
          <el-switch
            v-model="modelForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
            class="custom-switch"
            size="large"
          />
        </el-form-item>

        <el-form-item label="模型标签" prop="tags">
          <div class="custom-select" ref="selectRef">
            <div class="selected-tags-wrapper" @click="openTagSelect">
              <div v-if="modelForm.tags.length === 0" class="placeholder">
                请选择模型标签
              </div>
              <div v-else class="selected-tags-container">
                <el-tag
                  v-for="tag in modelForm.tags"
                  :key="tag"
                  closable
                  :type="getTagType(tag)"
                  size="small"
                  class="selected-tag"
                  :effect="getTagEffect(tag)"
                  @close.stop="removeTag(tag)"
                >
                  {{ getTagLabel(tag) }}
                </el-tag>
              </div>
            </div>

            <!-- 标签选择弹窗 -->
            <el-popover
              v-model:visible="showTagSelect"
              :width="400"
              trigger="manual"
              placement="bottom-start"
              popper-class="tag-select-popover"
            >
              <template #reference>
                <div class="select-trigger"></div>
      </template>
              <div class="tag-select-content">
                <div class="tag-options-container">
                  <div
                    v-for="tag in tagOptions"
                    :key="tag.value"
                    class="tag-option-wrapper"
                    :class="{ 'is-selected': modelForm.tags.includes(tag.value) }"
                    @click="toggleTag(tag.value)"
                  >
                    <el-tag 
                      :type="tag.type" 
                      size="default"
                      class="tag-option" 
                      :effect="modelForm.tags.includes(tag.value) ? 'dark' : 'light'"
                    >
                      {{ getTagLabel(tag.value) }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </el-popover>
          </div>
        </el-form-item>

        <!-- 模型来源选择 -->
        <el-form-item label="模型来源" prop="modelSource">
          <el-radio-group v-model="modelForm.modelSource" @change="handleSourceChange">
            <el-radio-button label="modelscope">ModelScope</el-radio-button>
            <el-radio-button label="huggingface">HuggingFace</el-radio-button>
            <el-radio-button label="upload">本地上传</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- 模型ID输入 (当选择 modelscope 或 huggingface 时显示) -->
        <el-form-item 
          v-if="modelForm.modelSource !== 'upload'" 
          :label="modelForm.modelSource === 'modelscope' ? 'ModelScope ID' : 'HuggingFace ID'" 
          prop="modelId"
        >
          <el-input 
            v-model="modelForm.modelId" 
            :placeholder="modelForm.modelSource === 'modelscope' ? '请输入ModelScope模型ID，例如：damo/nlp_structbert_backbone_base_std' : '请输入HuggingFace模型ID，例如：bert-base-uncased'"
            clearable
          >
            <template #prepend>
              <span v-if="modelForm.modelSource === 'modelscope'">ModelScope</span>
              <span v-else>HuggingFace</span>
            </template>
          </el-input>
          <div class="help-text">
            <el-text size="small" type="info">
              <template v-if="modelForm.modelSource === 'modelscope'">
                请前往 <el-link href="https://modelscope.cn/" target="_blank" type="primary">ModelScope</el-link> 查找模型ID
              </template>
              <template v-else>
                请前往 <el-link href="https://huggingface.co/models" target="_blank" type="primary">HuggingFace</el-link> 查找模型ID
              </template>
            </el-text>
          </div>
        </el-form-item>

        <!-- 模型文件上传 (仅当选择本地上传时显示) -->
        <el-form-item v-if="modelForm.modelSource === 'upload'" label="模型文件">
      <el-upload
            class="model-upload"
        :action="uploadUrl"
        :headers="uploadHeaders"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :before-upload="beforeUpload"
            :file-list="fileList"
            accept=".onnx,.pt,.pth,.bin,.params"
            :show-file-list="false"
            drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
              将压缩包拖到此处，或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
                支持的文件类型：.onnx, .pt, .pth, .bin, .params (大小限制：100MB)
          </div>
        </template>
      </el-upload>

          <!-- 上传进度显示 -->
          <div v-if="isUploading" class="upload-progress">
            <div class="progress-info">
              <span class="file-name">{{ currentFile?.name }}</span>
              <span class="progress-percentage">{{ uploadProgress }}%</span>
            </div>
            <el-progress 
              :percentage="uploadProgress"
              :format="(percentage) => ''"
              :stroke-width="8"
              status="success"
            />
            <div class="upload-stats">
              <span>文件大小：{{ formatFileSize(currentFile?.size) }}</span>
            </div>
          </div>

          <!-- 已上传文件显示 -->
          <div v-else-if="modelForm.filePath" class="file-preview">
            <el-card shadow="hover" class="file-card">
              <div class="file-info">
                <el-icon class="file-icon"><Document /></el-icon>
                <div class="file-details">
                  <div class="file-name">{{ modelForm.filePath.split('/').pop() }}</div>
                  <div class="file-size">{{ formatFileSize(modelForm.fileSize) }}</div>
                </div>
                <div class="file-actions">
                  <el-link :href="modelForm.filePath" type="primary" target="_blank">
                    <el-icon><Download /></el-icon>
                    下载
                  </el-link>
                </div>
              </div>
            </el-card>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            {{ dialogType === 'add' ? '创建' : '更新' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, UploadFilled, Document } from '@element-plus/icons-vue'
import {
  fetchModelList,
  createModel,
  updateModel,
  deleteModel
} from '@/api/model'

export default {
  name: 'ModelList',
  components: {
    Plus,
    Download,
    UploadFilled,
    Document
  },
  setup() {
    // 搜索表单
    const searchForm = reactive({
      name: '',
      vendor: '',
      modelName: ''
    })

    // 定义响应式数据
    const modelForm = reactive({
      id: null,
      name: '',
      description: '',
      vendor: '',
      modelName: '',
      version: '',
      filePath: '',
      fileSize: 0,
      status: 1,  // 添加状态字段，默认启用
      tags: [],
      modelSource: 'modelscope',  // 新增：模型来源，默认选择modelscope
      modelId: ''  // 新增：模型ID
    })

    // 文件列表
    const fileList = ref([])

    // 表单校验规则
    const rules = reactive({
      name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
      vendor: [{ required: true, message: '请选择厂商', trigger: 'change' }],
      modelName: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
      version: [{ required: true, message: '请输入版本号', trigger: 'blur' }],
      tags: [{ required: true, message: '请至少选择一个标签', trigger: 'change' }],
      modelSource: [{ required: true, message: '请选择模型来源', trigger: 'change' }],
      modelId: [
        { 
          required: false, 
          validator: (rule, value, callback) => {
            if (modelForm.modelSource !== 'upload' && !value) {
              callback(new Error('请输入模型ID'))
            } else {
              callback()
            }
          }, 
          trigger: 'blur' 
        }
      ]
    })

    // 预定义的标签选项
    const tagOptions = [
      // 基础模型类型
      { value: 'LORA', type: 'primary' },
      { value: 'VOICE', type: 'success' },
      { value: 'VIDEO', type: 'warning' },
      { value: 'TEXT', type: 'info' },
      { value: 'IMAGE', type: 'danger' },
      { value: 'MULTIMODAL', type: '' },
      { value: 'DOMAIN', type: 'success' },
      { value: 'BASE', type: 'info' }
    ]

    // 获取标签显示文本
    const getTagLabel = (tag) => {
      const labelMap = {
        'LORA': 'Lora模型',
        'VOICE': '语音模型',
        'VIDEO': '视频模型',
        'TEXT': '文本模型',
        'IMAGE': '图像模型',
        'MULTIMODAL': '多模态',
        'DOMAIN': '领域模型',
        'BASE': '基础模型'
      }
      return labelMap[tag] || tag
    }

    // 获取标签类型
    const getTagType = (tag) => {
      const option = tagOptions.find(opt => opt.value === tag)
      return option ? option.type : 'info'
    }

    // 获取标签效果
    const getTagEffect = (tag) => {
      return modelForm.tags.includes(tag) ? 'dark' : 'light'
    }

    // 列表数据
    const loading = ref(false)
    const modelList = ref([])
    const page = ref(1)
    const size = ref(10)
    const total = ref(0)

    // 对话框控制
    const dialogVisible = ref(false)
    const dialogType = ref('add') // add or edit
    const modelFormRef = ref(null)

    // 上传配置
    const uploadUrl = '/api/model/model/upload'
    const uploadHeaders = {
      Authorization: localStorage.getItem('token')
    }

    const handleSortChange = ({ prop, order }) => {
      // 更新排序参数
      const sortOrder = order === 'ascending' ? 'asc' : 'desc'
      getModelList({
        ...searchForm,
        sortBy: prop,
        sortOrder: sortOrder
      })
    }

    // 获取模型列表
    const getModelList = async (params = {}) => {
      loading.value = true
      try {
        const res = await fetchModelList({
          page: page.value,
          size: size.value,
          name: searchForm.name,
          vendor: searchForm.vendor,
          modelName: searchForm.modelName,
          sortBy: params.sortBy,
          sortOrder: params.sortOrder
        })
        if (res.code === 200) {
          modelList.value = res.data.content
          total.value = res.data.totalElements
        } else {
          ElMessage.error(res.message || '获取模型列表失败')
        }
      } catch (error) {
        ElMessage.error('获取模型列表失败：' + error.message)
      } finally {
        loading.value = false
      }
    }

    // 搜索
    const handleSearch = () => {
      page.value = 1
      getModelList()
    }

    // 重置搜索
    const resetSearch = () => {
      searchForm.name = ''
      searchForm.vendor = ''
      searchForm.modelName = ''
      handleSearch()
    }

    // 提交表单
    const handleSubmit = async () => {
      if (!modelFormRef.value) return
      
      await modelFormRef.value.validate(async (valid, fields) => {
        if (valid) {
          loading.value = true
          try {
            // 构建提交数据
            const submitData = {
              name: modelForm.name,
              description: modelForm.description || '',
              vendor: modelForm.vendor,
              modelName: modelForm.modelName,
              version: modelForm.version,
              filePath: modelForm.filePath || '',
              fileSize: modelForm.fileSize || 0,
              status: modelForm.status,  // 添加状态字段
              tags: modelForm.tags || [],
              modelSource: modelForm.modelSource,  // 模型来源
              modelId: modelForm.modelId || ''     // 模型ID
            }
            
            let response
            if (dialogType.value === 'edit') {
              if (!modelForm.id) {
                ElMessage.error('编辑时ID不能为空')
                return
              }
              response = await updateModel(modelForm.id, submitData)
            } else {
              response = await createModel(submitData)
            }

            if (response.code === 200) {
              ElMessage.success(dialogType.value === 'edit' ? '更新成功' : '创建成功')
              dialogVisible.value = false
              getModelList() // 刷新列表
            } else {
              ElMessage.error(response.message || '操作失败')
            }
          } catch (error) {
            console.error('提交失败:', error)
            ElMessage.error('操作失败，请重试')
          } finally {
            loading.value = false
          }
        } else {
          console.error('表单验证失败:', fields)
        }
      })
    }

    // 打开编辑对话框
    const handleEdit = (row) => {
      console.log('编辑行数据:', row)
      dialogType.value = 'edit'
      // 使用解构赋值来复制数据
      const { id, name, description, vendor, modelName, version, filePath, fileSize, tags, status, modelSource, modelId } = row
      Object.assign(modelForm, {
        id,
        name,
        description: description || '',
        vendor,
        modelName,
        version,
        filePath: filePath || '',
        fileSize: fileSize || 0,
        status: status ?? 1,  // 如果status为undefined或null，则默认为1
        tags: tags || [],
        modelSource: modelSource || 'modelscope',  // 默认为modelscope
        modelId: modelId || ''
      })
      console.log('设置后的表单数据:', modelForm)
      dialogVisible.value = true
    }

    // 打开新建对话框
    const handleAdd = () => {
      dialogType.value = 'add'
      // 重置表单
      Object.assign(modelForm, {
        id: null,
        name: '',
        description: '',
        vendor: '',
        modelName: '',
        version: '',
        filePath: '',
        fileSize: 0,
        status: 1,  // 新建时默认启用
        tags: [],
        modelSource: 'modelscope',  // 默认选择modelscope
        modelId: ''
      })
      dialogVisible.value = true
    }

    // 删除模型
    const handleDelete = (row) => {
      ElMessageBox.confirm('确认删除该模型吗？', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteModel(row.id)
          if (res.code === 200) {
            ElMessage.success('删除成功')
            getModelList()
          } else {
            ElMessage.error(res.message || '删除失败')
          }
        } catch (error) {
          ElMessage.error('删除失败：' + error.message)
        }
      })
    }

    // 上传相关
    const handleUpload = () => {
      // This function is no longer needed as the upload dialog is removed.
      // The file upload is now handled directly by el-upload.
      // If you need to trigger the file input, you might need to use a ref or a method to focus it.
      // For now, we'll just remove the dialog call.
    }

    // 上传进度
    const uploadProgress = ref(0)
    const isUploading = ref(false)
    const currentFile = ref(null)

    // 允许的文件类型
    const allowedFileTypes = [
      'application/zip',
      'application/x-rar-compressed',
      'application/x-7z-compressed',
      'application/x-zip-compressed'
    ]

    // 文件上传前的验证
    const beforeUpload = (file) => {
      // 检查文件类型
      if (!allowedFileTypes.includes(file.type)) {
        ElMessage.error('只支持上传压缩包文件（.zip、.rar、.7z）')
        return false
      }
      
      // 检查文件大小（限制为100GB）
      const maxSize = 100 * 1024 * 1024 * 1024
      if (file.size > maxSize) {
        ElMessage.error('文件大小不能超过100GB')
        return false
      }

      // 重置上传进度
      uploadProgress.value = 0
      isUploading.value = true
      currentFile.value = file
      
      return true
    }

    // 上传进度回调
    const onUploadProgress = (event) => {
      if (event.total > 0) {
        uploadProgress.value = Math.round((event.loaded / event.total) * 100)
      }
    }

    // 文件上传成功回调
    const handleUploadSuccess = (response) => {
      if (response.code === 200) {
        modelForm.filePath = response.data.filePath
        modelForm.fileSize = response.data.fileSize
        ElMessage.success('文件上传成功')
      } else {
        ElMessage.error(response.message || '文件上传失败')
      }
    }

    // 文件上传失败回调
    const handleUploadError = (error) => {
      console.error('文件上传失败:', error)
      ElMessage.error('文件上传失败，请重试')
    }

    // 分页相关
    const handleSizeChange = (val) => {
      size.value = val
      getModelList()
    }

    const handleCurrentChange = (val) => {
      page.value = val
      getModelList()
    }

    // 格式化文件大小
    const formatFileSize = (bytes) => {
      if (!bytes) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`
    }

    // 移除标签
    const removeTag = (tag) => {
      const index = modelForm.tags.indexOf(tag)
      if (index > -1) {
        modelForm.tags.splice(index, 1)
      }
    }

    // 标签选择弹窗控制
    const showTagSelect = ref(false)
    const selectRef = ref(null)

    // 打开标签选择
    const openTagSelect = () => {
      showTagSelect.value = true
    }

    // 切换标签选择状态
    const toggleTag = (tagValue) => {
      const index = modelForm.tags.indexOf(tagValue)
      if (index > -1) {
        modelForm.tags.splice(index, 1)
      } else {
        modelForm.tags.push(tagValue)
      }
    }

    // 状态显示格式化
    const formatStatus = (row) => {
      return row.status === 1 ? '启用' : '禁用'
    }

    // 状态切换处理
    const handleStatusChange = async (row) => {
      // 保存当前状态
      const currentStatus = row.status;
      // 计算目标状态
      const targetStatus = currentStatus === 1 ? 0 : 1;
      
      // 设置当前行的加载状态
      row.statusLoading = true;
      
      try {
        const response = await updateModel(row.id, {
          name: row.name,
          description: row.description,
          vendor: row.vendor,
          modelName: row.modelName,
          version: row.version,
          filePath: row.filePath,
          fileSize: row.fileSize,
          status: targetStatus,
          tags: row.tags,
          modelSource: row.modelSource || 'modelscope',
          modelId: row.modelId || ''
        });

        if (response.code === 200) {
          ElMessage.success(`${targetStatus === 1 ? '启用' : '禁用'}成功`);
          // 更新成功后再更新本地状态
          row.status = targetStatus;
        } else {
          ElMessage.error(response.message || '操作失败');
          // 操作失败时恢复原状态
          row.status = currentStatus;
        }
      } catch (error) {
        console.error('状态更新失败:', error);
        ElMessage.error('状态更新失败，请重试');
        // 操作失败时恢复原状态
        row.status = currentStatus;
      } finally {
        // 清除加载状态
        row.statusLoading = false;
      }
    };

    // 处理模型来源变化
    const handleSourceChange = (value) => {
      console.log('模型来源变化:', value)
      // 当切换为本地上传时，清空模型ID
      if (value === 'upload') {
        modelForm.modelId = ''
      }
      // 当切换为远程模型时，清空文件路径
      if (value !== 'upload') {
        modelForm.filePath = ''
        modelForm.fileSize = 0
      }
    }

    onMounted(() => {
      getModelList()
    })

    return {
      searchForm,
      modelForm,
      rules,
      tagOptions,
      getTagLabel,
      getTagType,
      getTagEffect,
      modelFormRef,
      loading,
      modelList,
      page,
      size,
      total,
      dialogVisible,
      dialogType,
      uploadUrl,
      uploadHeaders,
      fileList,
      handleSearch,
      resetSearch,
      handleAdd,
      handleEdit,
      handleDelete,
      handleSubmit,
      handleSourceChange,
      handleUpload,
      beforeUpload,
      handleUploadSuccess,
      handleUploadError,
      onUploadProgress,
      uploadProgress,
      isUploading,
      currentFile,
      formatFileSize,
      handleSizeChange,
      handleCurrentChange,
      handleSortChange,
      formatFileSize,
      getTagType,
      getTagLabel,
      getTagEffect,
      removeTag,
      showTagSelect,
      selectRef,
      openTagSelect,
      toggleTag,
      formatStatus,
      handleStatusChange
    }
  }
}
</script>

<style scoped>
.model-list {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.operation-bar {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
  display: flex;
  justify-content: flex-end;
  white-space: nowrap;
}

.model-upload {
  width: 100%;
}

.model-upload :deep(.el-upload) {
  width: 100%;
}

.model-upload :deep(.el-upload-dragger) {
  width: 100%;
  height: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(145deg, #f6f8fc 0%, #f0f4f8 100%);
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  transition: all 0.3s;
}

.model-upload :deep(.el-upload-dragger:hover) {
  border-color: var(--el-color-primary);
  background: linear-gradient(145deg, #f8faff 0%, #f2f6fc 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.model-upload :deep(.el-icon--upload) {
  font-size: 48px;
  color: var(--el-color-primary);
  margin-bottom: 16px;
}

.model-upload :deep(.el-upload__text) {
  color: #606266;
  font-size: 16px;
  margin-top: 10px;
}

.model-upload :deep(.el-upload__text em) {
  color: var(--el-color-primary);
  font-style: normal;
  font-weight: 600;
}

.model-upload :deep(.el-upload__tip) {
  text-align: center;
  color: #909399;
  font-size: 14px;
  margin-top: 8px;
}

.file-preview {
  margin-top: 16px;
}

.file-card {
  background: #f8fafc;
  border: 1px solid #e4e7ed;
  transition: all 0.3s;
}

.file-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-icon {
  font-size: 32px;
  color: var(--el-color-primary);
}

.file-details {
  flex: 1;
}

.file-name {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
  font-weight: 500;
}

.file-size {
  font-size: 12px;
  color: #909399;
}

.file-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-actions .el-link {
  display: flex;
  align-items: center;
  gap: 4px;
}

.upload-progress {
  margin-top: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.file-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.progress-percentage {
  font-size: 14px;
  color: var(--el-color-primary);
  font-weight: 600;
}

.upload-stats {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

/* 确保进度条组件样式正确显示 */
:deep(.el-progress-bar__outer) {
  background-color: #e9ecef;
}

:deep(.el-progress-bar__inner) {
  transition: width 0.3s ease;
}

:deep(.el-progress) {
  margin-bottom: 0;
}

/* 标签样式 */
.el-tag {
  margin: 2px;
  font-weight: 500;
}

/* 标签颜色样式 */
.el-tag--primary {
  --el-tag-bg-color: var(--el-color-primary-light-8);
  --el-tag-border-color: var(--el-color-primary);
  --el-tag-hover-bg-color: var(--el-color-primary-light-7);
  color: var(--el-color-primary-dark-1);
}

.el-tag--success {
  --el-tag-bg-color: var(--el-color-success-light-8);
  --el-tag-border-color: var(--el-color-success);
  --el-tag-hover-bg-color: var(--el-color-success-light-7);
  color: var(--el-color-success-dark-1);
}

.el-tag--warning {
  --el-tag-bg-color: var(--el-color-warning-light-8);
  --el-tag-border-color: var(--el-color-warning);
  --el-tag-hover-bg-color: var(--el-color-warning-light-7);
  color: var(--el-color-warning-dark-1);
}

.el-tag--danger {
  --el-tag-bg-color: var(--el-color-danger-light-8);
  --el-tag-border-color: var(--el-color-danger);
  --el-tag-hover-bg-color: var(--el-color-danger-light-7);
  color: var(--el-color-danger-dark-1);
}

.el-tag--info {
  --el-tag-bg-color: var(--el-color-info-light-8);
  --el-tag-border-color: var(--el-color-info);
  --el-tag-hover-bg-color: var(--el-color-info-light-7);
  color: var(--el-color-info-dark-1);
}

/* 暗色主题标签 */
.el-tag--dark {
  &.el-tag--primary {
    background-color: var(--el-color-primary);
    border-color: var(--el-color-primary);
    color: var(--el-color-white);
  }

  &.el-tag--success {
    background-color: var(--el-color-success);
    border-color: var(--el-color-success);
    color: var(--el-color-white);
  }

  &.el-tag--warning {
    background-color: var(--el-color-warning);
    border-color: var(--el-color-warning);
    color: var(--el-color-white);
  }

  &.el-tag--danger {
    background-color: var(--el-color-danger);
    border-color: var(--el-color-danger);
    color: var(--el-color-white);
  }

  &.el-tag--info {
    background-color: var(--el-color-info);
    border-color: var(--el-color-info);
    color: var(--el-color-white);
  }
}

/* 标签选择器中的标签样式 */
.tag-option {
  width: 100%;
  text-align: center;
  transition: all 0.3s;
  cursor: pointer;
  border-width: 1.5px;
  font-weight: 500;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  }
}

/* 选中的标签样式 */
.tag-option-wrapper.is-selected .tag-option {
  transform: translateY(-2px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  border-width: 2px;
}

/* 表格中的标签样式 */
.model-table {
  .el-tag {
    border-width: 1.5px;
    font-weight: 500;
    opacity: 1;
    
    &:hover {
      opacity: 0.9;
    }
  }
}

.el-select-dropdown__item {
  .el-tag {
    margin-right: 0;
    margin-bottom: 0;
  }
}

.tag-option {
  margin: 0;
  width: 100%;
}

.el-select :deep(.el-select__tags) {
  max-width: calc(100% - 30px);
}

.el-select :deep(.el-select__tags-text) {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 优化分组样式 */
:deep(.el-select-group__title) {
  padding: 8px 12px;
  font-size: 13px;
  font-weight: bold;
  color: var(--el-text-color-secondary);
  background-color: var(--el-fill-color-light);
}

:deep(.el-select-group__wrap) {
  margin: 0;
}

:deep(.el-select-group) {
  margin: 0;
}

.el-select-dropdown__item {
  padding: 8px 12px;
}

/* 标签选择器样式 */
.tag-select {
  --el-select-border-color-hover: var(--el-color-primary);
}

.tag-select :deep(.el-select__wrapper) {
  background-color: var(--el-fill-color-blank);
  border-radius: 8px;
  transition: all 0.3s ease;
  border: 1px solid var(--el-border-color);
  padding: 4px 8px;
  min-height: 40px;
}

.tag-select :deep(.el-select__wrapper:hover) {
  border-color: var(--el-color-primary-light-7);
  box-shadow: 0 0 0 1px var(--el-color-primary-light-8);
}

.tag-select :deep(.el-select__input) {
  margin: 4px 0;
  flex: 1;
  min-width: 80px;
}

.tag-select :deep(.el-input__wrapper) {
  box-shadow: none !important;
  background: none !important;
}

.tag-select :deep(.el-input__inner) {
  min-height: 32px;
  height: auto;
  cursor: pointer;
}

/* 标签样式增强 */
.selected-tag.el-tag--primary {
  background-color: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary-light-5);
  color: var(--el-color-primary);
}

.selected-tag.el-tag--success {
  background-color: var(--el-color-success-light-9);
  border-color: var(--el-color-success-light-5);
  color: var(--el-color-success);
}

.selected-tag.el-tag--warning {
  background-color: var(--el-color-warning-light-9);
  border-color: var(--el-color-warning-light-5);
  color: var(--el-color-warning);
}

.selected-tag.el-tag--danger {
  background-color: var(--el-color-danger-light-9);
  border-color: var(--el-color-danger-light-5);
  color: var(--el-color-danger);
}

.selected-tag.el-tag--info {
  background-color: var(--el-color-info-light-9);
  border-color: var(--el-color-info-light-5);
  color: var(--el-color-info);
}

/* 暗色主题标签增强 */
.selected-tag.el-tag--dark {
  color: var(--el-color-white);
  border: none;
}

/* 标签关闭按钮样式 */
.selected-tag :deep(.el-tag__close) {
  color: inherit;
  opacity: 0.7;
}

.selected-tag :deep(.el-tag__close:hover) {
  color: inherit;
  opacity: 1;
  background-color: rgba(0, 0, 0, 0.1);
}

/* 选择框中的标签溢出处理 */
.tag-select :deep(.el-select__tags) {
  flex-wrap: wrap;
  margin: 0;
  padding: 0;
  max-width: 100%;
}

/* 标签选项样式 */
.tag-option-content {
  display: flex;
  align-items: center;
  padding: 4px 0;
}

.tag-option {
  margin: 0;
  width: 100%;
  transition: all 0.3s;
  font-size: 13px;
}

.el-select-dropdown__item {
  padding: 0 15px;
}

.el-select-dropdown__item.selected .tag-option {
  transform: translateX(5px);
}

/* 表格中的标签组样式 */
.tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 4px 0;
}

.model-tag {
  transition: all 0.3s ease;
  cursor: default;
  font-size: 12px;
  border-radius: 4px;
}

.model-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 标签颜色主题 */
:deep(.el-tag--primary) {
  --el-tag-bg-color: rgba(64, 158, 255, 0.1);
  --el-tag-border-color: rgba(64, 158, 255, 0.2);
  --el-tag-hover-color: var(--el-color-primary);
}

:deep(.el-tag--success) {
  --el-tag-bg-color: rgba(103, 194, 58, 0.1);
  --el-tag-border-color: rgba(103, 194, 58, 0.2);
  --el-tag-hover-color: var(--el-color-success);
}

:deep(.el-tag--warning) {
  --el-tag-bg-color: rgba(230, 162, 60, 0.1);
  --el-tag-border-color: rgba(230, 162, 60, 0.2);
  --el-tag-hover-color: var(--el-color-warning);
}

:deep(.el-tag--danger) {
  --el-tag-bg-color: rgba(245, 108, 108, 0.1);
  --el-tag-border-color: rgba(245, 108, 108, 0.2);
  --el-tag-hover-color: var(--el-color-danger);
}

:deep(.el-tag--info) {
  --el-tag-bg-color: rgba(144, 147, 153, 0.1);
  --el-tag-border-color: rgba(144, 147, 153, 0.2);
  --el-tag-hover-color: var(--el-color-info);
}

/* 暗色主题标签 */
:deep(.el-tag--dark) {
  --el-tag-text-color: var(--el-color-white);
  border: none;
}

/* 选中项的特殊效果 */
.el-select-dropdown__item.selected {
  background: linear-gradient(to right, var(--el-color-primary-light-9), transparent);
}

/* 选中标签容器样式 */
.selected-tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 2px;
  min-height: 28px;
}

.selected-tag {
  margin: 2px;
  max-width: 150px;
  display: flex;
  align-items: center;
}

.selected-tag :deep(.el-tag__close) {
  right: -4px;
}

/* 自定义选择器样式 */
.custom-select {
  position: relative;
  width: 100%;
  min-height: 32px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 5px;
  cursor: text;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 5px;
}

.custom-select:hover {
  border-color: #c0c4cc;
}

.custom-select:focus-within {
  border-color: #409eff;
}

.selected-tag {
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 8px;
  background-color: #f0f2f5;
  border-radius: 4px;
  font-size: 14px;
  color: #333;
  font-weight: 500;
  margin: 2px;
}

.tag-input {
  border: none;
  outline: none;
  flex: 1;
  min-width: 80px;
  height: 28px;
  font-size: 14px;
  color: #000000;
  background: transparent;
  opacity: 1 !important;
}

.tag-input:disabled {
  background: transparent;
  color: #000000;
  opacity: 1 !important;
  -webkit-text-fill-color: #000000;
}

.tag-input::placeholder {
  color: #333;
  opacity: 1;
  font-weight: 500;
}

.tag-suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  max-height: 200px;
  overflow-y: auto;
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 1000;
  margin-top: 4px;
}

.tag-suggestion-item {
  padding: 8px 12px;
  cursor: pointer;
  font-size: 14px;
  color: #000000;
  font-weight: 500;
  opacity: 1 !important;
}

.tag-suggestion-item:hover {
  background-color: #f5f7fa;
}

.tag-suggestion-item.selected {
  background-color: #e6f3ff;
  color: #409eff;
}

.selected-tag .remove-tag {
  margin-left: 4px;
  cursor: pointer;
  color: #666;
  font-weight: bold;
}

.selected-tag .remove-tag:hover {
  color: #f56c6c;
}

/* 标签输入框中已选择的标签样式 */
.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 2px;
}

.selected-tag {
  background-color: #ecf5ff;
  border: 1px solid #b3d8ff;
  color: #000000;
  font-weight: 600;
  padding: 5px 10px;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
  transition: all 0.3s;
  opacity: 1 !important;
}

.selected-tag:hover {
  background-color: #e6f0fc;
  border-color: #a0cfff;
}

/* 弹窗样式 */
:deep(.tag-select-dialog) {
  .el-dialog__body {
    padding: 20px;
  }
}

.tag-options-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
  padding: 8px;
}

.tag-option-wrapper {
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  justify-content: center;
}

.tag-option {
  width: 100%;
  text-align: center;
  transition: all 0.3s;
  cursor: pointer;
}

.tag-option-wrapper.is-selected .tag-option {
  transform: translateY(-2px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

/* 标签颜色样式 */
.selected-tag.el-tag--primary,
.tag-option.el-tag--primary {
  --el-tag-bg-color: var(--el-color-primary-light-9);
  --el-tag-border-color: var(--el-color-primary-light-5);
  --el-tag-text-color: var(--el-color-primary);
}

.selected-tag.el-tag--success,
.tag-option.el-tag--success {
  --el-tag-bg-color: var(--el-color-success-light-9);
  --el-tag-border-color: var(--el-color-success-light-5);
  --el-tag-text-color: var(--el-color-success);
}

.selected-tag.el-tag--warning,
.tag-option.el-tag--warning {
  --el-tag-bg-color: var(--el-color-warning-light-9);
  --el-tag-border-color: var(--el-color-warning-light-5);
  --el-tag-text-color: var(--el-color-warning);
}

.selected-tag.el-tag--danger,
.tag-option.el-tag--danger {
  --el-tag-bg-color: var(--el-color-danger-light-9);
  --el-tag-border-color: var(--el-color-danger-light-5);
  --el-tag-text-color: var(--el-color-danger);
}

.selected-tag.el-tag--info,
.tag-option.el-tag--info {
  --el-tag-bg-color: var(--el-color-info-light-9);
  --el-tag-border-color: var(--el-color-info-light-5);
  --el-tag-text-color: var(--el-color-info);
}

/* 暗色主题标签 */
.selected-tag.el-tag--dark,
.tag-option.el-tag--dark {
  --el-tag-bg-color: var(--el-color-primary);
  --el-tag-border-color: var(--el-color-primary);
  --el-tag-text-color: var(--el-color-white);
}

/* 标签选择弹窗样式 */
:deep(.tag-select-popover) {
  padding: 12px !important;
  margin-top: 4px !important;
}

.tag-select-content {
  max-height: 300px;
  overflow-y: auto;
}

.tag-options-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
  gap: 8px;
}

.tag-option-wrapper {
  cursor: pointer;
  transition: all 0.3s;
}

.tag-option {
  width: 100%;
  text-align: center;
  transition: all 0.3s;
  cursor: pointer;
}

.tag-option-wrapper.is-selected .tag-option {
  transform: translateY(-2px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

/* 滚动条样式 */
.tag-select-content {
  scrollbar-width: thin;
  scrollbar-color: var(--el-border-color-lighter) transparent;
}

.tag-select-content::-webkit-scrollbar {
  width: 6px;
}

.tag-select-content::-webkit-scrollbar-track {
  background: transparent;
}

.tag-select-content::-webkit-scrollbar-thumb {
  background-color: var(--el-border-color-lighter);
  border-radius: 3px;
}

.custom-switch {
  --el-switch-on-color: #13ce66;
  --el-switch-off-color: #ff4949;
}

.custom-switch :deep(.el-switch__core) {
  background-color: var(--el-switch-off-color);
  border: none;
  height: 24px !important;
  width: 50px !important;
}

.custom-switch :deep(.el-switch__core .el-switch__action) {
  height: 20px !important;
  width: 20px !important;
  margin: 2px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.custom-switch.is-checked :deep(.el-switch__core) {
  background-color: var(--el-switch-on-color) !important;
}

.custom-switch :deep(.el-switch__label) {
  font-size: 14px;
  font-weight: bold;
  height: 24px;
  line-height: 24px;
}

.custom-switch :deep(.el-switch__label--left) {
  color: var(--el-switch-off-color);
  margin-right: 8px;
}

.custom-switch :deep(.el-switch__label--right) {
  color: var(--el-switch-on-color);
  margin-left: 8px;
}

.custom-switch.is-checked :deep(.el-switch__label--right) {
  color: var(--el-switch-on-color);
}

.custom-switch.is-checked :deep(.el-switch__label--left) {
  color: #909399;
}

/* 增加悬停效果 */
.custom-switch:hover :deep(.el-switch__core) {
  opacity: 0.9;
}

/* 增加过渡动画 */
.custom-switch :deep(.el-switch__core),
.custom-switch :deep(.el-switch__label) {
  transition: all 0.3s ease-in-out;
}

/* 确保输入框和标签的文字清晰可见 */
:deep(.el-input),
:deep(.el-input__wrapper),
:deep(.el-input__inner),
:deep(.el-tag),
:deep(.el-select),
:deep(.el-select__input),
:deep(.el-select-dropdown__item) {
  color: #000000 !important;
  -webkit-text-fill-color: #000000 !important;
  opacity: 1 !important;
  font-weight: 500 !important;
}

/* 确保禁用状态下的文字仍然清晰可见 */
:deep(.is-disabled),
:deep(.is-disabled *),
:deep([disabled]),
:deep([disabled] *) {
  color: #000000 !important;
  -webkit-text-fill-color: #000000 !important;
  opacity: 1 !important;
  background-color: transparent !important;
}

/* 输入框占位符文字样式 */
:deep(.el-input__inner::placeholder),
:deep(.el-select__input::placeholder) {
  color: #a8abb2;
  opacity: 1;
  font-weight: normal;
}

/* 标签选择器特定样式 */
.el-form-item.is-success .el-input__wrapper,
.el-form-item.is-error .el-input__wrapper,
.el-form-item.is-validating .el-input__wrapper {
  background-color: transparent !important;
}

.custom-select input {
  color: #000000 !important;
  font-weight: 500 !important;
  opacity: 1 !important;
  background: none !important;
}

/* 新增模型来源相关样式 */
.help-text {
  margin-top: 8px;
}

.model-source-selector {
  width: 100%;
}

.model-id-input {
  width: 100%;
}

/* 模型来源选择器样式 */
:deep(.el-radio-group) {
  display: flex;
  gap: 8px;
}

:deep(.el-radio-button__inner) {
  font-weight: 500;
  padding: 8px 16px;
  border-radius: 6px;
  transition: all 0.3s;
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: var(--el-color-primary);
  border-color: var(--el-color-primary);
  color: #fff;
  box-shadow: 0 2px 4px rgba(64, 158, 255, 0.3);
}

:deep(.el-radio-button__inner:hover) {
  color: var(--el-color-primary);
  border-color: var(--el-color-primary-light-7);
  background-color: var(--el-color-primary-light-9);
}

/* 模型ID输入框样式 */
:deep(.el-input-group__prepend) {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  font-weight: 600;
  border-color: var(--el-color-primary-light-7);
}

:deep(.el-input-group .el-input__wrapper) {
  border-left: none;
}

/* 帮助文本样式 */
.help-text :deep(.el-text) {
  line-height: 1.5;
}

.help-text :deep(.el-link) {
  font-weight: 600;
  text-decoration: none;
}

.help-text :deep(.el-link):hover {
  text-decoration: underline;
}
</style> 