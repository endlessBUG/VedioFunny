package com.vediofun.model.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 节点环境检查响应DTO
 * 
 * @author VedioFun Team
 */
@Data
public class NodeEnvironmentCheckResponse {
    
    /**
     * 检查ID
     */
    private String checkId;
    
    /**
     * 检查状态
     */
    private String status;
    
    /**
     * 检查消息
     */
    private String message;
    
    /**
     * 总节点数
     */
    private Integer totalNodes;
    
    /**
     * 在线节点数
     */
    private Integer onlineNodes;
    
    /**
     * 离线节点数
     */
    private Integer offlineNodes;
    
    /**
     * 节点环境信息列表
     */
    private List<NodeEnvironmentInfo> nodeInfos;
    
    /**
     * 环境检查摘要
     */
    private EnvironmentSummary summary;
    
    /**
     * 检查时间
     */
    private LocalDateTime checkTime;
    
    /**
     * 检查耗时(秒)
     */
    private Double checkDuration;
    
    /**
     * 错误信息
     */
    private String error;
} 