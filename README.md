# VedioFun 全栈项目

这是一个基于Gradle多模块架构的全栈项目，包含Vue 3 + Element Plus前端和Spring Boot后端。

## 项目架构
https://sca.aliyun.com/

### 📁 项目结构
```
vedio-funny/
├── vedio-funny-frontend/    # Vue 3前端模块
├── vedio-funny-common/      # 公共模块
├── build.gradle            # 根构建文件
├── settings.gradle         # 模块配置
└── 微服务模块/
    ├── vedio-funny-gateway/     # API网关服务 (8080)
    ├── vedio-funny-auth/        # 认证服务 (8001)
    ├── vedio-funny-user/        # 用户服务 (8002)
    ├── vedio-funny-model/       # 模型服务 (8003)
    ├── vedio-funny-server/      # 服务器管理 (8004)
    ├── vedio-funny-file/        # 文件服务 (8005)
    └── vedio-funny-admin/       # 管理后台 (8006)
```

### 🏗️ 微服务架构
基于Spring Cloud Alibaba构建的微服务架构：
- **注册中心**: Nacos (服务发现、配置管理)
- **API网关**: Spring Cloud Gateway (路由转发、负载均衡)
- **服务调用**: OpenFeign (声明式HTTP客户端)
- **限流熔断**: Sentinel (流量控制、熔断降级)
- **配置管理**: Nacos Config (动态配置)

## 技术栈

### 前端 (vedio-funny-frontend)
- **前端框架**: Vue 3
- **UI 组件库**: Element Plus
- **状态管理**: Vuex 4
- **路由管理**: Vue Router 4
- **构建工具**: Vue CLI
- **样式预处理**: Sass
- **HTTP 客户端**: Axios

### 后端 (vedio-funny-backend)
- **框架**: Spring Boot 3.2.1
- **安全**: Spring Security + JWT
- **数据库**: Spring Data JPA + MySQL/H2
- **缓存**: Redis
- **文档**: Swagger/OpenAPI
- **构建工具**: Gradle

### 公共模块 (vedio-funny-common)
- **工具库**: Hutool, Apache Commons
- **JSON处理**: Jackson
- **数据验证**: Bean Validation

## 功能特性

### 前端功能
- 🎨 **现代化设计** - 基于 Element Plus 的优雅界面设计
- 📱 **响应式布局** - 支持桌面端和移动端
- 🔐 **用户认证** - 完整的登录/登出功能
- 👥 **用户管理** - 用户列表、添加、编辑、删除
- 🎬 **模型管理** - 视频、文本、语音、向量、LoRA模型管理
- 🏪 **模型市场** - LibLibAI风格的模型展示和搜索
- 🖥️ **服务器管理** - 服务器状态监控和管理
- 📊 **数据统计** - 仪表盘数据展示
- ⚙️ **系统设置** - 基本设置、安全设置、邮件设置等

### 后端功能
- 🔒 **安全认证** - JWT令牌认证和权限控制
- 📊 **数据持久化** - JPA + MySQL/H2数据库
- 🚀 **缓存支持** - Redis缓存提升性能
- 📚 **API文档** - 自动生成Swagger文档
- 🔧 **健康检查** - 应用状态监控
- 📁 **文件上传** - 支持大文件上传
- 🌐 **跨域支持** - 开发环境CORS配置

## 项目结构

```
src/
├── components/          # 公共组件
├── layout/             # 布局组件
│   └── index.vue       # 主布局
├── router/             # 路由配置
│   └── index.js
├── store/              # Vuex 状态管理
│   └── index.js
├── views/              # 页面组件
│   ├── Dashboard.vue   # 仪表盘
│   ├── Login.vue       # 登录页
│   ├── user/           # 用户管理
│   │   ├── UserList.vue
│   │   └── UserProfile.vue
│   └── system/         # 系统管理
│       └── Settings.vue
├── App.vue             # 根组件
└── main.js             # 入口文件
```

## 快速开始

