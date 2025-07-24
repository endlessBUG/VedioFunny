package com.vediofun.model.service;

import com.vediofun.model.dto.*;
import com.vediofun.model.dto.RayLLMLaunchRequest;
import com.vediofun.model.dto.RayClusterContext;
import com.vediofun.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 部署运行时处理器
 * 负责节点环境检查、Ray集群部署等功能
 * 
 * @author VedioFun Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeploymentService {
    
    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    /**
     * 执行Ray部署工作流
     */
    public RayDeploymentResponse executeRayDeploymentWorkflow(RayDeploymentRequest request) {
        String deploymentId = UUID.randomUUID().toString();
        log.info("开始执行Ray部署工作流 - 部署ID: {}, 模型: {}", deploymentId, request.getModelName());
        
        RayDeploymentResponse response = new RayDeploymentResponse();
        response.setDeploymentId(deploymentId);
        response.setStatus("IN_PROGRESS");
        response.setSteps(new ArrayList<>());
        response.setTimestamp(LocalDateTime.now());
        
        // 创建部署上下文
        RayClusterContext context = RayClusterContext.builder().build();
        
        try {
            List<String> nodeIds = request.getNodeIds();
            String masterNodeId = nodeIds.get(0);
            
            // 预先批量查找所有节点实例，供后续步骤使用
            Map<String, ServiceInstance> nodeInstanceMap = findAllNodeInstances(nodeIds);
            log.info("找到节点实例: {}/{}", nodeInstanceMap.size(), nodeIds.size());
            
            // 步骤1: 检查节点环境
            log.info("步骤1/5: 检查节点环境");
            NodeEnvironmentCheckResponse envCheckResponse = checkNodesEnvironmentWithInstances(nodeIds, nodeInstanceMap);
            addDeploymentStep(response, "环境检查", envCheckResponse.getStatus(), envCheckResponse);
            
            // 步骤2: 安装Python和Ray环境
            log.info("步骤2/5: 安装Python和Ray环境");
            Map<String, Object> installResult = installRayEnvironmentWithInstances(nodeInstanceMap);
            addDeploymentStep(response, "环境安装", "COMPLETED", installResult);
            
            // 步骤3: 创建Ray集群，更新上下文
            log.info("步骤3/5: 创建Ray集群");
            context = createRayClusterWithContext(masterNodeId, nodeInstanceMap, request, context);
            addDeploymentStep(response, "集群创建", "COMPLETED", context);
            
            // 步骤4: 下载模型到主节点
            log.info("步骤4/5: 下载模型到主节点");
            ServiceInstance masterInstance = nodeInstanceMap.get(masterNodeId);
            Map<String, Object> downloadResult = downloadModelToMasterNode(masterInstance, request);
            addDeploymentStep(response, "模型下载", "COMPLETED", downloadResult);
            
            // 步骤5: 启动RayLLM服务，使用上下文
            log.info("步骤5/5: 启动RayLLM服务");
            Map<String, Object> launchResult = launchRayLLMServiceWithContext(context, nodeInstanceMap, request);
            addDeploymentStep(response, "RayLLM启动", "COMPLETED", launchResult);
            
            response.setStatus("COMPLETED");
            log.info("Ray部署工作流完成 - 部署ID: {}", deploymentId);
            
        } catch (Exception e) {
            log.error("Ray部署工作流失败 - 部署ID: {}", deploymentId, e);
            response.setStatus("FAILED");
            response.setError(e.getMessage());
            addDeploymentStep(response, "部署失败", "FAILED", Map.of("error", e.getMessage()));
        }
        
        return response;
    }

    /**
     * 创建环境检查请求对象
     */
    private NodeEnvironmentCheckRequest createEnvironmentCheckRequest(List<String> nodeIds) {
        NodeEnvironmentCheckRequest request = new NodeEnvironmentCheckRequest();
        request.setNodeIds(nodeIds);
        request.setDetailedCheck(true);
        request.setCheckGpu(true);
        request.setCheckPython(true);
        request.setCheckRay(true);
        request.setTimeoutSeconds(30);
        return request;
    }
    
    /**
     * 使用预查找的节点实例检查节点环境
     */
    public NodeEnvironmentCheckResponse checkNodesEnvironmentWithInstances(List<String> nodeIds, Map<String, ServiceInstance> nodeInstanceMap) {
        // 内部创建环境检查请求
        NodeEnvironmentCheckRequest request = createEnvironmentCheckRequest(nodeIds);
        String checkId = "check-" + UUID.randomUUID().toString().substring(0, 8);
        long startTime = System.currentTimeMillis();
        
        log.info("开始节点环境检查 - CheckId: {}, Nodes: {}", checkId, request.getNodeIds());
        
        NodeEnvironmentCheckResponse response = new NodeEnvironmentCheckResponse();
        response.setCheckId(checkId);
        response.setStatus("IN_PROGRESS");
        response.setCheckTime(LocalDateTime.now());
        response.setTotalNodes(request.getNodeIds().size());
        
        try {
            // 2. 并行检查每个节点环境（使用预查找的实例）
            List<CompletableFuture<NodeEnvironmentInfo>> futures = new ArrayList<>();
            
            for (String nodeId : request.getNodeIds()) {
                ServiceInstance nodeInstance = nodeInstanceMap.get(nodeId);
                CompletableFuture<NodeEnvironmentInfo> future = CompletableFuture.supplyAsync(
                    () -> checkSingleNodeEnvironmentWithInstance(nodeId, nodeInstance, request), 
                    executorService
                );
                futures.add(future);
            }
            
            // 3. 等待所有检查完成并收集结果
            List<NodeEnvironmentInfo> nodeInfos = new ArrayList<>();
            int onlineCount = 0;
            
            for (CompletableFuture<NodeEnvironmentInfo> future : futures) {
                NodeEnvironmentInfo nodeInfo = future.get();
                nodeInfos.add(nodeInfo);
                
                if ("ONLINE".equals(nodeInfo.getStatus())) {
                    onlineCount++;
                }
            }
            
            response.setNodeInfos(nodeInfos);
            response.setOnlineNodes(onlineCount);
            response.setOfflineNodes(request.getNodeIds().size() - onlineCount);
            
            // 4. 生成环境摘要
            EnvironmentSummary summary = generateEnvironmentSummary(nodeInfos);
            response.setSummary(summary);
            
            response.setStatus("COMPLETED");
            response.setMessage("节点环境检查完成");
            
            double duration = (System.currentTimeMillis() - startTime) / 1000.0;
            response.setCheckDuration(duration);
            
            log.info("节点环境检查完成 - CheckId: {}, 在线: {}/{}, 耗时: {}秒", 
                    checkId, onlineCount, request.getNodeIds().size(), duration);
            
            return response;
            
        } catch (Exception e) {
            log.error("节点环境检查失败 - CheckId: {}", checkId, e);
            response.setStatus("FAILED");
            response.setError("环境检查失败: " + e.getMessage());
            
            double duration = (System.currentTimeMillis() - startTime) / 1000.0;
            response.setCheckDuration(duration);
            
            return response;
        }
    }
    
    /**
     * 使用预查找的节点实例安装Ray环境
     */
    private Map<String, Object> installRayEnvironmentWithInstances(Map<String, ServiceInstance> nodeInstanceMap) {
        log.info("开始在节点上安装Ray环境...");
        
        Map<String, Object> result = new HashMap<>();
        List<String> successNodes = Collections.synchronizedList(new ArrayList<>());
        List<String> failedNodes = Collections.synchronizedList(new ArrayList<>());
        java.util.concurrent.atomic.AtomicInteger minicondaInstallCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger rayInstallCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger modelEnginesInstallCount = new java.util.concurrent.atomic.AtomicInteger(0);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 并行处理所有节点的环境安装
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            
            for (Map.Entry<String, ServiceInstance> entry : nodeInstanceMap.entrySet()) {
                String nodeId = entry.getKey();
                ServiceInstance instance = entry.getValue();
                
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        log.info("检查节点 {} 的Ray环境状态", nodeId);
                        
                                                 // 首先检查节点当前的环境状态
                         NodeEnvironmentCheckRequest envRequest = new NodeEnvironmentCheckRequest();
                         envRequest.setNodeIds(Arrays.asList(nodeId));
                         envRequest.setDetailedCheck(false);
                         
                         NodeEnvironmentInfo nodeEnvInfo = checkSingleNodeEnvironmentWithInstance(nodeId, instance, envRequest);
                        
                                                 if (nodeEnvInfo == null || !"ONLINE".equals(nodeEnvInfo.getStatus())) {
                             log.warn("节点 {} 不在线，跳过环境安装", nodeId);
                             failedNodes.add(nodeId + " (节点离线)");
                             return;
                         }
                        
                        boolean needsMiniconda = !Boolean.TRUE.equals(nodeEnvInfo.getPythonInstalled());
                        boolean needsRay = !Boolean.TRUE.equals(nodeEnvInfo.getRayInstalled());
                        boolean needsModelEngines = !Boolean.TRUE.equals(nodeEnvInfo.getModelEnginesInstalled());
                        
                        log.info("节点 {} 环境状态 - Python: {}, Ray: {}, ModelEngines: {}", 
                                nodeId, nodeEnvInfo.getPythonInstalled(), nodeEnvInfo.getRayInstalled(), nodeEnvInfo.getModelEnginesInstalled());
                        
                        if (!needsMiniconda && !needsRay && !needsModelEngines) {
                            log.info("节点 {} 环境已完整，跳过安装", nodeId);
                            successNodes.add(nodeId + " (已就绪)");
                            return;
                        }
                        
                        // 需要安装的情况
                        String nodeUrl = instance.getUri().toString();
                        
                        // 合并安装调用，一次API调用完成所有需要的安装
                        if (needsMiniconda || needsRay || needsModelEngines) {
                            log.info("在节点 {} 上安装环境 - Miniconda: {}, Ray: {}, ModelEngines: {}", 
                                    nodeId, needsMiniconda, needsRay, needsModelEngines);
                            
                            InstallEnvironmentResult installResult = installEnvironmentOnNode(nodeUrl, nodeId, needsMiniconda, needsRay);
                            
                            if (installResult.isSuccess()) {
                                if (installResult.isMinicondaInstalled()) {
                                    minicondaInstallCount.incrementAndGet();
                                }
                                if (installResult.isRayInstalled()) {
                                    rayInstallCount.incrementAndGet();
                                }
                                if (installResult.isModelEnginesInstalled()) {
                                    modelEnginesInstallCount.incrementAndGet();
                                }
                                log.info("节点 {} 环境安装成功 - Miniconda: {}, Ray: {}, ModelEngines: {}", 
                                        nodeId, installResult.isMinicondaInstalled(), installResult.isRayInstalled(), installResult.isModelEnginesInstalled());
            } else {
                                log.error("节点 {} 环境安装失败: {}", nodeId, installResult.getErrorMessage());
                                failedNodes.add(nodeId + " (" + installResult.getErrorMessage() + ")");
                                return;
                            }
                        }
                        
                                                 successNodes.add(nodeId);
                        
        } catch (Exception e) {
                         log.error("节点 {} 环境安装异常", nodeId, e);
                         failedNodes.add(nodeId + " (安装异常: " + e.getMessage() + ")");
                     }
                                 }, executorService);
                
                futures.add(future);
            }
            
            // 等待所有节点安装完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            double duration = (System.currentTimeMillis() - startTime) / 1000.0;
            
            // 构建结果
            result.put("status", failedNodes.isEmpty() ? "SUCCESS" : "PARTIAL_SUCCESS");
            result.put("totalNodes", nodeInstanceMap.size());
            result.put("successNodes", successNodes);
            result.put("failedNodes", failedNodes);
            result.put("minicondaInstallCount", minicondaInstallCount.get());
            result.put("rayInstallCount", rayInstallCount.get());
            result.put("modelEnginesInstallCount", modelEnginesInstallCount.get());
            result.put("installDuration", duration);
            result.put("message", String.format("环境安装完成 - 成功: %d, 失败: %d", 
                    successNodes.size(), failedNodes.size()));
            
            log.info("Ray环境安装完成 - 总节点: {}, 成功: {}, 失败: {}, 耗时: {}秒", 
                    nodeInstanceMap.size(), successNodes.size(), failedNodes.size(), duration);
            
            return result;
            
        } catch (Exception e) {
            log.error("Ray环境安装过程中发生异常", e);
            double duration = (System.currentTimeMillis() - startTime) / 1000.0;
            
            result.put("status", "ERROR");
            result.put("error", "环境安装失败: " + e.getMessage());
            result.put("installDuration", duration);
            
            return result;
        }
    }
    

    
    /**
     * 使用预查找的节点实例检查单个节点环境
     */
    private NodeEnvironmentInfo checkSingleNodeEnvironmentWithInstance(String nodeId, ServiceInstance nodeInstance, NodeEnvironmentCheckRequest request) {
        NodeEnvironmentInfo nodeInfo = new NodeEnvironmentInfo();
        nodeInfo.setNodeId(nodeId);
        
        try {
            // 1. 检查节点实例是否存在
            if (nodeInstance == null) {
                nodeInfo.setStatus("NOT_FOUND");
                nodeInfo.setError("节点实例未在服务注册中心找到");
                return nodeInfo;
            }
            
            String nodeUrl = nodeInstance.getUri().toString();
            String nodeAddress = nodeInstance.getHost();
            nodeInfo.setIpAddress(nodeAddress);
            
            log.info("检查节点环境: {} - URL: {}", nodeId, nodeUrl);
            
            // 2. 调用worker节点的/api/model/model/check-environment端点
            NodeEnvironmentCheckResponse envResponse = callWorkerNodeCheckEnvironment(nodeUrl, request);
            if (envResponse != null && "COMPLETED".equals(envResponse.getStatus())) {
                // 从响应中获取第一个节点的环境信息（因为我们只查询了一个节点）
                if (envResponse.getNodeInfos() != null && !envResponse.getNodeInfos().isEmpty()) {
                    return envResponse.getNodeInfos().get(0);
                }
            }
            
            // 3. 如果调用失败，设置错误状态
            nodeInfo.setStatus("ERROR");
            nodeInfo.setError("无法获取节点环境信息");
            return nodeInfo;
            
        } catch (Exception e) {
            log.error("节点环境检查失败 - NodeId: {}", nodeId, e);
            nodeInfo.setStatus("ERROR");
            nodeInfo.setError(e.getMessage());
            return nodeInfo;
        }
    }
    
    /**
     * 生成环境摘要
     */
    private EnvironmentSummary generateEnvironmentSummary(List<NodeEnvironmentInfo> nodeInfos) {
        EnvironmentSummary summary = new EnvironmentSummary();
        
        int pythonReady = 0;
        int rayReady = 0;
        int gpuAvailable = 0;
        int totalGpus = 0;
        int availableGpus = 0;
        long totalGpuMemory = 0;
        long availableGpuMemory = 0;
        int totalCpuCores = 0;
        long totalMemory = 0;
        long availableMemory = 0;
        long totalDisk = 0;
        long availableDisk = 0;
        
        String recommendedMaster = null;
        int maxGpus = 0;
        
        for (NodeEnvironmentInfo nodeInfo : nodeInfos) {
            if (!"ONLINE".equals(nodeInfo.getStatus())) {
                continue;
            }
            
            // 统计Python就绪节点
            if (Boolean.TRUE.equals(nodeInfo.getPythonInstalled())) {
                pythonReady++;
            }
            
            // 统计Ray就绪节点
            if (Boolean.TRUE.equals(nodeInfo.getRayInstalled())) {
                rayReady++;
            }
            
            // 统计CPU
            if (nodeInfo.getCpuInfo() != null) {
                totalCpuCores += nodeInfo.getCpuInfo().getLogicalCores();
            }
            
            // 统计内存
            if (nodeInfo.getMemoryInfo() != null) {
                totalMemory += nodeInfo.getMemoryInfo().getTotalMemoryMB();
                availableMemory += nodeInfo.getMemoryInfo().getFreeMemoryMB();
            }
            
            // 统计GPU
            if (nodeInfo.getGpuInfos() != null && !nodeInfo.getGpuInfos().isEmpty()) {
                gpuAvailable++;
                int nodeGpuCount = nodeInfo.getGpuInfos().size();
                totalGpus += nodeGpuCount;
                
                for (GpuInfo gpu : nodeInfo.getGpuInfos()) {
                    if ("READY".equals(gpu.getStatus()) || "IDLE".equals(gpu.getStatus())) {
                        availableGpus++;
                    }
                    totalGpuMemory += gpu.getTotalMemoryMB();
                    availableGpuMemory += gpu.getFreeMemoryMB();
                }
                
                // 推荐GPU最多的节点作为主节点
                if (nodeGpuCount > maxGpus) {
                    maxGpus = nodeGpuCount;
                    recommendedMaster = nodeInfo.getNodeId();
                }
            }
            
            // 统计磁盘
            if (nodeInfo.getDiskInfo() != null) {
                totalDisk += nodeInfo.getDiskInfo().getTotalDiskGB();
                availableDisk += nodeInfo.getDiskInfo().getFreeDiskGB();
            }
        }
        
        summary.setPythonReadyNodes(pythonReady);
        summary.setRayReadyNodes(rayReady);
        summary.setGpuAvailableNodes(gpuAvailable);
        summary.setTotalGpuCount(totalGpus);
        summary.setAvailableGpuCount(availableGpus);
        summary.setTotalGpuMemoryMB(totalGpuMemory);
        summary.setAvailableGpuMemoryMB(availableGpuMemory);
        summary.setTotalCpuCores(totalCpuCores);
        summary.setTotalMemoryMB(totalMemory);
        summary.setAvailableMemoryMB(availableMemory);
        summary.setTotalDiskGB(totalDisk);
        summary.setAvailableDiskGB(availableDisk);
        summary.setRecommendedMasterNode(recommendedMaster);
        
        // 计算环境就绪率
        int onlineNodes = (int) nodeInfos.stream().mapToInt(node -> "ONLINE".equals(node.getStatus()) ? 1 : 0).sum();
        summary.setEnvironmentPassRate(onlineNodes > 0 ? (double) pythonReady / onlineNodes * 100 : 0.0);
        
        // 判断集群是否就绪
        summary.setClusterReady(onlineNodes >= 1 && pythonReady >= 1);
        
        return summary;
    }
    
    /**
     * 添加部署步骤记录
     */
    private void addDeploymentStep(RayDeploymentResponse response, String stepName, String status, Object details) {
        DeploymentStep step = new DeploymentStep(stepName, status, details);
        response.getSteps().add(step);
    }
    
    /**
     * 调用worker节点的check-environment端点
     */
    private NodeEnvironmentCheckResponse callWorkerNodeCheckEnvironment(String nodeUrl, NodeEnvironmentCheckRequest request) {
        try {
            String checkEnvironmentUrl = nodeUrl + "/model/check-environment";
            log.info("调用worker节点环境检查API: {}", checkEnvironmentUrl);
            
            // 使用common模块的Result类作为响应类型
            ResponseEntity<Result<NodeEnvironmentInfo>> response = restTemplate.exchange(
                checkEnvironmentUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Result<NodeEnvironmentInfo>>() {}
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Result<NodeEnvironmentInfo> workerResponse = response.getBody();
                
                if (workerResponse.isSuccess() && workerResponse.getData() != null) {
                    // 直接使用响应中的NodeEnvironmentInfo对象
                    NodeEnvironmentInfo nodeInfo = workerResponse.getData();
                    
                    NodeEnvironmentCheckResponse result = new NodeEnvironmentCheckResponse();
                    result.setStatus("COMPLETED");
                    result.setMessage("节点环境检查完成");
                    result.setCheckId("worker-" + System.currentTimeMillis());
                    result.setNodeInfos(List.of(nodeInfo));
                    
        return result;
                }
            }
            
            log.warn("Worker节点环境检查API调用失败: {}", checkEnvironmentUrl);
            return null;
            
        } catch (Exception e) {
            log.error("调用worker节点环境检查API异常: {}", nodeUrl, e);
            return null;
        }
    }
    

    
    /**
     * 批量查找所有节点实例
     */
    private Map<String, ServiceInstance> findAllNodeInstances(List<String> nodeIds) {
        Map<String, ServiceInstance> nodeInstanceMap = new HashMap<>();
        
        try {
            // 获取所有服务实例
            List<String> services = discoveryClient.getServices();
            log.debug("发现的服务列表: {}", services);
            
            for (String serviceName : services) {
                List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
                for (ServiceInstance instance : instances) {
                    String instanceId = instance.getInstanceId();
                    String host = instance.getHost();
                    Map<String, String> metadata = instance.getMetadata();
                    
                    log.debug("检查实例: ID={}, Host={}, Metadata={}", instanceId, host, metadata);
                    
                    // 匹配所有需要的节点
                    for (String nodeId : nodeIds) {
                        if (nodeId.equals(instanceId) || 
                            nodeId.equals(host) ||
                            nodeId.equals(metadata.get("nodeId")) ||
                            nodeId.equals(metadata.get("node-id")) ||
                            instanceId.contains(nodeId) ||
                            host.contains(nodeId)) {
                            log.info("找到匹配的节点实例: {} -> {}", nodeId, instance.getUri());
                            nodeInstanceMap.put(nodeId, instance);
                            break; // 找到匹配后跳出当前节点的循环
                        }
                    }
                }
            }
            
            log.info("批量查找节点完成 - 找到: {}/{}", nodeInstanceMap.size(), nodeIds.size());
            return nodeInstanceMap;
            
        } catch (Exception e) {
            log.error("批量查找节点实例失败", e);
            return nodeInstanceMap; // 返回已找到的部分结果
        }
    }
    
    /**
     * 在指定节点安装环境（合并Miniconda和Ray安装）
     */
    private InstallEnvironmentResult installEnvironmentOnNode(String nodeUrl, String nodeId, boolean needsMiniconda, boolean needsRay) {
        try {
            log.info("开始在节点 {} 安装环境 - Miniconda: {}, Ray: {}, ModelEngines: true", nodeId, needsMiniconda, needsRay);
            
            String installUrl = nodeUrl + "/model/install-environment";
            
            // 构建安装请求
            InstallEnvironmentRequest request = new InstallEnvironmentRequest();
            request.setInstallMiniconda(needsMiniconda);
            request.setInstallRay(needsRay);
            request.setInstallModelEngines(true); // 总是安装模型引擎依赖
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<InstallEnvironmentRequest> httpEntity = new HttpEntity<>(request, headers);
            
            // 调用节点的环境安装接口
            Result<InstallEnvironmentResult> response = restTemplate.exchange(
                installUrl,
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<Result<InstallEnvironmentResult>>() {}
            ).getBody();
            
            if (response != null && response.isSuccess()) {
                InstallEnvironmentResult installResult = response.getData();
                log.info("节点 {} 环境安装完成: {}", nodeId, response.getMessage());
                return installResult;
            } else {
                log.error("节点 {} 环境安装失败: {}", nodeId, response != null ? response.getMessage() : "API调用失败");
                return InstallEnvironmentResult.failure(response != null ? response.getMessage() : "API调用失败");
            }
            
        } catch (Exception e) {
            log.error("节点 {} 环境安装异常", nodeId, e);
            return InstallEnvironmentResult.failure("安装异常: " + e.getMessage());
        }
    }
    
    /**
     * 使用上下文创建Ray集群
     */
    private RayClusterContext createRayClusterWithContext(String masterNodeId, Map<String, ServiceInstance> nodeInstanceMap, 
                                                         RayDeploymentRequest request, RayClusterContext context) {
        try {
            List<String> nodeIds = new ArrayList<>(nodeInstanceMap.keySet());
            String clusterAddress = "ray://" + masterNodeId + ":6379";
            
            // 安全地获取工作节点列表
            List<String> workerNodes = new ArrayList<>();
            if (nodeIds.size() > 1) {
                workerNodes = nodeIds.subList(1, nodeIds.size());
            }
            
            log.info("开始创建Ray集群 - 主节点: {}, 工作节点: {}, 集群地址: {}", 
                    masterNodeId, workerNodes, clusterAddress);
            
            // 1. 启动主节点Ray集群
            ServiceInstance masterInstance = nodeInstanceMap.get(masterNodeId);
            if (masterInstance == null) {
                throw new RuntimeException("无法找到主节点实例: " + masterNodeId);
            }
            
            boolean masterStarted = startRayHeadNode(masterInstance, request);
            if (!masterStarted) {
                throw new RuntimeException("主节点Ray集群启动失败");
            }
            
            // 2. 等待主节点启动完成
            log.info("等待主节点Ray集群启动完成...");
            Thread.sleep(5000); // 等待5秒
            
            // 3. 启动工作节点并加入集群
            if (!workerNodes.isEmpty()) {
                log.info("启动 {} 个工作节点加入集群...", workerNodes.size());
                boolean workersJoined = startRayWorkerNodes(workerNodes, nodeInstanceMap, clusterAddress, request);
                if (!workersJoined) {
                    log.warn("部分工作节点加入集群失败，但继续执行");
                }
            } else {
                log.info("没有工作节点需要启动");
            }
            
            // 4. 验证集群状态
            log.info("验证Ray集群状态...");
            boolean clusterHealthy = verifyRayClusterHealth(masterInstance, nodeIds.size());
            if (!clusterHealthy) {
                log.warn("集群健康检查失败，但继续执行");
            }
            
            // 构建Ray集群上下文
            return RayClusterContext.builder()
                    .clusterAddress(clusterAddress)
                    .masterNode(masterNodeId)
                    .workerNodes(workerNodes)
                    .totalNodes(nodeIds.size())
                    .clusterStatus("READY")
                    .additionalInfo("集群创建完成 - 主节点: " + masterNodeId + 
                                  ", 工作节点: " + workerNodes.size() + 
                                  ", 集群地址: " + clusterAddress)
                    .build();
                    
        } catch (Exception e) {
            log.error("创建Ray集群失败", e);
            return RayClusterContext.builder()
                    .clusterStatus("FAILED")
                    .additionalInfo("集群创建失败: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * 启动主节点Ray集群
     */
    private boolean startRayHeadNode(ServiceInstance masterInstance, RayDeploymentRequest request) {
        try {
            log.info("启动主节点Ray集群 - 节点: {}:{}", masterInstance.getHost(), masterInstance.getPort());
            
            // 构建Ray集群启动请求
            Map<String, Object> clusterConfig = new HashMap<>();
            clusterConfig.put("headNode", true);
            clusterConfig.put("rayPort", 6379);
            clusterConfig.put("dashboardPort", 8265);
            clusterConfig.put("objectStorePort", 6380);
            clusterConfig.put("gcsServerPort", 6379);
            clusterConfig.put("minWorkerPort", 10002);
            clusterConfig.put("maxWorkerPort", 19999);
            clusterConfig.put("numCpus", request.getRayConfig() != null ? request.getRayConfig().getNumCpus() : 4);
            clusterConfig.put("numGpus", request.getRayConfig() != null ? request.getRayConfig().getNumGpus() : 0);
            clusterConfig.put("memory", request.getRayConfig() != null ? request.getRayConfig().getMemory() : "8G");
            
            // 调用主节点的Ray集群启动接口
            String startUrl = "http://" + masterInstance.getHost() + ":" + masterInstance.getPort() + "/model/ray/start-head";
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(clusterConfig);
            ResponseEntity<Result> responseEntity = restTemplate.exchange(
                    startUrl,
                    HttpMethod.POST,
                    entity,
                    Result.class
            );
            
            Result response = responseEntity.getBody();
            if (response != null && response.isSuccess()) {
                log.info("主节点Ray集群启动成功");
                return true;
            } else {
                log.error("主节点Ray集群启动失败: {}", response != null ? response.getMessage() : "未知错误");
                return false;
            }
            
        } catch (Exception e) {
            log.error("启动主节点Ray集群异常", e);
            return false;
        }
    }
    
    /**
     * 启动工作节点并加入集群
     */
    private boolean startRayWorkerNodes(List<String> workerNodeIds, Map<String, ServiceInstance> nodeInstanceMap, 
                                      String clusterAddress, RayDeploymentRequest request) {
        try {
            log.info("启动 {} 个工作节点加入集群: {}", workerNodeIds.size(), clusterAddress);
            
            // 并行启动所有工作节点
            List<CompletableFuture<Boolean>> futures = new ArrayList<>();
            
            for (String workerNodeId : workerNodeIds) {
                ServiceInstance workerInstance = nodeInstanceMap.get(workerNodeId);
                if (workerInstance == null) {
                    log.warn("无法找到工作节点实例: {}", workerNodeId);
                    continue;
                }
                
                CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(
                    () -> joinRayCluster(workerInstance, clusterAddress, request),
                    executorService
                );
                futures.add(future);
            }
            
            // 等待所有工作节点启动完成
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            
            // 设置超时时间
            try {
                allFutures.get(60, TimeUnit.SECONDS); // 60秒超时
            } catch (TimeoutException e) {
                log.warn("工作节点启动超时，但继续执行");
            }
            
            // 统计成功启动的工作节点数量
            int successCount = 0;
            for (CompletableFuture<Boolean> future : futures) {
                try {
                    if (future.get(5, TimeUnit.SECONDS)) {
                        successCount++;
                    }
                } catch (Exception e) {
                    log.warn("获取工作节点启动结果失败", e);
                }
            }
            
            log.info("工作节点启动完成 - 成功: {}/{}", successCount, workerNodeIds.size());
            return successCount > 0; // 至少有一个工作节点成功启动
            
        } catch (Exception e) {
            log.error("启动工作节点异常", e);
            return false;
        }
    }
    
    /**
     * 单个工作节点加入集群
     */
    private boolean joinRayCluster(ServiceInstance workerInstance, String clusterAddress, RayDeploymentRequest request) {
        try {
            log.info("工作节点加入集群 - 节点: {}:{}, 集群: {}", 
                    workerInstance.getHost(), workerInstance.getPort(), clusterAddress);
            
            // 构建加入集群请求
            Map<String, Object> joinRequest = new HashMap<>();
            joinRequest.put("clusterAddress", clusterAddress);
            joinRequest.put("rayPort", 10001);
            joinRequest.put("numCpus", request.getRayConfig() != null ? request.getRayConfig().getNumCpus() : 4);
            joinRequest.put("numGpus", request.getRayConfig() != null ? request.getRayConfig().getNumGpus() : 0);
            joinRequest.put("memory", request.getRayConfig() != null ? request.getRayConfig().getMemory() : "8G");
            
            // 调用工作节点的加入集群接口
            String joinUrl = "http://" + workerInstance.getHost() + ":" + workerInstance.getPort() + "/model/ray/join-cluster";
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(joinRequest);
            ResponseEntity<Result> responseEntity = restTemplate.exchange(
                    joinUrl,
                    HttpMethod.POST,
                    entity,
                    Result.class
            );
            
            Result response = responseEntity.getBody();
            if (response != null && response.isSuccess()) {
                log.info("工作节点加入集群成功 - 节点: {}:{}", workerInstance.getHost(), workerInstance.getPort());
                return true;
            } else {
                log.error("工作节点加入集群失败 - 节点: {}:{}, 错误: {}", 
                         workerInstance.getHost(), workerInstance.getPort(), 
                         response != null ? response.getMessage() : "未知错误");
                return false;
            }
            
        } catch (Exception e) {
            log.error("工作节点加入集群异常 - 节点: {}:{}", workerInstance.getHost(), workerInstance.getPort(), e);
            return false;
        }
    }
    
    /**
     * 验证Ray集群健康状态
     */
    private boolean verifyRayClusterHealth(ServiceInstance masterInstance, int expectedNodes) {
        try {
            log.info("验证Ray集群健康状态 - 期望节点数: {}", expectedNodes);
            
            // 调用主节点的集群状态检查接口
            String statusUrl = "http://" + masterInstance.getHost() + ":" + masterInstance.getPort() + "/model/ray/cluster-status";
            
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("clusterAddress", "ray://" + masterInstance.getHost() + ":6379");
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody);
            ResponseEntity<Result> responseEntity = restTemplate.exchange(
                    statusUrl,
                    HttpMethod.POST,
                    entity,
                    Result.class
            );
            
            Result response = responseEntity.getBody();
            if (response != null && response.isSuccess()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> statusData = (Map<String, Object>) response.getData();
                
                // 从RayClusterStatus对象中获取正确的字段名
                String clusterStatus = (String) statusData.get("status");
                Integer actualNodes = (Integer) statusData.get("nodeCount");
                
                log.info("集群状态: {}, 实际节点数: {}/{}", clusterStatus, actualNodes, expectedNodes);
                
                return "healthy".equals(clusterStatus) && actualNodes != null && actualNodes >= expectedNodes;
            } else {
                log.warn("获取集群状态失败: {}", response != null ? response.getMessage() : "未知错误");
                return false;
            }
            
        } catch (Exception e) {
            log.warn("验证集群健康状态异常", e);
            return false;
        }
    }
    
    /**
     * 使用上下文启动RayLLM服务
     */
    private Map<String, Object> launchRayLLMServiceWithContext(RayClusterContext context, Map<String, ServiceInstance> nodeInstanceMap, 
                                                             RayDeploymentRequest request) {
        long startTime = System.currentTimeMillis();
        String modelName = request.getModelName();
        
        Map<String, Object> result = new HashMap<>();
        result.put("modelName", modelName);
        result.put("clusterAddress", context.getClusterAddress());
        
        try {
            log.info("开始启动RayLLM服务 - 模型: {}, 集群: {}", modelName, context.getClusterAddress());
            
            // 获取主节点的ServiceInstance
            ServiceInstance masterInstance = nodeInstanceMap.get(context.getMasterNode());
            if (masterInstance == null) {
                throw new RuntimeException("无法找到主节点实例: " + context.getMasterNode());
            }
            
            // 构建RayLLM启动请求
            RayLLMLaunchRequest launchRequest = new RayLLMLaunchRequest();
            launchRequest.setModelName(modelName);
            launchRequest.setModelId(request.getModelId());
            launchRequest.setModelSource(request.getModelSource());
            launchRequest.setClusterAddress(context.getClusterAddress());
            launchRequest.setDeploymentType(request.getDeploymentType());
            launchRequest.setRayConfig(request.getRayConfig());
            
            // 发送启动RayLLM服务请求到主节点 - 使用正确的host和port
            String launchUrl = "http://" + masterInstance.getHost() + ":" + masterInstance.getPort() + "/model/launch-rayLLM";
            
            HttpEntity<RayLLMLaunchRequest> launchEntity = new HttpEntity<>(launchRequest);
            ResponseEntity<Result> launchResponseEntity = restTemplate.exchange(
                    launchUrl,
                    HttpMethod.POST,
                    launchEntity,
                    Result.class
            );
            
            Result launchResponse = launchResponseEntity.getBody();
            if (launchResponse != null && launchResponse.isSuccess()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> launchData = (Map<String, Object>) launchResponse.getData();
                
                if ("SUCCESS".equals(launchData.get("status"))) {
                    String serviceEndpoint = (String) launchData.get("serviceEndpoint");
                    String serviceStatus = (String) launchData.get("serviceStatus");
                    Integer maxConcurrency = (Integer) launchData.get("maxConcurrency");
                    String gpuMemoryUsage = (String) launchData.get("gpuMemoryUsage");
                    
                    result.put("status", "SUCCESS");
                    result.put("serviceEndpoint", serviceEndpoint);
                    result.put("serviceStatus", serviceStatus);
                    result.put("maxConcurrency", maxConcurrency);
                    result.put("gpuMemoryUsage", gpuMemoryUsage);
                    
                    long duration = System.currentTimeMillis() - startTime;
                    String launchTime = duration / 1000.0 + " seconds";
                    result.put("launchTime", launchTime);
                    
                    log.info("RayLLM服务启动完成 - 模型: {}, 端点: {}, 状态: {}, 耗时: {}", 
                            modelName, serviceEndpoint, serviceStatus, launchTime);
                } else {
                    String errorMsg = (String) launchData.get("error");
                    throw new RuntimeException("RayLLM服务启动失败: " + (errorMsg != null ? errorMsg : "未知错误"));
                }
            } else {
                String errorMsg = "未知错误";
                if (launchResponse != null) {
                    errorMsg = launchResponse.getMessage();
                }
                throw new RuntimeException("RayLLM服务启动失败: " + errorMsg);
            }
            
        } catch (Exception e) {
            log.error("RayLLM服务启动失败 - 模型: {}, 节点: {}:{}", 
                    modelName, 
                    nodeInstanceMap.get(context.getMasterNode()) != null ? nodeInstanceMap.get(context.getMasterNode()).getHost() : "unknown",
                    nodeInstanceMap.get(context.getMasterNode()) != null ? nodeInstanceMap.get(context.getMasterNode()).getPort() : "unknown", 
                    e);
            result.put("status", "FAILED");
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 下载模型到主节点
     */
    private Map<String, Object> downloadModelToMasterNode(ServiceInstance masterInstance, RayDeploymentRequest request) {
        long startTime = System.currentTimeMillis();
        String modelName = request.getModelName();
        String modelSource = request.getModelSource();
        
        Map<String, Object> result = new HashMap<>();
        result.put("modelName", modelName);
        result.put("modelSource", modelSource);
        
        try {
            log.info("开始下载模型到主节点 - 模型: {}, 来源: {}, 节点: {}:{}", 
                    modelName, modelSource, masterInstance.getHost(), masterInstance.getPort());
            
            // 构建下载请求
            Map<String, Object> downloadRequest = new HashMap<>();
            downloadRequest.put("modelName", modelName);
            downloadRequest.put("modelSource", modelSource);
            downloadRequest.put("modelId", request.getModelId());
            downloadRequest.put("downloadTimeout", 3600); // 1小时超时
            
            // 发送下载请求到主节点 - 使用正确的host和port
            String downloadUrl = "http://" + masterInstance.getHost() + ":" + masterInstance.getPort() + "/model/download-model";
            
            HttpEntity<Map<String, Object>> downloadEntity = new HttpEntity<>(downloadRequest);
            ResponseEntity<Result> downloadResponseEntity = restTemplate.exchange(
                    downloadUrl,
                    HttpMethod.POST,
                    downloadEntity,
                    Result.class
            );
            
            Result downloadResponse = downloadResponseEntity.getBody();
            if (downloadResponse != null && downloadResponse.isSuccess() && downloadResponse.getData() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> downloadData = (Map<String, Object>) downloadResponse.getData();
                
                if ("SUCCESS".equals(downloadData.get("status"))) {
                    String downloadPath = (String) downloadData.get("downloadPath");
                    String modelSize = (String) downloadData.get("modelSize");
                    String checksum = (String) downloadData.get("checksum");
                    
                    result.put("status", "SUCCESS");
                    result.put("downloadPath", downloadPath);
                    result.put("modelSize", modelSize);
                    result.put("checksum", checksum);
                    
                    long duration = System.currentTimeMillis() - startTime;
                    String downloadTime = duration / 1000.0 + " seconds";
                    result.put("downloadTime", downloadTime);
                    
                    log.info("模型下载完成 - 模型: {}, 路径: {}, 大小: {}, 耗时: {}", 
                            modelName, downloadPath, modelSize, downloadTime);
                } else {
                    String errorMsg = (String) downloadData.get("error");
                    throw new RuntimeException("模型下载失败: " + (errorMsg != null ? errorMsg : "未知错误"));
                }
            } else {
                String errorMsg = "未知错误";
                if (downloadResponse != null) {
                    errorMsg = downloadResponse.getMessage();
                }
                throw new RuntimeException("模型下载失败: " + errorMsg);
            }
            
        } catch (Exception e) {
            log.error("模型下载失败 - 模型: {}, 节点: {}", modelName, masterInstance.getHost() + ":" + masterInstance.getPort(), e);
            result.put("status", "FAILED");
            result.put("error", e.getMessage());
            
            // 如果是网络连接失败，可能是节点不存在对应的API，提供降级方案
            if (e.getMessage().contains("Connection refused") || e.getMessage().contains("404")) {
                log.warn("节点可能不支持模型下载API，使用本地模型路径");
                result.put("status", "SUCCESS");
                result.put("downloadPath", "/tmp/ray/models/" + modelName);
                result.put("modelSize", "Unknown");
                result.put("note", "使用本地模型路径，需要手动确保模型存在");
            }
        }
        
        return result;
    }
} 