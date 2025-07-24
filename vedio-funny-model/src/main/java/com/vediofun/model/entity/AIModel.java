package com.vediofun.model.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * AI模型实体类
 */
@Entity
@Table(name = "ai_models")
@EntityListeners(AuditingEntityListener.class)
public class AIModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 模型名称
     */
    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;
    
    /**
     * 模型版本
     */
    @Column(name = "version", length = 20)
    private String version;
    
    /**
     * 模型描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    /**
     * 模型文件路径
     */
    @Column(name = "file_path", length = 500)
    private String filePath;
    
    /**
     * 模型类型 (ONNX, PyTorch, TensorFlow等)
     */
    @Column(name = "model_type", length = 50)
    private String modelType;
    
    /**
     * 模型状态 (ACTIVE, INACTIVE, TRAINING等)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private ModelStatus status = ModelStatus.INACTIVE;
    
    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 默认构造函数
    public AIModel() {}
    
    // 构造函数
    public AIModel(String modelName, String version, String description) {
        this.modelName = modelName;
        this.version = version;
        this.description = description;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getModelName() {
        return modelName;
    }
    
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public String getModelType() {
        return modelType;
    }
    
    public void setModelType(String modelType) {
        this.modelType = modelType;
    }
    
    public ModelStatus getStatus() {
        return status;
    }
    
    public void setStatus(ModelStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * 模型状态枚举
     */
    public enum ModelStatus {
        ACTIVE("活跃"),
        INACTIVE("不活跃"), 
        TRAINING("训练中"),
        DEPLOYING("部署中"),
        ERROR("错误");
        
        private final String description;
        
        ModelStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
} 