package com.vediofun.model.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Ray部署响应DTO
 * 
 * @author VedioFun Team
 */
@Data
public class RayDeploymentResponse {
    
    /**
     * 部署ID
     */
    private String deploymentId;
    
    /**
     * 部署状态
     */
    private String status;
    
    /**
     * 部署消息
     */
    private String message;
    
    /**
     * 集群地址
     */
    private String clusterAddress;
    
    /**
     * 服务端点
     */
    private String serviceEndpoint;
    
    /**
     * 部署步骤
     */
    private List<DeploymentStep> steps;
    
    /**
     * 创建时间
     */
    private LocalDateTime timestamp;
    
    /**
     * 错误信息
     */
    private String error;
} 