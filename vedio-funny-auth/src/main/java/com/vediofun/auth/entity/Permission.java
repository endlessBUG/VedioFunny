package com.vediofun.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 权限实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "permissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "permission_code", unique = true, nullable = false, length = 100)
    private String permissionCode;

    @Column(name = "permission_name", nullable = false, length = 100)
    private String permissionName;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 20)
    private ResourceType resourceType;

    @Column(name = "resource_path", length = 500)
    private String resourcePath;

    @Column(name = "http_method", length = 20)
    private String httpMethod;

    @Column(name = "parent_id")
    private Long parentId = 0L;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "status")
    private Integer status = 1;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    // 角色权限关联（多对多，反向）
    @JsonIgnore
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<Role> roles;

    /**
     * 资源类型枚举
     */
    public enum ResourceType {
        MENU("菜单"),
        BUTTON("按钮"),
        API("接口");

        private final String description;

        ResourceType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 检查权限是否启用
     */
    public boolean isEnabled() {
        return status == 1;
    }
} 