<template>
  <div class="model-add">
    <el-card>
      <template #header>
        <span>添加新模型</span>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="模型名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入模型名称" />
        </el-form-item>
        <el-form-item label="模型分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" @change="handleCategoryChange">
            <el-option label="视频" value="video" />
            <el-option label="文本" value="text" />
            <el-option label="语音" value="audio" />
            <el-option label="向量" value="vector" />
            <el-option label="LoRA" value="lora" />
          </el-select>
        </el-form-item>
        <el-form-item label="版本" prop="version">
          <el-input v-model="form.version" placeholder="请输入版本号" />
        </el-form-item>
        <el-form-item label="提供商" prop="provider">
          <el-input v-model="form.provider" placeholder="请输入提供商" />
        </el-form-item>
        <el-form-item label="模型大小" prop="size">
          <el-input v-model="form.size" placeholder="如：175B/12B/340M" />
        </el-form-item>
        
        <!-- LoRA 特有字段 -->
        <template v-if="form.category === 'lora'">
          <el-form-item label="基础模型" prop="baseModel">
            <el-select v-model="form.baseModel" placeholder="请选择基础模型" filterable allow-create>
              <el-option label="Stable Diffusion 1.5" value="Stable Diffusion 1.5" />
              <el-option label="Stable Diffusion 2.1" value="Stable Diffusion 2.1" />
              <el-option label="SDXL Base" value="SDXL Base" />
              <el-option label="SDXL Turbo" value="SDXL Turbo" />
              <el-option label="Midjourney" value="Midjourney" />
              <el-option label="NovelAI" value="NovelAI" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="触发词" prop="triggerWords">
            <el-input 
              v-model="triggerWordsInput" 
              placeholder="请输入触发词，用逗号分隔" 
              @blur="updateTriggerWords"
            />
            <div class="trigger-words-display" v-if="form.triggerWords && form.triggerWords.length > 0">
              <el-tag 
                v-for="(word, index) in form.triggerWords" 
                :key="index"
                closable
                @close="removeTriggerWord(index)"
                style="margin-right: 8px; margin-top: 8px;"
              >
                {{ word }}
              </el-tag>
            </div>
          </el-form-item>
          
          <el-form-item label="推荐强度" prop="recommendedStrength">
            <el-slider 
              v-model="form.recommendedStrength" 
              :min="0.1" 
              :max="1.5" 
              :step="0.1"
              show-input
              :show-input-controls="false"
            />
          </el-form-item>
        </template>
        
        <el-form-item label="准确率" prop="accuracy">
          <el-input-number v-model="form.accuracy" :min="0" :max="100" />
          <span class="tip">%</span>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="active">启用</el-radio>
            <el-radio label="inactive">禁用</el-radio>
            <el-radio label="maintenance">维护中</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入模型描述" />
        </el-form-item>
        <el-form-item label="参数配置" prop="config">
          <el-input v-model="form.config" type="textarea" :rows="3" placeholder="请输入参数配置(JSON格式)" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">提交</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

export default {
  name: 'ModelAdd',
  setup() {
    const router = useRouter()
    const formRef = ref(null)
    const triggerWordsInput = ref('')
    
    const form = reactive({
      name: '',
      category: '',
      version: '',
      provider: '',
      size: '',
      // LoRA 特有字段
      baseModel: '',
      triggerWords: [],
      recommendedStrength: 0.8,
      accuracy: 90,
      status: 'active',
      description: '',
      config: ''
    })
    
    const rules = {
      name: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
      category: [{ required: true, message: '请选择模型分类', trigger: 'change' }],
      version: [{ required: true, message: '请输入版本号', trigger: 'blur' }],
      provider: [{ required: true, message: '请输入提供商', trigger: 'blur' }],
      size: [{ required: true, message: '请输入模型大小', trigger: 'blur' }],
      baseModel: [
        { 
          required: true, 
          message: '请选择基础模型', 
          trigger: 'change',
          validator: (rule, value, callback) => {
            if (form.category === 'lora' && !value) {
              callback(new Error('LoRA模型必须选择基础模型'))
            } else {
              callback()
            }
          }
        }
      ],
      accuracy: [{ required: true, message: '请输入准确率', trigger: 'blur' }],
      status: [{ required: true, message: '请选择状态', trigger: 'change' }],
      description: [{ required: true, message: '请输入描述', trigger: 'blur' }],
      config: [{ required: true, message: '请输入参数配置', trigger: 'blur' }]
    }
    
    const handleCategoryChange = () => {
      // 当分类改变时，重置LoRA特有字段
      if (form.category !== 'lora') {
        form.baseModel = ''
        form.triggerWords = []
        form.recommendedStrength = 0.8
        triggerWordsInput.value = ''
      } else {
        // 为LoRA设置默认配置
        form.config = JSON.stringify({
          strength: form.recommendedStrength,
          clipSkip: 2,
          steps: 20
        }, null, 2)
      }
    }
    
    const updateTriggerWords = () => {
      if (triggerWordsInput.value.trim()) {
        const words = triggerWordsInput.value
          .split(',')
          .map(word => word.trim())
          .filter(word => word && !form.triggerWords.includes(word))
        
        form.triggerWords = [...form.triggerWords, ...words]
        triggerWordsInput.value = ''
      }
    }
    
    const removeTriggerWord = (index) => {
      form.triggerWords.splice(index, 1)
    }
    
    const handleSubmit = () => {
      formRef.value.validate(valid => {
        if (valid) {
          // 为LoRA模型构建特殊的配置
          if (form.category === 'lora') {
            const loraConfig = {
              strength: form.recommendedStrength,
              clipSkip: 2,
              steps: 20,
              baseModel: form.baseModel,
              triggerWords: form.triggerWords
            }
            form.config = JSON.stringify(loraConfig, null, 2)
          }
          
          console.log('提交的模型数据:', form)
          ElMessage.success('模型添加成功！')
          router.push('/model/list')
        }
      })
    }
    
    const handleReset = () => {
      formRef.value.resetFields()
      triggerWordsInput.value = ''
      form.triggerWords = []
    }
    
    return { 
      formRef, 
      form, 
      rules, 
      triggerWordsInput,
      handleSubmit, 
      handleReset, 
      handleCategoryChange,
      updateTriggerWords,
      removeTriggerWord
    }
  }
}
</script>

<style scoped>
.model-add {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.tip {
  margin-left: 8px;
  color: #909399;
}

.trigger-words-display {
  margin-top: 8px;
  padding: 8px;
  background-color: #f5f7fa;
  border-radius: 4px;
  border: 1px dashed #dcdfe6;
  min-height: 40px;
}

.trigger-words-display:empty::before {
  content: '暂无触发词';
  color: #c0c4cc;
  font-size: 12px;
}

:deep(.el-slider) {
  margin-right: 20px;
}

:deep(.el-slider__input) {
  width: 80px;
}
</style> 