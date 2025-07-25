package com.vediofun.model.service;

import com.vediofun.model.entity.Model;
import com.vediofun.model.entity.ModelDeploymentInstance;
import com.vediofun.model.dto.InstallEnvironmentRequest;
import com.vediofun.model.dto.InstallEnvironmentResult;
import com.vediofun.model.dto.RayDeploymentRequest;
import com.vediofun.model.dto.RayDeploymentResponse;
import com.vediofun.model.dto.NodeEnvironmentCheckRequest;
import com.vediofun.model.dto.NodeEnvironmentCheckResponse;
import com.vediofun.model.dto.NodeEnvironmentInfo;
import com.vediofun.model.dto.RayClusterInfo;
import com.vediofun.model.dto.RayClusterStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 模型服务接口，定义模型管理的基本操作
 */
public interface ModelService {

    /**
     * 获取模型列表
     *
     * @param page     当前页码
     * @param size     每页大小
     * @param name     模型名称（可选）
     * @param vendor   模型供应商（可选）
     * @param modelName 模型类型名称（可选）
     * @param sortBy   排序字段（可选）
     * @param sortOrder 排序方式（asc/desc）
     * @return 分页的模型列表
     */
    Page<Model> getModelList(int page, int size, String name, String vendor, String modelName, String sortBy, String sortOrder);

    /**
     * 根据ID获取模型
     *
     * @param id 模型的唯一标识
     * @return 模型对象
     */
    Model getModelById(Long id);

    /**
     * 创建模型
     *
     * @param model 要创建的模型对象
     * @return 创建后的模型对象
     */
    Model createModel(Model model);

    /**
     * 更新模型
     *
     * @param id    模型的唯一标识
     * @param model 更新后的模型对象
     * @return 更新后的模型对象
     */
    Model updateModel(Long id, Model model);

    /**
     * 删除模型
     *
     * @param id 模型的唯一标识
     */
    void deleteModel(Long id);

    /**
     * 上传模型文件
     *
     * @param id   模型的唯一标识
     * @param file 上传的文件
     * @return 更新后的模型对象
     */
    Model uploadModelFile(Long id, MultipartFile file);



    /**
     * 检查当前节点环境
     *
     * @return 当前节点环境信息
     */
    NodeEnvironmentInfo checkCurrentNodeEnvironment();
    
    /**
     * 安装Miniconda环境
     * 
     * @return 安装结果信息
     */
    String installMiniconda();
    
    /**
     * 安装Ray环境
     * 
     * @return 安装结果信息
     */
    String installRay();
    
    /**
     * 安装环境（合并安装）
     * 
     * @param request 安装请求
     * @return 安装结果
     */
    InstallEnvironmentResult installEnvironment(InstallEnvironmentRequest request);

    /**
     * 部署模型到Ray集群
     *
     * @param request 部署请求对象
     * @return 部署结果信息
     */
    RayDeploymentResponse deployModelToRayCluster(RayDeploymentRequest request);

    /**
     * 创建Ray集群
     */
    Map<String, Object> createRayCluster(Map<String, Object> clusterConfig);
    
    /**
     * 启动Ray Head节点
     */
    RayClusterInfo startRayHead(Map<String, Object> request);
    
    /**
     * 加入Ray集群
     */
    RayClusterInfo joinRayCluster(Map<String, Object> request);
    
    /**
     * 获取Ray集群状态
     */
    RayClusterStatus getRayClusterStatus(Map<String, Object> request);

    /**
     * 启动RayLLM服务
     *
     * @param modelName 模型名称
     * @param modelPath 模型路径
     * @param clusterAddress Ray集群地址
     * @param maxConcurrency 最大并发数
     * @param modelEngine 模型引擎（vllm/tgi等）
     * @return 启动结果信息
     */
    Map<String, Object> launchRayLLMService(String modelName, String modelPath, 
                                          String clusterAddress, Integer maxConcurrency, 
                                          String modelEngine);

    /**
     * 保存模型部署实例
     *
     * @param instance 部署实例对象
     * @return 保存后的部署实例对象
     */
    ModelDeploymentInstance saveDeploymentInstance(ModelDeploymentInstance instance);

    /**
     * 更新部署实例状态
     *
     * @param instanceId 实例ID
     * @param status 新状态
     * @param errorMessage 错误信息（可选）
     * @return 更新后的部署实例对象
     */
    ModelDeploymentInstance updateDeploymentInstanceStatus(Long instanceId, 
                                                         ModelDeploymentInstance.DeploymentStatus status, 
                                                         String errorMessage);

    /**
     * 根据模型ID获取部署实例列表
     *
     * @param modelId 模型ID
     * @return 部署实例列表
     */
    List<ModelDeploymentInstance> getDeploymentInstancesByModelId(Long modelId);

    /**
     * 获取运行中的部署实例列表
     *
     * @return 运行中的部署实例列表
     */
    List<ModelDeploymentInstance> getRunningDeploymentInstances();
}
