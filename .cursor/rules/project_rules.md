# 项目开发规范和提示词

## 项目架构
- 前端项目: vedio-funny-frontend (Vue 3 + Element Plus)
- 后端项目: 基于 Spring Cloud Alibaba 的微服务架构

### 后端微服务架构
```
vedio-funny-parent                    # 父项目，管理依赖版本
├── vedio-funny-common               # 公共模块
├── vedio-funny-registry            # 注册中心服务
├── vedio-funny-sentinel           # Sentinel 控制台服务
├── vedio-funny-gateway           # 网关服务 (Spring Cloud Gateway)
├── vedio-funny-auth            # 认证服务
├── vedio-funny-user           # 用户服务
├── vedio-funny-model         # 模型服务
├── vedio-funny-tracing      # 链路追踪服务
└── vedio-funny-frontend    # 前端项目
```

## 技术栈版本要求
### 前端
```json
{
  "vue": "^3.3.4",
  "element-plus": "^2.3.8",
  "vue-router": "^4.2.4",
  "vuex": "^4.1.0"
}
```

### 后端
```xml
<properties>
    <!-- JDK 版本 -->
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    
    <!-- Spring 生态 -->
    <spring-boot.version>3.2.1</spring-boot.version>
    <spring-cloud.version>2023.0.0</spring-cloud.version>
    <spring-cloud-alibaba.version>2023.0.1.0</spring-cloud-alibaba.version>
    
    <!-- 中间件 -->
    <nacos.version>2.3.0</nacos.version>
    <sentinel.version>1.8.6</sentinel.version>
    <mysql.version>8.0.33</mysql.version>
    <druid.version>1.2.21</druid.version>
    
    <!-- 工具库 -->
    <mybatis-plus.version>3.5.12</mybatis-plus.version>
    <hutool.version>5.8.22</hutool.version>
    <knife4j.version>4.4.0</knife4j.version>
    <jwt.version>0.11.5</jwt.version>
    <redisson.version>3.24.3</redisson.version>
    
    <!-- 监控追踪 -->
    <micrometer-tracing.version>1.2.2</micrometer-tracing.version>
    <prometheus.version>0.16.0</prometheus.version>
</properties>
```

## 开发规范

### 1. 代码组织
- 严格遵循设计模式和开闭原则
- 一个方法不超过10行，适度拆分保持代码清晰
- 优先使用成熟方案和已有类库
- 组件化开发，保持组件的单一职责

### 2. Element Plus 使用规范
- 按钮类型使用 `type="link"` 替代 `type="text"`（避免废弃警告）
- 图标组件统一从 `@element-plus/icons-vue` 导入
- 组件样式需要导入 `element-plus/dist/index.css`

### 3. 后端微服务规范
#### 3.1 服务注册与发现 (vedio-funny-registry)
- 使用 Nacos 2.3.0 作为注册中心
- 服务命名规范: ${spring.application.name}
- 必须配置健康检查
- 支持多环境配置

#### 3.2 配置管理
- 配置文件必须集中存储在 Nacos Config
- 按环境区分配置: dev/test/prod
- 敏感配置使用 Jasypt 加密
- 配置文件分类：
  - bootstrap.yml: 基础配置
  - application.yml: 应用配置
  - application-{profile}.yml: 环境配置

#### 3.3 服务网关 (vedio-funny-gateway)
- 基于 Spring Cloud Gateway
- 统一的路由规则
- 统一认证鉴权
- 基于 Sentinel 的限流配置
- 跨域处理

#### 3.4 认证服务 (vedio-funny-auth)
- 基于 JWT 的认证机制
- Token 刷新机制
- 用户权限管理
- 第三方登录集成

#### 3.5 服务保护 (vedio-funny-sentinel)
- Sentinel 1.8.6 限流配置
- 熔断降级策略
- 系统保护规则
- 热点参数限流

#### 3.6 链路追踪 (vedio-funny-tracing)
- 基于 Micrometer Tracing
- Zipkin 集成
- 性能监控
- 调用链分析

#### 3.7 缓存规范
- Redisson 3.24.3 集成
- 键名规范: 业务:模块:功能:id
- 合理设置过期时间
- 大key处理方案

