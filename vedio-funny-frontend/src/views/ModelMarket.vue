<template>
  <div class="model-market">
    <div class="market-container">
      <!-- 搜索区域 -->
      <div class="search-section">
        <div class="search-container">
          <el-input
            v-model="searchQuery"
            placeholder="搜索模型..."
            size="large"
            class="search-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" size="large" class="search-btn">搜索</el-button>
        </div>
        
        <!-- 分类标签 -->
        <div class="category-tags">
          <el-tag
            v-for="category in categories"
            :key="category"
            :type="selectedCategory === category ? 'primary' : 'info'"
            @click="selectedCategory = category"
            class="category-tag"
          >
            {{ category }}
          </el-tag>
        </div>
      </div>

      <div class="main-content">
        <!-- 右侧内容区域 -->
        <div class="content-area">
          <!-- 模型网格 -->
          <div class="model-grid">
            <div
              v-for="model in filteredModels"
              :key="model.id"
              class="model-card"
              @click="viewModel(model)"
            >
              <div class="model-image">
                <img :src="model.image" :alt="model.name" />
                <div class="model-overlay">
                  <el-button type="primary" size="small">查看详情</el-button>
                </div>
              </div>
              
              <div class="model-info">
                <h4 class="model-title">{{ model.name }}</h4>
                <p class="model-description">{{ model.description }}</p>
                
                <div class="model-tags">
                  <el-tag
                    v-for="tag in model.tags"
                    :key="tag"
                    size="small"
                    type="info"
                  >
                    {{ tag }}
                  </el-tag>
                </div>
                
                <div class="model-stats">
                  <div class="stat-item">
                    <el-icon><View /></el-icon>
                    <span>{{ model.views }}</span>
                  </div>
                  <div class="stat-item">
                    <el-icon><Star /></el-icon>
                    <span>{{ model.likes }}</span>
                  </div>
                  <div class="stat-item">
                    <el-icon><Download /></el-icon>
                    <span>{{ model.downloads }}</span>
                  </div>
                </div>
                
                <div class="model-author">
                  <img :src="model.author.avatar" :alt="model.author.name" class="author-avatar" />
                  <span class="author-name">{{ model.author.name }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'

export default {
  name: 'ModelMarket',
  setup() {
    const searchQuery = ref('')
    const selectedCategory = ref('all')

    const categories = ref([
      'all',
      'checkpoint',
      'lora',
      'controlnet',
      'vae',
      'embedding',
      'upscaler',
      'animation',
      'face',
      'style'
    ])



    const models = ref([
      {
        id: 1,
        name: 'F1 Kontext',
        description: 'Context Connected Intelligence Activated',
        type: 'Checkpoint',
        image: 'https://picsum.photos/300/400?random=1',
        views: 257100,
        likes: 1200,
        downloads: 184,
        author: {
          name: '格兰菲学会',
          avatar: 'https://picsum.photos/40/40?random=10'
        }
      },
      {
        id: 2,
        name: 'F.1真实CG',
        description: '真实人物生成模型',
        type: 'LoRA',
        image: 'https://picsum.photos/300/400?random=2',
        views: 95600,
        likes: 800,
        downloads: 51,
        author: {
          name: '小火汀',
          avatar: 'https://picsum.photos/40/40?random=11'
        }
      },
      {
        id: 3,
        name: 'F.1-欧美时尚模特Lora',
        description: '欧美时尚人物风格',
        type: 'LoRA',
        image: 'https://picsum.photos/300/400?random=3',
        views: 86300,
        likes: 1070,
        downloads: 90,
        author: {
          name: '有记设计',
          avatar: 'https://picsum.photos/40/40?random=12'
        }
      },
      {
        id: 4,
        name: 'F1超级精致',
        description: '超精致人物渲染',
        type: 'Checkpoint',
        image: 'https://picsum.photos/300/400?random=4',
        views: 140000,
        likes: 950,
        downloads: 290,
        author: {
          name: '月满西楼',
          avatar: 'https://picsum.photos/40/40?random=13'
        }
      },
      {
        id: 5,
        name: '三次元Evan',
        description: '三次元人物生成',
        type: 'LoRA',
        image: 'https://picsum.photos/300/400?random=5',
        views: 30100,
        likes: 180,
        downloads: 0,
        author: {
          name: '智慧月光3598',
          avatar: 'https://picsum.photos/40/40?random=14'
        }
      },
      {
        id: 6,
        name: 'FG手绘风R1.0',
        description: '手绘插画风格',
        type: 'LoRA',
        image: 'https://picsum.photos/300/400?random=6',
        views: 121000,
        likes: 860,
        downloads: 305,
        author: {
          name: '落叶33',
          avatar: 'https://picsum.photos/40/40?random=15'
        }
      }
    ])

    const filteredModels = computed(() => {
      let result = models.value
      
      if (selectedCategory.value !== 'all') {
        result = result.filter(model => 
          model.type.toLowerCase().includes(selectedCategory.value.toLowerCase())
        )
      }
      
      if (searchQuery.value) {
        result = result.filter(model =>
          model.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
          model.description.toLowerCase().includes(searchQuery.value.toLowerCase())
        )
      }
      
      return result
    })

    const getModelTypeColor = (type) => {
      const colors = {
        'Checkpoint': 'primary',
        'LoRA': 'success',
        'ControlNet': 'warning',
        'VAE': 'info'
      }
      return colors[type] || 'default'
    }

    const formatNumber = (num) => {
      if (num >= 1000000) {
        return (num / 1000000).toFixed(1) + 'M'
      } else if (num >= 1000) {
        return (num / 1000).toFixed(1) + 'k'
      }
      return num.toString()
    }

    const selectCategory = (category) => {
      selectedCategory.value = category
    }



    const handleSearch = () => {
      console.log('搜索:', searchQuery.value)
    }

    const viewModel = (model) => {
      console.log('查看模型:', model)
    }



    onMounted(() => {
      // 初始化数据
    })

    return {
      searchQuery,
      selectedCategory,
      categories,
      filteredModels,
      getModelTypeColor,
      formatNumber,
      selectCategory,
      handleSearch,
      viewModel
    }
  }
}
</script>

<style scoped>
.model-market {
  padding: 0;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.market-container {
  padding: 20px;
  max-width: none;
  margin: 0;
}

.search-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.search-container {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.search-input {
  flex: 1;
  max-width: 600px;
}

.search-btn {
  min-width: 80px;
}

.category-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.category-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.category-tag:hover {
  transform: translateY(-2px);
}

.main-content {
  display: block;
  width: 100%;
}

.content-area {
  width: 100%;
}

.model-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.model-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
}

.model-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.model-image {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.model-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.model-card:hover .model-image img {
  transform: scale(1.05);
}

.model-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.model-card:hover .model-overlay {
  opacity: 1;
}

.model-info {
  padding: 16px;
}

.model-title {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 8px 0;
  color: #303133;
  line-height: 1.4;
}

.model-description {
  font-size: 14px;
  color: #606266;
  margin: 0 0 12px 0;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.model-tags {
  margin-bottom: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.model-stats {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 12px;
  color: #909399;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.model-author {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
}

.author-name {
  font-size: 12px;
  color: #909399;
}

.load-more {
  text-align: center;
  padding: 20px;
}

  /* 响应式设计 */
  @media (max-width: 768px) {
    .market-container {
      padding: 15px;
    }

    .model-grid {
      grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
      gap: 12px;
    }

    .search-container {
      flex-direction: column;
    }

    .search-input {
      max-width: 100%;
    }
  }

@media (max-width: 480px) {
  .market-container {
    padding: 10px;
  }

  .model-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .category-tags {
    justify-content: center;
  }

  .model-stats {
    flex-wrap: wrap;
    gap: 8px;
  }
}

@media (min-width: 1400px) {
  .model-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}
</style> 