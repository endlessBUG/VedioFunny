package com.vediofun.auth.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 刷新令牌实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "device_info", length = 200)
    private String deviceInfo;

    @Column(name = "client_ip", length = 50)
    private String clientIp;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(nullable = false)
    private Integer status = 1;

    @CreationTimestamp
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    // 关联用户
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 检查令牌是否有效
     */
    public boolean isValid() {
        return status == 1 && expiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * 检查令牌是否过期
     */
    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }
} 