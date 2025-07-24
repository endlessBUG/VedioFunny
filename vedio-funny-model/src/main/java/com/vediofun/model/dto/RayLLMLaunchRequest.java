package com.vediofun.model.dto;

import lombok.Data;

/**
 * RayLLM启动请求DTO
 */
@Data
public class RayLLMLaunchRequest {
    
    /**
     * 模型名称
     */
    private String modelName;
    
    /**
     * 模型ID
     */
    private Long modelId;
    
    /**
     * 模型来源
     */
    private String modelSource;
    
    /**
     * 集群地址
     */
    private String clusterAddress;
    
    /**
     * 部署类型
     */
    private String deploymentType;
    
    /**
     * Ray配置
     */
    private RayConfig rayConfig;
} 