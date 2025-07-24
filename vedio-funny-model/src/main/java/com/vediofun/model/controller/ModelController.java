package com.vediofun.model.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.vediofun.common.utils.RedisConnectionTest;
import com.vediofun.common.utils.RedisUtil;
import com.vediofun.common.result.Result;
import com.vediofun.model.entity.Model;
import com.vediofun.model.service.ModelService;
import com.vediofun.model.dto.InstallEnvironmentRequest;
import com.vediofun.model.dto.InstallEnvironmentResult;
import com.vediofun.model.dto.RayDeploymentRequest;
import com.vediofun.model.dto.RayDeploymentResponse;
import com.vediofun.model.dto.NodeEnvironmentCheckRequest;
import com.vediofun.model.dto.NodeEnvironmentCheckResponse;
import com.vediofun.model.dto.NodeEnvironmentInfo;
import com.vediofun.model.dto.RayClusterInfo;
import com.vediofun.model.dto.RayClusterStatus;
import com.vediofun.model.dto.ModelDownloadRequest;
import com.vediofun.model.dto.ModelDownloadData;
import com.vediofun.model.service.ModelDownloadService;
import com.vediofun.model.service.ModelScopeDownloadService;
import com.vediofun.common.result.Result;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.vediofun.model.service.CosService;
import com.vediofun.model.dto.RayLLMLaunchRequest;

/**
 * 模型服务控制器
 * 
 * @author VedioFun Team
 */
@RestController
@RequestMapping("/model")
@Tag(name = "模型服务", description = "AI模型管理与推理服务")
@RequiredArgsConstructor
public class ModelController {

    private static final Logger log = LoggerFactory.getLogger(ModelController.class);

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private String serverPort;
    
    @Autowired
    private RedisUtil redisUtil;

