package com.vediofun.model.dto;

import lombok.Data;
import java.util.Map;

/**
 * Ray集群状态DTO
 * 用于封装Ray集群的状态信息
 */
@Data
public class RayClusterStatus {
    
    /**
     * 集群状态（healthy/unhealthy）
     */
    private String status;
    
    /**
     * Ray版本
     */
    private String rayVersion;
    
    /**
     * 集群地址
     */
    private String clusterAddress;
    
    /**
     * 节点总数
     */
    private int nodeCount;
    
    /**
     * 活跃节点数
     */
    private int activeNodes;
    
    /**
     * 资源信息
     */
    private Map<String, Object> resources;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 创建健康的集群状态
     */
    public static RayClusterStatus healthy(String clusterAddress, String rayVersion, int nodeCount, Map<String, Object> resources) {
        RayClusterStatus status = new RayClusterStatus();
        status.setStatus("healthy");
        status.setClusterAddress(clusterAddress);
        status.setRayVersion(rayVersion);
        status.setNodeCount(nodeCount);
        status.setActiveNodes(nodeCount);
        status.setResources(resources);
        return status;
    }
    
    /**
     * 创建不健康的集群状态
     */
    public static RayClusterStatus unhealthy(String errorMessage) {
        RayClusterStatus status = new RayClusterStatus();
        status.setStatus("unhealthy");
        status.setErrorMessage(errorMessage);
        status.setNodeCount(0);
        status.setActiveNodes(0);
        return status;
    }
} 