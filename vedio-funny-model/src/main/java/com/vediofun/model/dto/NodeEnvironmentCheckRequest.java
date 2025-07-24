package com.vediofun.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 节点环境检查请求DTO
 * 
 * @author VedioFun Team
 */
@Data
public class NodeEnvironmentCheckRequest {
    
    /**
     * 节点ID列表
     */
    private List<String> nodeIds;
    
    /**
     * 是否检查详细信息
     */
    private Boolean detailedCheck = true;
    
    /**
     * 是否检查GPU信息
     */
    private Boolean checkGpu = true;
    
    /**
     * 是否检查Python环境
     */
    private Boolean checkPython = true;
    
    /**
     * 是否检查Ray环境
     */
    private Boolean checkRay = true;
    
    /**
     * 超时时间(秒)
     */
    private Integer timeoutSeconds = 30;
} 