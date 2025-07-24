package com.vediofun.model.dto;

import lombok.Data;
import lombok.Builder;
import java.util.List;

/**
 * Ray集群上下文DTO
 * 用于在部署流程中传递集群信息，替代Map<String, Object>
 */
@Data
@Builder
public class RayClusterContext {
    
    /**
     * 集群地址
     */
    private String clusterAddress;
    
    /**
     * 主节点ID
     */
    private String masterNode;
    
    /**
     * 工作节点列表
     */
    private List<String> workerNodes;
    
    /**
     * 总节点数
     */
    private Integer totalNodes;
    
    /**
     * 集群状态
     */
    private String clusterStatus;
    
    /**
     * 额外信息
     */
    private String additionalInfo;
} 