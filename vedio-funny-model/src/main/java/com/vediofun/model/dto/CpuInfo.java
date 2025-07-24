package com.vediofun.model.dto;

import lombok.Data;

/**
 * CPU信息DTO
 * 
 * @author VedioFun Team
 */
@Data
public class CpuInfo {
    
    /**
     * CPU型号
     */
    private String model;
    
    /**
     * CPU架构
     */
    private String architecture;
    
    /**
     * 物理核心数
     */
    private Integer physicalCores;
    
    /**
     * 逻辑核心数
     */
    private Integer logicalCores;
    
    /**
     * 最大频率(GHz)
     */
    private Double maxFrequency;
    
    /**
     * 当前频率(GHz)
     */
    private Double currentFrequency;
    
    /**
     * CPU使用率(%)
     */
    private Double cpuUsage;
    
    /**
     * 1分钟平均负载
     */
    private Double loadAverage1min;
    
    /**
     * 5分钟平均负载
     */
    private Double loadAverage5min;
    
    /**
     * 15分钟平均负载
     */
    private Double loadAverage15min;
} 