package com.vediofun.model.dto;

import lombok.Data;

/**
 * RayLLM启动结果数据DTO
 */
@Data
public class RayLLMData {
    
    /**
     * 启动状态（SUCCESS/FAILED）
     */
    private String status;
    
    /**
     * 服务端点
     */
    private String serviceEndpoint;
    
    /**
     * 服务状态
     */
    private String serviceStatus;
    
    /**
     * 最大并发数
     */
    private Integer maxConcurrency;
    
    /**
     * GPU内存使用
     */
    private String gpuMemoryUsage;
    
    /**
     * 启动耗时
     */
    private String startupTime;
    
    /**
     * 健康检查状态
     */
    private String healthCheck;
    
    /**
     * 健康检查错误
     */
    private String healthError;
    
    /**
     * 错误信息
     */
    private String error;
    
    /**
     * 备注信息
     */
    private String note;
} 