package com.vediofun.model.dto;

import lombok.Data;

/**
 * 环境检查摘要DTO
 * 
 * @author VedioFun Team
 */
@Data
public class EnvironmentSummary {
    
    /**
     * Python就绪节点数
     */
    private Integer pythonReadyNodes;
    
    /**
     * Ray就绪节点数
     */
    private Integer rayReadyNodes;
    
    /**
     * GPU可用节点数
     */
    private Integer gpuAvailableNodes;
    
    /**
     * 总GPU数量
     */
    private Integer totalGpuCount;
    
    /**
     * 可用GPU数量
     */
    private Integer availableGpuCount;
    
    /**
     * 总GPU内存(MB)
     */
    private Long totalGpuMemoryMB;
    
    /**
     * 可用GPU内存(MB)
     */
    private Long availableGpuMemoryMB;
    
    /**
     * 总CPU核心数
     */
    private Integer totalCpuCores;
    
    /**
     * 总内存大小(MB)
     */
    private Long totalMemoryMB;
    
    /**
     * 可用内存大小(MB)
     */
    private Long availableMemoryMB;
    
    /**
     * 总磁盘空间(GB)
     */
    private Long totalDiskGB;
    
    /**
     * 可用磁盘空间(GB)
     */
    private Long availableDiskGB;
    
    /**
     * 集群就绪状态
     */
    private Boolean clusterReady;
    
    /**
     * 建议的主节点ID
     */
    private String recommendedMasterNode;
    
    /**
     * 环境检查通过率(%)
     */
    private Double environmentPassRate;
} 