<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6" v-for="card in statsCards" :key="card.title">
          <el-card class="stats-card" shadow="hover">
            <div class="card-content">
              <div class="card-icon" :style="{ backgroundColor: card.color }">
                <el-icon><component :is="card.icon" /></el-icon>
              </div>
              <div class="card-info">
                <div class="card-title">{{ card.title }}</div>
                <div class="card-value">{{ card.value }}</div>
                <div class="card-trend" :class="card.trend > 0 ? 'up' : 'down'">
                  <el-icon><component :is="card.trend > 0 ? 'ArrowUp' : 'ArrowDown'" /></el-icon>
                  {{ Math.abs(card.trend) }}%
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>访问量趋势</span>
                <el-button type="text">查看详情</el-button>
              </div>
            </template>
            <div class="chart-placeholder">
              <el-empty description="图表区域" />
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>用户分布</span>
                <el-button type="text">查看详情</el-button>
              </div>
            </template>
            <div class="chart-placeholder">
              <el-empty description="图表区域" />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 最近活动 -->
    <div class="recent-activities">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>最近活动</span>
              </div>
            </template>
            <el-timeline>
              <el-timeline-item
                v-for="activity in recentActivities"
                :key="activity.id"
                :timestamp="activity.time"
                :type="activity.type"
              >
                {{ activity.content }}
              </el-timeline-item>
            </el-timeline>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>待办事项</span>
                <el-button type="text">添加</el-button>
              </div>
            </template>
            <el-table :data="todoList" style="width: 100%">
              <el-table-column prop="title" label="任务" />
              <el-table-column prop="priority" label="优先级" width="80">
                <template #default="scope">
                  <el-tag :type="getPriorityType(scope.row.priority)">
                    {{ scope.row.priority }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="80">
                <template #default="scope">
                  <el-tag :type="scope.row.status === '已完成' ? 'success' : 'warning'">
                    {{ scope.row.status }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'

export default {
  name: 'Dashboard',
  setup() {
    const statsCards = ref([
      {
        title: '总用户数',
        value: '12,345',
        icon: 'User',
        color: '#409eff',
        trend: 12.5
      },
      {
        title: '今日访问',
        value: '1,234',
        icon: 'View',
        color: '#67c23a',
        trend: 8.2
      },
      {
        title: '订单数量',
        value: '456',
        icon: 'ShoppingCart',
        color: '#e6a23c',
        trend: -2.1
      },
      {
        title: '收入金额',
        value: '¥89,123',
        icon: 'Money',
        color: '#f56c6c',
        trend: 15.3
      }
    ])

    const recentActivities = ref([
      {
        id: 1,
        content: '用户张三登录了系统',
        time: '2024-01-15 10:30',
        type: 'primary'
      },
      {
        id: 2,
        content: '新增订单 #12345',
        time: '2024-01-15 09:15',
        type: 'success'
      },
      {
        id: 3,
        content: '系统更新完成',
        time: '2024-01-15 08:00',
        type: 'info'
      },
      {
        id: 4,
        content: '用户李四修改了个人信息',
        time: '2024-01-14 16:45',
        type: 'warning'
      }
    ])

    const todoList = ref([
      {
        title: '完成项目文档',
        priority: '高',
        status: '进行中'
      },
      {
        title: '代码审查',
        priority: '中',
        status: '待处理'
      },
      {
        title: '用户反馈处理',
        priority: '高',
        status: '已完成'
      },
      {
        title: '系统测试',
        priority: '低',
        status: '待处理'
      }
    ])

    const getPriorityType = (priority) => {
      const types = {
        '高': 'danger',
        '中': 'warning',
        '低': 'info'
      }
      return types[priority] || 'info'
    }

    return {
      statsCards,
      recentActivities,
      todoList,
      getPriorityType
    }
  }
}
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stats-cards {
  margin-bottom: 20px;
}

.stats-card {
  margin-bottom: 20px;
}

.card-content {
  display: flex;
  align-items: center;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: white;
  font-size: 24px;
}

.card-info {
  flex: 1;
}

.card-title {
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.card-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.card-trend.up {
  color: #67c23a;
}

.card-trend.down {
  color: #f56c6c;
}

.charts-section {
  margin-bottom: 20px;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-placeholder {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.recent-activities {
  margin-bottom: 20px;
}

:deep(.el-card__header) {
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-timeline-item__content) {
  color: #606266;
}
</style> 