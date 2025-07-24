package com.vediofun.model.dto;

import lombok.Data;

/**
 * 环境安装结果
 */
@Data
public class InstallEnvironmentResult {
    
    /**
     * 安装是否成功
     */
    private boolean success;
    
    /**
     * Miniconda是否已安装
     */
    private boolean minicondaInstalled;
    
    /**
     * Ray是否已安装
     */
    private boolean rayInstalled;
    
    /**
     * 模型引擎依赖是否已安装
     */
    private boolean modelEnginesInstalled;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 安装详情信息
     */
    private String details;
    
    /**
     * 创建成功结果
     */
    public static InstallEnvironmentResult success(boolean minicondaInstalled, boolean rayInstalled, boolean modelEnginesInstalled, String details) {
        InstallEnvironmentResult result = new InstallEnvironmentResult();
        result.setSuccess(true);
        result.setMinicondaInstalled(minicondaInstalled);
        result.setRayInstalled(rayInstalled);
        result.setModelEnginesInstalled(modelEnginesInstalled);
        result.setDetails(details);
        return result;
    }
    
    /**
     * 创建失败结果
     */
    public static InstallEnvironmentResult failure(String errorMessage) {
        InstallEnvironmentResult result = new InstallEnvironmentResult();
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        return result;
    }
} 