# VedioFun链路追踪Starter

VedioFun微服务项目的链路追踪模块，基于Spring Boot 3.x和Micrometer Tracing实现。

## 功能特性

- 🚀 **开箱即用**: 零配置启动，自动适配Web应用和Gateway应用
- 🏷️ **自动标签**: 自动为HTTP请求添加TraceId和SpanId到响应头
- 📊 **智能采样**: 支持采样率控制和环境特定配置
- 🔧 **灵活配置**: 支持丰富的配置选项和排除规则
- 🎯 **异常追踪**: 自动记录异常信息到trace
- 📝 **日志集成**: 日志自动包含TraceId和SpanId

## 快速开始

### 1. 添加依赖

在需要链路追踪的模块中添加依赖：

```xml
<dependency>
    <groupId>com.vediofun</groupId>
    <artifactId>vedio-funny-tracing</artifactId>
</dependency>
```

### 2. 启动Zipkin服务

```bash
docker run -d -p 9411:9411 openzipkin/zipkin:latest
```

### 3. 无需代码修改

Starter会自动配置链路追踪功能，无需任何代码修改。

## 使用示例

### 手动操作Trace

```java
@Autowired
private TracingUtils tracingUtils;

// 获取当前TraceId
String traceId = tracingUtils.getCurrentTraceId();

// 添加业务标签
tracingUtils.addTag("user.id", "123");
tracingUtils.addTag("business.type", "order");

// 创建子Span执行特定操作
String result = tracingUtils.executeInNewSpan("db-query", () -> {
    // 执行数据库查询
    return queryDatabase();
});

// 记录错误
try {
    doSomething();
} catch (Exception e) {
    tracingUtils.recordError(e);
    throw e;
}
```

### 配置示例

```yaml
vediofun:
  tracing:
    enabled: true
    sampling:
      probability: 0.1  # 10%采样率
    zipkin:
      endpoint: http://localhost:9411/api/v2/spans
    http:
      include-trace-id-in-response: true
      exclude-patterns:
        - /actuator/**
        - /health
```

## 配置说明

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `vediofun.tracing.enabled` | `true` | 是否启用链路追踪 |
| `vediofun.tracing.sampling.probability` | `0.1` | 采样率(0.0-1.0) |
| `vediofun.tracing.zipkin.endpoint` | `http://localhost:9411/api/v2/spans` | Zipkin端点 |
| `vediofun.tracing.http.enabled` | `true` | 是否启用HTTP追踪 |
| `vediofun.tracing.http.include-trace-id-in-response` | `true` | 响应头是否包含TraceId |

更多配置选项请参考 `application-tracing-example.yml`。

## 查看链路追踪

启动应用后，访问 http://localhost:9411 打开Zipkin UI查看链路追踪信息。

## 环境适配

- **开发环境**: 建议100%采样 (`probability: 1.0`)
- **测试环境**: 建议50%采样 (`probability: 0.5`)
- **生产环境**: 建议1-10%采样 (`probability: 0.01-0.1`)

## 注意事项

1. 生产环境请合理设置采样率，避免性能影响
2. Zipkin服务需要单独部署和维护
3. 如需关闭链路追踪，设置 `vediofun.tracing.enabled: false`