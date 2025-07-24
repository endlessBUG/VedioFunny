package com.vediofun.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "models")
public class Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "vendor", length = 50)
    private String vendor;  // 厂商

    @Column(name = "model_name", length = 100)
    private String modelName;  // 模型名称

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "version", length = 50)
    private String version;

    @Column(name = "status", nullable = false)
    private Integer status = 1; // 1: 启用, 0: 禁用

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "model_tags", joinColumns = @JoinColumn(name = "model_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @CreationTimestamp
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;
} 