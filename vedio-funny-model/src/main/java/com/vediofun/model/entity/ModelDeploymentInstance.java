package com.vediofun.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 模型部署实例实体类
 * 记录模型的部署信息和运行状态
 */
@Data
@Entity
@Table(name = "model_deployment_instances")
public class ModelDeploymentInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的模型ID
     */
    @Column(name = "model_id", nullable = false)
    private Long modelId;

    /**
     * 模型名称（冗余字段，便于查询）
     */
    @Column(name = "model_name", length = 100)
    private String modelName;

    /**
     * 模型路径
     */
    @Column(name = "model_path", length = 500)
    private String modelPath;

    /**
     * 部署类型：RAY_CLUSTER, SINGLE_NODE, DOCKER等
     */
    @Column(name = "deployment_type", length = 50, nullable = false)
    private String deploymentType;

    /**
     * 集群地址
     */
    @Column(name = "cluster_address", length = 200)
    private String clusterAddress;

    /**
     * 服务端点URL
     */
    @Column(name = "service_endpoint", length = 200)
    private String serviceEndpoint;

    /**
     * 部署状态：DEPLOYING, RUNNING, STOPPED, ERROR, UNKNOWN
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private DeploymentStatus status = DeploymentStatus.DEPLOYING;

    /**
     * 模型引擎：VLLM, TGI, RAY_SERVE等
     */
    @Column(name = "model_engine", length = 50)
    private String modelEngine;

    /**
     * 最大并发数
     */
    @Column(name = "max_concurrency")
    private Integer maxConcurrency;

    /**
     * 节点列表（JSON格式存储）
     */
    @Column(name = "node_ids", columnDefinition = "TEXT")
    private String nodeIds;

    /**
     * 部署配置（JSON格式存储额外配置信息）
     */
    @Column(name = "deployment_config", columnDefinition = "TEXT")
    private String deploymentConfig;

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 部署者用户ID
     */
    @Column(name = "deployed_by")
    private Long deployedBy;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    /**
     * 最后健康检查时间
     */
    @Column(name = "last_health_check")
    private LocalDateTime lastHealthCheck;

    /**
     * 部署状态枚举
     */
    public enum DeploymentStatus {
        DEPLOYING("部署中"),
        RUNNING("运行中"),
        STOPPED("已停止"),
        ERROR("错误"),
        UNKNOWN("未知");

        private final String description;

        DeploymentStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
} 