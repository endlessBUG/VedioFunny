package com.vediofun.model.dto;

import lombok.Data;
import java.util.Map;

/**
 * Ray集群信息DTO
 * 用于封装Ray集群操作的结果信息
 */
@Data
public class RayClusterInfo {
    
    /**
     * 操作是否成功
     */
    private boolean success;
    
    /**
     * 集群地址
     */
    private String clusterAddress;
    
    /**
     * 节点类型（head/worker）
     */
    private String nodeType;
    
    /**
     * 节点状态
     */
    private String nodeStatus;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 详细信息
     */
    private Map<String, Object> details;
    
    /**
     * 进程ID
     */
    private String processId;
    
    /**
     * 创建成功的集群信息
     */
    public static RayClusterInfo success(String clusterAddress, String nodeType, String message) {
        RayClusterInfo info = new RayClusterInfo();
        info.setSuccess(true);
        info.setClusterAddress(clusterAddress);
        info.setNodeType(nodeType);
        info.setNodeStatus(message);
        return info;
    }
    
    /**
     * 创建失败的集群信息
     */
    public static RayClusterInfo failure(String errorMessage) {
        RayClusterInfo info = new RayClusterInfo();
        info.setSuccess(false);
        info.setErrorMessage(errorMessage);
        return info;
    }
} 