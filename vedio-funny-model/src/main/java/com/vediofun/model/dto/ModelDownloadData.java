package com.vediofun.model.dto;

import lombok.Data;

/**
 * 模型下载结果数据DTO
 */
@Data
public class ModelDownloadData {
    
    /**
     * 下载状态（SUCCESS/FAILED）
     */
    private String status;
    
    /**
     * 下载路径
     */
    private String downloadPath;
    
    /**
     * 模型大小
     */
    private String modelSize;
    
    /**
     * 文件校验和
     */
    private String checksum;
    
    /**
     * 下载耗时
     */
    private String downloadTime;
    
    /**
     * 模型目录
     */
    private String modelDirectory;
    
    /**
     * 错误信息
     */
    private String error;
    
    /**
     * 备注信息
     */
    private String note;
} 