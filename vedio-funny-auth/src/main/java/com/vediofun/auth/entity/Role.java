package com.vediofun.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 角色实体类
 */
@Data
@Entity
@Table(name = "roles")
@EqualsAndHashCode(callSuper = false)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_code", nullable = false, length = 50, unique = true)
    private String roleCode;

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private Integer status = 1;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    // 角色权限关联（多对多）
    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;

    // 用户角色关联（多对多，反向）
    @JsonIgnore
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<User> users;

    /**
     * 检查角色是否启用
     */
    public boolean isEnabled() {
        return status == 1;
    }
} 