package com.vediofun.model.service.impl;

import com.vediofun.model.entity.Model;
import com.vediofun.model.entity.ModelDeploymentInstance;
import com.vediofun.model.repository.ModelRepository;
import com.vediofun.model.repository.ModelDeploymentInstanceRepository;
import com.vediofun.model.service.ModelService;
import com.vediofun.model.dto.InstallEnvironmentRequest;
import com.vediofun.model.dto.InstallEnvironmentResult;
import com.vediofun.model.dto.RayClusterInfo;
import com.vediofun.model.dto.RayClusterStatus;
import com.vediofun.model.dto.RayDeploymentRequest;
import com.vediofun.model.dto.RayDeploymentResponse;
import com.vediofun.model.dto.NodeEnvironmentInfo;
import com.vediofun.model.dto.GpuInfo;
import com.vediofun.model.dto.CpuInfo;
import com.vediofun.model.dto.MemoryInfo;
import com.vediofun.model.dto.DiskInfo;
import com.vediofun.model.dto.NetworkInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;

import com.vediofun.model.service.DeploymentService;
import com.vediofun.model.util.ResourceUtil;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModelServiceImpl implements ModelService {

    private final ModelRepository modelRepository;
    private final ModelDeploymentInstanceRepository deploymentInstanceRepository;

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private DiscoveryClient discoveryClient;
    
    @Autowired
    private DeploymentService deploymentRuntime;
    
    @Value("${spring.application.name}")
    private String applicationName;
    
    @Autowired
    private ServletWebServerApplicationContext webServerAppCtxt;
    
    @Autowired
    private ResourceUtil resourceUtil;
    
    private String getClusterServiceUrl(String nodeId) {
        String serviceId = "vedio-funny-model"; // 修改为正确的服务ID
        
        // 从注册中心获取服务实例
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
        if (instances == null || instances.isEmpty()) {
            throw new RuntimeException("服务实例不可用: " + serviceId);
        }
        
        // 根据nodeId选择特定实例
        Optional<ServiceInstance> targetInstance = instances.stream()
            .filter(instance -> instance.getInstanceId().equals(nodeId))
            .findFirst();
            
        if (!targetInstance.isPresent()) {
            throw new RuntimeException("指定节点不存在: " + nodeId);
        }
        
        return targetInstance.get().getUri().toString();
    }

    @Override
    public Page<Model> getModelList(int page, int size, String name, String vendor, String modelName, String sortBy, String sortOrder) {
        // 创建排序对象，默认按创建时间倒序
        Sort sort = Sort.by(Sort.Direction.DESC, "createdTime");
        
        // 如果指定了排序字段和顺序，则使用指定的排序
        if (StringUtils.hasText(sortBy) && StringUtils.hasText(sortOrder)) {
            Sort.Direction direction = sortOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(direction, sortBy);
        }
        
        // 确保页码不为负数
        page = Math.max(0, page);
        // 确保每页大小在合理范围内
        size = Math.min(100, Math.max(1, size));
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Specification<Model> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(name)) {
                predicates.add(cb.like(root.get("name"), "%" + name + "%"));
            }

            if (StringUtils.hasText(vendor)) {
                predicates.add(cb.equal(root.get("vendor"), vendor));
            }

            if (StringUtils.hasText(modelName)) {
                predicates.add(cb.like(root.get("modelName"), "%" + modelName + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return modelRepository.findAll(spec, pageable);
    }

    @Override
    public Model getModelById(Long id) {
        return modelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Model not found with id: " + id));
    }

    @Override
    public Model createModel(Model model) {
        return modelRepository.save(model);
    }

    @Override
    public Model updateModel(Long id, Model model) {
        // 使用路径参数中的 id 而不是 model.getId() 保证数据一致性
        if (!modelRepository.existsById(id)) {
            throw new RuntimeException("Model not found with id: " + id);
        }
        
        // 强制使用路径参数 id 更新模型
        model.setId(id);
        return modelRepository.save(model);
    }

    @Override
    public void deleteModel(Long id) {
        if (!modelRepository.existsById(id)) {
            throw new RuntimeException("Model not found with id: " + id);
        }
        modelRepository.deleteById(id);
    }

    @Override
    public Model uploadModelFile(Long id, MultipartFile file) {
        Model model = getModelById(id);
        // 处理文件上传逻辑
        String filePath = handleFileUpload(file);
        model.setFilePath(filePath);
        model.setFileSize(file.getSize());
        return modelRepository.save(model);
    }

    private String handleFileUpload(MultipartFile file) {
        // 实现文件上传逻辑
        // 返回文件存储路径
        return "uploads/" + file.getOriginalFilename();
    }

    @Override
    public NodeEnvironmentInfo checkCurrentNodeEnvironment() {
        log.info("开始检查当前节点环境");
        
        NodeEnvironmentInfo nodeInfo = new NodeEnvironmentInfo();
        
        try {
            // 1. 获取当前节点标识
            String currentNodeId = getCurrentNodeId();
            nodeInfo.setNodeId(currentNodeId);
            nodeInfo.setStatus("ONLINE");
            
            // 2. 获取本机IP地址
            String localIp = getLocalIpAddress();
            nodeInfo.setIpAddress(localIp);
            
            // 3. 检查操作系统信息
            String osInfo = checkOsInfo();
            nodeInfo.setOsInfo(osInfo);
            
            // 4. 检查Python和Ray环境（使用ray-env-check.sh脚本）
            checkRayEnvironmentWithScript(nodeInfo);
            
            // 6. 检查CPU信息
            CpuInfo cpuInfo = checkCpuInfo();
            nodeInfo.setCpuInfo(cpuInfo);
            
            // 7. 检查内存信息
            MemoryInfo memoryInfo = checkMemoryInfo();
            nodeInfo.setMemoryInfo(memoryInfo);
            
            // 8. 检查GPU信息
            List<GpuInfo> gpuInfos = checkGpuInfo();
            nodeInfo.setGpuInfos(gpuInfos);
            
            // 9. 检查磁盘信息
            DiskInfo diskInfo = checkDiskInfo();
            nodeInfo.setDiskInfo(diskInfo);
            
            // 10. 检查网络信息
            NetworkInfo networkInfo = checkNetworkInfo();
            nodeInfo.setNetworkInfo(networkInfo);
            
            log.info("当前节点环境检查完成 - NodeId: {}, IP: {}, Python: {}, Ray: {}, GPU: {}", 
                    currentNodeId, localIp, nodeInfo.getPythonInstalled(), 
                    nodeInfo.getRayInstalled(), gpuInfos != null ? gpuInfos.size() : 0);
            
            return nodeInfo;
            
        } catch (Exception e) {
            log.error("当前节点环境检查失败", e);
            
            nodeInfo.setNodeId("current-node");
            nodeInfo.setStatus("ERROR");
            nodeInfo.setError("环境检查失败: " + e.getMessage());
            
            return nodeInfo;
        }
    }
    
    @Override
    public RayClusterInfo startRayHead(Map<String, Object> request) {
        log.info("开始启动Ray Head节点 - 请求参数: {}", request);
        
        try {
            // 从请求中获取配置参数
            Boolean headNode = (Boolean) request.get("headNode");
            Integer rayPort = (Integer) request.get("rayPort");
            Integer dashboardPort = (Integer) request.get("dashboardPort");
            Integer objectStorePort = (Integer) request.get("objectStorePort");
            Integer gcsServerPort = (Integer) request.get("gcsServerPort");
            Integer minWorkerPort = (Integer) request.get("minWorkerPort");
            Integer maxWorkerPort = (Integer) request.get("maxWorkerPort");
            Integer numCpus = (Integer) request.get("numCpus");
            Integer numGpus = (Integer) request.get("numGpus");
            
            // 安全地处理memory字段，可能是String或Integer
            String memory = "8589934592"; // 默认值 8GB in bytes
            Object memoryObj = request.get("memory");
            if (memoryObj != null) {
                if (memoryObj instanceof String) {
                    String memoryStr = (String) memoryObj;
                    // 如果是字符串格式（如"8G"），转换为字节
                    if (memoryStr.endsWith("G") || memoryStr.endsWith("g")) {
                        int gb = Integer.parseInt(memoryStr.substring(0, memoryStr.length() - 1));
                        memory = String.valueOf(gb * 1024 * 1024 * 1024L);
                    } else if (memoryStr.endsWith("M") || memoryStr.endsWith("m")) {
                        int mb = Integer.parseInt(memoryStr.substring(0, memoryStr.length() - 1));
                        memory = String.valueOf(mb * 1024 * 1024L);
                    } else {
                        memory = memoryStr;
                    }
                } else if (memoryObj instanceof Integer) {
                    // 如果是整数，假设是GB，转换为字节
                    int gb = (Integer) memoryObj;
                    memory = String.valueOf(gb * 1024L * 1024L * 1024L);
                } else {
                    memory = memoryObj.toString();
                }
            }
            
            // 检测操作系统，为Mac设置更小的对象存储大小
            String os = System.getProperty("os.name").toLowerCase();
            String objectStoreMemory = memory;
            if (os.contains("mac")) {
                // Mac上对象存储大小限制为2GB
                objectStoreMemory = "2147483648"; // 2GB in bytes
                log.info("检测到Mac系统，对象存储大小设置为2GB");
            }
            
            // 设置默认值
            rayPort = rayPort != null ? rayPort : 6379;
            dashboardPort = dashboardPort != null ? dashboardPort : 8265;
            objectStorePort = objectStorePort != null ? objectStorePort : 6380;
            gcsServerPort = gcsServerPort != null ? gcsServerPort : 6379;
            minWorkerPort = minWorkerPort != null ? minWorkerPort : 10002;
            maxWorkerPort = maxWorkerPort != null ? maxWorkerPort : 19999;
            numCpus = numCpus != null ? numCpus : 4;
            numGpus = numGpus != null ? numGpus : 0;
            
            log.info("Ray Head节点配置 - 端口: {}, Dashboard: {}, CPU: {}, GPU: {}, 内存: {}", 
                    rayPort, dashboardPort, numCpus, numGpus, memory);
            
            // 构建Ray启动命令
            StringBuilder commandBuilder = new StringBuilder();
            commandBuilder.append("ray start --head");
            commandBuilder.append(" --port=").append(rayPort);
            commandBuilder.append(" --dashboard-port=").append(dashboardPort);
            commandBuilder.append(" --object-manager-port=").append(objectStorePort);
            commandBuilder.append(" --min-worker-port=").append(minWorkerPort);
            commandBuilder.append(" --max-worker-port=").append(maxWorkerPort);
            commandBuilder.append(" --num-cpus=").append(numCpus);
            if (numGpus > 0) {
                commandBuilder.append(" --num-gpus=").append(numGpus);
            }
            commandBuilder.append(" --memory=").append(memory);
            commandBuilder.append(" --object-store-memory=").append(objectStoreMemory);
            commandBuilder.append(" --include-dashboard=true");
            commandBuilder.append(" --dashboard-host=0.0.0.0");
            commandBuilder.append(" --temp-dir=/tmp/ray");
            
            String command = commandBuilder.toString();

            // 构建完整的命令，先加载ray.env环境并激活conda环境再执行ray命令
            String rayEnvPath = resourceUtil.getRayEnvPath();
            String fullCommand = String.format("source %s && source $CONDA_HOME/etc/profile.d/conda.sh && conda activate $RAY_ENV_NAME && %s", 
                    rayEnvPath, command);
            log.info("执行Ray启动命令: {}", fullCommand);

            // 执行Ray启动命令
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", fullCommand);
            processBuilder.redirectErrorStream(true);
            
            // 设置环境变量
            Map<String, String> env = processBuilder.environment();
            env.put("RAY_ENABLE_WINDOWS_OR_OSX_CLUSTER", "1");
            env.put("RAY_TMPDIR", "/tmp/ray");
            
            Process process = processBuilder.start();
            
            // 读取输出
            StringBuilder output = new StringBuilder();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                // 等待一下让服务完全启动
                Thread.sleep(3000);
                
                // 获取集群地址
                String localIp = getLocalIpAddress();
                String clusterAddress = String.format("ray://%s:%d", localIp, rayPort);
                
                log.info("Ray Head节点启动成功 - 集群地址: {}", clusterAddress);
                log.info("Ray输出: {}", output.toString());
                
                return RayClusterInfo.success(clusterAddress, "head",
                        String.format("Ray Head节点启动成功，集群地址: %s", clusterAddress));
            } else {
                log.error("Ray Head节点启动失败 - 退出码: {}, 输出: {}", exitCode, output.toString());
                return RayClusterInfo.failure("Ray Head节点启动失败: " + output.toString());
            }

        } catch (Exception e) {
            log.error("Ray Head节点启动异常", e);
            return RayClusterInfo.failure("Ray Head节点启动异常: " + e.getMessage());
        }
    }

    @Override
    public RayClusterInfo joinRayCluster(Map<String, Object> request) {
        log.info("开始加入Ray集群 - 请求参数: {}", request);

        try {
            // 从请求中获取配置参数
            String clusterAddress = (String) request.get("clusterAddress");
            Integer rayPort = (Integer) request.get("rayPort");
            Integer numCpus = (Integer) request.get("numCpus");
            Integer numGpus = (Integer) request.get("numGpus");
            
            // 安全地处理memory字段，可能是String或Integer
            String memory = "8589934592"; // 默认值 8GB in bytes
            Object memoryObj = request.get("memory");
            if (memoryObj != null) {
                if (memoryObj instanceof String) {
                    String memoryStr = (String) memoryObj;
                    // 如果是字符串格式（如"8G"），转换为字节
                    if (memoryStr.endsWith("G") || memoryStr.endsWith("g")) {
                        int gb = Integer.parseInt(memoryStr.substring(0, memoryStr.length() - 1));
                        memory = String.valueOf(gb * 1024 * 1024 * 1024L);
                    } else if (memoryStr.endsWith("M") || memoryStr.endsWith("m")) {
                        int mb = Integer.parseInt(memoryStr.substring(0, memoryStr.length() - 1));
                        memory = String.valueOf(mb * 1024 * 1024L);
                    } else {
                        memory = memoryStr;
                    }
                } else if (memoryObj instanceof Integer) {
                    // 如果是整数，假设是GB，转换为字节
                    int gb = (Integer) memoryObj;
                    memory = String.valueOf(gb * 1024L * 1024L * 1024L);
                } else {
                    memory = memoryObj.toString();
                }
            }
            
            // 检测操作系统，为Mac设置更小的对象存储大小
            String os = System.getProperty("os.name").toLowerCase();
            String objectStoreMemory = memory;
            if (os.contains("mac")) {
                // Mac上对象存储大小限制为2GB
                objectStoreMemory = "2147483648"; // 2GB in bytes
                log.info("检测到Mac系统，对象存储大小设置为2GB");
            }
            
            // 验证必要参数
            if (clusterAddress == null || clusterAddress.trim().isEmpty()) {
                throw new IllegalArgumentException("集群地址不能为空");
            }
            
            // 设置默认值
            rayPort = rayPort != null ? rayPort : 6381;
            numCpus = numCpus != null ? numCpus : 2;
            numGpus = numGpus != null ? numGpus : 0;
            
            log.info("Worker节点配置 - 集群: {}, 端口: {}, CPU: {}, GPU: {}, 内存: {}", 
                    clusterAddress, rayPort, numCpus, numGpus, memory);

            // 解析集群地址
            String headAddress = clusterAddress.replace("ray://", "");
            
            // 构建Ray启动命令
            StringBuilder commandBuilder = new StringBuilder();
            commandBuilder.append("ray start");
            commandBuilder.append(" --address=").append(headAddress);
            commandBuilder.append(" --port=").append(rayPort);
            commandBuilder.append(" --num-cpus=").append(numCpus);
            if (numGpus > 0) {
                commandBuilder.append(" --num-gpus=").append(numGpus);
            }
            commandBuilder.append(" --memory=").append(memory);
            commandBuilder.append(" --object-store-memory=").append(objectStoreMemory);
            commandBuilder.append(" --temp-dir=/tmp/ray");
            
            String command = commandBuilder.toString();
            log.info("执行Ray加入集群命令: {}", command);
            
            // 构建完整的命令，先加载ray.env环境并激活conda环境再执行ray命令
            String rayEnvPath = resourceUtil.getRayEnvPath();
            String fullCommand = String.format("source %s && source $CONDA_HOME/etc/profile.d/conda.sh && conda activate $RAY_ENV_NAME && %s", 
                    rayEnvPath, command);
            
            // 执行Ray加入集群命令
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", fullCommand);
            processBuilder.redirectErrorStream(true);
            
            // 设置环境变量
            Map<String, String> env = processBuilder.environment();
            env.put("RAY_ENABLE_WINDOWS_OR_OSX_CLUSTER", "1");
            env.put("RAY_TMPDIR", "/tmp/ray");
            
            Process process = processBuilder.start();
            
            // 读取输出
            StringBuilder output = new StringBuilder();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                // 等待一下让连接建立
                Thread.sleep(2000);
                
                log.info("Worker节点成功加入Ray集群: {}", clusterAddress);
                log.info("Ray输出: {}", output.toString());
                
                return RayClusterInfo.success(clusterAddress, "worker",
                        String.format("Worker节点成功加入集群: %s", clusterAddress));
            } else {
                log.error("Worker节点加入集群失败 - 退出码: {}, 输出: {}", exitCode, output.toString());
                return RayClusterInfo.failure("Worker节点加入集群失败: " + output.toString());
            }

        } catch (Exception e) {
            log.error("Worker节点加入集群异常", e);
            return RayClusterInfo.failure("Worker节点加入集群异常: " + e.getMessage());
        }
    }

    @Override
    public RayClusterStatus getRayClusterStatus(Map<String, Object> request) {
        log.info("开始查询Ray集群状态 - 请求参数: {}", request);
        
        try {
            log.info("开始检查Ray集群状态");
            
            String clusterAddress = (String) request.get("clusterAddress");
            
            // 构建完整的命令，先加载ray.env环境并激活conda环境再执行ray命令
            String rayEnvPath = resourceUtil.getRayEnvPath();
            String fullCommand = String.format("source %s && source $CONDA_HOME/etc/profile.d/conda.sh && conda activate $RAY_ENV_NAME && ray status", 
                    rayEnvPath);
            
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", fullCommand);
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            StringBuilder output = new StringBuilder();
            
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                // 解析输出获取集群信息
                String statusOutput = output.toString();
                log.info("Ray状态输出: {}", statusOutput);
                
                // 尝试解析节点数量
                int nodeCount = parseNodeCountFromStatus(statusOutput);
                
                // 尝试获取Ray版本
                String rayVersion = getRayVersionFromEnv();
                
                // 创建成功状态
                Map<String, Object> resources = new HashMap<>();
                resources.put("output", statusOutput);
                resources.put("nodeCount", nodeCount);
                resources.put("rayVersion", rayVersion);
                
                return RayClusterStatus.healthy(clusterAddress, rayVersion, nodeCount, resources);
            } else {
                log.error("Ray集群状态查询失败 - 退出码: {}, 输出: {}", exitCode, output.toString());
                return RayClusterStatus.unhealthy("Ray集群状态查询失败: " + output.toString());
            }
            
        } catch (Exception e) {
            log.error("Ray集群状态查询异常", e);
            return RayClusterStatus.unhealthy("状态查询异常: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> launchRayLLMService(String modelName, String modelPath, 
                                                 String clusterAddress, Integer maxConcurrency, 
                                                 String modelEngine) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("开始启动RayLLM服务 - 模型: {}, 路径: {}, 集群: {}", modelName, modelPath, clusterAddress);
            
        // 1. 验证参数
            if (modelName == null || modelName.trim().isEmpty()) {
                throw new IllegalArgumentException("模型名称不能为空");
            }
            if (modelPath == null || modelPath.trim().isEmpty()) {
                throw new IllegalArgumentException("模型路径不能为空");
            }
            
            // 2. 检查模型文件是否存在
            if (!checkModelFileExists(modelPath)) {
                throw new RuntimeException("模型文件不存在: " + modelPath);
            }
            
            // 3. 检查Ray环境
            NodeEnvironmentInfo envInfo = checkCurrentNodeEnvironment();
            if (envInfo.getRayInstalled() == null || !envInfo.getRayInstalled()) {
                throw new RuntimeException("Ray环境未安装，请先安装Ray环境");
            }
            
            // 4. 启动RayLLM服务
            Map<String, Object> launchResult = startRayLLMProcess(
                    modelName, modelPath, maxConcurrency, modelEngine);
            
            if ("SUCCESS".equals(launchResult.get("status"))) {
                result.put("status", "SUCCESS");
                result.put("serviceEndpoint", launchResult.get("serviceEndpoint"));
                result.put("serviceStatus", "RUNNING");
                result.put("maxConcurrency", maxConcurrency);
                result.put("modelEngine", modelEngine);
                result.put("gpuMemoryUsage", launchResult.get("gpuMemoryUsage"));
                result.put("modelPath", modelPath);
                
                long duration = System.currentTimeMillis() - startTime;
                result.put("launchTime", duration / 1000.0 + " seconds");
                
                log.info("RayLLM服务启动成功 - 模型: {}, 端点: {}, 耗时: {}ms", 
                        modelName, launchResult.get("serviceEndpoint"), duration);
            } else {
                result.put("status", "FAILED");
                result.put("error", launchResult.get("error"));
            }
            
        } catch (Exception e) {
            log.error("RayLLM服务启动失败 - 模型: {}", modelName, e);
            result.put("status", "FAILED");
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 检查模型文件是否存在
     */
    private boolean checkModelFileExists(String modelPath) {
        try {
            // 这里可以检查本地文件系统或对象存储
            java.io.File file = new java.io.File(modelPath);
            if (file.exists()) {
                return true;
            }
            
            // 检查是否是目录（对于多文件模型）
            java.io.File dir = new java.io.File(modelPath);
            if (dir.isDirectory() && dir.list().length > 0) {
                return true;
            }
            
            // 可以添加其他检查逻辑，比如检查对象存储
            log.warn("模型文件/目录不存在: {}", modelPath);
            return false;
            
        } catch (Exception e) {
            log.error("检查模型文件时出错: {}", modelPath, e);
            return false;
        }
    }
    
    /**
     * 连接到Ray集群
     */
    private void connectToRayCluster(String clusterAddress) {
        try {
            log.info("连接到Ray集群: {}", clusterAddress);
            
            // 使用ray.env配置
            String rayEnvPath = resourceUtil.getRayEnvPath();
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("bash", "-c", 
                    "source " + rayEnvPath + " && " +
                    "ray start --address=" + clusterAddress + " --block");
            
            Process process = pb.start();
            
            // 等待连接完成（最多等待30秒）
            boolean finished = process.waitFor(30, java.util.concurrent.TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("连接Ray集群超时");
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new RuntimeException("连接Ray集群失败，退出码: " + exitCode);
            }
            
            log.info("成功连接到Ray集群: {}", clusterAddress);
            
        } catch (Exception e) {
            throw new RuntimeException("连接Ray集群失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 启动RayLLM进程
     */
    private Map<String, Object> startRayLLMProcess(String modelName, String modelPath, 
                                                 Integer maxConcurrency, String modelEngine) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("启动RayLLM进程 - 模型: {}, 引擎: {}", modelName, modelEngine);
            
            // 构建RayLLM启动命令
            String command = buildRayLLMCommand(modelName, modelPath, maxConcurrency, modelEngine);
            
            // 打印完整的启动命令
            log.info("RayLLM启动命令: {}", command);
            
            // 启动进程（前台运行，但异步处理）
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("bash", "-c", command);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // 异步读取输出
            CompletableFuture<StringBuilder> outputFuture = CompletableFuture.supplyAsync(() -> {
                StringBuilder output = new StringBuilder();
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                        log.info("RayLLM启动输出: {}", line);
                    }
                } catch (Exception e) {
                    log.error("读取RayLLM输出失败", e);
                }
                return output;
            });
            
            // 等待一段时间让服务启动
            Thread.sleep(15000); // 等待15秒
            
            // 检查进程是否还在运行
            boolean isRunning = process.isAlive();
            log.info("RayLLM进程运行状态: {}", isRunning);
            
            // 检查端口是否被监听
            boolean portListening = checkPortListening(8000);
            log.info("端口8000监听状态: {}", portListening);
            
            // 根据进程状态和端口状态判断启动结果
            if (isRunning && portListening) {
                result.put("status", "SUCCESS");
                result.put("serviceEndpoint", "http://127.0.0.1:8000");
                result.put("gpuMemoryUsage", "未知");
                log.info("RayLLM进程启动成功 - 模型: {}", modelName);
                    } else {
                result.put("status", "FAILED");
                result.put("error", "RayLLM启动失败 - 进程运行: " + isRunning + ", 端口监听: " + portListening);
                log.error("RayLLM进程启动失败 - 模型: {}", modelName);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("启动RayLLM进程失败 - 模型: {}", modelName, e);
            result.put("status", "FAILED");
            result.put("error", "启动RayLLM进程失败: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * 检查端口是否被监听
     */
    private boolean checkPortListening(int port) {
        try {
            // 使用netstat命令检查端口
            ProcessBuilder pb = new ProcessBuilder("lsof", "-i", ":" + port);
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            log.warn("检查端口{}监听状态失败", port, e);
            return false;
        }
    }
    
    /**
     * 构建RayLLM启动命令
     */
    private String buildRayLLMCommand(String modelName, String modelPath,
                                    Integer maxConcurrency, String modelEngine) {
        StringBuilder cmd = new StringBuilder();
        
        // 加载环境变量
        String rayEnvPath = resourceUtil.getRayEnvPath();
        cmd.append("source ").append(rayEnvPath).append(" && ");
        
        // 根据模型引擎构建命令
        switch (modelEngine.toLowerCase()) {
            case "vllm":
                // 只添加必要的环境变量
                cmd.append("export VLLM_CPU_KVCACHE_SPACE=8 && ");
                cmd.append("${CONDA_HOME}/envs/${CONDA_ENV_NAME}/bin/python -m vllm.entrypoints.openai.api_server ")
                   .append("--model ").append(modelPath).append(" ")
                   .append("--host 0.0.0.0 ")
                   .append("--port 8000");
                break;
                
            case "tgi":
                cmd.append("${CONDA_HOME}/envs/${CONDA_ENV_NAME}/bin/python -m text_generation_server.cli.serve ")
                   .append("--model-id ").append(modelPath).append(" ")
                   .append("--hostname 0.0.0.0 ")
                   .append("--port 8000 ");
                if (maxConcurrency != null) {
                    cmd.append("--max-concurrent-requests ").append(maxConcurrency).append(" ");
                }
                break;
                
            default:
                // 默认使用简单的Ray Serve
                cmd.append("${CONDA_HOME}/envs/${CONDA_ENV_NAME}/bin/ray serve start ")
                   .append("--model-path ").append(modelPath).append(" ")
                   .append("--model-name ").append(modelName);
                break;
        }
        
        return cmd.toString();
    }
    
    /**
     * 从输出中提取服务端点
     */
    private String extractServiceEndpoint(String output) {
        // 尝试从输出中提取实际的服务端点
        if (output.contains("localhost:8000")) {
            return "http://localhost:8000";
        }
        if (output.contains("0.0.0.0:8000")) {
            return "http://localhost:8000";
        }
        // 默认端点
        return "http://localhost:8000";
    }
    
    /**
     * 添加动态环境变量到命令中
     */
    private void addDynamicEnvironmentVariables(StringBuilder cmd) {
        boolean hasGpu = detectGpuAvailability();
        
        if (!hasGpu) {
            cmd.append("export VLLM_SKIP_CPU_INIT=1 && ");
            cmd.append("export TORCH_USE_CUDA_DSA=0 && ");
            cmd.append("export VLLM_USE_CPU_ONLY=1 && ");
            cmd.append("export CUDA_VISIBLE_DEVICES=\"\" && ");
            cmd.append("export NVIDIA_VISIBLE_DEVICES=\"\" && ");
            cmd.append("export HIP_VISIBLE_DEVICES=\"\" && ");
            cmd.append("export XFORMERS_DISABLED=1 && ");  // 禁用xformers
            cmd.append("export VLLM_USE_TRITON=0 && ");  // 禁用Triton
            cmd.append("export DISABLE_XFORMERS=1 && ");  // 另一种禁用xformers的方法
            cmd.append("export VLLM_CPU_ONLY=1 && ");  // 强制CPU模式
            cmd.append("export PYTORCH_ENABLE_MPS_FALLBACK=1 && ");  // 禁用MPS
            cmd.append("export VLLM_CPU_KVCACHE_SPACE=8 && ");  // 设置CPU KV缓存空间为8GB
            log.info("检测到无GPU环境，已添加CPU优化环境变量到启动命令");
        } else {
            log.info("检测到GPU环境，使用默认GPU加速模式");
        }
    }
    
    /**
     * 根据GPU检测结果添加设备参数
     */
    private void addDeviceParameter(StringBuilder cmd) {
        boolean hasGpu = detectGpuAvailability();
        
        if (!hasGpu) {
            cmd.append("--device cpu ");
            log.info("添加CPU设备参数到启动命令");
        } else {
            log.info("使用默认GPU设备参数");
        }
    }
    
    /**
     * 检测GPU可用性
     */
    private boolean detectGpuAvailability() {
        // 方法1: 检查CUDA环境变量
        String cudaVisibleDevices = System.getenv("CUDA_VISIBLE_DEVICES");
        if (cudaVisibleDevices != null && cudaVisibleDevices.trim().isEmpty()) {
            return false; // CUDA被显式禁用
        }
        
        // 方法2: 检查nvidia-smi命令（仅NVIDIA GPU）
        try {
            ProcessBuilder pb = new ProcessBuilder("nvidia-smi", "--query-gpu=name", "--format=csv,noheader");
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream()))) {
                    String line = reader.readLine();
                    if (line != null && !line.trim().isEmpty()) {
                        log.info("检测到NVIDIA GPU: {}", line.trim());
                        return true; // 找到NVIDIA GPU
                    }
                }
            }
        } catch (Exception e) {
            // nvidia-smi不可用，继续其他检测方法
        }
        
        // 方法3: macOS默认使用CPU模式（vLLM在macOS上通常需要CPU模式）
        if (System.getProperty("os.name", "").toLowerCase().contains("mac")) {
            log.info("检测到macOS系统，vLLM使用CPU模式以确保兼容性");
            return false; // macOS强制使用CPU模式
        }
        
        // 默认假设无GPU（保守策略）
        return false;
    }
    
    /**
     * 从输出中提取GPU内存使用情况
     */
    private String extractGpuMemoryUsage(String output) {
        // 尝试从输出中提取GPU内存使用情况
        // 这里可以添加更复杂的解析逻辑
        return "未知";
    }

    /**
     * 获取当前节点在DiscoveryClient中的实例ID
     */
    private String getCurrentNodeId() {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(applicationName);
            String currentHost = getLocalIpAddress();
            int currentPort = getActualPort();

            // 匹配当前主机和端口的实例
            for (ServiceInstance instance : instances) {
                if (instance.getHost().equals(currentHost) && instance.getPort() == currentPort) {
                    return instance.getInstanceId();
                }
            }

            // 如果没有找到匹配的实例，返回默认格式
            return currentHost + ":" + currentPort + "#" + System.currentTimeMillis() % 1000;

        } catch (Exception e) {
            log.warn("无法从DiscoveryClient获取当前节点ID", e);
            // 降级处理：使用本机IP作为节点ID
            return getLocalIpAddress();
        }
    }

    /**
     * 获取实际运行端口
     */
    private int getActualPort() {
        try {
            return webServerAppCtxt.getWebServer().getPort();
        } catch (Exception e) {
            log.warn("无法获取实际端口，使用默认值", e);
            return 8080; // 默认端口
        }
    }

    /**
     * 获取本机真实IP地址（非127.0.0.1）
     */
    private String getLocalIpAddress() {
        try {
            // 获取所有网络接口
            java.util.Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface networkInterface = interfaces.nextElement();

                // 跳过回环接口和未启用的接口
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                java.util.Enumeration<java.net.InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    java.net.InetAddress address = addresses.nextElement();

                    // 只选择IPv4地址，且不是回环地址，不是链路本地地址
                    if (address instanceof java.net.Inet4Address &&
                        !address.isLoopbackAddress() &&
                        !address.isLinkLocalAddress() &&
                        !address.isSiteLocalAddress()) {
                        return address.getHostAddress();
                    }
                }
            }

            // 如果没有找到公网IP，则查找内网IP
            interfaces = java.net.NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface networkInterface = interfaces.nextElement();

                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                java.util.Enumeration<java.net.InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    java.net.InetAddress address = addresses.nextElement();

                    // 选择IPv4内网地址
                    if (address instanceof java.net.Inet4Address &&
                        !address.isLoopbackAddress() &&
                        !address.isLinkLocalAddress()) {
                        return address.getHostAddress();
                    }
                }
            }

            // 最后的降级处理
            java.net.InetAddress inetAddress = java.net.InetAddress.getLocalHost();
            String hostAddress = inetAddress.getHostAddress();

            // 如果仍然是127.0.0.1，尝试其他方法
            if ("127.0.0.1".equals(hostAddress)) {
                // 尝试连接外部地址来获取本机IP
                try (java.net.Socket socket = new java.net.Socket()) {
                    socket.connect(new java.net.InetSocketAddress("8.8.8.8", 80));
                    return socket.getLocalAddress().getHostAddress();
                } catch (Exception ex) {
                    log.warn("无法通过连接外部地址获取本机IP", ex);
                }
            }

            return hostAddress;

        } catch (Exception e) {
            log.warn("获取本机IP地址失败", e);
            return "unknown";
        }
    }

    /**
     * 检查操作系统信息
     */
    private String checkOsInfo() {
        try {
            String osName = System.getProperty("os.name");
            String osVersion = System.getProperty("os.version");
            String osArch = System.getProperty("os.arch");
            return String.format("%s %s (%s)", osName, osVersion, osArch);
        } catch (Exception e) {
            log.warn("获取操作系统信息失败", e);
            return "Unknown OS";
        }
    }

    /**
     * 使用ray-env-check.sh脚本检查Python和Ray环境
     */
    private void checkRayEnvironmentWithScript(NodeEnvironmentInfo nodeInfo) {
        try {
            log.info("使用ray-env-check.sh脚本检查Ray环境");

            // 获取脚本路径
            String scriptPath = getScriptPath("ray-env-check.sh");

            // 执行ray-env-check.sh脚本获取JSON输出 - 使用新的方法传递环境变量
            ProcessBuilder processBuilder = resourceUtil.createScriptProcessBuilder(scriptPath, "json");

            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();

            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            String scriptOutput = output.toString().trim();

            log.info("ray-env-check.sh脚本输出: {}", scriptOutput);

            if (exitCode == 0 || exitCode == 1) { // 0=准备就绪, 1=未准备就绪但有输出
                // 解析JSON输出
                parseRayEnvironmentJson(scriptOutput, nodeInfo);
                } else {
                log.error("ray-env-check.sh脚本执行失败，退出码: {}", exitCode);
                setDefaultRayEnvironmentInfo(nodeInfo, "脚本执行失败: 退出码 " + exitCode);
            }

        } catch (Exception e) {
            log.error("使用ray-env-check.sh检查Ray环境失败", e);
            setDefaultRayEnvironmentInfo(nodeInfo, "脚本执行异常: " + e.getMessage());
        }
    }

    /**
     * 解析ray-env-check.sh脚本的JSON输出
     */
    private void parseRayEnvironmentJson(String jsonOutput, NodeEnvironmentInfo nodeInfo) {
        try {
            // 查找JSON开始位置（跳过可能的非JSON前缀）
            int jsonStart = jsonOutput.indexOf("{");
            if (jsonStart == -1) {
                log.error("未找到有效的JSON输出: {}", jsonOutput);
                setDefaultRayEnvironmentInfo(nodeInfo, "JSON格式错误");
                return;
            }

            String jsonPart = jsonOutput.substring(jsonStart);
            log.debug("解析JSON: {}", jsonPart);

            // 简单的JSON解析（避免引入复杂依赖）
            boolean condaAvailable = jsonPart.contains("\"available\": true");
            boolean rayEnvExists = jsonPart.contains("\"exists\": true");
            boolean rayInstalled = jsonPart.contains("\"installed\": true");
            boolean rayReady = jsonPart.contains("\"rayReady\": true");
            boolean modelEnginesInstalled = jsonPart.contains("\"modelEnginesInstalled\": true");

            // 提取环境名称
            String envName = extractJsonValue(jsonPart, "name");
            String condaPath = extractJsonValue(jsonPart, "path");

            // 设置Python环境信息
            if (condaAvailable && rayEnvExists) {
                nodeInfo.setPythonInstalled(true);
                nodeInfo.setPythonVersion("Python 3.12 (conda: " + envName + ")");
            } else if (condaAvailable) {
                nodeInfo.setPythonInstalled(true);
                nodeInfo.setPythonVersion("Conda可用，但Ray环境不存在");
                } else {
                nodeInfo.setPythonInstalled(false);
                nodeInfo.setPythonVersion("Conda未安装");
            }

            // 设置Ray环境信息
            if (rayInstalled && rayReady) {
                nodeInfo.setRayInstalled(true);
                nodeInfo.setRayVersion("Ray 2.8.0 (环境: " + envName + ")");
            } else if (rayEnvExists) {
                nodeInfo.setRayInstalled(false);
                nodeInfo.setRayVersion("Ray环境存在但Ray未安装");
            } else {
                nodeInfo.setRayInstalled(false);
                nodeInfo.setRayVersion("Ray环境不存在");
            }
            
            // 设置模型引擎依赖信息
            if (modelEnginesInstalled) {
                nodeInfo.setModelEnginesInstalled(true);
            } else {
                nodeInfo.setModelEnginesInstalled(false);
            }
            
            log.info("Ray环境检查完成 - Python: {}, Ray: {}, ModelEngines: {}", 
                    nodeInfo.getPythonInstalled(), nodeInfo.getRayInstalled(), nodeInfo.getModelEnginesInstalled());
            
        } catch (Exception e) {
            log.error("解析Ray环境JSON失败", e);
            setDefaultRayEnvironmentInfo(nodeInfo, "JSON解析错误: " + e.getMessage());
        }
    }
    
    /**
     * 从JSON字符串中提取指定键的值
     */
    private String extractJsonValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\":\\s*\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
            log.debug("提取JSON值失败: key={}, error={}", key, e.getMessage());
        }
        return "unknown";
    }
    
    /**
     * 设置默认的Ray环境信息（当检查失败时）
     */
    private void setDefaultRayEnvironmentInfo(NodeEnvironmentInfo nodeInfo, String errorMessage) {
        nodeInfo.setPythonInstalled(false);
        nodeInfo.setPythonVersion("检查失败: " + errorMessage);
        nodeInfo.setRayInstalled(false);
        nodeInfo.setRayVersion("检查失败: " + errorMessage);
        nodeInfo.setModelEnginesInstalled(false);
    }
    
    /**
     * 获取脚本文件路径
     */
    private String getScriptPath(String scriptName) {
        // 从classpath中获取脚本路径
        return resourceUtil.getScriptPath(scriptName);
    }
    
    /**
     * 获取Ray命令的完整路径（从conda环境中）
     */
    private String getRayCommandPath() {
        try {
            // 从ray.env配置文件中读取配置
            Map<String, String> envConfig = loadRayEnvConfig();
            String condaEnvName = envConfig.getOrDefault("CONDA_ENV_NAME", "ray-env");
            String condaHome = envConfig.getOrDefault("CONDA_HOME", System.getProperty("user.home") + "/miniconda3");
            
            // Ray命令在conda环境中的路径
            return String.format("%s/envs/%s/bin/ray", condaHome, condaEnvName);
        } catch (Exception e) {
            log.warn("获取Ray命令路径失败，使用默认路径", e);
            return "ray"; // 降级到系统PATH
        }
    }
    
    /**
     * 获取conda环境激活命令
     */
    private String getCondaActivateCommand() {
        try {
            Map<String, String> envConfig = loadRayEnvConfig();
            String condaHome = envConfig.getOrDefault("CONDA_HOME", System.getProperty("user.home") + "/miniconda3");
            String condaEnvName = envConfig.getOrDefault("CONDA_ENV_NAME", "ray-env");
            
            return String.format("%s/etc/profile.d/conda.sh && conda activate %s", condaHome, condaEnvName);
        } catch (Exception e) {
            log.warn("获取conda激活命令失败，使用默认命令", e);
            return "echo 'conda not available'";
        }
    }
    
    /**
     * 从ray.env配置文件中获取Ray版本
     */
    private String getRayVersionFromEnv() {
        try {
            Map<String, String> envConfig = loadRayEnvConfig();
            return envConfig.getOrDefault("RAY_VERSION", "2.8.0");
        } catch (Exception e) {
            log.warn("从配置文件获取Ray版本失败，使用默认版本", e);
            return "2.8.0";
        }
    }
    
    /**
     * 从ray status输出中解析节点数量
     */
    private int parseNodeCountFromStatus(String statusOutput) {
        try {
            // 简单的节点数解析逻辑
            if (statusOutput.contains("Total:")) {
                // 尝试解析 "Total: X nodes" 格式
                String[] lines = statusOutput.split("\n");
                for (String line : lines) {
                    if (line.contains("Total:") && line.contains("nodes")) {
                        String[] parts = line.split("\\s+");
                        for (int i = 0; i < parts.length - 1; i++) {
                            if ("Total:".equals(parts[i]) && parts[i + 1].matches("\\d+")) {
                                return Integer.parseInt(parts[i + 1]);
                            }
                        }
                    }
                }
            }
            
            // 如果无法解析，返回默认值
            return 1;
        } catch (Exception e) {
            log.warn("解析Ray集群节点数失败", e);
            return 1;
        }
    }
    
    /**
     * 加载ray.env配置文件
     */
    private Map<String, String> loadRayEnvConfig() {
        // 直接使用resourceUtil的方法
        return resourceUtil.loadRayEnvConfig();
    }
    

    
    /**
     * 检查CPU信息
     */
    private CpuInfo checkCpuInfo() {
        CpuInfo cpuInfo = new CpuInfo();
        
        try {
            // 获取CPU核心数
            int cores = Runtime.getRuntime().availableProcessors();
            cpuInfo.setLogicalCores(cores);
            cpuInfo.setPhysicalCores(cores); // 简化处理，实际可能需要更复杂的逻辑
            
            // 获取系统负载
            java.lang.management.OperatingSystemMXBean osBean = 
                java.lang.management.ManagementFactory.getOperatingSystemMXBean();
            double loadAverage = osBean.getSystemLoadAverage();
            cpuInfo.setLoadAverage1min(loadAverage >= 0 ? loadAverage : 0.0);
            
            // 设置CPU架构
            cpuInfo.setArchitecture(System.getProperty("os.arch"));
            
            // 获取CPU使用率（基于系统负载估算）
            if (loadAverage >= 0) {
                double cpuUsage = Math.min(100.0, (loadAverage / cores) * 100);
                cpuInfo.setCpuUsage(cpuUsage);
                } else {
                cpuInfo.setCpuUsage(0.0);
            }
            
        } catch (Exception e) {
            log.warn("获取CPU信息失败", e);
            cpuInfo.setLogicalCores(1);
            cpuInfo.setPhysicalCores(1);
            cpuInfo.setLoadAverage1min(0.0);
            cpuInfo.setArchitecture("unknown");
            cpuInfo.setCpuUsage(0.0);
        }
        
        return cpuInfo;
    }
    
    /**
     * 检查内存信息
     */
    private MemoryInfo checkMemoryInfo() {
        MemoryInfo memoryInfo = new MemoryInfo();
        
        try {
            java.lang.management.MemoryMXBean memoryBean = 
                java.lang.management.ManagementFactory.getMemoryMXBean();
            
            java.lang.management.MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
            java.lang.management.MemoryUsage nonHeapMemory = memoryBean.getNonHeapMemoryUsage();
            
            // 获取JVM内存信息（注意：这不是系统总内存）
            long totalMemory = heapMemory.getMax() + nonHeapMemory.getMax();
            long usedMemory = heapMemory.getUsed() + nonHeapMemory.getUsed();
            
            memoryInfo.setTotalMemoryMB(totalMemory / (1024 * 1024));
            memoryInfo.setUsedMemoryMB(usedMemory / (1024 * 1024));
            memoryInfo.setFreeMemoryMB((totalMemory - usedMemory) / (1024 * 1024));
            
            double usage = totalMemory > 0 ? (double) usedMemory / totalMemory * 100 : 0.0;
            memoryInfo.setMemoryUsage(usage);
            
            // 设置交换分区信息（简化处理）
            memoryInfo.setTotalSwapMB(0L);
            memoryInfo.setUsedSwapMB(0L);
            memoryInfo.setFreeSwapMB(0L);
            memoryInfo.setSwapUsage(0.0);
            
        } catch (Exception e) {
            log.warn("获取内存信息失败", e);
            memoryInfo.setTotalMemoryMB(0L);
            memoryInfo.setUsedMemoryMB(0L);
            memoryInfo.setFreeMemoryMB(0L);
            memoryInfo.setMemoryUsage(0.0);
            memoryInfo.setTotalSwapMB(0L);
            memoryInfo.setUsedSwapMB(0L);
            memoryInfo.setFreeSwapMB(0L);
            memoryInfo.setSwapUsage(0.0);
        }
        
        return memoryInfo;
    }
    
    /**
     * 检查GPU信息
     */
    private List<GpuInfo> checkGpuInfo() {
        List<GpuInfo> gpuInfos = new ArrayList<>();
        
        try {
            // 尝试执行nvidia-smi命令获取GPU信息
            Process process = Runtime.getRuntime().exec(
                "nvidia-smi --query-gpu=index,name,memory.total,memory.used,memory.free --format=csv,noheader,nounits");
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    GpuInfo gpuInfo = new GpuInfo();
                    gpuInfo.setIndex(Integer.parseInt(parts[0].trim()));
                    gpuInfo.setName(parts[1].trim());
                    gpuInfo.setTotalMemoryMB(Long.parseLong(parts[2].trim()));
                    gpuInfo.setUsedMemoryMB(Long.parseLong(parts[3].trim()));
                    gpuInfo.setFreeMemoryMB(Long.parseLong(parts[4].trim()));
                    
                    double memoryUtilization = gpuInfo.getTotalMemoryMB() > 0 ? 
                        (double) gpuInfo.getUsedMemoryMB() / gpuInfo.getTotalMemoryMB() * 100 : 0.0;
                    gpuInfo.setMemoryUtilization(memoryUtilization);
                    
                    // 设置其他GPU信息的默认值
                    gpuInfo.setGpuUtilization(0.0);
                    gpuInfo.setStatus("ONLINE");
                    
                    gpuInfos.add(gpuInfo);
                }
            }
            
            reader.close();
            process.waitFor();
            
        } catch (Exception e) {
            log.warn("无法获取GPU信息，可能没有安装NVIDIA驱动或GPU");
            // 返回空列表表示没有GPU
        }
        
        return gpuInfos;
    }
    
    /**
     * 检查磁盘信息
     */
    private DiskInfo checkDiskInfo() {
        DiskInfo diskInfo = new DiskInfo();
        
        try {
            java.io.File root = new java.io.File("/");
            
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            
            diskInfo.setTotalDiskGB(totalSpace / (1024 * 1024 * 1024));
            diskInfo.setFreeDiskGB(freeSpace / (1024 * 1024 * 1024));
            diskInfo.setUsedDiskGB(usedSpace / (1024 * 1024 * 1024));
            
            double usage = totalSpace > 0 ? (double) usedSpace / totalSpace * 100 : 0.0;
            diskInfo.setDiskUsage(usage);
            
            // 设置其他磁盘信息的默认值
            diskInfo.setDiskType("SSD"); // 简化处理
            diskInfo.setFileSystemType("ext4"); // 简化处理
            diskInfo.setReadSpeedMBps(0.0);
            diskInfo.setWriteSpeedMBps(0.0);
            
        } catch (Exception e) {
            log.warn("获取磁盘信息失败", e);
            diskInfo.setTotalDiskGB(0L);
            diskInfo.setFreeDiskGB(0L);
            diskInfo.setUsedDiskGB(0L);
            diskInfo.setDiskUsage(0.0);
            diskInfo.setDiskType("unknown");
            diskInfo.setFileSystemType("unknown");
            diskInfo.setReadSpeedMBps(0.0);
            diskInfo.setWriteSpeedMBps(0.0);
        }
        
        return diskInfo;
    }
    
    /**
     * 检查网络信息
     */
    private NetworkInfo checkNetworkInfo() {
        NetworkInfo networkInfo = new NetworkInfo();
        
        try {
            java.net.InetAddress localHost = java.net.InetAddress.getLocalHost();
            networkInfo.setHostname(localHost.getHostName());
            networkInfo.setIpAddress(localHost.getHostAddress());
            
            // 检查网络连通性（ping Google DNS）
            java.net.InetAddress googleDns = java.net.InetAddress.getByName("8.8.8.8");
            boolean reachable = googleDns.isReachable(5000); // 5秒超时
            networkInfo.setNetworkStatus(reachable ? "CONNECTED" : "DISCONNECTED");
            
            // 设置其他网络信息的默认值
            networkInfo.setPrimaryInterface("eth0");
            networkInfo.setSubnetMask("255.255.255.0");
            networkInfo.setGateway("192.168.1.1");
            networkInfo.setDnsServers("8.8.8.8,8.8.4.4");
            networkInfo.setBandwidthMbps(1000.0);
            networkInfo.setLatencyMs(reachable ? 10.0 : -1.0);
            networkInfo.setUploadSpeedMBps(0.0);
            networkInfo.setDownloadSpeedMBps(0.0);
            
        } catch (Exception e) {
            log.warn("获取网络信息失败", e);
            networkInfo.setHostname("unknown");
            networkInfo.setIpAddress("127.0.0.1");
            networkInfo.setNetworkStatus("ERROR");
            networkInfo.setPrimaryInterface("unknown");
            networkInfo.setSubnetMask("unknown");
            networkInfo.setGateway("unknown");
            networkInfo.setDnsServers("unknown");
            networkInfo.setBandwidthMbps(0.0);
            networkInfo.setLatencyMs(-1.0);
            networkInfo.setUploadSpeedMBps(0.0);
            networkInfo.setDownloadSpeedMBps(0.0);
        }
        
        return networkInfo;
    }
    
    @Override
    public String installMiniconda() {
        log.info("开始执行Miniconda安装");
        
        try {
            // 获取安装脚本路径
            String scriptPath = getMinicondaInstallScriptPath();
            
            // 执行安装脚本（自动化模式） - 使用新的方法传递环境变量
            ProcessBuilder processBuilder = resourceUtil.createScriptProcessBuilder(scriptPath);
            
            // 设置环境变量启用自动化模式
            processBuilder.environment().put("AUTO_INSTALL", "true");
            processBuilder.environment().put("CONDA_ALWAYS_YES", "true");
            processBuilder.environment().put("CI", "true");
            
            Process process = processBuilder.start();
            
            // 读取安装输出
            StringBuilder output = new StringBuilder();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    log.info("Miniconda安装: {}", line);
                }
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                log.info("Miniconda安装成功");
                return "Miniconda安装成功";
            } else {
                log.error("Miniconda安装失败，退出码: {}", exitCode);
                return "Miniconda安装失败，退出码: " + exitCode;
            }
            
        } catch (Exception e) {
            log.error("Miniconda安装异常", e);
            return "Miniconda安装异常: " + e.getMessage();
        }
    }
    
    @Override
    public String installRay() {
        log.info("开始执行Ray安装");
        
        try {
            // 获取Ray安装脚本路径
            String scriptPath = getRayInstallScriptPath();
            
            // 执行安装脚本（自动化模式） - 使用新的方法传递环境变量
            ProcessBuilder processBuilder = resourceUtil.createScriptProcessBuilder(scriptPath);
            
            // 设置环境变量启用自动化模式
            processBuilder.environment().put("AUTO_INSTALL", "true");
            processBuilder.environment().put("CONDA_ALWAYS_YES", "true");
            processBuilder.environment().put("CI", "true");
            
            Process process = processBuilder.start();
            
            // 读取安装输出
            StringBuilder output = new StringBuilder();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    log.info("Ray安装: {}", line);
                }
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                log.info("Ray安装成功");
                return "Ray安装成功";
                    } else {
                log.error("Ray安装失败，退出码: {}", exitCode);
                return "Ray安装失败，退出码: " + exitCode;
            }
            
        } catch (Exception e) {
            log.error("Ray安装异常", e);
            return "Ray安装异常: " + e.getMessage();
        }
    }
    
    /**
     * 获取Miniconda安装脚本路径
     */
    private String getMinicondaInstallScriptPath() {
        // 从classpath中获取脚本路径
        return resourceUtil.getScriptPath("install-miniconda.sh");
    }
    
    /**
     * 获取Ray安装脚本路径（实际上是验证脚本，它包含Ray安装逻辑）
     */
    private String getRayInstallScriptPath() {
        // 从classpath中获取脚本路径
        return resourceUtil.getScriptPath("setup-ray-env.sh");
    }
    
    @Override
    public InstallEnvironmentResult installEnvironment(InstallEnvironmentRequest request) {
        log.info("开始执行环境安装 - Miniconda: {}, Ray: {}, ModelEngines: {}", 
                request.isInstallMiniconda(), request.isInstallRay(), request.isInstallModelEngines());
        
        boolean minicondaInstalled = false;
        boolean rayInstalled = false;
        boolean modelEnginesInstalled = false;
        StringBuilder details = new StringBuilder();
        
        try {
            // 安装Miniconda
            if (request.isInstallMiniconda()) {
                log.info("安装Miniconda...");
                String minicondaResult = installMiniconda();
                if (minicondaResult.contains("成功")) {
                    minicondaInstalled = true;
                    details.append("Miniconda安装成功; ");
                } else {
                    details.append("Miniconda安装失败: ").append(minicondaResult).append("; ");
                    return InstallEnvironmentResult.failure("Miniconda安装失败: " + minicondaResult);
                }
            }
            
            // 安装Ray
            if (request.isInstallRay()) {
                log.info("安装Ray...");
                String rayResult = installRay();
                if (rayResult.contains("成功")) {
                    rayInstalled = true;
                    details.append("Ray安装成功; ");
                } else {
                    details.append("Ray安装失败: ").append(rayResult).append("; ");
                    return InstallEnvironmentResult.failure("Ray安装失败: " + rayResult);
                }
            }
            
            // 安装模型引擎依赖
            if (request.isInstallModelEngines()) {
                log.info("安装模型引擎依赖...");
                String modelEnginesResult = installModelEngineDependencies();
                if (modelEnginesResult.contains("成功")) {
                    modelEnginesInstalled = true;
                    details.append("模型引擎依赖安装成功; ");
                } else {
                    details.append("模型引擎依赖安装失败: ").append(modelEnginesResult).append("; ");
                    return InstallEnvironmentResult.failure("模型引擎依赖安装失败: " + modelEnginesResult);
                }
            }
            
            log.info("环境安装完成 - Miniconda: {}, Ray: {}, ModelEngines: {}", 
                    minicondaInstalled, rayInstalled, modelEnginesInstalled);
            return InstallEnvironmentResult.success(minicondaInstalled, rayInstalled, modelEnginesInstalled, details.toString());
            
        } catch (Exception e) {
            log.error("环境安装异常", e);
            return InstallEnvironmentResult.failure("环境安装异常: " + e.getMessage());
        }
    }
    
    @Override
    public RayDeploymentResponse deployModelToRayCluster(RayDeploymentRequest request) {
        // 1. 验证请求参数
        if (request == null) {
            throw new IllegalArgumentException("部署请求不能为空");
        }
        
        log.info("开始验证Ray模型部署请求 - ModelId: {}, Source: {}, NodeIds: {}", 
                request.getModelId(), request.getModelSource(), 
                request.getNodeIds() != null ? request.getNodeIds() : "null");
        
        // 2. 验证节点ID列表
        if (request.getNodeIds() == null || request.getNodeIds().isEmpty()) {
            String errorMsg = String.format("节点ID列表不能为空 - 当前值: %s", 
                request.getNodeIds() != null ? "空列表" : "null");
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        
        // 3. 验证节点ID列表中的每个元素
        for (int i = 0; i < request.getNodeIds().size(); i++) {
            String nodeId = request.getNodeIds().get(i);
            if (nodeId == null || nodeId.trim().isEmpty()) {
                String errorMsg = String.format("节点ID列表中第%d个元素无效 - 值: %s", i + 1, nodeId);
                log.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }
        }
        
        log.info("开始Ray模型部署流程 - ModelId: {}, Source: {}, Nodes: {}", 
                request.getModelId(), request.getModelSource(), request.getNodeIds());
        
        try {
            // 2. 执行完整的Ray部署流程
            return executeRayDeploymentWorkflow(request);
            
        } catch (Exception e) {
            log.error("Ray模型部署失败", e);
            
            // 创建失败响应
            RayDeploymentResponse response = new RayDeploymentResponse();
            response.setStatus("FAILED");
            response.setError(e.getMessage());
            response.setTimestamp(LocalDateTime.now());
            
            throw new RuntimeException("Ray模型部署失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 执行完整的Ray部署工作流程
     */
    private RayDeploymentResponse executeRayDeploymentWorkflow(RayDeploymentRequest request) {
        log.info("委托给DeploymentRuntime执行Ray部署工作流程");
        return deploymentRuntime.executeRayDeploymentWorkflow(request);
    }
    
    @Override
    public Map<String, Object> createRayCluster(Map<String, Object> clusterConfig) {
        try {
            log.info("创建Ray集群 - 配置: {}", clusterConfig);
            
            // 获取集群配置
            Long modelId = (Long) clusterConfig.get("modelId");
            String modelName = (String) clusterConfig.get("modelName");
            String masterAddress = (String) clusterConfig.getOrDefault("clusterAddress", "127.0.0.1:6379");
            
            // 实际创建集群的逻辑（这里只是模拟）
            Map<String, Object> result = new HashMap<>();
            result.put("status", "SUCCESS");
            result.put("message", "Ray集群创建成功");
            result.put("clusterId", "ray-cluster-" + UUID.randomUUID().toString().substring(0, 8));
            result.put("clusterAddress", masterAddress);
            result.put("isMaster", true);
            result.put("startTime", LocalDateTime.now());
            
            log.info("Ray集群创建成功 - 集群地址: {}", masterAddress);
            return result;
        } catch (Exception e) {
            log.error("Ray集群创建失败", e);
            throw new RuntimeException("Ray集群创建失败: " + e.getMessage(), e);
        }
    }
    

    
    private String getDefaultRayConfig() {
        // 返回默认的Ray配置
        return "{\"numWorkers\": 2, \"resources\": {\"CPU\": 4, \"GPU\": 1}}";
    }
    
    
    private Long getCurrentUserId() {
        // TODO: 实现获取当前用户ID的逻辑
        return 1L; // 示例值
    }
    
    /**
     * 安装模型引擎依赖（VLLM、TGI等）
     */
    private String installModelEngineDependencies() {
        try {
            log.info("开始安装模型引擎依赖...");
            
            // 加载ray.env配置
            Map<String, String> envConfig = loadRayEnvConfig();
            String condaHome = envConfig.getOrDefault("CONDA_HOME", System.getProperty("user.home") + "/miniconda3");
            String condaEnvName = envConfig.getOrDefault("CONDA_ENV_NAME", "ray-env");
            
            // 检测操作系统
            String os = System.getProperty("os.name").toLowerCase();
            boolean isMac = os.contains("mac");
            
            // 构建安装命令 - 主要安装VLLM和transformers，TGI可选安装
            StringBuilder cmd = new StringBuilder();
            String rayEnvPath = resourceUtil.getRayEnvPath();
            cmd.append("source ").append(rayEnvPath).append(" && ");
            cmd.append("source ").append(condaHome).append("/bin/activate ").append(condaEnvName).append(" && ");
            
            // Mac系统使用指定版本的vllm
            if (isMac) {
                cmd.append("pip install vllm==0.9.2 transformers accelerate");
                log.info("检测到Mac系统，安装vllm版本0.9.2");
            } else {
                cmd.append("pip install vllm transformers accelerate");
                log.info("非Mac系统，安装最新版本的vllm");
            }
            
            log.info("执行模型引擎依赖安装命令: {}", cmd.toString());
            
            // 执行安装命令
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("bash", "-c", cmd.toString());
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            
            // 读取输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    log.info("模型引擎安装输出: {}", line);
                }
            }
            
            // 等待进程完成
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                log.info("模型引擎依赖安装成功");
                return "成功";
            } else {
                log.error("模型引擎依赖安装失败，退出码: {}", exitCode);
                return "失败: 退出码 " + exitCode;
            }
            
        } catch (Exception e) {
            log.error("模型引擎依赖安装异常", e);
            return "失败: " + e.getMessage();
        }
    }

    @Override
    public ModelDeploymentInstance saveDeploymentInstance(ModelDeploymentInstance instance) {
        log.info("保存模型部署实例 - 模型ID: {}, 部署类型: {}", instance.getModelId(), instance.getDeploymentType());
        return deploymentInstanceRepository.save(instance);
    }

    @Override
    public ModelDeploymentInstance updateDeploymentInstanceStatus(Long instanceId, 
                                                                ModelDeploymentInstance.DeploymentStatus status, 
                                                                String errorMessage) {
        log.info("更新部署实例状态 - 实例ID: {}, 新状态: {}", instanceId, status);
        
        Optional<ModelDeploymentInstance> optionalInstance = deploymentInstanceRepository.findById(instanceId);
        if (optionalInstance.isPresent()) {
            ModelDeploymentInstance instance = optionalInstance.get();
            instance.setStatus(status);
            if (errorMessage != null) {
                instance.setErrorMessage(errorMessage);
            }
            instance.setLastHealthCheck(LocalDateTime.now());
            return deploymentInstanceRepository.save(instance);
        } else {
            throw new RuntimeException("部署实例不存在，ID: " + instanceId);
        }
    }

    @Override
    public List<ModelDeploymentInstance> getDeploymentInstancesByModelId(Long modelId) {
        log.info("查询模型部署实例 - 模型ID: {}", modelId);
        return deploymentInstanceRepository.findByModelId(modelId);
    }

    @Override
    public List<ModelDeploymentInstance> getRunningDeploymentInstances() {
        log.info("查询运行中的部署实例");
        return deploymentInstanceRepository.findRunningInstances();
    }
} 