#### 3.8 数据库规范
- MySQL 8.0.33
- Druid 1.2.21 连接池
- MyBatis-Plus 3.5.12
- 分库分表策略（可选）

### 4. API 文档规范
- 使用 Knife4j 4.4.0
- RESTful API 设计
- 详细的接口说明
- 请求/响应示例

### 5. 监控告警
- Prometheus + Grafana 监控
- 服务状态监控
- 业务指标监控
- 资源使用监控

### 6. 微服务安全规范
- OAuth2 认证流程
- JWT Token 规范
- 权限控制粒度
- 数据权限设计

### 7. 分布式任务调度
- XXL-Job 配置规范
- 任务分片策略
- 失败重试机制
- 告警通知配置

### 8. Vue 项目配置
```javascript
// vite.config.js 必要配置
define: {
  __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: JSON.stringify(false),
  __VUE_PROD_DEVTOOLS__: JSON.stringify(false),
  __VUE_OPTIONS_API__: JSON.stringify(true),
  __VUE_PROD_TIPS__: JSON.stringify(false)
}
```

### 9. 路由和状态管理
- 路由配置位于 `@/router/index.js`
- 权限控制位于 `@/router/permission.js`
- Vuex 状态管理模块化，位于 `@/store/modules/`

### 10. API 请求规范
- 统一使用 axios 封装的请求方法
- API 路径前缀: `/api`
- 请求拦截器处理 token
- 响应拦截器统一处理错误

### 11. 布局和样式
- 使用 Element Plus 的布局组件
- SCSS 预处理器
- 响应式设计
- 主题色统一管理

### 12. 用户认证
- Token 存储在 localStorage
- 使用 Bearer token 认证
- 支持 token 刷新机制

## 开发环境要求
### 基础环境
- JDK 1.8+
- Maven 3.6+
- Node.js 16+
- npm 8+
- Docker 20+

### 中间件
- Nacos 2.2.x
- Sentinel 1.8.x
- Seata 1.6.x
- Redis 6.x
- RocketMQ 4.x
- MySQL 8.x

### 开发工具
- IDEA Ultimate
- Nacos Console
- Sentinel Dashboard
- Seata Console
- XXL-Job Admin

## 部署架构
```
                        Nginx
                          │
                          ▼
                     Gateway(集群)
                          │
        ┌────────────────┼────────────────┐
        ▼                ▼                ▼
    Auth服务集群     Model服务集群    User服务集群
        │                │                │
        └────────────────┼────────────────┘
                          │
        ┌────────────────┼────────────────┐
        ▼                ▼                ▼
      Nacos           Sentinel          Seata
        │                │                │
        └────────────────┼────────────────┘
                          │
                     Redis集群
                          │
                     MySQL集群
```

## 常见问题解决方案

1. 依赖安装问题
```bash
rm -rf node_modules
npm install
```

2. 开发服务器启动
```bash
cd vedio-funny-frontend
npm run serve
```

3. 生产构建
```bash
npm run build
```

### 分布式相关问题
1. 注册中心连接问题
```bash
# 检查配置
cat bootstrap.yml
# 检查网络
telnet nacos-server 8848
```

2. 分布式事务问题
```sql
-- 检查undo_log表
SELECT * FROM undo_log WHERE branch_id = 'xxx'
```

3. 配置刷新问题
```bash
# 手动刷新配置
curl -X POST http://localhost:8080/actuator/refresh
```

## 测试账号
- admin/123456
- test/123456
- demo/123456

## 注意事项
1. 代码提交前进行 lint 检查
2. 保持依赖版本的一致性
3. 遵循 Vue 3 组合式 API 的最佳实践
4. 注意处理组件的生命周期
5. 合理使用 Vue 的响应式系统 

## 监控和运维
1. 服务监控
   - Spring Boot Admin
   - Prometheus + Grafana
   - ELK日志系统

2. 链路追踪
   - SkyWalking
   - 调用链分析
   - 性能分析

3. 告警配置
   - 服务状态告警
   - 业务异常告警
   - 资源使用告警 