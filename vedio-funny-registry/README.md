# VedioFun Nacos 注册与配置中心

基于 Nacos 3.0.2 的微服务注册与配置中心，专为 VedioFun 项目定制。

## 目录结构

```
vedio-funny-registry/
├── nacos/                          # Nacos 服务器文件
│   ├── bin/                        # 启动脚本
│   ├── conf/                       # 配置文件
│   └── data/                       # 数据存储
├── configs/                        # 配置文件目录
│   └── vedio-funny-common.yml      # 通用配置文件
├── start.cmd                       # 基础启动脚本
├── start-with-configs.cmd          # 带配置导入的启动脚本
├── import-configs.cmd              # 配置导入工具 (CMD 版本)
├── import-configs.ps1              # 配置导入工具 (PowerShell 版本)
├── import-configs-simple.cmd       # 简化版配置导入工具
├── nacos-config-import.properties  # Nacos 配置导入配置
└── README.md                       # 说明文档
```

## 快速启动

### 方式1：基础启动（仅启动 Nacos）
```cmd
start.cmd
```

### 方式2：完整启动（启动 + 配置导入）
```cmd
start-with-configs.cmd
```

### 方式3：手动启动
```cmd
# 1. 启动 Nacos
cd nacos/bin
startup.cmd -m standalone

# 2. 导入配置（可选）
cd ../../
import-configs.cmd
```

## 配置管理

### 自动配置导入
启动后会自动将 `configs/` 目录下的所有 `.yml` 文件导入到 Nacos 配置中心：

- **vedio-funny-common.yml**: 所有微服务的通用配置

### 手动配置导入

#### CMD 版本（推荐）
```cmd
# 导入所有配置文件
import-configs.cmd

# 指定 Nacos 服务器地址
import-configs.cmd -server "http://192.168.1.100:8848"

# 指定配置目录
import-configs.cmd -dir "my-configs"

# 简化版本（适用于未启用认证的 Nacos）
import-configs-simple.cmd
```

#### PowerShell 版本（备用）
```powershell
# 导入所有配置文件
.\import-configs.ps1

# 指定 Nacos 服务器地址
.\import-configs.ps1 -NacosServer "http://192.168.1.100:8848"

# 指定配置目录
.\import-configs.ps1 -ConfigDir "my-configs"
```

### 配置结构
所有配置使用以下结构：
- **Group**: DEFAULT_GROUP
- **Namespace**: public（默认）
- **Data ID**: 配置文件名（如 vedio-funny-common.yml）

## 微服务配置

各微服务通过以下配置引用通用配置：

```yaml
spring:
  config:
    import:
      # 本地备用配置
      - optional:classpath:application-common.yml
      # Nacos 通用配置（优先级更高）
      - optional:nacos:vedio-funny-common.yml?group=DEFAULT_GROUP&refreshEnabled=true
  
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        group: DEFAULT_GROUP
      config:
        server-addr: localhost:8848
        import-check.enabled: false
```

## 访问信息

- **控制台地址**: http://localhost:8848/nacos
- **用户名**: nacos
- **密码**: nacos

## 功能特性

✅ **服务注册与发现**: 支持微服务自动注册和发现  
✅ **配置管理**: 集中管理所有微服务配置  
✅ **配置热更新**: 支持运行时配置更新  
✅ **健康检查**: 自动监控服务健康状态  
✅ **负载均衡**: 内置负载均衡策略  
✅ **命名空间**: 支持多环境隔离  
✅ **配置加密**: 支持敏感配置加密存储  

## 部署建议

### 开发环境
- 使用单机模式启动
- 配置存储在本地文件
- 使用默认的内嵌数据库

### 生产环境
- 使用集群模式部署
- 配置存储在外部数据库（MySQL）
- 启用配置加密和访问控制
- 配置监控和告警

## 故障排查

### 服务启动失败
1. 检查端口 8848 是否被占用
2. 检查 Java 环境是否正确安装
3. 查看 `nacos/logs/start.out` 日志文件

### 配置导入失败
1. 确认 Nacos 服务已启动
2. 检查网络连接
3. 验证用户名密码是否正确

### 微服务连接失败
1. 检查微服务配置中的 Nacos 地址
2. 确认服务注册配置正确
3. 查看微服务启动日志

## 技术支持

如遇问题，请查看：
1. Nacos 官方文档: https://nacos.io/
2. VedioFun 项目文档
3. 提交 Issue 到项目仓库 