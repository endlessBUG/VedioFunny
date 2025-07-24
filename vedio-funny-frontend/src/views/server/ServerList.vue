<template>
  <div class="server-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <h2>服务器管理</h2>
        <p>管理和监控连接的服务器</p>
      </div>
    </div>

    <!-- 新增按钮容器 -->
    <div class="button-container">
      <el-button type="primary" icon="CloudUpload" @click="openDeployDialog">部署</el-button>
      <el-button type="success" icon="School">训练</el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon online">
          <el-icon><Monitor /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ onlineServers }}</div>
          <div class="stat-label">在线服务器</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon offline">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ offlineServers }}</div>
          <div class="stat-label">离线服务器</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon total">
          <el-icon><Monitor /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ totalServers }}</div>
          <div class="stat-label">总服务器数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon cpu">
          <el-icon><Cpu /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ averageCpuUsage }}</div>
          <div class="stat-label">平均CPU使用率</div>
        </div>
      </div>
    </div>

    <!-- 服务器列表表格 -->
    <div class="table-container">
      <el-table :data="modelInstances" stripe style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="instanceId" label="实例ID" width="320" />
        <el-table-column prop="ip" label="IP地址" width="120" />
        <el-table-column prop="port" label="端口" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'UP' ? 'success' : 'danger'">
              {{ row.status === 'UP' ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="元数据">
          <template #default="{ row }">
            <el-tooltip effect="dark" placement="right" popper-class="metadata-tooltip">
              <template #content>
                <pre class="metadata-pre">{{ JSON.stringify(row.metadata, null, 2) }}</pre>
              </template>
              <span style="font-size:12px; white-space:normal; word-break:break-all;">
                {{ row.metadata?.description || '-' }}
              </span>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页（如需） -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="totalServers"
        layout="total, sizes, prev, pager, next, jumper"
      />
    </div>

    <!-- 部署模型模态框 -->
    <el-dialog
      v-model="deployDialogVisible"
      title="部署模型"
      width="600px"
    >
      <el-form ref="form" :model="deployForm" label-width="130px">
        <el-form-item label="选中服务器">
          <div class="selected-servers">
            <el-tag
              v-for="(server, index) in selectedServers"
              :key="index"
              style="margin-right: 8px; margin-bottom: 8px;"
            >
              {{ server.instanceId }} ({{ server.ip }})
            </el-tag>
          </div>
        </el-form-item>

        <!-- 修改：部署方式选择框 -->
        <el-form-item label="部署方式">
          <el-select v-model="deployForm.deploymentType" placeholder="请选择部署方式" style="width: 100%">
            <el-option
              v-for="type in deploymentTypes"
              :key="type.value"
              :label="type.label"
              :value="type.value"
            />
          </el-select>
        </el-form-item>

        <!-- 模型来源选择 -->
        <el-form-item label="模型来源">
          <el-select v-model="deployForm.modelSource" placeholder="请选择模型来源" style="width: 100%">
            <el-option
              v-for="source in modelSources"
              :key="source.value"
              :label="source.label"
              :value="source.value"
            />
          </el-select>
        </el-form-item>

        <!-- 修改：选择模型改为手动输入 -->
        <el-form-item label="模型名称">
          <el-input v-model="deployForm.modelName" placeholder="请输入模型名称" style="width: 100%" />
        </el-form-item>

        <!-- 动态参数区域 -->
        <div v-if="deployForm.deploymentType === 'ray'" class="parameter-group">
          <h4>Ray分布式部署参数</h4>
          <!-- 修改：实例数量 -->
          <el-form-item label="实例数量">
              <el-slider
                v-model="deployForm.rayConfig.instanceCount"
                :min="1"
                :max="10"
                show-input
                input-size="small"
                style="width: 100%"
              />
          </el-form-item>
          <!-- 修改：最小工作节点 -->
          <el-form-item label="最小工作节点">
              <el-slider
                v-model="deployForm.rayConfig.minWorkers"
                :min="1"
                :max="5"
                show-input
                input-size="small"
                style="width: 100%"
              />
          </el-form-item>
          <!-- 修改：最大工作节点 -->
          <el-form-item label="最大工作节点">
              <el-slider
                v-model="deployForm.rayConfig.maxWorkers"
                :min="1"
                :max="20"
                show-input
                input-size="small"
                style="width: 100%"
              />
          </el-form-item>
          <!-- 修改：每节点CPU核心 -->
          <el-form-item label="每节点CPU核心">
              <el-slider
                v-model="deployForm.rayConfig.numCpus"
                :min="1"
                :max="32"
                :step="1"
                show-input
                input-size="small"
                style="width: 100%"
              />
          </el-form-item>
          <el-form-item label="每节点GPU数量">
            <el-slider v-model="deployForm.rayConfig.numGpus" :min="0" :max="8" :step="1" show-input />
          </el-form-item>
          <el-form-item label="内存限制(GB)">
            <el-slider v-model="deployForm.rayConfig.memory" :min="1" :max="128" :step="1" show-input />
          </el-form-item>
          <el-form-item label="运行时环境">
            <el-select v-model="deployForm.rayConfig.runtimeEnv" style="width: 100%">
              <el-option label="Python 3.8" value="py38" />
              <el-option label="Python 3.9" value="py39" />
              <el-option label="Python 3.10" value="py310" />
            </el-select>
          </el-form-item>
          <el-form-item label="工作负载类型">
            <el-select v-model="deployForm.rayConfig.workloadType" style="width: 100%">
              <el-option label="CPU密集型" value="cpu_intensive" />
              <el-option label="GPU密集型" value="gpu_intensive" />
              <el-option label="内存密集型" value="memory_intensive" />
            </el-select>
          </el-form-item>
        </div>

        <div v-if="deployForm.deploymentType === 'vllm'" class="parameter-group">
          <h4>vLLM推理服务参数</h4>
          <el-form-item label="最大批处理大小">
            <el-input-number v-model="deployForm.vLLMConfig.maxBatchSize" :min="1" :max="2048" style="width: 100%" />
          </el-form-item>
          <el-form-item label="张量并行数">
            <el-input-number v-model="deployForm.vLLMConfig.tensorParallelSize" :min="1" :max="8" />
          </el-form-item>
          <el-form-item label="最大模型长度">
            <el-input-number v-model="deployForm.vLLMConfig.maxModelLength" :min="256" :max="32768" style="width: 100%" />
          </el-form-item>
          <el-form-item label="块大小">
            <el-select v-model="deployForm.vLLMConfig.blockSize" style="width: 100%">
              <el-option label="8" value="8" />
              <el-option label="16" value="16" />
              <el-option label="32" value="32" />
            </el-select>
          </el-form-item>
          <el-form-item label="调度策略">
            <el-select v-model="deployForm.vLLMConfig.schedulingPolicy" style="width: 100%">
              <el-option label="优先级调度" value="priority" />
              <el-option label="FIFO" value="fifo" />
            </el-select>
          </el-form-item>
          <el-form-item label="GPU内存百分比">
            <el-slider v-model="deployForm.vLLMConfig.gpuMemoryUtilization" :min="0.1" :max="1.0" :step="0.05" show-input />
          </el-form-item>
          <el-form-item label="交换空间(GB)">
            <el-input-number v-model="deployForm.vLLMConfig.swapSpace" :min="1" :max="128" style="width: 100%" />
          </el-form-item>
          <el-form-item label="量化模式">
            <el-select v-model="deployForm.vLLMConfig.quantization" style="width: 100%">
              <el-option label="FP16" value="fp16" />
              <el-option label="INT8" value="int8" />
              <el-option label="W8A16" value="w8a16" />
            </el-select>
          </el-form-item>
        </div>

        <div v-if="deployForm.deploymentType === 'local'" class="parameter-group">
          <h4>本地测试部署参数</h4>
          <el-form-item label="调试端口">
            <el-input-number v-model="deployForm.localConfig.debugPort" :min="1024" :max="65535" />
          </el-form-item>
          <el-form-item label="使用CUDA">
            <el-switch v-model="deployForm.localConfig.useCuda" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="deployDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="deployModel">确认部署</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { Monitor, Warning, Cpu, CloudUpload } from '@element-plus/icons-vue'
import { getModelInstances } from '@/api/registry'
import { getModelList, deployModelToCluster } from '@/api/model'
import { ElMessage } from 'element-plus'

export default {
  name: 'ServerList',
  components: {
    Monitor,
    Warning,
    Cpu,
    CloudUpload
  },
  setup() {
    const currentPage = ref(1)
    const pageSize = ref(20)
    const modelInstances = ref([])
    const selectedServers = ref([])
    const deployDialogVisible = ref(false)
    const modelList = ref([])
    const deploymentTypes = ref([
      { label: 'Ray分布式部署', value: 'ray' },
      { label: 'vLLM推理服务', value: 'vllm' },
      { label: '本地测试部署', value: 'local' }
    ])

    const modelSources = ref([
      { label: 'ModelScope', value: 'modelscope' },
      { label: 'HuggingFace', value: 'huggingface' }
    ])

    const deployForm = ref({
      modelName: '', // 模型名称
      modelSource: 'modelscope', // 模型来源，默认为 ModelScope
      deploymentType: 'ray', // 默认部署方式为 Ray
      rayConfig: {
        instanceCount: 1,
        minWorkers: 1,
        maxWorkers: 5,
        numCpus: 4,
        numGpus: 0,
        memory: 16,
        runtimeEnv: 'py39',
        workloadType: 'cpu_intensive'
      },
      vLLMConfig: {
        maxBatchSize: 128,
        tensorParallelSize: 1,
        maxModelLength: 2048,
        blockSize: '16',
        schedulingPolicy: 'priority',
        gpuMemoryUtilization: 0.9,
        swapSpace: 4,
        quantization: 'fp16'
      },
      localConfig: {
        debugPort: 5000, // 调试端口
        useCuda: true, // 是否使用CUDA
      }
    })

    let modelTimer = null

    // 统计全部基于modelInstances
    const totalServers = computed(() => modelInstances.value.length)
    const onlineServers = computed(() => modelInstances.value.filter(s => s.status === 'UP').length)
    const offlineServers = computed(() => modelInstances.value.filter(s => s.status !== 'UP').length)
    // 没有CPU数据，平均CPU使用率设为'-'
    const averageCpuUsage = computed(() => '-')

    const fetchModelInstances = async () => {
      try {
        const res = await getModelInstances()
        modelInstances.value = res.data || []
        console.log('modelInstances', modelInstances.value)
      } catch (e) {
        modelInstances.value = []
      }
    }

    const fetchModelList = async () => {
      try {
        const res = await getModelList()
        modelList.value = res.data || []
      } catch (e) {
        modelList.value = []
      }
    }

    const handleSelectionChange = (val) => {
      selectedServers.value = val
    }

    const openDeployDialog = () => {
      if (selectedServers.value.length === 0) {
        ElMessage.warning('请先选择服务器')
        return
      }
      fetchModelList()
      deployDialogVisible.value = true
    }

    const deployModel = async () => {
      if (!deployForm.value.modelName) {
        ElMessage.warning('请先输入模型名称')
        return
      }

      try {
        ElMessage.info('正在提交部署请求...')
        
        console.log('Selected servers:', selectedServers.value)
        const nodeIds = selectedServers.value.map(s => s.instanceId)
        console.log('Extracted nodeIds:', nodeIds)
        
        // 验证nodeIds
        if (!nodeIds || nodeIds.length === 0) {
          ElMessage.error('请先选择至少一个服务器')
          return
        }
        
        // 检查是否有无效的nodeId
        const invalidNodeIds = nodeIds.filter(id => !id || id.trim() === '')
        if (invalidNodeIds.length > 0) {
          ElMessage.error('选择的服务器中包含无效的实例ID')
          return
        }
        
        const deployData = {
          modelName: deployForm.value.modelName,
          modelSource: deployForm.value.modelSource,
          deploymentType: deployForm.value.deploymentType,
          nodeIds: nodeIds,
          rayConfig: JSON.stringify(deployForm.value.rayConfig)
        }
        
        console.log('Deploy data to send:', deployData)
        
        // 调用后端部署接口
        const response = await deployModelToCluster(deployData)
        
        ElMessage.success('部署请求已提交，后端正在处理...')
        deployDialogVisible.value = false
        
        console.log('部署响应:', response)
        
      } catch (error) {
        console.error('部署请求失败:', error)
        ElMessage.error(`部署失败: ${error.message || '请求失败'}`)
      }
    }

    onMounted(() => {
      fetchModelInstances()
      modelTimer = setInterval(fetchModelInstances, 60000)
    })
    onBeforeUnmount(() => {
      clearInterval(modelTimer)
    })

    return {
      currentPage,
      pageSize,
      modelInstances,
      totalServers,
      onlineServers,
      offlineServers,
      averageCpuUsage,
      selectedServers,
      deployDialogVisible,
      modelList,
      deploymentTypes,
      modelSources,
      deployForm, // 修改：返回 deployForm
      deployModel,
      handleSelectionChange,
      openDeployDialog
    }
  }
}
</script>

<style scoped>
/* 统计卡片样式 */
.stats-cards {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background-color: #fff;
  border-radius: 4px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
}

.stat-icon {
  font-size: 24px;
  margin-right: 10px;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-number {
  font-size: 20px;
  font-weight: bold;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

/* 服务器列表表格样式 */
.table-container {
  margin-top: 20px;
}

.el-table {
  font-size: 14px;
}

.el-table .el-tag {
  white-space: nowrap;
}

/* 分页组件样式 */
.pagination {
  margin-top: 20px;
  text-align: right;
}

.el-pagination {
  display: inline-block;
}

/* 其他样式 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.button-container {
  margin-bottom: 20px;
}

.parameter-group {
  border: 1px solid #e4e4e4;
  border-radius: 6px;
  padding: 15px;
  margin-bottom: 15px;
  background-color: #f9f9f9;
}

.parameter-group h4 {
  margin: 0 0 10px 0;
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 8px;
}
</style>
