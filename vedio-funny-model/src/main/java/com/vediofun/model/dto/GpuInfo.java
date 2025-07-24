package com.vediofun.model.dto;

import lombok.Data;

/**
 * GPU信息DTO
 * 
 * @author VedioFun Team
 */
@Data
public class GpuInfo {
    
    /**
     * GPU索引
     */
    private Integer index;
    
    /**
     * GPU名称
     */
    private String name;
    
    /**
     * GPU驱动版本
     */
    private String driverVersion;
    
    /**
     * CUDA版本
     */
    private String cudaVersion;
    
    /**
     * 总内存大小(MB)
     */
    private Long totalMemoryMB;
    
    /**
     * 已使用内存大小(MB)
     */
    private Long usedMemoryMB;
    
    /**
     * 可用内存大小(MB)
     */
    private Long freeMemoryMB;
    
    /**
     * GPU使用率(%)
     */
    private Double gpuUtilization;
    
    /**
     * 内存使用率(%)
     */
    private Double memoryUtilization;
    
    /**
     * GPU温度(℃)
     */
    private Integer temperature;
    
    /**
     * 功耗(W)
     */
    private Double powerDraw;
    
    /**
     * 最大功耗(W)
     */
    private Double maxPowerLimit;
    
    /**
     * GPU状态
     */
    private String status;
    
    /**
     * 是否支持CUDA
     */
    private Boolean cudaSupported;
    
    /**
     * 计算能力
     */
    private String computeCapability;
} 