    private final ModelService modelService;
    private final CosService cosService;
    private final ModelDownloadService modelDownloadService;
    private final ModelScopeDownloadService modelScopeDownloadService;

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查模型服务是否正常运行")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", applicationName);
        result.put("port", serverPort);
        result.put("timestamp", LocalDateTime.now());
        result.put("message", "🤖 VedioFun模型服务运行正常");
        return result;
    }

    /**
     * 服务信息
     */
    @GetMapping("/info")
    @Operation(summary = "服务信息", description = "获取模型服务基本信息")
    public Map<String, Object> info() {
        Map<String, Object> result = new HashMap<>();
        result.put("serviceName", applicationName);
        result.put("port", serverPort);
        result.put("version", "1.0.0");
        result.put("description", "VedioFun模型服务 - AI模型管理与推理");
        result.put("team", "VedioFun Team");
        result.put("features", new String[]{
            "模型管理", "推理服务", "模型训练", "性能监控"
        });
        return result;
    }

    /**
     * 模拟模型推理
     */
    @PostMapping("/inference")
    @Operation(summary = "模型推理", description = "执行AI模型推理任务")
    public Map<String, Object> inference(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        
        String modelName = (String) request.getOrDefault("modelName", "default-model");
        Object inputData = request.get("inputData");
        
        // 模拟推理过程
        result.put("modelName", modelName);
        result.put("inputData", inputData);
        result.put("result", "推理结果: " + Math.random());
        result.put("confidence", Math.random());
        result.put("processTime", "150ms");
        result.put("timestamp", LocalDateTime.now());
        result.put("status", "success");
        
        return result;
    }

    /**
     * 获取模型列表
     */
    @GetMapping("/models")
    @Operation(summary = "模型列表", description = "获取可用的AI模型列表")
    public Map<String, Object> getModels() {
        Map<String, Object> result = new HashMap<>();
        
        String[] models = {
            "video-classification-v1",
            "object-detection-v2", 
            "facial-recognition-v1",
            "content-analysis-v3",
            "recommendation-engine-v2"
        };
        
        result.put("models", models);
        result.put("total", models.length);
        result.put("service", applicationName);
        result.put("timestamp", LocalDateTime.now());
        
        return result;
    }

    /**
     * 模型状态检查
     */
    @GetMapping("/models/{modelName}/status")
    @Operation(summary = "模型状态", description = "检查指定模型的运行状态")
    public Map<String, Object> getModelStatus(@PathVariable String modelName) {
        Map<String, Object> result = new HashMap<>();
        
        result.put("modelName", modelName);
        result.put("status", "running");
        result.put("version", "1.0.0");
        result.put("accuracy", 0.95);
        result.put("loadTime", "2.5s");
        result.put("memoryUsage", "512MB");
        result.put("gpuUsage", "30%");
        result.put("lastUpdated", LocalDateTime.now());
        
        return result;
    }

    /**
     * Echo测试
     */
    @GetMapping("/echo/{message}")
    @Operation(summary = "Echo测试", description = "简单的回声测试")
    public Map<String, Object> echo(@PathVariable String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("echo", message);
        result.put("service", applicationName);
        result.put("port", serverPort);
        result.put("timestamp", LocalDateTime.now());
        return result;
    }
    
    /**
     * Redis连接测试
     */

    @GetMapping("/list")
    @Operation(summary = "获取模型列表", description = "分页获取模型列表")
    public ResponseEntity<Map<String, Object>> getModelList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "vendor", required = false) String vendor,
            @RequestParam(value = "modelName", required = false) String modelName,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "sortOrder", required = false) String sortOrder,
            HttpServletRequest request) {
        try {
            log.info("Received request for model list - Path: {}, Query: {}", request.getRequestURI(), request.getQueryString());
            log.info("Parameters - page: {}, size: {}, name: {}, vendor: {}, modelName: {}, sortBy: {}, sortOrder: {}", 
                    page, size, name, vendor, modelName, sortBy, sortOrder);
            
            // 确保页码不小于1
            page = Math.max(1, page);
            // 确保每页大小在合理范围内
            size = Math.min(100, Math.max(1, size));
            
            Page<Model> pageResult = modelService.getModelList(page - 1, size, name, vendor, modelName, sortBy, sortOrder);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取模型列表成功");
            response.put("data", pageResult);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取模型列表失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取模型列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getModelById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取模型成功");
        response.put("data", modelService.getModelById(id));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createModel(@RequestBody Model model) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "创建模型成功");
        response.put("data", modelService.createModel(model));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新模型", description = "更新模型信息")
    public ResponseEntity<Map<String, Object>> updateModel(@PathVariable Long id, @RequestBody Model model) {
        if (id == null) {
            throw new IllegalArgumentException("模型ID不能为空");
        }
        
        log.info("Updating model with id: {}", id);
        
        try {
            Model updatedModel = modelService.updateModel(id, model);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "更新模型成功");
            response.put("data", updatedModel);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to update model with id: " + id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "更新模型失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteModel(@PathVariable Long id) {
        modelService.deleteModel(id);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "删除模型成功");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadModelFile(@RequestParam("file") MultipartFile file) {
        String filePath = cosService.uploadFile(file, "models");
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "上传模型文件成功");
        response.put("data", Map.of(
            "filePath", filePath,
            "fileSize", file.getSize()
        ));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/ray/create-cluster")
    @Operation(summary = "创建Ray集群", description = "在指定节点上创建Ray集群")
    public ResponseEntity<Map<String, Object>> createRayCluster(
            @RequestBody Map<String, Object> clusterConfig,
            HttpServletRequest request) {
        try {
            log.info("Received Ray集群创建请求 - 配置: {}", clusterConfig);
            
            // 调用服务层创建集群
            Map<String, Object> result = modelService.createRayCluster(clusterConfig);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Ray集群创建请求已提交");
            response.put("data", result);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Ray集群创建请求处理失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "Ray集群创建失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/ray/join-cluster")
    @Operation(summary = "加入Ray集群", description = "将当前节点加入到现有的Ray集群")
    public ResponseEntity<Map<String, Object>> joinRayCluster(
            @RequestBody Map<String, Object> joinParams,
            HttpServletRequest request) {
        try {
            log.info("Received Ray集群加入请求 - 参数: {}", joinParams);
            
            // 调用服务层加入集群
            RayClusterInfo result = modelService.joinRayCluster(joinParams);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Ray集群加入请求已提交");
            response.put("data", result);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Ray集群加入请求处理失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "Ray集群加入失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/ray/start-head")
    @Operation(summary = "启动Ray Head节点", description = "在当前节点启动Ray Head节点")
    public ResponseEntity<Map<String, Object>> startRayHead(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        try {
            log.info("Received Ray Head节点启动请求 - 参数: {}", request);
            
            // 调用服务层启动Ray Head节点
            RayClusterInfo result = modelService.startRayHead(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Ray Head节点启动请求已提交");
            response.put("data", result);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Ray Head节点启动请求处理失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "Ray Head节点启动失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/ray/cluster-status")
    @Operation(summary = "查询Ray集群状态", description = "获取Ray集群的当前状态信息")
    public ResponseEntity<Map<String, Object>> getRayClusterStatus(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        try {
            log.info("Received Ray集群状态查询请求 - 参数: {}", request);
            
            // 调用服务层查询集群状态
            RayClusterStatus result = modelService.getRayClusterStatus(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Ray集群状态查询成功");
            response.put("data", result);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Ray集群状态查询失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "Ray集群状态查询失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/deploy-cluster")
    @Operation(summary = "部署Ray集群", description = "将模型部署到Ray分布式集群")
    public ResponseEntity<Map<String, Object>> deployModelToRayCluster(
            @RequestBody RayDeploymentRequest request,
            HttpServletRequest httpRequest) {
        try {
            log.info("Received Ray集群部署请求 - Model: {}, Source: {}, NodeIds: {}", 
                    request.getModelId(), request.getModelSource(), 
                    request.getNodeIds() != null ? request.getNodeIds() : "null");
            
            // 调用服务层部署模型
            RayDeploymentResponse result = modelService.deployModelToRayCluster(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Ray集群部署请求已提交");
            response.put("data", result);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Ray集群部署请求处理失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "Ray集群部署失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/check-environment")
    @Operation(summary = "检查当前节点环境", description = "检查当前节点的环境信息，包括GPU、CPU、内存等")
    public ResponseEntity<Result<NodeEnvironmentInfo>> checkCurrentNodeEnvironment() {
        try {
            log.info("接收当前节点环境检查请求");
            
            // 调用服务层检查当前节点环境
            NodeEnvironmentInfo result = modelService.checkCurrentNodeEnvironment();
            
            return ResponseEntity.ok(Result.success("当前节点环境检查完成", result));
            
        } catch (Exception e) {
            log.error("当前节点环境检查失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("当前节点环境检查失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/install-environment")
    @Operation(summary = "安装环境", description = "在当前节点安装Miniconda和/或Ray环境")
    public ResponseEntity<Result<InstallEnvironmentResult>> installEnvironment(@RequestBody InstallEnvironmentRequest request) {
        try {
            log.info("接收环境安装请求 - Miniconda: {}, Ray: {}", request.isInstallMiniconda(), request.isInstallRay());
            InstallEnvironmentResult result = modelService.installEnvironment(request);
            return ResponseEntity.ok(Result.success("环境安装完成", result));
        } catch (Exception e) {
            log.error("环境安装失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("环境安装失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/install-miniconda")
    @Operation(summary = "安装Miniconda", description = "在当前节点安装Miniconda环境")
    public ResponseEntity<Result<String>> installMiniconda() {
        try {
            log.info("接收Miniconda安装请求");
            String result = modelService.installMiniconda();
            return ResponseEntity.ok(Result.success("Miniconda安装完成", result));
        } catch (Exception e) {
            log.error("Miniconda安装失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("Miniconda安装失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/download-model")
    @Operation(summary = "下载模型", description = "在当前节点下载指定模型")
    public ResponseEntity<Result<ModelDownloadData>> downloadModel(
            @RequestBody ModelDownloadRequest request,
            HttpServletRequest httpRequest) {
        try {
            log.info("Received 模型下载请求 - 模型: {}, 来源: {}", request.getModelName(), request.getModelSource());
            
            // 调用真实的模型下载服务
            Map<String, Object> result = modelDownloadService.downloadModel(
                    request.getModelName(), 
                    request.getModelSource(), 
                    request.getModelId()
            );
            
            if ("SUCCESS".equals(result.get("status"))) {
                // 将Map结果转换为DTO
                ModelDownloadData data = new ModelDownloadData();
                data.setStatus((String) result.get("status"));
                data.setDownloadPath((String) result.get("downloadPath"));
                data.setModelSize((String) result.get("modelSize"));
                data.setChecksum((String) result.get("checksum"));
                data.setDownloadTime((String) result.get("downloadTime"));
                data.setModelDirectory((String) result.get("modelDirectory"));
                data.setNote((String) result.get("note"));
                
                log.info("模型下载完成 - 模型: {}, 路径: {}, 大小: {}", 
                        request.getModelName(), data.getDownloadPath(), data.getModelSize());
                        
                return ResponseEntity.ok(Result.success("模型下载完成", data));
            } else {
                // 下载失败
                ModelDownloadData data = new ModelDownloadData();
                data.setStatus((String) result.get("status"));
                data.setError((String) result.get("error"));
                data.setNote((String) result.get("note"));
                
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Result.error("模型下载失败: " + result.get("error")));
            }
            
        } catch (Exception e) {
            log.error("模型下载失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("模型下载失败: " + e.getMessage()));
        }
    }
    
    @PostMapping("/launch-rayLLM")
    @Operation(summary = "启动RayLLM服务", description = "在当前节点启动RayLLM服务")
    public ResponseEntity<Result<Map<String, Object>>> launchRayLLM(
            @RequestBody RayLLMLaunchRequest request,
            HttpServletRequest httpRequest) {
        try {
            log.info("Received RayLLM启动请求 - 参数: {}", request);
            
            // 参数验证
            String modelName = request.getModelName();
            String modelPath = getModelPathFromRequest(request);
            String clusterAddress = request.getClusterAddress();
            Integer maxConcurrency = 10; // 可以从request中获取或使用默认值
            String modelEngine = "vllm"; // 可以从request中获取或使用默认值
            
            if (modelName == null || modelName.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Result.error("模型名称不能为空"));
            }
            
            if (modelPath == null || modelPath.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Result.error("模型路径不能为空"));
            }
            
            // 调用服务层启动RayLLM
            Map<String, Object> launchResult = modelService.launchRayLLMService(
                    modelName, modelPath, clusterAddress, maxConcurrency, modelEngine);
            
            if ("SUCCESS".equals(launchResult.get("status"))) {
                log.info("RayLLM服务启动完成 - 模型: {}, 端点: {}", 
                        modelName, launchResult.get("serviceEndpoint"));
                return ResponseEntity.ok(Result.success("RayLLM服务启动完成", launchResult));
            } else {
                String errorMsg = (String) launchResult.get("error");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Result.error("RayLLM服务启动失败: " + errorMsg));
            }
            
        } catch (Exception e) {
            log.error("RayLLM服务启动失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("RayLLM服务启动失败: " + e.getMessage()));
        }
    }
    
    /**
     * 从请求中获取模型路径
     */
    private String getModelPathFromRequest(RayLLMLaunchRequest request) {
        // 这里可以根据modelSource和modelId构建路径
        String modelSource = request.getModelSource();
        Long modelId = request.getModelId();
        
        if ("local".equals(modelSource) && modelId != null) {
            // 从数据库获取模型路径
            try {
                Model model = modelService.getModelById(modelId);
                return model != null ? model.getFilePath() : null;
            } catch (Exception e) {
                log.warn("无法获取模型路径: modelId={}", modelId, e);
                return null;
            }
        } else if ("huggingface".equals(modelSource) || "modelscope".equals(modelSource)) {
            // 构建下载路径
            return "/tmp/vedio-funny/models/" + request.getModelName().replace("/", "_");
        }
        
        return null;
    }
    
    @GetMapping("/rayLLM/health")
    @Operation(summary = "RayLLM健康检查", description = "检查RayLLM服务健康状态")
    public ResponseEntity<Map<String, Object>> rayLLMHealthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "RayLLM");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/install-ray")
    @Operation(summary = "安装Ray", description = "在当前节点安装Ray环境")
    public ResponseEntity<Result<String>> installRay() {
        try {
            log.info("接收Ray安装请求");
            String result = modelService.installRay();
            return ResponseEntity.ok(Result.success("Ray安装完成", result));
        } catch (Exception e) {
            log.error("Ray安装失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("Ray安装失败: " + e.getMessage()));
        }
    }
} 