### 环境要求
- **JDK 17+** - 微服务运行环境
- **Node.js 18+** - 前端开发环境
- **Nacos 2.3.0+** - 服务注册中心
- **Redis 6.0+** - 缓存服务 (可选)
- **MySQL 8.0+** - 数据库 (生产环境，开发环境使用H2)

### 1. 启动基础设施
```bash
# 1. 下载并启动 Nacos
# 下载: https://github.com/alibaba/nacos/releases
cd nacos/bin
startup.cmd -m standalone
# 访问: http://localhost:8848/nacos (nacos/nacos)

# 2. 启动 Redis (可选)
redis-server
```

### 2. 启动微服务 (推荐)
```bash
# 查看详细启动指南
start-microservices.bat

# 或按顺序手动启动:
# 网关服务 (8080)
cd vedio-funny-gateway && gradlew bootRun

# 认证服务 (8001)  
cd vedio-funny-auth && gradlew bootRun

# 用户服务 (8002)
cd vedio-funny-user && gradlew bootRun

# 模型服务 (8003)
cd vedio-funny-model && gradlew bootRun
```

### 3. 启动前端
```bash
cd vedio-funny-frontend
npm install
npm run serve
```

### 4. 访问地址
- **前端管理界面**: http://localhost:8081
- **API网关**: http://localhost:8080  
- **Nacos控制台**: http://localhost:8848/nacos
- **各服务API文档**: http://localhost:800X/swagger-ui.html

> 📖 **详细文档**: 
> - [微服务架构文档](README-MICROSERVICES.md) - Spring Cloud Alibaba完整指南
> - [Gradle构建文档](README-gradle.md) - 构建和部署指南

## 默认账号

- **用户名**: admin
- **密码**: 123456

## 页面说明

### 1. 登录页面 (`/login`)
- 用户登录界面
- 表单验证
- 记住密码功能

### 2. 仪表盘 (`/dashboard`)
- 数据统计卡片
- 图表展示区域
- 最近活动时间线
- 待办事项列表

### 3. 用户管理 (`/user`)
- **用户列表** (`/user/list`)
  - 用户搜索和筛选
  - 用户增删改查
  - 分页显示
  - 状态切换
- **用户资料** (`/user/profile`)
  - 个人信息编辑
  - 安全设置
  - 登录记录

### 4. 系统设置 (`/system/settings`)
- **基本设置**: 系统名称、Logo、描述等
- **安全设置**: 密码策略、登录限制等
- **邮件设置**: SMTP 配置、邮件发送
- **通知设置**: 通知方式、时间配置
- **备份设置**: 自动备份、手动备份

## 组件说明

### Layout 布局组件
- 响应式侧边栏导航
- 顶部导航栏
- 面包屑导航
- 用户下拉菜单

### 表单组件
- 完整的表单验证
- 多种输入控件
- 文件上传功能
- 日期时间选择器

### 表格组件
- 数据展示
- 排序和筛选
- 分页功能
- 操作按钮

## 自定义配置

### 修改主题色
在 `src/main.js` 中修改 Element Plus 的主题配置：

```javascript
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 自定义主题色
const app = createApp(App)
app.use(ElementPlus, {
  config: {
    color: '#409eff'
  }
})
```

### 添加新页面
1. 在 `src/views/` 下创建新的页面组件
2. 在 `src/router/index.js` 中添加路由配置
3. 在侧边栏菜单中添加对应的菜单项

## 开发建议

1. **组件化开发**: 将重复的 UI 元素封装成组件
2. **状态管理**: 使用 Vuex 管理全局状态
3. **路由守卫**: 添加路由权限控制
4. **错误处理**: 统一的错误处理机制
5. **性能优化**: 使用懒加载和代码分割

## 浏览器支持

- Chrome >= 87
- Firefox >= 78
- Safari >= 14
- Edge >= 88

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！

## 更新日志

### v1.0.0 (2024-01-15)
- 初始版本发布
- 基础功能实现
- 用户管理模块
- 系统设置模块 