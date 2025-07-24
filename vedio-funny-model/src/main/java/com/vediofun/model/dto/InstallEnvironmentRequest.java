package com.vediofun.model.dto;

import lombok.Data;

/**
 * 环境安装请求
 */
@Data
public class InstallEnvironmentRequest {
    
    /**
     * 是否安装Miniconda
     */
    private boolean installMiniconda;
    
    /**
     * 是否安装Ray
     */
    private boolean installRay;
    
    /**
     * 是否安装模型引擎依赖（VLLM、TGI等）
     */
    private boolean installModelEngines;
} 