package com.vediofun.model.dto;

import lombok.Data;

import java.util.List;

/**
 * Ray部署请求DTO
 * 
 * @author VedioFun Team
 */
@Data
public class RayDeploymentRequest {
    
    /**
     * 模型ID
     */
    private Long modelId;
    
    /**
     * 模型名称
     */
    private String modelName;
    
    /**
     * 模型来源（modelscope/huggingface）
     */
    private String modelSource;
    
    /**
     * 节点ID列表
     */
    private List<String> nodeIds;
    
    /**
     * Ray配置
     */
    private RayConfig rayConfig;
    
    /**
     * 部署类型
     */
    private String deploymentType;
    
    /**
     * 用户ID
     */
    private Long userId;
} 