package com.vediofun.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 节点环境信息DTO
 * 
 * @author VedioFun Team
 */
@Data
public class NodeEnvironmentInfo {
    
    /**
     * 节点ID
     */
    private String nodeId;
    
    /**
     * 节点IP地址
     */
    private String ipAddress;
    
    /**
     * 节点状态
     */
    private String status;
    
    /**
     * 操作系统信息
     */
    private String osInfo;
    
    /**
     * Python版本
     */
    private String pythonVersion;
    
    /**
     * Ray版本
     */
    private String rayVersion;
    
    /**
     * Python是否已安装
     */
    private Boolean pythonInstalled;
    
    /**
     * Ray是否已安装
     */
    private Boolean rayInstalled;
    
    /**
     * 模型引擎依赖是否已安装（VLLM、TGI等）
     */
    private Boolean modelEnginesInstalled;
    
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