package com.vediofun.model.dto;

import lombok.Data;

/**
 * Ray集群配置DTO
 * 
 * @author VedioFun Team
 */
@Data
public class RayConfig {
    
    /**
     * 实例数量
     */
    private Integer instanceCount = 1;
    
    /**
     * 最小工作节点数
     */
    private Integer minWorkers = 1;
    
    /**
     * 最大工作节点数
     */
    private Integer maxWorkers = 5;
    
    /**
     * 每节点CPU核心数
     */
    private Integer numCpus = 4;
    
    /**
     * 每节点GPU数量
     */
    private Integer numGpus = 0;
    
    /**
     * 内存限制(GB)
     */
    private Integer memory = 16;
    
    /**
     * 运行时环境
     */
    private String runtimeEnv = "py39";
    
    /**
     * 工作负载类型
     */
    private String workloadType = "cpu_intensive";
    
    /**
     * 集群地址
     */
    private String clusterAddress;
} 