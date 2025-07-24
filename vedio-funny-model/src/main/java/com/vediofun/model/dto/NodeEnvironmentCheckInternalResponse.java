package com.vediofun.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 内部节点环境检查响应DTO
 * 
 * @author VedioFun Team
 */
@Data
public class NodeEnvironmentCheckInternalResponse {
    
    /**
     * 节点状态
     */
    private String status;
    
    /**
     * 节点ID
     */
    private String nodeId;
    
    /**
     * 主机名
     */
    private String hostname;
    
    /**
     * 操作系统信息
     */
    private String osInfo;
    
    /**
     * 架构信息
     */
    private String architecture;
    
    /**
     * Python是否安装
     */
    private Boolean pythonInstalled;
    
    /**
     * Python版本
     */
    private String pythonVersion;
    
    /**
     * Ray是否安装
     */
    private Boolean rayInstalled;
    
    /**
     * Ray版本
     */
    private String rayVersion;
    
    /**
     * CPU信息
     */
    private CpuInfo cpuInfo;
    
    /**
     * 内存信息
     */
    private MemoryInfo memoryInfo;
    
    /**
     * GPU信息列表
     */
    private List<GpuInfo> gpuInfos;
    
    /**
     * 磁盘信息
     */
    private DiskInfo diskInfo;
    
    /**
     * 网络信息
     */
    private NetworkInfo networkInfo;
    
    /**
     * 错误信息
     */
    private String error;
} 