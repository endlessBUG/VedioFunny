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
 * 用户实体类
 */
@Data
@Entity
@Table(name = "users")
@EqualsAndHashCode(callSuper = false)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(length = 100)
    private String nickname;

    @Column(length = 500)
    private String avatar;

    @Column(length = 100, unique = true)
    private String email;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType = UserType.USER;

    @Column(nullable = false)
    private Integer status = 1;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @Column(name = "last_login_ip", length = 50)
    private String lastLoginIp;

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

    // 用户角色关联（多对多）
    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    // 刷新令牌（一对多）
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RefreshToken> refreshTokens;

    // 登录日志（一对多）
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LoginLog> loginLogs;

    /**
     * 用户类型枚举
     */
    public enum UserType {
        ADMIN("管理员"),
        USER("普通用户"),
        GUEST("访客");

        private final String description;

        UserType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 检查用户是否启用
     */
    public boolean isEnabled() {
        return status == 1;
    }

    /**
     * 检查用户是否为管理员
     */
    public boolean isAdmin() {
        return userType == UserType.ADMIN;
    }
} 