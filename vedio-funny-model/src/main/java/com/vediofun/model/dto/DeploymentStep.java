package com.vediofun.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部署步骤DTO
 * 
 * @author VedioFun Team
 */
@Data
public class DeploymentStep {
    
    /**
     * 步骤名称
     */
    private String name;
    
    /**
     * 步骤状态
     */
    private String status;
    
    /**
     * 步骤时间戳
     */
    private LocalDateTime timestamp;
    
    /**
     * 步骤详情
     */
    private Object details;
    
    /**
     * 错误信息
     */
    private String error;
    
    public DeploymentStep() {}
    
    public DeploymentStep(String name, String status, Object details) {
        this.name = name;
        this.status = status;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
} 