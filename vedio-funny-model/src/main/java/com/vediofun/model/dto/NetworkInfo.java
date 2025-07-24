package com.vediofun.model.dto;

import lombok.Data;

/**
 * 网络信息DTO
 * 
 * @author VedioFun Team
 */
@Data
public class NetworkInfo {
    
    /**
     * 主机名
     */
    private String hostname;
    
    /**
     * 主网卡名称
     */
    private String primaryInterface;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * 子网掩码
     */
    private String subnetMask;
    
    /**
     * 网关地址
     */
    private String gateway;
    
    /**
     * DNS服务器
     */
    private String dnsServers;
    
    /**
     * 网络带宽(Mbps)
     */
    private Double bandwidthMbps;
    
    /**
     * 网络延迟(ms)
     */
    private Double latencyMs;
    
    /**
     * 上传速度(MB/s)
     */
    private Double uploadSpeedMBps;
    
    /**
     * 下载速度(MB/s)
     */
    private Double downloadSpeedMBps;
    
    /**
     * 网络状态
     */
    private String networkStatus;
} 