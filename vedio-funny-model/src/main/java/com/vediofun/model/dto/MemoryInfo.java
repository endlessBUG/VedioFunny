package com.vediofun.model.dto;

import lombok.Data;

/**
 * 内存信息DTO
 * 
 * @author VedioFun Team
 */
@Data
public class MemoryInfo {
    
    /**
     * 总内存大小(MB)
     */
    private Long totalMemoryMB;
    
    /**
     * 已使用内存大小(MB)
     */
    private Long usedMemoryMB;
    
    /**
     * 可用内存大小(MB)
     */
    private Long freeMemoryMB;
    
    /**
     * 缓存内存大小(MB)
     */
    private Long cachedMemoryMB;
    
    /**
     * 缓冲区内存大小(MB)
     */
    private Long bufferedMemoryMB;
    
    /**
     * 内存使用率(%)
     */
    private Double memoryUsage;
    
    /**
     * 交换分区总大小(MB)
     */
    private Long totalSwapMB;
    
    /**
     * 交换分区已使用大小(MB)
     */
    private Long usedSwapMB;
    
    /**
     * 交换分区可用大小(MB)
     */
    private Long freeSwapMB;
    
    /**
     * 交换分区使用率(%)
     */
    private Double swapUsage;
} 