<template>
  <div class="app-wrapper">
    <!-- 主内容区 -->
    <div class="main-container">
      <!-- 顶部导航栏 -->
      <div class="navbar">
        <div class="left">
          <el-button 
            link
            @click="toggleSidebar"
            class="hamburger-btn"
          >
            <el-icon><Fold v-if="sidebarOpened" /><Expand v-else /></el-icon>
          </el-button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path" :to="item.path">
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="user?.avatar || defaultAvatar" />
              <span>{{ user?.nickname || user?.username || 'Admin' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="settings">设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      <!-- 横向菜单栏 -->
      <el-menu
        mode="horizontal"
        :default-active="activeMenu"
        background-color="#fff"
        text-color="#333"
        active-text-color="#409EFF"
        class="top-menu"
        :show-timeout="0"
      >
        <template v-for="route in routes" :key="route.path">
          <el-menu-item
            v-if="Array.isArray(route.children) && route.children.length === 1"
            :key="route.path"
            :index="normalizePath(route.path, route.children[0].path)"
            @click="navigateTo(normalizePath(route.path, route.children[0].path))"
          >
            <el-icon class="menu-icon">
              <component :is="route.children[0].meta?.icon || route.meta?.icon" />
            </el-icon>
            {{ route.children[0].meta?.title }}
          </el-menu-item>
          <el-sub-menu
            v-else-if="Array.isArray(route.children) && route.children.length > 1"
            :key="route.path + '-group'"
            :index="route.path"
          >
            <template #title>
              <el-icon class="menu-icon">
                <component :is="route.meta?.icon" />
              </el-icon>
              {{ route.meta?.title }}
            </template>
            <el-menu-item
              v-for="child in route.children"
              :key="child.path"
              :index="normalizePath(route.path, child.path)"
              @click="navigateTo(normalizePath(route.path, child.path))"
            >
              <el-icon class="menu-icon">
                <component :is="getChildIcon(child.meta?.title)" />
              </el-icon>
              {{ child.meta?.title }}
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
      <!-- 内容区域 -->
      <div class="app-main">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script>
import { computed, ref, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRoute, useRouter } from 'vue-router'
import { ElLoading, ElMessage } from 'element-plus'
import { PermissionUtil } from '@/utils/permission'
import {
  Fold,
  Expand,
  ArrowDown,
  Odometer,
  User,
  UserFilled,
  List,
  Shop,
  Monitor,
  Setting,
  Menu
} from '@element-plus/icons-vue'

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

function normalizePath(parent, child) {
  if (!child) return parent
  if (child.startsWith('/')) return child
  if (parent.endsWith('/')) return parent + child
  return parent + '/' + child
}

function filterHiddenRoutes(routes) {
  return routes
    .filter(route => !route.hidden)
    .map(route => {
      const r = { ...route }
      if (r.children) {
        r.children = filterHiddenRoutes(r.children)
      }
      return r
    })
}

export default {
  name: 'Layout',
  components: {
    Fold,
    Expand,
    ArrowDown,
    Odometer,
    User,
    UserFilled,
    List,
    Shop,
    Monitor,
    Setting,
    Menu
  },
  setup() {
    const store = useStore()
    const route = useRoute()
    const router = useRouter()

    const sidebarOpened = computed(() => store.state.app?.sidebar?.opened ?? true)
    const user = computed(() => store.state.user?.userInfo)
    const routes = computed(() => {
      const allRoutes = filterHiddenRoutes(router.options.routes)
      return PermissionUtil.generateMenus(allRoutes)
    })

    // 确保页面刷新时权限信息正确初始化
    onMounted(() => {
      const hasPermissions = store.getters['user/permissions'].length > 0
      if (!hasPermissions && store.getters['user/userInfo']) {
        console.log('Layout组件挂载，初始化用户权限信息')
        store.dispatch('user/initUserFromStorage')
      }
    })

    const breadcrumbs = computed(() => {
      const matched = route.matched.filter(item => item.meta && item.meta.title)
      return matched.map(item => ({
        title: item.meta.title,
        path: item.path
      }))
    })

    const activeMenu = computed(() => route.path)

    const toggleSidebar = () => {
      store.commit('app/TOGGLE_SIDEBAR')
    }

    const handleCommand = async (command) => {
      switch (command) {
        case 'profile':
          router.push('/user/profile')
          break
        case 'settings':
          router.push('/system/settings')
          break
        case 'logout':
          try {
            const loading = ElLoading.service({
              lock: true,
              text: '正在登出...',
              background: 'rgba(0, 0, 0, 0.7)'
            })
            
            await store.dispatch('user/logout')
            loading.close()
            ElMessage.success('登出成功')
            router.push('/login')
          } catch (error) {
            console.error('登出失败:', error)
            ElMessage.warning('登出失败，但将跳转到登录页')
            router.push('/login')
          }
          break
      }
    }

    const isActiveRoute = (path) => {
      return route.path === path
    }

    const navigateTo = (path) => {
      router.push(path)
    }

    const getChildIcon = (title) => {
      const iconMap = {
        '仪表盘': 'Odometer',
        '用户列表': 'User',
        '用户资料': 'UserFilled',
        '模型列表': 'List',
        '模型市场': 'Shop',
        '服务器列表': 'Monitor',
        '系统设置': 'Setting'
      }
      return iconMap[title] || 'Menu'
    }

    return {
      sidebarOpened,
      user,
      routes,
      breadcrumbs,
      toggleSidebar,
      handleCommand,
      isActiveRoute,
      navigateTo,
      getChildIcon,
      defaultAvatar,
      normalizePath,
      activeMenu
    }
  }
}
</script>

<style lang="scss" scoped>
.app-wrapper {
  display: flex;
  flex-direction: column;
  height: 100vh;
}
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-left: 0 !important;
}

.navbar {
  height: 60px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  
  .left {
    display: flex;
    align-items: center;
    
    .hamburger-btn {
      margin-right: 16px;
      font-size: 20px;
    }
  }
  
  .right {
    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      
      .el-avatar {
        margin-right: 8px;
      }
      
      span {
        margin-right: 4px;
      }
    }
  }
}

.top-menu {
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  margin-bottom: 0;
  border-radius: 0;
  z-index: 10;
  display: flex;
  justify-content: center;
}
.top-menu .el-menu--horizontal {
  justify-content: center;
}

/* 提升Element Plus横向菜单hover响应速度 */
:deep(.el-menu--horizontal .el-sub-menu__title) {
  transition: background-color 0.1s;
}
:deep(.el-menu--horizontal) {
  --el-menu-horizontal-sub-menu-transition: all 0.1s !important;
}
</style> 