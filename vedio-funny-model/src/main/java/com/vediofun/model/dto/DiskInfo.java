package com.vediofun.model.dto;

import lombok.Data;

/**
 * 磁盘信息DTO
 * 
 * @author VedioFun Team
 */
@Data
public class DiskInfo {
    
    /**
     * 总磁盘空间(GB)
     */
    private Long totalDiskGB;
    
    /**
     * 已使用磁盘空间(GB)
     */
    private Long usedDiskGB;
    
    /**
     * 可用磁盘空间(GB)
     */
    private Long freeDiskGB;
    
    /**
     * 磁盘使用率(%)
     */
    private Double diskUsage;
    
    /**
     * 磁盘读取速度(MB/s)
     */
    private Double readSpeedMBps;
    
    /**
     * 磁盘写入速度(MB/s)
     */
    private Double writeSpeedMBps;
    
    /**
     * 磁盘类型(SSD/HDD)
     */
    private String diskType;
    
    /**
     * 文件系统类型
     */
    private String fileSystemType;
} 