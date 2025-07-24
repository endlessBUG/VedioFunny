package com.vediofun.model.dto;

import lombok.Data;

/**
 * 模型下载请求DTO
 */
@Data
public class ModelDownloadRequest {
    
    /**
     * 模型名称
     */
    private String modelName;
    
    /**
     * 模型来源（huggingface/modelscope等）
     */
    private String modelSource;
    
    /**
     * 模型ID
     */
    private Long modelId;
} 